package com.starfantasy.goety.combat.apostle;

import com.Polarice3.Goety.client.particles.ModParticleTypes;
import com.Polarice3.Goety.common.entities.projectiles.*;
import com.Polarice3.Goety.common.entities.util.*;
import com.Polarice3.Goety.common.magic.spells.nether.FireBlastSpell;
import com.Polarice3.Goety.common.items.ModItems;
import com.Polarice3.Goety.config.MobsConfig;
import com.Polarice3.Goety.utils.WandUtil;
import com.starfantasy.goety.entity.ApostleServantEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/** Native spell entities retain their own damage, duration and targeting rules. */
public final class ApostleServantNativeSpells {
    public static final int MONOLITH=1, INFERNO=2, CLOUD=3, TORNADO=4, ROAR=5;
    private ApostleServantNativeSpells() { }

    public static void particles(ApostleServantEntity caster, int spell) {
        double r=.5,g=.5,b=.5;
        switch(spell) {
            case 0 -> {r=1;g=.6;b=0;}
            case MONOLITH -> {r=.1;g=.1;b=.8;}
            case CLOUD -> {r=.3;g=0;b=0;}
            case TORNADO -> {r=1;g=.1;b=.1;}
            case ROAR -> {r=.8;g=.3;b=.8;}
        }
        var hand=ApostleSpellSupport.castingHand(caster);
        ((ServerLevel)caster.m_9236_()).m_8767_(ModParticleTypes.CULT_SPELL.get(),
                hand.f_82479_,hand.f_82480_,hand.f_82481_,0,r,g,b,1);
    }

    public static boolean canCloud(ApostleServantEntity caster) {
        return MobsConfig.ApostleHellCloud.get() && caster.m_9236_().m_45976_(SpellEntity.class,
                caster.m_20191_().m_82400_(64)).stream().filter(e->e.m_269323_()==caster).count()<2;
    }
    public static boolean canTornado(ApostleServantEntity caster) {
        return MobsConfig.ApostleTornado.get() && caster.m_9236_().m_45976_(net.minecraft.world.entity.Entity.class,
                caster.m_20191_().m_82400_(64)).stream().noneMatch(e->e instanceof FireTornado || e instanceof FireTornadoTrap);
    }
    public static void cloud(ApostleServantEntity caster, LivingEntity target) {
        HellCloud cloud=new HellCloud(caster.m_9236_(),caster,target);
        cloud.setRadius(caster.isSecondPhase()?3:2); cloud.setLifeSpan(1200);
        caster.m_9236_().m_7967_(cloud);
    }
    public static void tornado(ApostleServantEntity caster, LivingEntity target) {
        BlockPos.MutableBlockPos pos=target.m_20183_().m_122032_();
        while(pos.m_123342_()>caster.m_9236_().m_141937_() && !caster.m_9236_().m_8055_(pos).m_280555_()) pos.m_122173_(Direction.DOWN);
        FireTornadoTrap trap=new FireTornadoTrap(caster.m_9236_(),pos.m_123341_(),pos.m_123342_()+1,pos.m_123343_());
        trap.setOwner(caster); trap.setDuration(60); caster.m_9236_().m_7967_(trap);
    }
    public static void roarTrap(ApostleServantEntity caster) {
        FireBlastTrap trap=new FireBlastTrap(caster.m_9236_(),caster.m_20185_(),caster.m_20186_()+.25,caster.m_20189_());
        trap.setOwner(caster);
        // Keep the native Apostle's branch order and radii.
        trap.setAreaOfEffect(caster.m_21223_()<caster.m_21233_()/2?6:caster.m_21223_()<caster.m_21233_()/4?4.5F:3);
        caster.m_9236_().m_7967_(trap);
    }
    public static void roar(ApostleServantEntity caster) {
        if(!caster.isSecondPhase()) {
            FireBlastSpell spell=new FireBlastSpell();
            spell.mobSpellResult(caster,new ItemStack(ModItems.NETHER_STAFF.get()),WandUtil.getStats(caster,spell));
        }
    }
    public static void meteor(ApostleServantEntity caster) {
        BlockPos.MutableBlockPos pos=new BlockPos.MutableBlockPos(caster.m_20208_(.2),caster.m_20186_(),caster.m_20262_(.2));
        while(pos.m_123342_()<caster.m_20186_()+64 && !caster.m_9236_().m_8055_(pos).m_60804_(caster.m_9236_(),pos)) pos.m_122173_(Direction.UP);
        if(pos.m_123342_()<=caster.m_20186_()+32) return;
        int range=caster.m_21223_()<caster.m_21233_()/2?450:900;
        var random=caster.m_217043_();
        NetherMeteor meteor=new NetherMeteor(caster.m_9236_(),caster,
                random.m_188503_(range)*(random.m_188499_()?1:-1),-900,random.m_188503_(range)*(random.m_188499_()?1:-1));
        meteor.setDangerous(net.minecraftforge.event.ForgeEventFactory.getMobGriefingEvent(caster.m_9236_(),caster) && MobsConfig.ApocalypseMode.get());
        meteor.m_6034_(pos.m_123341_(),pos.m_123342_(),pos.m_123343_()); caster.m_9236_().m_7967_(meteor);
    }
}
