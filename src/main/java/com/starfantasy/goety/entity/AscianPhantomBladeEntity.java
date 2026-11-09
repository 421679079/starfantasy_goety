package com.starfantasy.goety.entity;

import com.starfantasy.goety.registry.AscianKingRegistry;
import java.util.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.*;
import net.minecraftforge.network.NetworkHooks;

/** Piercing, wall-passing crescent. Swept oriented hit volume follows the rendered tilt. */
public final class AscianPhantomBladeEntity extends Projectile implements net.minecraftforge.entity.IEntityAdditionalSpawnData {
    public static final int LIFETIME=30,COLOR=0xAA55FF;
    public static final double SPEED=2;
    private static final EntityDataAccessor<Float> ROLL=SynchedEntityData.m_135353_(AscianPhantomBladeEntity.class,EntityDataSerializers.f_135029_);
    private final Set<UUID> hit=new HashSet<>();
    private long spellEpoch;
    private Vec3 clientTarget;
    public AscianPhantomBladeEntity(EntityType<? extends AscianPhantomBladeEntity> type,Level level){super(type,level);this.f_19794_=true;this.m_20242_(true);}
    @Override protected void m_8097_(){this.f_19804_.m_135372_(ROLL,45F);}
    public float roll(){return this.f_19804_.m_135370_(ROLL);}
    public static AscianPhantomBladeEntity spawn(AscianKingEntity boss,Vec3 position,LivingEntity target,float roll){
        var e=new AscianPhantomBladeEntity(AscianKingRegistry.PHANTOM_BLADE.get(),boss.m_9236_());
        Vec3 direction=target.m_20191_().m_82399_().m_82520_(0,.4,0).m_82546_(position).m_82541_();
        if(direction.m_82556_()<1E-10)direction=boss.m_20154_().m_82541_();
        e.m_5602_(boss);e.spellEpoch=boss.combat.spells.epoch();e.f_19804_.m_135381_(ROLL,roll);
        e.m_6034_(position.f_82479_,position.f_82480_,position.f_82481_);e.m_20256_(direction.m_82490_(SPEED));
        e.face();e.f_19859_=e.m_146908_();e.f_19860_=e.m_146909_();boss.m_9236_().m_7967_(e);return e;
    }
    private void face(){Vec3 v=this.m_20184_();this.m_146922_((float)Math.toDegrees(Math.atan2(v.f_82479_,v.f_82481_)));this.m_146926_((float)Math.toDegrees(Math.atan2(v.f_82480_,Math.hypot(v.f_82479_,v.f_82481_))));}
    @Override public void m_6453_(double x,double y,double z,float yaw,float pitch,int steps,boolean teleport){
        if(teleport||this.f_19797_==0){super.m_6453_(x,y,z,yaw,pitch,steps,teleport);clientTarget=null;}
        else clientTarget=new Vec3(x,y,z);
        this.m_146922_(yaw);this.m_146926_(pitch);
    }
    @Override public void m_8119_(){
        super.m_8119_();
        if(this.m_9236_().f_46443_){if(clientTarget!=null){this.m_6034_(clientTarget.f_82479_,clientTarget.f_82480_,clientTarget.f_82481_);clientTarget=null;}return;}
        if(this.f_19797_>LIFETIME){this.m_146870_();return;}
        if(!(this.m_19749_() instanceof AscianKingEntity boss)||!valid(boss)){this.m_146870_();return;}
        Vec3 from=this.m_20182_(),motion=this.m_20184_(),to=from.m_82549_(motion),forward=motion.m_82541_();
        Vec3 right=new Vec3(forward.f_82481_,0,-forward.f_82479_);right=right.m_82556_()<1E-10?new Vec3(1,0,0):right.m_82541_();
        Vec3 up=forward.m_82537_(right);double angle=Math.toRadians(roll());
        Vec3 width=right.m_82490_(Math.cos(angle)).m_82549_(up.m_82490_(Math.sin(angle)));
        Vec3 normal=right.m_82490_(-Math.sin(angle)).m_82549_(up.m_82490_(Math.cos(angle)));
        for(LivingEntity target:this.m_9236_().m_45976_(LivingEntity.class,new AABB(from,to).m_82400_(7))){
            if(!valid(boss))return;
            if(boss.combat.skip(target)||hit.contains(target.m_20148_()))continue;
            AABB box=target.m_20191_();Vec3 center=box.m_82399_().m_82546_(from);
            double along=center.m_82526_(forward),across=center.m_82526_(width),thickness=center.m_82526_(normal);
            double pad=padding(box,forward);
            if(along < -pad||along>motion.m_82553_()+5.85+pad||Math.abs(across)>4.41+padding(box,width)||Math.abs(thickness)>.2+padding(box,normal))continue;
            hit.add(target.m_20148_());boss.combat.spells.phantomBladeHit(target,this);
        }
        if(!valid(boss))return;
        // Deliberately no block clip or entity stop: all thirty moves can pass through terrain.
        this.m_6034_(to.f_82479_,to.f_82480_,to.f_82481_);
        if(this.f_19797_>=LIFETIME)this.m_146870_();
    }
    private boolean valid(AscianKingEntity boss){return !this.m_213877_()&&boss.m_6084_()&&boss.combat.spells.epoch()==spellEpoch;}
    private static double padding(AABB box,Vec3 axis){return (Math.abs(axis.f_82479_)*(box.f_82291_-box.f_82288_)+Math.abs(axis.f_82480_)*(box.f_82292_-box.f_82289_)+Math.abs(axis.f_82481_)*(box.f_82293_-box.f_82290_))*.5;}
    @Override public AABB m_6921_(){return this.m_20191_().m_82400_(15);}
    @Override protected void m_7380_(CompoundTag n){
        super.m_7380_(n);n.m_128356_("SpellEpoch",spellEpoch);n.m_128405_("Age",this.f_19797_);n.m_128350_("Roll",roll());
        CompoundTag ids=new CompoundTag();for(UUID id:hit)ids.m_128379_(id.toString(),true);n.m_128365_("HitTargets",ids);
    }
    @Override public void writeSpawnData(net.minecraft.network.FriendlyByteBuf b){b.writeInt(this.f_19797_);}
    @Override public void readSpawnData(net.minecraft.network.FriendlyByteBuf b){this.f_19797_=b.readInt();}
    @Override protected void m_7378_(CompoundTag n){
        super.m_7378_(n);spellEpoch=n.m_128454_("SpellEpoch");this.f_19797_=n.m_128451_("Age");this.f_19804_.m_135381_(ROLL,n.m_128457_("Roll"));
        hit.clear();for(String key:n.m_128469_("HitTargets").m_128431_())try{hit.add(UUID.fromString(key));}catch(IllegalArgumentException ignored){}
    }
    @Override public net.minecraft.network.protocol.Packet<net.minecraft.network.protocol.game.ClientGamePacketListener> m_5654_(){return NetworkHooks.getEntitySpawningPacket(this);}
}
