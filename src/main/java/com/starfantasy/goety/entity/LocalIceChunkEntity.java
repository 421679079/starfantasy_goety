package com.starfantasy.goety.entity;

import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.Level;
import net.minecraftforge.entity.IEntityAdditionalSpawnData;
import net.minecraftforge.network.NetworkHooks;

/** Shared lightweight ice prop. No Goety entity tick, damage or server particle utility. */
public abstract class LocalIceChunkEntity extends Entity implements IEntityAdditionalSpawnData {
    private UUID ownerUuid,targetUuid;
    public int hovering;
    protected LocalIceChunkEntity(EntityType<?> type,Level level){super(type,level);this.m_20242_(true);}
    public void setOwner(LivingEntity owner){ownerUuid=owner==null?null:owner.m_20148_();}
    public void setTarget(LivingEntity target){targetUuid=target==null?null:target.m_20148_();}
    public LivingEntity m_269323_(){return resolve(ownerUuid);}
    public LivingEntity getTarget(){return resolve(targetUuid);}
    public void setExtraDamage(float ignored) { /* Damage belongs to each spell controller. */ }
    private LivingEntity resolve(UUID id){if(id==null||!(this.m_9236_() instanceof ServerLevel level))return null;Entity entity=level.m_8791_(id);return entity instanceof LivingEntity living?living:null;}
    public abstract boolean isStarting();
    public void damageTargets(LivingEntity target) { }
    protected void clientFormation(){if(this.m_9236_().f_46443_&&isStarting())com.starfantasy.goety.client.IceChunkEffect.formation(this);}
    protected void impact(float scale){com.starfantasy.goety.network.StarFantasyGoetyNetwork.sendIceImpact(this,scale);}
    @Override protected void m_8097_(){}
    @Override public boolean m_6783_(double distance){return distance<65536;}
    @Override protected void m_7378_(CompoundTag n){if(n.m_128403_("IceOwner"))ownerUuid=n.m_128342_("IceOwner");if(n.m_128403_("IceTarget"))targetUuid=n.m_128342_("IceTarget");hovering=n.m_128451_("LocalHovering");this.f_19797_=n.m_128451_("LocalAge");}
    @Override protected void m_7380_(CompoundTag n){if(ownerUuid!=null)n.m_128362_("IceOwner",ownerUuid);if(targetUuid!=null)n.m_128362_("IceTarget",targetUuid);n.m_128405_("LocalHovering",hovering);n.m_128405_("LocalAge",this.f_19797_);}
    @Override public void writeSpawnData(FriendlyByteBuf buffer){CompoundTag n=new CompoundTag();this.m_7380_(n);buffer.m_130079_(n);}
    @Override public void readSpawnData(FriendlyByteBuf buffer){CompoundTag n=buffer.m_130260_();if(n!=null)this.m_7378_(n);}
    @Override public net.minecraft.network.protocol.Packet<net.minecraft.network.protocol.game.ClientGamePacketListener> m_5654_(){return NetworkHooks.getEntitySpawningPacket(this);}
}
