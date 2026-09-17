package com.starfantasy.goety.combat;

import com.Polarice3.Goety.common.entities.ally.Summoned;
import com.starfantasy.goety.StarFantasyGoetyMod;
import com.starfantasy.goety.entity.ApollyonEntity;
import com.starfantasy.goety.entity.ApollyonPageantApostleEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.warden.AngerLevel;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.ai.behavior.EntityTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraftforge.event.entity.living.LivingChangeTargetEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

/** Redirect existing combatants before clearing attacks on the invulnerable host. */
@Mod.EventBusSubscriber(modid = StarFantasyGoetyMod.MODID)
public final class ApollyonPageantAggro {
    /** Shares the live controller selection across the two compilation mappings. */
    public interface Encounter {
        LivingEntity pageantRedirectTarget(LivingEntity attacker);
    }

    // Transient, per-creature participation; no world scans or saved aggro.
    private static final Map<Mob, Participation> PARTICIPANTS = new WeakHashMap<>();

    private static final class Participation {
        final UUID boss;
        UUID assignedTarget;
        Participation(ApollyonEntity boss) { this.boss = boss.getUUID(); }
    }

    private ApollyonPageantAggro() {}

    private static LivingEntity redirectTarget(Entity boss, Mob attacker) {
        return boss instanceof Encounter encounter ? encounter.pageantRedirectTarget(attacker) : null;
    }

    private static ApollyonEntity host(Entity entity, ServerLevel level) {
        if (entity instanceof ApollyonPageantApostleEntity actor && actor.pageantOwnerUuid() != null) {
            entity = level.getEntity(actor.pageantOwnerUuid());
        }
        return entity instanceof ApollyonEntity boss && boss.isPageantCombatLocked() ? boss : null;
    }

    private static boolean attackable(Entity entity) {
        return entity instanceof ApollyonPageantApostleEntity actor && actor.isAlive()
                && actor.getHealth() > 0.0F && actor.isPageantDamageable() && !actor.isMonolithProtected();
    }

    private static boolean blocked(Entity entity, ServerLevel level) {
        return host(entity, level) != null && !attackable(entity);
    }

    private static boolean participating(Mob mob, ApollyonEntity boss) {
        if (boss == null || !boss.isAlive() || !boss.isPageantCombatLocked()
                || boss.getPageantState() == ApollyonPageantController.PENDING_RESTART
                || mob == boss || mob.level() != boss.level() || !mob.isAlive()) return false;
        var home = boss.arenaHomePosition();
        double dx = mob.getX() - home.x, dz = mob.getZ() - home.z;
        return dx * dx + dz * dz <= ApollyonEntity.ARENA_RADIUS * ApollyonEntity.ARENA_RADIUS
                && Math.abs(mob.getY() - home.y) <= 8.0D;
    }

