package com.starfantasy.goety.client;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SmokeParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;

/** Exactly twice the scale factor of vanilla's LargeSmokeParticle (5.0 vs 2.5). */
public final class ApollyonCastingSmokeParticle extends SmokeParticle {
    private ApollyonCastingSmokeParticle(ClientLevel level, double x, double y, double z,
                                         double velocityX, double velocityY, double velocityZ,
                                         SpriteSet sprites) {
        super(level, x, y, z, velocityX, velocityY, velocityZ, 5.0F, sprites);
    }

    public static final class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle m_6966_(SimpleParticleType type, ClientLevel level,
                                double x, double y, double z,
                                double velocityX, double velocityY, double velocityZ) {
            return new ApollyonCastingSmokeParticle(
                    level, x, y, z, velocityX, velocityY, velocityZ, this.sprites);
        }
    }
}
