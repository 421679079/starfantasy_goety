package com.starfantasy.goety.client;

import com.starfantasy.goety.entity.HadesServantEntity;
import com.starfantasy.goety.entity.riding.HadesRiderSeat;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

/** Render-only seating: neither player coordinates nor server collision/attack state are changed. */
public final class HadesSeatInterpolation {
    private static final Map<HadesServantEntity, Clock> CLOCKS = new WeakHashMap<>();
    private HadesSeatInterpolation() { }

    public static HadesRiderSeat.Pose pose(HadesServantEntity entity, float partialTick) {
        return CLOCKS.computeIfAbsent(entity, key -> new Clock()).sample(entity, partialTick);
    }

    public static Vec3 renderOffset(Entity passenger, float partialTick) {
        if (!(passenger.m_20202_() instanceof HadesServantEntity hades) || !hades.m_6084_()) return Vec3.f_82478_;
        float partial = Mth.m_14036_(partialTick, 0, 1);
        Vec3 base = interpolated(hades, partial), actual = interpolated(passenger, partial);
        float yaw = Mth.m_14189_(partial, hades.f_20884_, hades.f_20883_);
        Vec3 desired = HadesRiderSeat.position(pose(hades, partial), base, yaw)
                .m_82520_(0, passenger.m_6049_(), 0);
        // Keep the server's protection against nearby walls/ceilings in the visual correction too.
        return Entity.m_198894_(passenger, desired.m_82546_(actual),
                passenger.m_20191_().m_82383_(actual.m_82546_(passenger.m_20182_())),
                passenger.m_9236_(), java.util.List.of());
    }

    private static Vec3 interpolated(Entity entity, float partial) {
        return new Vec3(Mth.m_14139_(partial, entity.f_19854_, entity.m_20185_()),
                Mth.m_14139_(partial, entity.f_19855_, entity.m_20186_()),
                Mth.m_14139_(partial, entity.f_19856_, entity.m_20189_()));
    }

    private static final class Clock {
        private double lastTime = Double.NaN, anchorTime, anchorAge, displayAge, blendStart;
        private int phase = -1, reportedAge = -1;
        private HadesRiderSeat.Pose lastPose, blendFrom;

        HadesRiderSeat.Pose sample(HadesServantEntity entity, float partial) {
            double now = entity.f_19797_ + (double)partial;
            int age = entity.attackAge();
            int nextPhase = entity.attackType() * 10 + (entity.attackType() == HadesServantEntity.CLAW_COMBO && age >= 30
                    || entity.attackType() == HadesServantEntity.DIVE_RAY && age >= 60 ? 1 : 0);
            if (entity.attackType() == 0 && entity.isRiderMoving()) nextPhase = 2;
            if (now == lastTime && nextPhase == phase && reportedAge == age) return lastPose;
            double dt = Double.isNaN(lastTime) ? 0 : Math.max(0, now - lastTime);
            boolean reset = phase != nextPhase || Double.isNaN(lastTime) || now < lastTime || dt > 10;
            if (reset) {
                blendFrom = lastPose;
                blendStart = now;
                phase = nextPhase;
                displayAge = age - 1 + partial;
                anchorAge = age + partial;
                anchorTime = now;
            } else {
                if (reportedAge != age) {
                    anchorAge = age + partial;
                    anchorTime = now;
                }
                displayAge += dt;
                double error = anchorAge + now - anchorTime - 1 - displayAge;
                displayAge += error * (1 - Math.exp(-dt * 0.35));
            }
            reportedAge = age;
            lastTime = now;
            HadesRiderSeat.Pose target = HadesRiderSeat.poseAt(entity, displayAge,
                    entity.m_9236_().m_46467_() + partial - 1);
            float blend = (float)Math.min(1, Math.max(0, (now - blendStart) / 3));
            lastPose = blendFrom == null || blend >= 1 ? target : new HadesRiderSeat.Pose(
                    blend(blendFrom.body(), target.body(), blend), blend(blendFrom.head(), target.head(), blend));
            if (blend >= 1) blendFrom = null;
            return lastPose;
        }
    }

    private static HadesRiderSeat.BonePose blend(HadesRiderSeat.BonePose a, HadesRiderSeat.BonePose b, float t) {
        Vector3f rotation = new Vector3f();
        for (int axis = 0; axis < 3; axis++) {
            double from = a.rotation().get(axis), delta = b.rotation().get(axis) - from;
            delta = Math.atan2(Math.sin(delta), Math.cos(delta));
            rotation.setComponent(axis, (float)(from + delta * t));
        }
        return new HadesRiderSeat.BonePose(new Vector3f(a.position()).lerp(b.position(), t), rotation,
                new Vector3f(a.scale()).lerp(b.scale(), t));
    }
}
