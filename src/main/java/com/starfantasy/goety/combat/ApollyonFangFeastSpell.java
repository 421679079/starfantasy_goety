package com.starfantasy.goety.combat;

import com.starfantasy.goety.entity.ApollyonEntity;
import com.starfantasy.goety.entity.ApollyonFangEntity;
import com.starfantasy.library.vfx.StarFantasyVfx;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

/** Apollyon's independent, fixed two-wave copy of the Witch King's fang pattern. */
public final class ApollyonFangFeastSpell {
    public static final int CAST_TICKS = 80;
    private static final int FIRST_WARNING_TICK = 40;
    private static final int FIRST_VISUAL_TICK = 52;
    private static final int FIRST_HIT_TICK = 60;
    private static final int SECOND_VISUAL_TICK = 72;
    private static final int SECOND_HIT_TICK = 80;
    private static final double HIT_RADIUS = 1.5D;
    private static final double HIT_RADIUS_SQR = HIT_RADIUS * HIT_RADIUS;
    private static final float DAMAGE = 40.0F;
    private static final float HEAL_RATIO = 0.10F;

    private final ApollyonEntity boss;
    private Vec3 anchor;
    private List<Vec3> points = List.of();
    private final List<ApollyonFangEntity> visuals = new ArrayList<>();

    public ApollyonFangFeastSpell(ApollyonEntity boss) {
        this.boss = boss;
    }

    public void tick(int tick, LivingEntity target) {
        if (tick == FIRST_WARNING_TICK) {
            anchor = target.m_20182_();
            warn(false);
        } else if (tick == FIRST_VISUAL_TICK || tick == SECOND_VISUAL_TICK) {
            spawnVisuals();
        } else if (tick == FIRST_HIT_TICK) {
            damageWave();
            warn(true);
        } else if (tick == SECOND_HIT_TICK) {
            damageWave();
        }
    }

    private void spawnVisuals() {
        for (Vec3 point : points) {
            float yaw = (float) Math.toDegrees(Math.atan2(
                    point.f_82481_ - anchor.f_82481_, point.f_82479_ - anchor.f_82479_));
            ApollyonFangEntity visual = ApollyonFangEntity.spawn(boss, point, yaw);
            if (visual != null) visuals.add(visual);
        }
        boss.m_9236_().m_6263_(null, anchor.f_82479_, anchor.f_82480_, anchor.f_82481_,
                SoundEvents.f_11865_, SoundSource.HOSTILE, 2.0F, 1.0F);
    }

    private void warn(boolean secondWave) {
        points = pattern(anchor, secondWave).stream().map(this::ground).toList();
        for (Vec3 point : points) {
            StarFantasyVfx.redGroundWarningCircle(boss, point, 20, HIT_RADIUS);
        }
    }

    private Vec3 ground(Vec3 point) {
        var level = boss.m_9236_();
        int base = Mth.m_14107_(point.f_82480_);
        for (int y = Math.min(level.m_151558_() - 1, base + 3);
             y >= Math.max(level.m_141937_() + 1, base - 16); --y) {
            BlockPos feet = new BlockPos(
                    Mth.m_14107_(point.f_82479_), y, Mth.m_14107_(point.f_82481_));
            BlockPos support = feet.m_7495_();
            if (level.m_8055_(support).m_60783_(level, support, Direction.UP)
                    && !level.m_8055_(feet).m_280555_()) {
                return new Vec3(point.f_82479_, y + 0.06D, point.f_82481_);
            }
        }
        return point.m_82520_(0.0D, 0.06D, 0.0D);
    }

    private void damageWave() {
        AABB area = new AABB(anchor, anchor).m_82377_(12.0D, 20.0D, 12.0D);
        boolean dealtDamage = false;
        for (LivingEntity target : boss.m_9236_().m_45976_(LivingEntity.class, area)) {
            if (!target.m_6084_() || boss.isFriendlyEntity(target)
                    || target instanceof Player player && (player.m_7500_() || player.m_5833_())) {
                continue;
            }
            for (Vec3 point : points) {
                if (intersects(target.m_20191_(), point)) {
                    if (target.m_6469_(boss.m_269291_().m_269104_(boss, boss), DAMAGE)) {
                        dealtDamage = true;
                    }
                    break;
                }
            }
        }
        if (dealtDamage) {
            boss.m_5634_(boss.m_21233_() * HEAL_RATIO);
        }
    }

    public static boolean intersects(AABB box, Vec3 point) {
        double dx = point.f_82479_
                - Mth.m_14008_(point.f_82479_, box.f_82288_, box.f_82291_);
        double dz = point.f_82481_
                - Mth.m_14008_(point.f_82481_, box.f_82290_, box.f_82293_);
        return dx * dx + dz * dz <= HIT_RADIUS_SQR
                && box.f_82292_ > point.f_82480_ && box.f_82289_ < point.f_82480_ + 1.6D;
    }

    public static List<Vec3> pattern(Vec3 center, boolean secondWave) {
        var result = new ArrayList<Vec3>(secondWave ? 21 : 20);
        addSquarePerimeter(result, center, 3, secondWave);
        addSquarePerimeter(result, center, 4, secondWave);
        addSquarePerimeter(result, center, 5, secondWave);
        if (secondWave) {
            result.add(center);
        } else {
            for (int x : new int[]{-2, 2}) {
                for (int z : new int[]{-2, 2}) {
                    result.add(center.m_82520_(x, 0.0D, z));
                }
            }
        }
        return result;
    }

    private static void addSquarePerimeter(
            List<Vec3> result, Vec3 center, int size, boolean secondWave) {
        int start = -(size - 1) * 2;
        for (int row = 0; row < size; ++row) {
            for (int column = 0; column < size; ++column) {
                if (row != 0 && row != size - 1 && column != 0 && column != size - 1) {
                    continue;
                }
                boolean corner = (row == 0 || row == size - 1)
                        && (column == 0 || column == size - 1);
                boolean belongsToSecondWave = size == 4
                        ? !corner : (row + column) % 2 == 0;
                if (belongsToSecondWave == secondWave) {
                    result.add(center.m_82520_(
                            start + column * 4, 0.0D, start + row * 4));
                }
            }
        }
    }

    public void finish(boolean completed) {
        if (!completed) visuals.forEach(Entity::m_146870_);
        visuals.clear();
        points = List.of();
        anchor = null;
    }
}
