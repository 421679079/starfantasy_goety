package com.starfantasy.goety.entity;

import com.starfantasy.goety.combat.ascian.*;
import com.starfantasy.goety.registry.AscianKingRegistry;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.syncher.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.*;
import net.minecraftforge.entity.IEntityAdditionalSpawnData;
import net.minecraftforge.network.NetworkHooks;

/** Independent gameplay entity. Goety supplies only the textures and sounds used by its renderer. */
public final class AscianVoidRiftEntity extends Entity implements IEntityAdditionalSpawnData {
    public static final double RADIUS=16,HEIGHT=10;
    private static final EntityDataAccessor<Integer> OWNER_ID=SynchedEntityData.m_135353_(AscianVoidRiftEntity.class,EntityDataSerializers.f_135028_),
            DURATION=SynchedEntityData.m_135353_(AscianVoidRiftEntity.class,EntityDataSerializers.f_135028_);
    private UUID ownerUuid;
    private long spellEpoch;
    private boolean clientSound;
    public AscianVoidRiftEntity(EntityType<? extends AscianVoidRiftEntity> type,Level level){super(type,level);this.f_19794_=true;this.m_20242_(true);}
    @Override protected void m_8097_(){this.f_19804_.m_135372_(OWNER_ID,-1);this.f_19804_.m_135372_(DURATION,80);}
    public int getDuration(){return this.f_19804_.m_135370_(DURATION);}
    public AscianKingEntity owner(){
        Entity e=this.m_9236_() instanceof ServerLevel level&&ownerUuid!=null?level.m_8791_(ownerUuid):this.m_9236_().m_6815_(this.f_19804_.m_135370_(OWNER_ID));
        return e instanceof AscianKingEntity boss?boss:null;
    }
    public static AscianVoidRiftEntity spawn(AscianKingEntity boss,int duration){
        var e=new AscianVoidRiftEntity(AscianKingRegistry.VOID_RIFT.get(),boss.m_9236_());e.ownerUuid=boss.m_20148_();e.spellEpoch=boss.combat.spells.epoch();
        e.f_19804_.m_135381_(OWNER_ID,boss.m_19879_());e.f_19804_.m_135381_(DURATION,Math.max(1,duration));
        e.m_6034_(boss.m_20185_(),boss.m_20186_()+HEIGHT,boss.m_20189_());boss.m_9236_().m_7967_(e);
        e.m_5496_(com.Polarice3.Goety.init.ModSounds.VOID_RIFT_OPEN.get(),5,1);return e;
    }
    public void followOwner(){var boss=owner();if(boss!=null)this.m_6034_(boss.m_20185_(),boss.m_20186_()+HEIGHT,boss.m_20189_());}
    private boolean valid(AscianKingEntity boss){return !this.m_213877_()&&boss!=null&&boss.m_6084_()&&boss.m_9236_()==this.m_9236_()&&boss.combat.spells.epoch()==spellEpoch;}
    @Override public void m_8119_(){
        super.m_8119_();
        if(this.m_9236_().f_46443_){
            if(!clientSound){clientSound=true;com.starfantasy.goety.client.AscianVoidRiftRenderer.startSound(this);}
            return;
        }
        AscianKingEntity boss=owner();if(!valid(boss)||this.f_19797_>getDuration()){this.m_146870_();return;}
        if(this.f_19804_.m_135370_(OWNER_ID)!=boss.m_19879_())this.f_19804_.m_135381_(OWNER_ID,boss.m_19879_());
        followOwner();Vec3 center=this.m_20182_();
        for(Entity target:this.m_9236_().m_45976_(Entity.class,new AABB(center,center).m_82400_(RADIUS))){
            if(!valid(boss))return;
            if(target==this||target==boss||target.m_213877_()||boss.combat.friendly(target)
                    ||target instanceof AscianVoidRiftEntity r&&r.owner()==boss
                    ||target instanceof Projectile p&&p.m_19749_()==boss
                    ||com.Polarice3.Goety.utils.MobUtil.areAllies(boss,target)||com.Polarice3.Goety.utils.MobUtil.getOwner(target)==boss
                    ||target instanceof LivingEntity l&&boss.combat.skip(l))continue;
            Vec3 delta=center.m_82546_(target.m_20182_());double squared=delta.m_82556_();
            // Check the sphere before touching velocity: points in cube corners never receive a force.
            if(squared>RADIUS*RADIUS)continue;
            if(squared>1E-12){
                double distance=Math.sqrt(squared),strength=.25*Math.max(0,1-distance/RADIUS);
                if(target instanceof LivingEntity living&&living.m_21051_(Attributes.f_22278_)!=null)
                    strength*=Math.max(0,1-living.m_21133_(Attributes.f_22278_)*.5);
                if(strength>0){
                    target.m_20256_(target.m_20184_().m_82549_(delta.m_82490_(strength/distance)));target.f_19812_=true;
                    if(target instanceof Player){target.f_19864_=true;target.m_6853_(false);}
                }
            }
        }
        if(this.f_19797_>=getDuration())this.m_146870_();
    }
    @Override public AABB m_6921_(){return this.m_20191_().m_82400_(4);}
    @Override protected void m_7380_(CompoundTag n){if(ownerUuid!=null)n.m_128362_("RiftOwner",ownerUuid);n.m_128356_("SpellEpoch",spellEpoch);n.m_128405_("RiftAge",this.f_19797_);n.m_128405_("RiftDuration",getDuration());}
    @Override protected void m_7378_(CompoundTag n){if(n.m_128403_("RiftOwner"))ownerUuid=n.m_128342_("RiftOwner");spellEpoch=n.m_128454_("SpellEpoch");this.f_19797_=n.m_128451_("RiftAge");this.f_19804_.m_135381_(DURATION,Math.max(1,n.m_128451_("RiftDuration")));}
    @Override public void writeSpawnData(FriendlyByteBuf b){b.writeInt(this.f_19797_);}
    @Override public void readSpawnData(FriendlyByteBuf b){this.f_19797_=b.readInt();}
    @Override public net.minecraft.network.protocol.Packet<net.minecraft.network.protocol.game.ClientGamePacketListener> m_5654_(){return NetworkHooks.getEntitySpawningPacket(this);}
}
