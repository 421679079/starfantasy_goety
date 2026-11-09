package com.starfantasy.goety.client;

import com.starfantasy.goety.entity.AscianKingEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ParticleStatus;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Real vanilla flame particles, emitted once per client tick rather than per render pass. */
@Mod.EventBusSubscriber(modid="starfantasy_goety",value=Dist.CLIENT)
public final class AscianSummonParticles {
    @SubscribeEvent public static void tick(LivingEvent.LivingTickEvent event){
        if(!(event.getEntity() instanceof AscianKingEntity boss)||!boss.m_9236_().f_46443_
                ||!boss.m_6084_()||!boss.isSummoning())return;
        var minecraft=Minecraft.m_91087_();
        if(minecraft.f_91074_==null||minecraft.f_91073_!=boss.m_9236_()||minecraft.f_91074_.m_20280_(boss)>96*96)return;
        var quality=minecraft.f_91066_.m_231929_().m_231551_();
        if(quality==ParticleStatus.MINIMAL)return;
        float age=boss.summonAge(0);
        if(age>=48)return; // The last 12-tick flames disappear by the end of materialization.
        var random=boss.m_217043_();
        double targetY=boss.m_20186_()+6.3*AscianSummonMesh.reveal(age+12);
        // Vanilla RisingParticle damps velocity by .96 each tick. Converge in 12 ticks.
        double speed=.04/(1-Math.pow(.96,12));
        for(int i=0;i<(quality==ParticleStatus.DECREASED?6:12);i++){
            double angle=random.m_188500_()*Math.PI*2,radius=2.4+random.m_188500_()*1.6;
            double dx=Math.cos(angle)*radius,dz=Math.sin(angle)*radius,dy=.2+random.m_188500_()*.8;
            var flame=minecraft.f_91061_.m_107370_(ParticleTypes.f_123744_,
                    boss.m_20185_()+dx,targetY+dy,boss.m_20189_()+dz,0,0,0);
            if(flame!=null){
                flame.m_172260_(-dx*speed,-dy*speed,-dz*speed);
                flame.m_107257_(12);
            }
        }
    }
    private AscianSummonParticles(){}
}
