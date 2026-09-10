package com.starfantasy.goety.entity;

import com.Polarice3.Goety.client.particles.WindGatherParticleOption;
import com.Polarice3.Goety.common.entities.ModEntityType;
import com.Polarice3.Goety.common.entities.boss.Apostle;
import com.Polarice3.Goety.init.ModSounds;
import com.Polarice3.Goety.utils.ColorUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.PushReaction;
import net.minecraftforge.network.NetworkHooks;

/** Goety's full Apostle summoning presentation with a persisted, forced title. */
public final class DirectedApostleSummonEntity extends Entity {
    private static final String TITLE_TAG = "ApostleTitle";
    private static final EntityDataAccessor<Integer> TITLE =
            SynchedEntityData.m_135353_(
                    DirectedApostleSummonEntity.class, EntityDataSerializers.f_135028_);

    public DirectedApostleSummonEntity(
            EntityType<? extends DirectedApostleSummonEntity> type,
            Level level,
            int fixedTitle) {
        super(type, level);
        this.m_20242_(true);
        this.setTitleNumber(fixedTitle);
    }

    @Override
    protected void m_8097_() {
        this.f_19804_.m_135372_(TITLE, -1);
    }

    public void setTitleNumber(int title) {
        this.f_19804_.m_135381_(TITLE, title);
    }

    public int titleNumber() {
        return this.f_19804_.m_135370_(TITLE);
    }

    @Override
    protected void m_7378_(CompoundTag tag) {
        if (tag.m_128441_(TITLE_TAG)) {
            this.setTitleNumber(tag.m_128451_(TITLE_TAG));
        }
    }

    @Override
    protected void m_7380_(CompoundTag tag) {
        tag.m_128405_(TITLE_TAG, this.titleNumber());
    }

    @Override
    public void m_8119_() {
        super.m_8119_();
        this.m_20242_(true);

        if (this.f_19797_ == 150) {
            this.m_5496_(SoundEvents.f_12166_.get(), 1.0F, 1.0F);
            for (Player player : this.m_9236_().m_45976_(
                    Player.class, this.m_20191_().m_82400_(32.0D))) {
                player.m_5661_(Component.m_237115_("info.goety.apostle.summon"), true);
            }
            if (this.m_9236_() instanceof ServerLevel serverLevel) {
                Warden.m_219375_(serverLevel, this.m_20182_(), null, 32);
            }
        }

        if (this.f_19797_ == 300) {
            this.m_5496_(ModSounds.APOSTLE_AMBIENT.get(), 1.0F, 1.0F);
        }
        if (this.f_19797_ == 450) {
            this.m_5496_(SoundEvents.f_12090_, 1.0F, 1.0F);
        }

        if (!this.m_9236_().f_46443_ && this.m_9236_() instanceof ServerLevel serverLevel) {
            if (serverLevel.m_46791_() == Difficulty.PEACEFUL) {
                this.m_146870_();
                return;
            }

            if (this.f_19797_ >= 300) {
                serverLevel.m_8767_(ParticleTypes.f_123755_,
                        this.m_20208_(0.5D), this.m_20187_(), this.m_20262_(0.5D),
                        1, 0.0D, 0.0D, 0.0D, 0.0D);
            }

            float progress = Mth.m_14036_(this.f_19797_ / 450.0F, 0.0F, 1.0F);
            int interval = Mth.m_269140_(progress, 8, 2);
            int width = Mth.m_269140_(progress, 8, 15);
            float height = Mth.m_14179_(progress, 0.3F, 0.9F);
            if (this.f_19797_ % interval == 0) {
                double offsetX = (this.f_19796_.m_188500_() * 3.0D + 2.0D)
                        * (this.f_19796_.m_188499_() ? 1.0D : -1.0D);
                double offsetZ = (this.f_19796_.m_188500_() * 3.0D + 2.0D)
                        * (this.f_19796_.m_188499_() ? 1.0D : -1.0D);
                serverLevel.m_8767_(new WindGatherParticleOption(
                                new ColorUtil(this.f_19796_.m_188503_(3) == 0
                                        ? ChatFormatting.DARK_RED
                                        : ChatFormatting.BLACK),
                                width, height, 90, this.m_19879_()),
                        this.m_20185_() + offsetX,
                        this.m_20186_() + this.m_20206_(),
                        this.m_20189_() + offsetZ,
                        1, 0.0D, 0.0D, 0.0D, 0.0D);
            }

            if (this.f_19797_ == 450) {
                spawnFlameBurst(serverLevel);
                serverLevel.m_8606_(6000, 0, false, false);
                spawnDirectedApostle(serverLevel);
                this.m_146870_();
            }
        }
    }

    private void spawnFlameBurst(ServerLevel serverLevel) {
        for (int i = 0; i < 200; ++i) {
            float radius = this.f_19796_.m_188501_() * 4.0F;
            float angle = this.f_19796_.m_188501_() * ((float)Math.PI * 2.0F);
            double motionX = Mth.m_14089_(angle) * radius;
            double motionY = 0.01D + this.f_19796_.m_188500_() * 0.5D;
            double motionZ = Mth.m_14031_(angle) * radius;
            serverLevel.m_8767_(ParticleTypes.f_123744_,
                    this.m_20185_() + motionX * 0.1D,
                    this.m_20186_() + 0.3D,
                    this.m_20189_() + motionZ * 0.1D,
                    0, motionX, motionY, motionZ, 0.5D);
        }
    }

    private void spawnDirectedApostle(ServerLevel serverLevel) {
        int forcedTitle = this.titleNumber();
        if (forcedTitle < 0 || forcedTitle > 11) {
            forcedTitle = this.f_19796_.m_188503_(12);
        }

        Apostle apostle = new Apostle(ModEntityType.APOSTLE.get(), serverLevel);
        apostle.m_6034_(this.m_20185_(), this.m_20186_(), this.m_20189_());

        int nameIndex = this.f_19796_.m_188503_(18);
        apostle.m_6593_(Component.m_237115_("name.goety.apostle." + nameIndex)
                .m_7220_(Component.m_237115_("title.goety." + forcedTitle)));
        apostle.m_6518_(serverLevel,
                serverLevel.m_6436_(this.m_20183_()),
                MobSpawnType.MOB_SUMMONED,
                null,
                null);
        apostle.setTitleNumber(forcedTitle);
        apostle.TitleEffect(forcedTitle);
        if (forcedTitle == Apostle.ABHORRENT) {
            apostle.m_21153_(apostle.m_21233_());
        }
        serverLevel.m_7967_(apostle);
    }

    @Override
    public boolean m_6469_(net.minecraft.world.damagesource.DamageSource source, float amount) {
        return false;
    }

    @Override
    public boolean m_6094_() {
        return false;
    }

    @Override
    public boolean m_5829_() {
        return false;
    }

    @Override
    public PushReaction m_7752_() {
        return PushReaction.IGNORE;
    }

    @Override
    public Packet<ClientGamePacketListener> m_5654_() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}
