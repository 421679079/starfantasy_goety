package com.starfantasy.goety.client;

import com.starfantasy.goety.entity.LocalIceChunkEntity;
import com.Polarice3.Goety.client.particles.GatherFrostParticleOption;
import com.Polarice3.Goety.client.particles.CircleExplodeParticleOption;
import com.Polarice3.Goety.utils.ColorUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import java.util.Random;

/** All ice particles are generated locally; impact uses one compact message. */
public final class IceChunkEffect {
    public static void formation(LocalIceChunkEntity ice){
        var mc=Minecraft.m_91087_();var level=mc.f_91073_;if(level==null||mc.m_91288_()==null||mc.m_91288_().m_20238_(ice.m_20182_())>4096)return;
        Random random=new Random(((long)ice.m_19879_()<<32)^ice.f_19797_);
        Vec3 p=ice.m_20182_().m_82520_(0,1,0);
        double outer=ice instanceof com.starfantasy.goety.entity.ApollyonPageantIceChunkEntity?8:4;
        for(int i=0;i<8;i++){double a=random.nextDouble()*Math.PI*2,r=outer*(.75+random.nextDouble()*.25);
            level.m_7106_(new GatherFrostParticleOption(p),p.f_82479_+Math.cos(a)*r,p.f_82480_,p.f_82481_+Math.sin(a)*r,0,0,0);}
    }
    public static void impact(Vec3 p,float scale,long seed){
        var mc=Minecraft.m_91087_();var level=mc.f_91073_;if(level==null||mc.m_91288_()==null||mc.m_91288_().m_20238_(p)>16384)return;
        Random random=new Random(seed);
        int count=scale>1?512:256;
        var debris=new BlockParticleOption(ParticleTypes.f_123794_,Blocks.f_50354_.m_49966_());
        for(int i=0;i<count;i++)level.m_7106_(debris,p.f_82479_+random.nextGaussian()*scale*.75,p.f_82480_+1.5+random.nextGaussian()*scale*.75,p.f_82481_+random.nextGaussian()*scale*.75,random.nextGaussian(),random.nextGaussian(),random.nextGaussian());
        level.m_7106_(new CircleExplodeParticleOption(new ColorUtil(scale>1?0xFFFFBF:0xFFFEFF),5*scale,1),p.f_82479_,p.f_82480_,p.f_82481_,0,0,.5);
        if(scale<=1)for(int i=0;i<60;i++){double a=random.nextDouble()*Math.PI*2,r=random.nextDouble()*4;level.m_7106_(ParticleTypes.f_123759_,p.f_82479_,p.f_82480_,p.f_82481_,Math.cos(a)*r,.01+random.nextDouble()*.5,Math.sin(a)*r);}
        level.m_7785_(p.f_82479_,p.f_82480_,p.f_82481_,com.Polarice3.Goety.init.ModSounds.ICE_CHUNK_HIT.get(),net.minecraft.sounds.SoundSource.HOSTILE,2,1,false);
    }
    private IceChunkEffect(){}
}
