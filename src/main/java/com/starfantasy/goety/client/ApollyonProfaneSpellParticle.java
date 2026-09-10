package com.starfantasy.goety.client;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;

/** Dark-green tint of Goety's enlarged cult spell particle. */
public final class ApollyonProfaneSpellParticle extends TextureSheetParticle {
    private final SpriteSet sprites;

    private ApollyonProfaneSpellParticle(
            ClientLevel level, double x, double y, double z,
            double velocityX, double velocityY, double velocityZ, SpriteSet sprites) {
        super(level, x, y, z,
                0.5D - level.m_213780_().m_188500_(), velocityY,
                0.5D - level.m_213780_().m_188500_());
        this.sprites = sprites;
        this.f_172258_ = 0.96F;
        this.f_107226_ = -0.1F;
        this.f_172259_ = true;
        this.f_107216_ *= 0.2D;
        if (velocityX == 0.0D && velocityZ == 0.0D) {
            this.f_107215_ *= 0.1D;
            this.f_107217_ *= 0.1D;
        }
        this.f_107663_ *= 2.0F;
        this.f_107225_ = Math.max(8,
                (int) (8.0D / (Math.random() * 0.8D + 0.2D)));
        this.f_107219_ = false;
        this.m_107253_(0.13F, 0.46F, 0.08F);
        this.m_108339_(sprites);
    }

    @Override
    public void m_5989_() {
        super.m_5989_();
        this.m_108339_(this.sprites);
        this.m_107271_(Mth.m_14036_(
                1.0F - this.f_107224_ / (float) this.f_107225_ * 0.9F,
                0.05F, 1.0F));
    }

    @Override
    public ParticleRenderType m_7556_() {
        return ParticleRenderType.f_107431_;
    }

    @Override
    protected int m_6355_(float partialTick) {
        return 0xF000F0;
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
            return new ApollyonProfaneSpellParticle(
                    level, x, y, z, velocityX, velocityY, velocityZ, this.sprites);
        }
    }
}
