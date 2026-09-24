package com.starfantasy.goety.magic.focus;

import com.starfantasy.library.vfx.StarFantasyVfx;
import com.starfantasy.goety.magic.focus.entity.*;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Port of the Sword Attack super arrow formation, with ten damaging arrivals and fixed spell damage. */
@Mod.EventBusSubscriber(modid = "starfantasy_goety")
public final class FlowerArrowRain {
    public static final int ARROW_COUNT = 36, FIRST_IMPACT_TICK = 30, IMPACT_INTERVAL = 1, IMPACT_COUNT = 10, FINAL_DELAY = 10;
    private static final Map<UUID, Rain> ACTIVE = new LinkedHashMap<>();
    public static final int BLUE = 0x66DFFF, PINK = 0xED99EB;
    private static final int[] PALETTE = {BLUE, 0x829DFF, 0xB096FF, PINK};
    public static void clearRuntimeState() { ACTIVE.values().forEach(Rain::discard); ACTIVE.clear(); }
    @SubscribeEvent public static void stop(ServerStoppingEvent event) { clearRuntimeState(); }
    public static void spawn(ServerLevel level, LivingEntity caster, float damage, int finalHits) {
        if (!caster.isAlive()) return;
        Vec3 forward = Vec3.directionFromRotation(0, caster.getYRot());
        Vec3 right = new Vec3(-forward.z, 0, forward.x);
        Vec3 direction = forward.scale(25).add(0, -10, 0).normalize();
        Vec3 up = direction.cross(right).normalize();
        Vec3 center = BattleFocusPlacement.ground(level, caster, caster.position().add(forward.scale(15)));
        if (center == null) return;
        Rain rain = new Rain(level, caster, center, damage, finalHits);
        int[] slots = new int[IMPACT_COUNT];
        for (int i = 0; i < IMPACT_COUNT; i++) slots[i] = i;
        for (int i = IMPACT_COUNT - 1; i > 0; i--) {
            int other = caster.getRandom().nextInt(i + 1), slot = slots[i]; slots[i] = slots[other]; slots[other] = slot;
        }
        for (int i = 0; i < ARROW_COUNT; i++) {
            double x, y, depth; int travel;
            if (i < IMPACT_COUNT) {
                x = (slots[i] - (IMPACT_COUNT - 1) * .5) * 2; y = 0;
                travel = FIRST_IMPACT_TICK + i * IMPACT_INTERVAL;
                depth = 30 + i * 3.0 / (IMPACT_COUNT - 1);
            } else {
                int slot = i - IMPACT_COUNT;
                x = ((slot % 6) - 2.5) * 3.5 + (caster.getRandom().nextDouble() - .5) * .8;
                y = ((slot / 6) - 2) * 1.65 + (caster.getRandom().nextDouble() - .5) * .65;
                travel = FIRST_IMPACT_TICK + FINAL_DELAY + 2 + caster.getRandom().nextInt(8);
                depth = 35 + caster.getRandom().nextDouble() * 3;
            }
            Vec3 end = center.add(right.scale(x)).add(up.scale(y));
            Vec3 start = end.subtract(direction.scale(depth));
            int color = paletteColor(caster.getRandom().nextFloat());
            FlowerArrowEntity arrow = BattleFocusContent.FLOWER_ARROW.get().create(level);
            arrow.configure(start, end, travel, color); level.addFreshEntity(arrow); rain.arrows.add(arrow);
            if (i < IMPACT_COUNT) rain.impacts[i] = end;
        }
        level.playSound(null, caster.getX(), caster.getY(), caster.getZ(), BattleFocusContent.FLOWER_START.get(), SoundSource.PLAYERS, 1, 1);
        ACTIVE.put(UUID.randomUUID(), rain);
    }
    public static int paletteColor(float t) {
        float position = Math.max(0, Math.min(.999999F, t)) * (PALETTE.length - 1);
        int index = (int) position; float f = position - index;
        int a = PALETTE[index], b = PALETTE[index + 1], result = 0;
        for (int shift : new int[]{16, 8, 0}) result |= (int) (((a >> shift) & 255) * (1-f) + ((b >> shift) & 255) * f) << shift;
        return result;
    }
    @SubscribeEvent public static void tick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        for (var entry : new ArrayList<>(ACTIVE.entrySet())) {
            Rain rain = entry.getValue();
            if (!rain.caster.isAlive() || rain.caster.isRemoved() || rain.caster.level() != rain.level
                    || !rain.level.hasChunkAt(BlockPos.containing(rain.center))) {
                rain.discard(); ACTIVE.remove(entry.getKey()); continue;
            }
            long elapsed = rain.level.getGameTime() - rain.started;
            while (rain.hits < IMPACT_COUNT && elapsed >= FIRST_IMPACT_TICK + rain.hits * IMPACT_INTERVAL) rain.impact(rain.hits++);
            if (elapsed >= FIRST_IMPACT_TICK + FINAL_DELAY) {
                ACTIVE.remove(entry.getKey());
                try { rain.finish(); } catch (RuntimeException exception) { rain.discard(); throw exception; }
            }
        }
    }
    private static final class Rain {
        final ServerLevel level; final LivingEntity caster; final Vec3 center; final float damage; final int finalHits;
        final long started; final List<FlowerArrowEntity> arrows = new ArrayList<>();
        final Vec3[] impacts = new Vec3[IMPACT_COUNT]; int hits;
        Rain(ServerLevel level, LivingEntity caster, Vec3 center, float damage, int finalHits) {
            this.level=level; this.caster=caster; this.center=center; this.damage=damage; this.finalHits=finalHits; started=level.getGameTime();
        }
        void discard() { arrows.forEach(Entity::discard); }
        void impact(int index) {
            Vec3 point = impacts[index];
            if (index < arrows.size()) arrows.get(index).explode();
            level.playSound(null, point.x, point.y, point.z, SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 1.4F, .95F + caster.getRandom().nextFloat() * .1F);
            BattleFocusCombat.damage(level, caster, point, 5, caster.damageSources().sonicBoom(caster), damage, 1);
        }
        void finish() {
            // Remaining arrows explode at their current flight positions, without damage or sound.
            for (FlowerArrowEntity arrow : arrows) {
                if (!arrow.isRemoved() && !arrow.isExploded()) {
                    arrow.explode();
                }
            }
            StarFantasyVfx.finalExplosion(caster, center, 0xACA2FF, .75D, true);
            FlowerBurstRibbonEntity.spawn(level, center, 0x91CFFF, true);
            BattleFocusCombat.damage(level, caster, center, 30, caster.damageSources().sonicBoom(caster), damage, finalHits);
            level.playSound(null, center.x, center.y, center.z, BattleFocusContent.FLOWER_FINAL.get(), SoundSource.PLAYERS, 2.5F, 1F);
            StarFantasyVfx.areaShake(caster, center, 64, 40, .6F);
        }
    }
    private FlowerArrowRain() {}
}
