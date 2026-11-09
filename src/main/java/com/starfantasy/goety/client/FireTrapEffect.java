package com.starfantasy.goety.client;

import com.Polarice3.Goety.client.particles.ModParticleTypes;
import com.Polarice3.Goety.client.particles.WindParticle;
import com.Polarice3.Goety.common.entities.util.FireBlastTrap;
import net.minecraft.client.particle.Particle;
import com.starfantasy.goety.StarFantasyGoetyMod;
import com.starfantasy.goety.network.ClientboundFireTrapWavePacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ParticleStatus;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.UUID;

/** Local Goety-style visuals only. Hit detection and damage remain on the server. */
@Mod.EventBusSubscriber(modid = StarFantasyGoetyMod.MODID, value = Dist.CLIENT)
public final class FireTrapEffect {
    private static final List<Trap> ACTIVE = new ArrayList<>();
    private static ClientLevel currentLevel;
    private FireTrapEffect() {}

    /** Native roar traps keep their gameplay and existing particle spawning path. */
    public static Particle batchNativeWind(Particle particle) {
        ClientLevel level = Minecraft.m_91087_().f_91073_;
        if (level != null && particle != null && particle.getClass() == WindParticle.class
                && ((WindParticle)particle).getEntity() instanceof FireBlastTrap) {
            return new FireTrapWindParticle(level, (WindParticle)particle);
        }
        return particle;
    }

    public static void receive(ClientboundFireTrapWavePacket wave) {
        receiveScaled(wave,1F);
    }

    public static void receiveScaled(ClientboundFireTrapWavePacket wave,float visualScale) {
        ClientLevel level = Minecraft.m_91087_().f_91073_;
        switchLevel(level);
        if (level == null || !level.m_46472_().m_135782_().equals(wave.dimension())) return;
        if (wave.centers().isEmpty()) {
            ACTIVE.removeIf(t -> { if (!t.owner.equals(wave.owner())) return false; t.active = false; return true; });
            return;
        }
        // Account for packet transit without replaying a backlog of particles in one frame.
        int age = (int)Math.max(0L, Math.min(30L, level.m_46467_() - wave.startTick()));
        if (age >= 30) return;
        Random seeds = new Random(wave.seed());
        for (Vec3 center : wave.centers()) ACTIVE.add(new Trap(wave.owner(), wave.ownerId(), center, seeds.nextLong(), age,visualScale));
    }

    private static void switchLevel(ClientLevel level) {
        if (level == currentLevel) return;
        for (Trap trap : ACTIVE) trap.active = false;
        ACTIVE.clear(); currentLevel = level;
    }

    @SubscribeEvent
    public static void tick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft minecraft = Minecraft.m_91087_();
        ClientLevel level = minecraft.f_91073_;
        switchLevel(level);
        if (level == null || minecraft.m_91104_()) return;
        for (Iterator<Trap> it = ACTIVE.iterator(); it.hasNext();) {
            Trap trap = it.next();
            Entity owner = level.m_6815_(trap.ownerId);
            if (!trap.active || trap.age >= 30 || (owner != null &&
                    (!owner.m_20148_().equals(trap.owner) || !owner.m_6084_()))) {
                trap.active = false; it.remove(); continue;
            }
            trap.age++;
            trap.emit(level, minecraft);
        }
    }

    static final class Trap {
        final UUID owner;
        final int ownerId;
        final Vec3 base;
        final Random random;
        final float visualScale;
        int age;
        boolean active = true;
        Trap(UUID owner, int ownerId, Vec3 base, long seed, int age,float visualScale) {
            this.owner = owner; this.ownerId = ownerId; this.base = base;
            this.random = new Random(seed); this.age = age;
            this.visualScale=visualScale;
        }
        Vec3 position() {
            // Native traps accelerate upward by 0.25 blocks/tick after tick 20.
            int n = Math.max(0, age - 20);
            return base.m_82520_(0, 0.125D * n * (n + 1), 0);
        }
        private void disk(ClientLevel level, ParticleOptions type, int count, double velocityY, Vec3 center) {
            for (int i = 0; i < count; i++) {
                double angle = random.nextDouble() * Math.PI * 2;
                double radius = Math.sqrt(random.nextDouble()) * 1.5D * visualScale;
                level.m_7106_(type, center.f_82479_ + Math.cos(angle) * radius, center.f_82480_,
                        center.f_82481_ + Math.sin(angle) * radius, 0, velocityY, 0);
            }
        }
        void emit(ClientLevel level, Minecraft minecraft) {
            int n = Math.max(0, age - 21);
            Vec3 emission = base.m_82520_(0, 0.125D * n * (n + 1), 0);
            // Match ordinary server particle delivery distance; direct engine insertion
            // would otherwise render distant ribbons that vanilla would have rejected.
            Entity camera = minecraft.m_91288_();
            if (camera == null || camera.m_20238_(emission) > 1024.0D) return;
            disk(level, ModParticleTypes.BURNING.get(), 8, 0, emission);
            wind(level, minecraft, 0.5F + random.nextFloat() * 0.5F);
            wind(level, minecraft, 1.5F + random.nextFloat() * 0.5F);
            if (age == 20) {
                disk(level, ParticleTypes.f_123744_, 128, 0.25D, emission);
                level.m_7785_(base.f_82479_, base.f_82480_, base.f_82481_, SoundEvents.f_11913_,
                        SoundSource.NEUTRAL, 1.0F, 0.5F, false);
            }
        }
        private void wind(ClientLevel level, Minecraft minecraft, float width) {
            ParticleStatus quality = minecraft.f_91066_.m_231929_().m_231551_();
            if (quality == ParticleStatus.MINIMAL ||
                    (quality == ParticleStatus.DECREASED && random.nextInt(3) == 0)) return;
            minecraft.f_91061_.m_107344_(new FireTrapWindParticle(level, this, width*visualScale));
        }
    }
}
