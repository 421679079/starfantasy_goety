package validation;

import java.lang.reflect.Constructor;
import java.util.*;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.tree.analysis.*;
import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.transformer.IMixinTransformer;
import com.starfantasy.goety.compat.FinalStaffPriorityMixinPlugin;

public final class PriorityMixinRegression {
    private static final String SPELL = "com.Polarice3.Goety.common.magic.spells.";
    private static final Map<String, String> TARGETS = new LinkedHashMap<>();
    static {
        TARGETS.put("necromancy.ZombieSpell", "FinalStaffZombiePriorityMixin");
        TARGETS.put("necromancy.SkeletonSpell", "FinalStaffSkeletonPriorityMixin");
        TARGETS.put("wild.SlimySpell", "FinalStaffSlimePriorityMixin");
        TARGETS.put("wild.HuntingSpell", "FinalStaffHuntingPriorityMixin");
        TARGETS.put("wild.MaulingSpell", "FinalStaffMaulingPriorityMixin");
        TARGETS.put("SoulBoltSpell", "SoulBoltSpellMixin");
    }
    public static void main(String[] args) throws Exception {
        String scenario = args.length == 0 ? "normal" : args[0];
        MixinBootstrap.init();
        MixinEnvironment env = MixinEnvironment.getCurrentEnvironment();
        env.setSide(MixinEnvironment.Side.SERVER);
        HeadlessMixinService service = new HeadlessMixinService();
        for (var entry : TARGETS.entrySet()) {
            ClassNode node = service.getClassNode(SPELL + entry.getKey());
            check(FinalStaffPriorityMixinPlugin.supports(node, entry.getValue()), "preflight " + entry.getKey());
            if (!scenario.equals("normal")) mutate(node, scenario);
            HeadlessMixinService.overrides.put(SPELL + entry.getKey(), node);
        }
        Mixins.addConfiguration("priority-validation.mixins.json");
        Constructor<?> ctor = Class.forName("org.spongepowered.asm.mixin.transformer.MixinTransformer").getDeclaredConstructor();
        ctor.setAccessible(true);
        IMixinTransformer transformer = (IMixinTransformer) ctor.newInstance();
        int checked = 0;
        for (var entry : TARGETS.entrySet()) {
            String name = SPELL + entry.getKey();
            ClassNode node = service.getClassNode(name);
            boolean expected = scenario.equals("normal") || scenario.equals("shift-locals");
            check(FinalStaffPriorityMixinPlugin.supports(node, entry.getValue()) == expected, "guard " + name);
            transformer.transformClass(env, name, node);
            int calls = 0;
            for (MethodNode method : node.methods) {
                if (!method.name.equals("SpellResult")) continue;
                for (AbstractInsnNode instruction : method.instructions) {
                    if (instruction instanceof MethodInsnNode call && call.owner.equals(node.name)
                            && (call.name.contains("starfantasy$preferredVariant") || call.name.contains("starfantasy$preferredBolt")
                            || call.name.contains("starfantasy$namelessNecromancy"))) calls++;
                }
                // Validates stack shape and handler argument consumption on the transformed method.
                new Analyzer<>(new BasicVerifier()).analyze(node.name, method);
            }
            int expectedCalls = expected ? (entry.getKey().equals("SoulBoltSpell") ? 4 : 1) : 0;
            check(calls == expectedCalls, name + " injected calls: expected " + expectedCalls + ", got " + calls);
            System.out.println("PASS " + scenario + " " + name + " (" + calls + " hook calls)");
            checked++;
        }
        System.out.println("PASS real Mixin 0.8.5 transformations: " + checked + " [" + scenario + "]");
    }
    private static void mutate(ClassNode node, String scenario) {
        for (MethodNode method : node.methods) {
            if (!method.name.equals("SpellResult")) continue;
            switch (scenario) {
                case "missing-hook" -> {
                    for (AbstractInsnNode instruction : method.instructions) {
                        if (instruction instanceof MethodInsnNode call
                                && (call.name.equals("setTrueOwner") || call.name.equals("m_150930_"))) call.name += "Updated";
                    }
                }
                case "renamed-local" -> {
                    // SoulBolt has no local capture: simulate losing just one of its two linked hooks.
                    for (LocalVariableNode local : method.localVariables) {
                        if (local.name.equals("summonedentity") || local.name.equals("slimeServant")) local.name = "newSummon";
                    }
                    if (node.name.endsWith("SoulBoltSpell")) for (AbstractInsnNode instruction : method.instructions) {
                        if (instruction instanceof MethodInsnNode call && call.name.equals("typeStaff")) call.name += "Updated";
                    }
                }
                case "shift-locals" -> {
                    // Simulates adding an unrelated local before the summon. Discard frames so Mixin recomputes them.
                    for (AbstractInsnNode instruction : method.instructions.toArray()) {
                        if (instruction instanceof VarInsnNode var && var.var >= 5) var.var++;
                        if (instruction instanceof IincInsnNode inc && inc.var >= 5) inc.var++;
                        if (instruction instanceof FrameNode) method.instructions.remove(instruction);
                    }
                    for (LocalVariableNode local : method.localVariables) if (local.index >= 5) local.index++;
                    method.maxLocals++;
                }
                default -> throw new IllegalArgumentException(scenario);
            }
        }
    }
    private static void check(boolean condition, String message) { if (!condition) throw new AssertionError(message); }
}
