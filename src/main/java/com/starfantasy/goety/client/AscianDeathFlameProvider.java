package com.starfantasy.goety.client;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.FlameParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;

/** Keep the existing death-flame registry ID and motion, tint its sprite crimson. */
public final class AscianDeathFlameProvider implements ParticleProvider<SimpleParticleType> {
    private final FlameParticle.Provider vanilla;
    public AscianDeathFlameProvider(SpriteSet sprites){vanilla=new FlameParticle.Provider(sprites);}
    @Override public Particle m_6966_(SimpleParticleType type,ClientLevel level,double x,double y,double z,
            double vx,double vy,double vz){
        Particle flame=vanilla.m_6966_(type,level,x,y,z,vx,vy,vz);
        if(flame!=null)flame.m_107253_(1F,.04F,.04F);
        return flame;
    }
}