    private static void remember(Mob mob, ApollyonEntity boss) {
        Participation previous = PARTICIPANTS.get(mob);
        if (previous == null || !previous.boss.equals(boss.getUUID())) {
            PARTICIPANTS.put(mob, new Participation(boss));
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void changeTarget(LivingChangeTargetEvent event) {
        if (!(event.getEntity() instanceof Mob mob) || !(mob.level() instanceof ServerLevel level)
                || level.isClientSide()) return;
        ApollyonEntity boss = host(event.getNewTarget(), level);
        if (participating(mob, boss)) {
            remember(mob, boss);
            if (blocked(event.getNewTarget(), level)) event.setNewTarget(redirectTarget(boss, mob));
        } else {
            if (event.getNewTarget() != null) PARTICIPANTS.remove(mob);
            if (blocked(event.getNewTarget(), level)) event.setNewTarget(null);
        }
    }

    private static LivingEntity brainTarget(Mob mob) {
        var brain = mob.getBrain();
        return brain.hasMemoryValue(MemoryModuleType.ATTACK_TARGET)
                ? brain.getMemory(MemoryModuleType.ATTACK_TARGET).orElse(null) : null;
    }

    private static boolean unrelated(LivingEntity target, ApollyonEntity boss, ServerLevel level) {
        return target != null && target.isAlive() && host(target, level) != boss;
    }

    @SubscribeEvent
    public static void tick(LivingEvent.LivingTickEvent event) {
        if (!(event.getEntity() instanceof Mob mob) || !(mob.level() instanceof ServerLevel level)
                || level.isClientSide()) return;
        LivingEntity priority = mob instanceof Summoned servant ? servant.getPriorityTarget() : null;
        ApollyonEntity boss = host(mob.getTarget(), level);
        if (boss == null) boss = host(brainTarget(mob), level);
        if (boss == null) boss = host(priority, level);
        if (participating(mob, boss)) remember(mob, boss);

        Participation participation = PARTICIPANTS.get(mob);
        if (participation != null) {
            Entity owner = level.getEntity(participation.boss);
            boss = owner instanceof ApollyonEntity value ? value : null;
            if (!participating(mob, boss) || unrelated(mob.getTarget(), boss, level)
                    || unrelated(brainTarget(mob), boss, level) || unrelated(priority, boss, level)) {
                PARTICIPANTS.remove(mob);
            } else {
                // Keep a valid actor instead of oscillating between the two archers.
                LivingEntity target = attackable(mob.getTarget()) ? mob.getTarget() : redirectTarget(boss, mob);
                if (target != null && (mob.getTarget() != target
                        || !target.getUUID().equals(participation.assignedTarget)
                        || blocked(brainTarget(mob), level) || blocked(priority, level))) {
                    redirect(mob, target);
                    participation.assignedTarget = target.getUUID();
                } else if (target == null) {
                    participation.assignedTarget = null;
                }
            }
        }
        clearBlockedTargets(mob, level);
    }

    private static void redirect(Mob mob, LivingEntity target) {
        mob.getNavigation().stop();
        mob.stopUsingItem();
        mob.setTarget(target);
        // Respect another handler that rejects this target.
        if (mob.getTarget() != target) return;
        if (mob instanceof Summoned servant) {
            servant.setPriorityTarget(target);
            servant.setPriorityTime(600);
        }
        var brain = mob.getBrain();
        brain.setMemoryWithExpiry(MemoryModuleType.ANGRY_AT, target.getUUID(), 600L);
        brain.setMemoryWithExpiry(MemoryModuleType.ATTACK_TARGET, target, 600L);
        if (mob instanceof NeutralMob neutral) {
            neutral.setPersistentAngerTarget(target.getUUID());
            neutral.setRemainingPersistentAngerTime(600);
        }
        if (mob instanceof Warden warden) {
            warden.increaseAngerAt(target, AngerLevel.ANGRY.getMinimumAnger() + 20, false);
            warden.setAttackTarget(target);
        }
    }

    private static void clearBlockedTargets(Mob mob, ServerLevel level) {
        boolean stopAttack = false;
        if (blocked(mob.getTarget(), level)) {
            mob.setTarget(null);
            stopAttack = true;
        }
        if (mob instanceof Summoned servant && blocked(servant.getPriorityTarget(), level)) {
            servant.setPriorityTarget(null);
            servant.setPriorityTime(0);
            stopAttack |= mob.getTarget() == null;
        }
        if (blocked(mob.getLastHurtByMob(), level)) mob.setLastHurtByMob(null);
        if (blocked(mob.getLastHurtMob(), level)) mob.setLastHurtMob(null);

        var brain = mob.getBrain();
        if (brain.hasMemoryValue(MemoryModuleType.ATTACK_TARGET)
                && blocked(brain.getMemory(MemoryModuleType.ATTACK_TARGET).orElse(null), level)) {
            brain.eraseMemory(MemoryModuleType.ATTACK_TARGET);
            stopAttack = true;
        }
        if (brain.hasMemoryValue(MemoryModuleType.HURT_BY_ENTITY)
                && blocked(brain.getMemory(MemoryModuleType.HURT_BY_ENTITY).orElse(null), level)) {
            brain.eraseMemory(MemoryModuleType.HURT_BY_ENTITY);
            brain.eraseMemory(MemoryModuleType.HURT_BY);
        }
        if (brain.hasMemoryValue(MemoryModuleType.LOOK_TARGET)
                && brain.getMemory(MemoryModuleType.LOOK_TARGET).orElse(null) instanceof EntityTracker tracker
                && blocked(tracker.getEntity(), level)) {
            brain.eraseMemory(MemoryModuleType.LOOK_TARGET);
        }
        if (brain.hasMemoryValue(MemoryModuleType.WALK_TARGET)
                && brain.getMemory(MemoryModuleType.WALK_TARGET).orElseThrow().getTarget() instanceof EntityTracker tracker
                && blocked(tracker.getEntity(), level)) {
            brain.eraseMemory(MemoryModuleType.WALK_TARGET);
            stopAttack |= mob.getTarget() == null;
        }
        // Angry mobs can remember only a UUID after losing their immediate target.
        if (brain.hasMemoryValue(MemoryModuleType.ANGRY_AT)
                && blocked(level.getEntity(brain.getMemory(MemoryModuleType.ANGRY_AT).orElseThrow()), level)) {
            brain.eraseMemory(MemoryModuleType.ANGRY_AT);
        }
        if (mob instanceof NeutralMob neutral && neutral.getPersistentAngerTarget() != null
                && blocked(level.getEntity(neutral.getPersistentAngerTarget()), level)) {
            neutral.setPersistentAngerTarget(null);
            neutral.setRemainingPersistentAngerTime(0);
        }
        if (stopAttack && mob.getTarget() == null && brainTarget(mob) == null) {
            mob.getNavigation().stop();
            mob.stopUsingItem();
            mob.setAggressive(false);
        }
    }
}
