package com.starfantasy.goety.compat;

import java.util.List;
import java.util.Map;
import java.util.Set;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;
import org.spongepowered.asm.logging.Level;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import org.spongepowered.asm.service.MixinService;

/** Optional variant selection must not prevent startup when an upstream spell changes. */
public final class FinalStaffPriorityMixinPlugin implements IMixinConfigPlugin {
    private static final String ALLY = "com/Polarice3/Goety/common/entities/ally/";
    private static final Map<String, String> SUMMONS = Map.of(
            "FinalStaffZombiePriorityMixin", ALLY + "Summoned",
            "FinalStaffSkeletonPriorityMixin", ALLY + "undead/skeleton/AbstractSkeletonServant",
            "FinalStaffSlimePriorityMixin", ALLY + "SlimeServant",
            "FinalStaffHuntingPriorityMixin", ALLY + "Summoned",
            "FinalStaffMaulingPriorityMixin", ALLY + "Summoned");
    private static final String SPELL_ARGS = "(Lnet/minecraft/server/level/ServerLevel;"
            + "Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;"
            + "Lcom/Polarice3/Goety/common/magic/SpellStat;)V";
    private static final String OWNER_ARGS = "(Lnet/minecraft/world/entity/LivingEntity;)V";

    @Override public boolean shouldApplyMixin(String target, String mixin) {
        String simple = mixin.substring(mixin.lastIndexOf('.') + 1);
        if (!SUMMONS.containsKey(simple) && !simple.equals("SoulBoltSpellMixin")) return true;
        try {
            // ModLauncher rejects getClassNode(target, false). The default overload uses
            // its supported transformation pipeline without loading/initializing the class.
            ClassNode node = MixinService.getService().getBytecodeProvider().getClassNode(target);
            if (supports(node, simple)) return true;
        } catch (Exception exception) {
            warn(simple, "cannot inspect " + target + ": " + exception);
            return false;
        }
        warn(simple, "Goety spell structure changed: " + target);
        return false;
    }

    /** Inspect bytecode only: never load game classes during mixin discovery. */
    public static boolean supports(ClassNode node, String mixin) {
        MethodNode method = node.methods.stream().filter(m -> m.name.equals("SpellResult")
                && m.desc.equals(SPELL_ARGS)).findFirst().orElse(null);
        if (method == null) return false;
        if (mixin.equals("SoulBoltSpellMixin")) {
            // Both redirects form one feature; do not enable only half of the bolt selection.
            return calls(method, "net/minecraft/world/item/ItemStack", "m_150930_",
                    "(Lnet/minecraft/world/item/Item;)Z").size() == 1
                    && calls(method, node.name, "typeStaff", "(Lnet/minecraft/world/item/ItemStack;"
                    + "Lcom/Polarice3/Goety/api/magic/SpellType;)Z").size() == 3;
        }
        String owner = SUMMONS.get(mixin);
        if (owner == null) return false;
        List<MethodInsnNode> hooks = calls(method, owner, "setTrueOwner", OWNER_ARGS);
        if (hooks.size() != 1 || method.localVariables == null) return false;
        MethodInsnNode hook = hooks.get(0);
        int point = method.instructions.indexOf(hook);
        String localName = mixin.equals("FinalStaffSlimePriorityMixin") ? "slimeServant" : "summonedentity";
        List<LocalVariableNode> locals = method.localVariables.stream().filter(v ->
                v.name.equals(localName) && v.desc.equals("L" + owner + ";")
                && method.instructions.indexOf(v.start) <= point
                && method.instructions.indexOf(v.end) > point + 1).toList();
        if (locals.size() != 1) return false;
        // Confirm it is the receiver of setTrueOwner, rather than an unrelated local with this name.
        AbstractInsnNode argument = previousCode(hook);
        AbstractInsnNode receiver = previousCode(argument);
        return argument instanceof VarInsnNode arg && arg.getOpcode() == Opcodes.ALOAD && arg.var == 2
                && receiver instanceof VarInsnNode recv && recv.getOpcode() == Opcodes.ALOAD
                && recv.var == locals.get(0).index;
    }

    private static List<MethodInsnNode> calls(MethodNode method, String owner, String name, String desc) {
        java.util.ArrayList<MethodInsnNode> result = new java.util.ArrayList<>();
        for (AbstractInsnNode instruction : method.instructions) {
            if (instruction instanceof MethodInsnNode call && call.owner.equals(owner)
                    && call.name.equals(name) && call.desc.equals(desc)) result.add(call);
        }
        return result;
    }

    private static AbstractInsnNode previousCode(AbstractInsnNode node) {
        if (node == null) return null;
        do { node = node.getPrevious(); } while (node != null && node.getOpcode() < 0);
        return node;
    }

    private static void warn(String mixin, String reason) {
        MixinService.getService().getLogger("starfantasy_goety").log(Level.WARN,
                "[Staff priority] Skipped {}: {}. This spell keeps Goety's native selection.", mixin, reason);
    }

    @Override public void postApply(String target, ClassNode node, String mixin, IMixinInfo info) {
        String simple = mixin.substring(mixin.lastIndexOf('.') + 1);
        if (!SUMMONS.containsKey(simple) && !simple.equals("SoulBoltSpellMixin")) return;
        int applied = 0;
        for (MethodNode method : node.methods) {
            if (!method.name.equals("SpellResult") || !method.desc.equals(SPELL_ARGS)) continue;
            for (AbstractInsnNode instruction : method.instructions) {
                if (instruction instanceof MethodInsnNode call && call.owner.equals(node.name)
                        && (call.name.contains("starfantasy$preferredVariant")
                        || call.name.contains("starfantasy$preferredBolt")
                        || call.name.contains("starfantasy$namelessNecromancy"))) applied++;
            }
        }
        if (applied == 0) warn(simple, "optional hook was not applied (possibly changed by another mod)");
        else MixinService.getService().getLogger("starfantasy_goety").log(Level.INFO,
                "[Staff priority] Applied {} ({} hook calls).", simple, applied);
    }

    @Override public void onLoad(String pkg) {}
    @Override public String getRefMapperConfig() { return null; }
    @Override public void acceptTargets(Set<String> mine, Set<String> others) {}
    @Override public List<String> getMixins() { return null; }
    @Override public void preApply(String target, ClassNode node, String mixin, IMixinInfo info) {}
}
