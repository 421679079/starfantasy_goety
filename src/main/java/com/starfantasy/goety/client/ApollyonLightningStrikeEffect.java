package com.starfantasy.goety.client;

import com.Polarice3.Goety.Goety;
import com.Polarice3.Goety.client.particles.CircleExplodeParticleOption;
import com.Polarice3.Goety.client.particles.GatherTrailParticleOption;
import com.Polarice3.Goety.client.particles.SphereExplodeParticleOption;
import com.Polarice3.Goety.init.ModSounds;
import com.Polarice3.Goety.utils.ColorUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;

import java.util.Random;

/** Recreates MagicLightningTrap.finalizeAttack locally from one compact message. */
public final class ApollyonLightningStrikeEffect {
    private static final int MAGIC_LIGHTNING_COLOR = 11660252;
    private static final int MAIN_BOLT_LIFESPAN = 10;
    private static final int BRANCH_LIFESPAN = 12;

    private ApollyonLightningStrikeEffect() {
    }

    public static void spawn(Vec3 center, long seed, float effectSize) {
        ClientLevel level = Minecraft.m_91087_().f_91073_;
        if (level == null) {
            return;
        }

        ColorUtil color = new ColorUtil(MAGIC_LIGHTNING_COLOR);
        ColorUtil branchColor = new ColorUtil(
                color.red(), color.green(), color.blue(), 0.8F);
        level.m_7785_(center.f_82479_, center.f_82480_, center.f_82481_,
                (SoundEvent) ModSounds.THUNDER_STRIKE_FAST.get(),
                SoundSource.HOSTILE, 1.0F, 1.0F, false);
        float size = Math.max(0.1F, effectSize);
        level.m_7106_(new CircleExplodeParticleOption(color, size, 1),
                center.f_82479_, center.f_82480_, center.f_82481_, 0.0D, 0.0D, 0.0D);
        level.m_7106_(new SphereExplodeParticleOption(color, size, 1),
                center.f_82479_, center.f_82480_, center.f_82481_, 0.0D, 0.0D, 0.0D);

        Goety.PROXY.lightningBolt(center.m_82520_(0.0D, 250.0D, 0.0D), center,
                color, MAIN_BOLT_LIFESPAN);

        Random random = new Random(seed);
        for (int i = 0; i < 8; ++i) {
            Vec3 gatherFrom = center.m_82520_(
                    (random.nextFloat() - 0.5F) * 6.0D,
                    3.0D,
                    (random.nextFloat() - 0.5F) * 6.0D);
            level.m_7106_(new GatherTrailParticleOption(color, gatherFrom),
                    center.f_82479_, center.f_82480_, center.f_82481_,
                    0.0D, 0.0D, 0.0D);
        }

        Vec3 branchStart = center.m_82520_(0.0D, 1.0D, 0.0D);
        for (int i = 0; i < 16; ++i) {
            int xDirection = random.nextInt(9) - 4;
            int zDirection = random.nextInt(9) - 4;
            Vec3 branchEnd = branchStart.m_82520_(
                    random.nextDouble() * xDirection,
                    random.nextDouble(),
                    random.nextDouble() * zDirection);
            Goety.PROXY.shock(branchStart, branchEnd, branchColor, BRANCH_LIFESPAN);
        }
    }
}
