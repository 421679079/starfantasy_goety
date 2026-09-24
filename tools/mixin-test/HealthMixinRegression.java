package validation;

import java.lang.reflect.Constructor;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.tree.analysis.*;
import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.transformer.IMixinTransformer;

/** Apply the actual packaged guards to real Minecraft classes without starting a world. */
public final class HealthMixinRegression {
    public static void main(String[] args) throws Exception {
        MixinBootstrap.init();
        MixinEnvironment env = MixinEnvironment.getCurrentEnvironment();
        env.setSide(MixinEnvironment.Side.SERVER);
        Mixins.addConfiguration("health-validation.mixins.json");
        Constructor<?> ctor = Class.forName("org.spongepowered.asm.mixin.transformer.MixinTransformer").getDeclaredConstructor();
        ctor.setAccessible(true);
        IMixinTransformer transformer = (IMixinTransformer) ctor.newInstance();
        HeadlessMixinService service = new HeadlessMixinService();
        ClassNode living = service.getClassNode("net.minecraft.world.entity.LivingEntity");
        transformer.transformClass(env, "net.minecraft.world.entity.LivingEntity", living);
        MethodNode getter = living.methods.stream().filter(m -> m.name.contains("starFantasyLibrary$healthKey")).findFirst().orElseThrow();
        boolean correctField = false;
        for (AbstractInsnNode insn : getter.instructions) {
            if (insn instanceof FieldInsnNode field && field.name.equals("f_20961_")) correctField = true;
        }
        check(correctField, "health accessor resolves the real health key");
        new Analyzer<>(new BasicVerifier()).analyze(living.name, getter);
        int reductionHooks = 0;
        java.util.Set<String> attributes = new java.util.HashSet<>();
        for (MethodNode method : living.methods) {
            if (!method.name.equals("m_6515_") && !method.name.contains("starFantasyLibrary$applyDamageReductions")) continue;
            for (AbstractInsnNode instruction : method.instructions) {
                if (instruction instanceof MethodInsnNode call && method.name.equals("m_6515_")
                        && call.name.contains("starFantasyLibrary$applyDamageReductions")) reductionHooks++;
                if (instruction instanceof FieldInsnNode field
                        && field.owner.equals("com/starfantasy/library/combat/StarFantasyCombatAttributes")) attributes.add(field.name);
            }
            new Analyzer<>(new BasicVerifier()).analyze(living.name, method);
        }
        check(reductionHooks > 0, "shared damage reduction is injected");
        check(attributes.containsAll(java.util.List.of("FROST_RESISTANCE", "POISON_RESISTANCE",
                "LIGHTNING_RESISTANCE", "VOID_RESISTANCE", "PROJECTILE_RESISTANCE")), "all new damage attributes are applied");
        ClassNode data = service.getClassNode("net.minecraft.network.syncher.SynchedEntityData");
        transformer.transformClass(env, "net.minecraft.network.syncher.SynchedEntityData", data);
        int injections = 0, delegations = 0, policyCalls = 0;
        for (MethodNode method : data.methods) {
            if (!method.name.equals("m_276349_") && !method.name.equals("m_135381_")
                    && !method.name.contains("starFantasyLibrary$constrainHealth")) continue;
            for (AbstractInsnNode insn : method.instructions) {
                if (!(insn instanceof MethodInsnNode call)) continue;
                if (method.name.equals("m_276349_") && call.name.contains("starFantasyLibrary$constrainHealth")) injections++;
                if (method.name.equals("m_135381_") && call.name.equals("m_276349_")) delegations++;
                if (call.owner.equals("com/starfantasy/library/combat/CombatHealthProtection") && call.name.equals("constrain")) policyCalls++;
            }
            new Analyzer<>(new BasicVerifier()).analyze(data.name, method);
        }
        check(injections == 1, "exactly one guard on the forced setter");
        check(delegations == 1, "ordinary setter delegates to the guarded setter");
        check(policyCalls == 1, "guard calls the packaged entity health policy");
        System.out.println("PASS real Minecraft health accessor, both data setters and shared damage reductions: Mixin 0.8.5 + bytecode verification");
    }
    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
