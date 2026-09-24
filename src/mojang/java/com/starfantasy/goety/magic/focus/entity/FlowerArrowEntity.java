package com.starfantasy.goety.magic.focus.entity;

import com.starfantasy.library.vfx.StarFantasyStarArrowVisual;
import com.starfantasy.goety.magic.focus.FlowerArrowRain;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;

/** Cosmetic arrow; the cast owns its fixed damage timeline, independent of projectile collisions. */
public final class FlowerArrowEntity extends Entity implements StarFantasyStarArrowVisual, net.minecraftforge.entity.IEntityAdditionalSpawnData {
    public static final int EXPLOSION_TRAIL_TICKS = 20;
    private static final EntityDataAccessor<Integer> EXPLODED_AT = SynchedEntityData.defineId(FlowerArrowEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> COLOR = SynchedEntityData.defineId(FlowerArrowEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> TRAVEL = SynchedEntityData.defineId(FlowerArrowEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Long> BIRTH = SynchedEntityData.defineId(FlowerArrowEntity.class, EntityDataSerializers.LONG);
    private static final EntityDataAccessor<org.joml.Vector3f> PATH = SynchedEntityData.defineId(FlowerArrowEntity.class, EntityDataSerializers.VECTOR3);
    private Vec3 start = Vec3.ZERO;
    private boolean restored;
    public FlowerArrowEntity(EntityType<? extends FlowerArrowEntity> type, Level level) { super(type, level); noPhysics=true; setNoGravity(true); }
    public FlowerArrowEntity(net.minecraftforge.network.PlayMessages.SpawnEntity packet, Level level) { this(com.starfantasy.goety.magic.focus.BattleFocusContent.FLOWER_ARROW.get(),level); }
    public static FlowerArrowEntity createInstance(net.minecraftforge.network.PlayMessages.SpawnEntity packet, Level level) { return new FlowerArrowEntity(packet,level); }
    public static void clearRuntimeState() { FlowerArrowRain.clearRuntimeState(); }
    @Override protected void defineSynchedData() {
        entityData.define(COLOR,0x80CFFF);entityData.define(TRAVEL,30);entityData.define(BIRTH,-1L);
        entityData.define(PATH,new org.joml.Vector3f());
        entityData.define(EXPLODED_AT, -1);
    }
    public void configure(Vec3 start, Vec3 end, int travel, int color) {
        this.start=start;setPos(start);xo=xOld=start.x;yo=yOld=start.y;zo=zOld=start.z;
        entityData.set(PATH,end.subtract(start).toVector3f());entityData.set(TRAVEL,travel);
        entityData.set(BIRTH,level().getGameTime());entityData.set(COLOR,color);
        rotate(end.subtract(start));
    }
    // Spawn coordinates remain doubles; the synchronized path is relative, avoiding large-world float drift.
    @Override public void recreateFromPacket(net.minecraft.network.protocol.game.ClientboundAddEntityPacket packet) {
        super.recreateFromPacket(packet);start=position();
    }
    @Override public void writeSpawnData(net.minecraft.network.FriendlyByteBuf buffer) {
        buffer.writeDouble(start.x);buffer.writeDouble(start.y);buffer.writeDouble(start.z);
        buffer.writeVector3f(entityData.get(PATH));buffer.writeInt(travelTicks());
        buffer.writeLong(entityData.get(BIRTH));buffer.writeInt(starFantasyStarArrowColor());
        buffer.writeInt(entityData.get(EXPLODED_AT));
    }
    @Override public void readSpawnData(net.minecraft.network.FriendlyByteBuf buffer) {
        start=new Vec3(buffer.readDouble(),buffer.readDouble(),buffer.readDouble());
        entityData.set(PATH,buffer.readVector3f());entityData.set(TRAVEL,buffer.readInt());
        entityData.set(BIRTH,buffer.readLong());entityData.set(COLOR,buffer.readInt());
        entityData.set(EXPLODED_AT,buffer.readInt());
        Vec3 previous=sample(age()-1);setPos(sample(age()));
        xo=xOld=previous.x;yo=yOld=previous.y;zo=zOld=previous.z;rotate(path());
    }
    @Override public void lerpTo(double x,double y,double z,float yaw,float pitch,int steps,boolean teleport) { }
    public Vec3 path() { return new Vec3(entityData.get(PATH)); }
    public int travelTicks() { return entityData.get(TRAVEL); }
    public double age() { return entityData.get(BIRTH)<0?0:Math.max(0,level().getGameTime()-entityData.get(BIRTH)); }
    public Vec3 sample(double age) {
        if (isExploded()) age = Math.min(age, entityData.get(EXPLODED_AT));
        return start.add(path().scale(Math.max(0,Math.min(1,age/travelTicks()))));
    }
    public boolean isExploded() { return entityData.get(EXPLODED_AT) >= 0; }
    public double getExplosionTrailAge() { return isExploded() ? Math.max(0, age() - entityData.get(EXPLODED_AT)) : 0; }
    public void explode() {
        if (isExploded() || isRemoved()) return;
        entityData.set(EXPLODED_AT, (int) age());
        setPos(sample(age()));
        setDeltaMovement(Vec3.ZERO);
    }
    @Override public void tick() {
        super.tick();
        if(restored) { discard();return; }
        if (!isExploded() && age() >= travelTicks()) explode();
        if (isExploded()) {
            setDeltaMovement(Vec3.ZERO); setPos(sample(age()));
            if (getExplosionTrailAge() >= EXPLOSION_TRAIL_TICKS && !level().isClientSide) discard();
            return;
        }
        Vec3 delta=path().scale(1.0/travelTicks());setDeltaMovement(delta);rotate(delta);setPos(sample(age()));
    }
    private void rotate(Vec3 motion) {
        setYRot((float)Math.toDegrees(Math.atan2(motion.x,motion.z)));
        setXRot((float)Math.toDegrees(Math.atan2(motion.y,Math.sqrt(motion.x*motion.x+motion.z*motion.z))));
    }
    @Override public int starFantasyStarArrowColor(){return entityData.get(COLOR);}
    @Override public int starFantasyStarArrowLifetime(){return travelTicks()*3;}
    @Override public float starFantasyStarArrowVisualScale(){return .65F;}
    @Override public int starFantasyStarArrowTrailCount(){return 22;}
    @Override public Vec3 starFantasyStarArrowTrailPoint(int index){return starFantasyStarArrowTrailPoint(index,1);}
    @Override public boolean starFantasyStarArrowDepthTested(){return true;}
    @Override public Vec3 starFantasyStarArrowTrailPoint(int index,float partialTick){
        // ClientLevel advances its clock AFTER ticking entities. Re-sampling age() while
        // rendering therefore puts the trail a tick ahead of the head. Anchor to exactly
        // the same previous/current positions used by the entity renderer instead.
        Vec3 head=new Vec3(net.minecraft.util.Mth.lerp(partialTick,xOld,getX()),
                net.minecraft.util.Mth.lerp(partialTick,yOld,getY()),
                net.minecraft.util.Mth.lerp(partialTick,zOld,getZ()));
        Vec3 direction=path().normalize();
        double available=Math.max(0,head.subtract(start).dot(direction));
        double behind=Math.min(available,Math.max(0,index)*.6*path().length()/travelTicks());
        if (isExploded()) behind = Math.max(0, behind - (getExplosionTrailAge() + partialTick) * .6 * path().length() / travelTicks());
        return head.subtract(direction.scale(behind));
    }
    @Override protected void readAdditionalSaveData(CompoundTag tag){restored=true;}
    @Override protected void addAdditionalSaveData(CompoundTag tag) { }
    @Override public Packet<ClientGamePacketListener> getAddEntityPacket(){return NetworkHooks.getEntitySpawningPacket(this);}
}
