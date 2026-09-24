package com.starfantasy.goety.magic.focus;

import com.starfantasy.library.vfx.StarFantasyCageGeometry;

import com.Polarice3.Goety.common.effects.GoetyEffects;
import com.starfantasy.goety.magic.focus.entity.*;
import com.starfantasy.library.vfx.StarFantasyVfx;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.*;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Original cage geometry and 40/42/44/46/48 impact clock, independent of any held blade. */
@Mod.EventBusSubscriber(modid = "starfantasy_goety")
public final class EvernightCast {
    public static final int COLOR = 0xA50924;
    private static final Map<UUID, Cast> ACTIVE = new LinkedHashMap<>();
    public static void spawn(ServerLevel level, LivingEntity caster, float damage, int totalHits) {
        if (!caster.isAlive()) return;
        Vec3 center = BattleFocusPlacement.ground(level, caster,
                caster.position().add(Vec3.directionFromRotation(0, caster.getYRot()).scale(10)));
        if (center == null) return;
        EvernightCageEntity visual = BattleFocusContent.EVERNIGHT_CAGE.get().create(level);
        visual.configure(center, caster.getYRot(), COLOR);
        if (!level.addFreshEntity(visual)) return;
        Cast cast = new Cast(level, caster, visual, damage, totalHits);
        ACTIVE.put(UUID.randomUUID(), cast);
        BattleFocusPlacement.gather(level, caster, center, StarFantasyCageGeometry.DAMAGE_RADIUS);
        cast.controlTargets();
        level.playSound(null, caster.getX(), caster.getY(), caster.getZ(), BattleFocusContent.EVERNIGHT_START.get(), SoundSource.PLAYERS, 1, 1);
        level.playSound(null, center.x, center.y, center.z, BattleFocusContent.EVERNIGHT_CAGE_SOUND.get(), SoundSource.PLAYERS, 2, 1);
    }
    @SubscribeEvent public static void stop(ServerStoppingEvent event) {
        ACTIVE.values().forEach(cast -> cast.visual.discard()); ACTIVE.clear();
    }
    @SubscribeEvent public static void tick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        for (var entry : new ArrayList<>(ACTIVE.entrySet())) {
            Cast cast = entry.getValue();
            if (cast.visual.isRemoved() || !cast.level.hasChunkAt(BlockPos.containing(cast.center))) {
                cast.visual.discard(); ACTIVE.remove(entry.getKey()); continue;
            }
            long elapsed = cast.level.getGameTime() - cast.started;
            if (elapsed < StarFantasyCageGeometry.FINAL_TICK) cast.controlTargets();
            while (cast.impacts < StarFantasyCageGeometry.BLOOMS && elapsed >= StarFantasyCageGeometry.burstTick(cast.impacts)) cast.impact(cast.impacts++);
            if (elapsed >= StarFantasyCageGeometry.FINAL_TICK) {
                ACTIVE.remove(entry.getKey()); cast.finish();
            }
        }
    }
    private static final class Cast {
        final ServerLevel level; final LivingEntity caster; final EvernightCageEntity visual;
        final Vec3 center; final float damage; final long started; final int finalHits; int impacts;
        Cast(ServerLevel level, LivingEntity caster, EvernightCageEntity visual, float damage, int totalHits) {
            this.finalHits = Math.max(6, totalHits) - StarFantasyCageGeometry.BLOOMS;
            this.level=level; this.caster=caster; this.visual=visual; this.center=visual.position(); this.damage=damage; started=level.getGameTime();
        }
        void controlTargets() {
            // A short refreshed effect expires naturally on exit, avoiding removal of another spell's Tangled.
            int remaining = (int) Math.max(1, StarFantasyCageGeometry.FINAL_TICK - (level.getGameTime() - started));
            for (LivingEntity target : BattleFocusCombat.targets(level, caster, center, StarFantasyCageGeometry.DAMAGE_RADIUS)) {
                target.addEffect(new MobEffectInstance(GoetyEffects.TANGLED.get(), Math.min(2, remaining), 0, false, false, true), caster);
            }
        }
        void damage(int hits) {
            DamageSource source = new DamageSource(level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE)
                    .getHolderOrThrow(DamageTypes.WITHER), visual, caster);
            BattleFocusCombat.damage(level, caster, center, StarFantasyCageGeometry.DAMAGE_RADIUS, source, damage, hits);
        }
        void impact(int index) {
            if (index == 0) {
                Vec3 point = visual.worldPoint(StarFantasyCageGeometry.finalBurstCenter());
                level.playSound(null, point.x, point.y, point.z, BattleFocusContent.EVERNIGHT_BURST.get(), SoundSource.PLAYERS, 3, 1);
                StarFantasyVfx.areaShake(visual, center, 64, 0, 8, 22, .4F);
            }
            damage(1);
        }
        void finish() { StarFantasyVfx.areaShake(visual, center, 64, 22, .6F); damage(finalHits); }
    }
    private EvernightCast() {}
}
