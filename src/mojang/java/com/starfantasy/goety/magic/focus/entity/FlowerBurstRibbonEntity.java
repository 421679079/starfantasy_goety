package com.starfantasy.goety.magic.focus.entity;

import com.starfantasy.goety.magic.focus.BattleFocusContent;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;

/** Short-lived cosmetic ribbons. No collision, damage, or global screen filter. */
public final class FlowerBurstRibbonEntity extends Entity implements net.minecraftforge.entity.IEntityAdditionalSpawnData {
    private static final EntityDataAccessor<Integer> COLOR=SynchedEntityData.defineId(FlowerBurstRibbonEntity.class,EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> FINAL=SynchedEntityData.defineId(FlowerBurstRibbonEntity.class,EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Long> BIRTH=SynchedEntityData.defineId(FlowerBurstRibbonEntity.class,EntityDataSerializers.LONG);
    private boolean restored;
    public FlowerBurstRibbonEntity(EntityType<? extends FlowerBurstRibbonEntity> type,Level level){super(type,level);noPhysics=true;setNoGravity(true);}
    public FlowerBurstRibbonEntity(net.minecraftforge.network.PlayMessages.SpawnEntity packet,Level level){this(BattleFocusContent.FLOWER_BURST_RIBBON.get(),level);}
    public static void spawn(ServerLevel level,Vec3 center,int color,boolean finisher) {
        var ribbon=BattleFocusContent.FLOWER_BURST_RIBBON.get().create(level);
        // This entity anchors the visual to the nearby surface; combat still uses the original impact.
        var ground=level.clip(new net.minecraft.world.level.ClipContext(center.add(0,2,0),center.add(0,-8,0),
                net.minecraft.world.level.ClipContext.Block.COLLIDER,net.minecraft.world.level.ClipContext.Fluid.NONE,ribbon));
        double floor=ground.getType()==net.minecraft.world.phys.HitResult.Type.BLOCK?ground.getLocation().y:center.y;
        ribbon.setPos(center.x,Math.max(center.y,floor),center.z);
        ribbon.entityData.set(COLOR,color);ribbon.entityData.set(FINAL,finisher);
        ribbon.entityData.set(BIRTH,level.getGameTime());level.addFreshEntity(ribbon);
    }
    @Override protected void defineSynchedData(){entityData.define(COLOR,0x91CFFF);entityData.define(FINAL,false);entityData.define(BIRTH,-1L);}
    public int color(){return entityData.get(COLOR);}
    public boolean finisher(){return entityData.get(FINAL);}
    public float visualAge(float partial){return entityData.get(BIRTH)<0?0:Math.max(0,level().getGameTime()-entityData.get(BIRTH)+partial);}
    public int animationTicks(){return finisher()?40:24;}
    public int lifetime(){return animationTicks()+40;}
    public float visualRadius(){return (finisher()?13:5)*1.5F;}
    @Override public void writeSpawnData(net.minecraft.network.FriendlyByteBuf buffer) {
        buffer.writeInt(color());buffer.writeBoolean(finisher());buffer.writeLong(entityData.get(BIRTH));
    }
    @Override public void readSpawnData(net.minecraft.network.FriendlyByteBuf buffer) {
        entityData.set(COLOR,buffer.readInt());entityData.set(FINAL,buffer.readBoolean());entityData.set(BIRTH,buffer.readLong());
    }
    @Override public void tick(){super.tick();if(restored || visualAge(0)>=lifetime())discard();}
    @Override protected void readAdditionalSaveData(CompoundTag tag){restored=true;}
    @Override protected void addAdditionalSaveData(CompoundTag tag) { }
    @Override public Packet<ClientGamePacketListener> getAddEntityPacket(){return NetworkHooks.getEntitySpawningPacket(this);}
}
