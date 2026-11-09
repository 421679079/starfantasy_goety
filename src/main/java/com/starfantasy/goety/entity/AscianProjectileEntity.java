package com.starfantasy.goety.entity;

import com.starfantasy.goety.combat.ascian.AscianDamage;
import com.starfantasy.goety.registry.AscianKingRegistry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.projectile.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.*;
import net.minecraftforge.network.NetworkHooks;

/** Includes the skull style without inheriting WitherSkull or its explosion behaviour. */
public final class AscianProjectileEntity extends Projectile {
    private static final EntityDataAccessor<Integer> STYLE=SynchedEntityData.m_135353_(AscianProjectileEntity.class,EntityDataSerializers.f_135028_);
    private int phase=1,impactTicks;
    private Vec3 impact;
    public AscianProjectileEntity(EntityType<? extends AscianProjectileEntity> type,Level level) { super(type,level); this.m_20242_(true); }
    public static AscianProjectileEntity spawn(AscianKingEntity boss,Vec3 position,Vec3 velocity,int style,int phase,Vec3 impact,int ticks) {
        AscianProjectileEntity e=new AscianProjectileEntity(AscianKingRegistry.PROJECTILE.get(),boss.m_9236_());
        e.m_5602_(boss);e.phase=phase;e.impact=impact;e.impactTicks=ticks;e.f_19804_.m_135381_(STYLE,style);
        e.m_6034_(position.f_82479_,position.f_82480_,position.f_82481_); e.m_20256_(velocity);
        boss.m_9236_().m_7967_(e);return e;
    }
    public int style(){return this.f_19804_.m_135370_(STYLE);}
    @Override protected void m_8097_(){this.f_19804_.m_135372_(STYLE,1);}
    @Override public void m_8119_(){
        super.m_8119_();this.m_20242_(true);
        if(this.m_9236_().f_46443_)clientTrail();
        if(!this.m_9236_().f_46443_) {
            if(!(this.m_19749_() instanceof AscianKingEntity boss)||!boss.m_6084_()){this.m_146870_();return;}
            if(impact!=null && this.f_19797_>=impactTicks){detonate(boss,impact);return;}
            if(impact==null&&this.f_19797_>(style()==5?120:80)){this.m_146870_();return;}
            if(impact==null){HitResult hit=ProjectileUtil.m_278158_(this,this::m_5603_);if(hit.m_6662_()!=HitResult.Type.MISS){detonate(boss,hit.m_82450_());return;}}
        }
        Vec3 v=this.m_20184_();this.m_6034_(this.m_20185_()+v.f_82479_,this.m_20186_()+v.f_82480_,this.m_20189_()+v.f_82481_);
        ProjectileUtil.m_37284_(this,.2F);
    }
    @Override protected boolean m_5603_(Entity entity) {
        return impact==null && entity instanceof LivingEntity living && this.m_19749_() instanceof AscianKingEntity boss
                && !boss.combat.skip(living) && super.m_5603_(entity);
    }
    private void detonate(AscianKingEntity boss,Vec3 p){
        var damage=impact!=null?AscianDamage.ambience(style()):AscianDamage.barrage(style(),phase);
        if(impact==null)boss.combat.spells.barrageArea(p,2,damage,style(),style()==1&&phase==1);
        else boss.combat.spells.area(p,2,damage,style(),style()==1&&phase==1,0);
        boss.m_9236_().m_6263_(null,p.f_82479_,p.f_82480_,p.f_82481_,net.minecraft.sounds.SoundEvents.f_11913_,net.minecraft.sounds.SoundSource.HOSTILE,1,1);
        com.starfantasy.library.vfx.StarFantasyVfx.swordExplosion(this,p,4);
        com.starfantasy.library.vfx.StarFantasyVfx.horizontalRoarWave(this,p,1,14,4);
        this.m_146870_();
    }
    private void clientTrail(){
        if(style()==3)return;
        var primary=style()==2?net.minecraft.core.particles.ParticleTypes.f_175821_:style()>=4?net.minecraft.core.particles.ParticleTypes.f_123799_:net.minecraft.core.particles.ParticleTypes.f_123744_;
        var secondary=style()==2?net.minecraft.core.particles.ParticleTypes.f_123810_:style()>=4?net.minecraft.core.particles.ParticleTypes.f_123760_:net.minecraft.core.particles.ParticleTypes.f_123762_;
        Vec3 v=this.m_20184_().m_82490_(-.18);
        for(int i=0;i<3;i++)this.m_9236_().m_7106_(primary,this.m_20185_()+(this.f_19796_.m_188500_()-.5)*.22,this.m_20186_()+(this.f_19796_.m_188500_()-.5)*.22,this.m_20189_()+(this.f_19796_.m_188500_()-.5)*.22,v.f_82479_,v.f_82480_,v.f_82481_);
        this.m_9236_().m_7106_(secondary,this.m_20185_(),this.m_20186_(),this.m_20189_(),v.f_82479_*.35,v.f_82480_*.35,v.f_82481_*.35);
    }
    @Override protected void m_7380_(CompoundTag n){super.m_7380_(n);n.m_128405_("Style",style());n.m_128405_("Phase",phase);n.m_128405_("ImpactTicks",impactTicks);n.m_128405_("Age",this.f_19797_);if(impact!=null){n.m_128347_("ImpactX",impact.f_82479_);n.m_128347_("ImpactY",impact.f_82480_);n.m_128347_("ImpactZ",impact.f_82481_);}}
    @Override protected void m_7378_(CompoundTag n){super.m_7378_(n);this.f_19804_.m_135381_(STYLE,Math.max(1,Math.min(5,n.m_128451_("Style"))));phase=Math.max(1,Math.min(6,n.m_128451_("Phase")));impactTicks=n.m_128451_("ImpactTicks");this.f_19797_=n.m_128451_("Age");if(n.m_128441_("ImpactX"))impact=new Vec3(n.m_128459_("ImpactX"),n.m_128459_("ImpactY"),n.m_128459_("ImpactZ"));}
    @Override public net.minecraft.network.protocol.Packet<net.minecraft.network.protocol.game.ClientGamePacketListener> m_5654_(){return NetworkHooks.getEntitySpawningPacket(this);}
}
