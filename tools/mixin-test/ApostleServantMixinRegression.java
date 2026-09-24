package validation;

import java.lang.reflect.Constructor;
import java.util.HashSet;
import java.util.Set;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.tree.analysis.*;
import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.transformer.IMixinTransformer;

/** Native title hooks must tolerate a replaced remove method; servants must not inherit Apostle. */
public final class ApostleServantMixinRegression {
    public static void main(String[] args) throws Exception {
        MixinBootstrap.init();
        MixinEnvironment env = MixinEnvironment.getCurrentEnvironment();
        env.setSide(MixinEnvironment.Side.SERVER);
        Mixins.addConfiguration("apostle-servant-validation.mixins.json");
        Constructor<?> ctor = Class.forName("org.spongepowered.asm.mixin.transformer.MixinTransformer").getDeclaredConstructor();
        ctor.setAccessible(true);
        IMixinTransformer transformer = (IMixinTransformer) ctor.newInstance();
        HeadlessMixinService service = new HeadlessMixinService();
        String target = "com.Polarice3.Goety.common.entities.boss.Apostle";
        ClassNode node = service.getClassNode(target);
        // Reproduce the exact merged-method guard from the RevelationFix crash, without loading that mod.
        for (MethodNode method : node.methods) if (method.name.equals("m_142687_")) {
            if (method.visibleAnnotations == null) method.visibleAnnotations = new java.util.ArrayList<>();
            AnnotationNode merged = new AnnotationNode("Lorg/spongepowered/asm/mixin/transformer/meta/MixinMerged;");
            merged.values = new java.util.ArrayList<>(java.util.List.of("mixin", "com.mega.revelationfix.mixin.goety.ApollyonMixin", "priority", 1000));
            method.visibleAnnotations.add(merged);
        }
        transformer.transformClass(env, target, node);
        Set<String> hooks = new HashSet<>();
        for (MethodNode method : node.methods) {
            for (AbstractInsnNode insn : method.instructions) {
                if (insn instanceof MethodInsnNode call && call.name.contains("servant")) hooks.add(call.name);
            }
            if (method.name.contains("servant") || method.name.equals("m_8107_"))
                new Analyzer<>(new BasicVerifier()).analyze(node.name, method);
        }
        if (!hooks.isEmpty()) throw new AssertionError("Servant hooks leaked into hostile Apostle: " + hooks);
        ClassNode servant = service.getClassNode("com.starfantasy.goety.entity.ApostleServantEntity");
        if (!servant.superName.equals("com/Polarice3/Goety/common/entities/ally/Summoned"))
            throw new AssertionError("Servant must directly inherit Summoned");
        for (MethodNode method : servant.methods) new Analyzer<>(new BasicVerifier()).analyze(servant.name, method);
        System.out.println("PASS: independent servant bytecode; existing Apostle hooks tolerate RevelationFix-style merged remove");
    }
}
