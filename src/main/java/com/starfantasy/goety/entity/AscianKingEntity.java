package com.starfantasy.goety.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

/** Independent boss; animation and damage share a server-owned combat timeline. */
public final class AscianKingEntity extends PathfinderMob implements GeoEntity, com.starfantasy.library.combat.CombatHealthEntity {
    private static final EntityDataAccessor<Integer> ACTION = SynchedEntityData.m_135353_(AscianKingEntity.class, EntityDataSerializers.f_135028_);
    private static final EntityDataAccessor<Integer> ACTION_SERIAL = SynchedEntityData.m_135353_(AscianKingEntity.class, EntityDataSerializers.f_135028_);
    private static final EntityDataAccessor<Integer> VISUAL_PHASE = SynchedEntityData.m_135353_(AscianKingEntity.class, EntityDataSerializers.f_135028_);
    private static final EntityDataAccessor<Boolean> STUNNED = SynchedEntityData.m_135353_(AscianKingEntity.class, EntityDataSerializers.f_135035_);
    private static final EntityDataAccessor<Integer> SUMMON_TICKS = SynchedEntityData.m_135353_(AscianKingEntity.class, EntityDataSerializers.f_135028_);
    private boolean summonOwnsControl, summonPreviousAi, summonPreviousGravity;
    public static final int SUMMON_DURATION = 60;
    private int clientActionSerial = -1, clientActionTick;
    private final com.starfantasy.library.combat.CombatHealthProtection healthProtection = new com.starfantasy.library.combat.CombatHealthProtection();
    public final net.minecraft.server.level.ServerBossEvent bossBar = new net.minecraft.server.level.ServerBossEvent(
            net.minecraft.network.chat.Component.m_237113_("无影之王"), net.minecraft.world.BossEvent.BossBarColor.YELLOW, net.minecraft.world.BossEvent.BossBarOverlay.NOTCHED_10);
    public final com.starfantasy.goety.combat.ascian.AscianCombatController combat = new com.starfantasy.goety.combat.ascian.AscianCombatController(this);
    private boolean initialized;
    private double configuredHealth = -1;
    private int clientAnimationSerial = -1;
    private static final EntityDataAccessor<Boolean> WINGS = SynchedEntityData.m_135353_(AscianKingEntity.class, EntityDataSerializers.f_135035_);
    private static final EntityDataAccessor<Float> WING_SCALE = SynchedEntityData.m_135353_(AscianKingEntity.class, EntityDataSerializers.f_135029_);
    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation DEATH = RawAnimation.begin().thenPlayAndHold("death");
    public static final int DEATH_TICKS = 80;
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public AscianKingEntity(EntityType<? extends AscianKingEntity> type, Level level) {
        super(type, level);
        this.m_21530_(); // Explicitly summoned bosses must not naturally despawn.
        healthProtection.enable();
    }

    @Override public boolean m_6785_(double distance){return false;}
    @Override public boolean m_8023_(){return true;}

    @Override public void m_142687_(net.minecraft.world.entity.Entity.RemovalReason reason){
        if(!this.m_9236_().f_46443_&&reason==net.minecraft.world.entity.Entity.RemovalReason.DISCARDED&&this.m_6084_())
            com.mojang.logging.LogUtils.getLogger().warn("Ascian King discarded alive: uuid={}, position={}, persistent={}, callers={}",this.m_20148_(),this.m_20182_(),this.m_21532_(),
                    StackWalker.getInstance().walk(s->s.limit(8).map(f->f.getClassName()+"."+f.getMethodName()).collect(java.util.stream.Collectors.joining(" <- "))));
        super.m_142687_(reason);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.m_21552_().m_22268_(Attributes.f_22276_, 600)
                .m_22268_(Attributes.f_22279_, .15).m_22268_(Attributes.f_22278_, 1)
                .m_22268_(Attributes.f_22277_, 60).m_22268_(Attributes.f_22281_, 20)
                .m_22268_(Attributes.f_22284_, 10).m_22268_(Attributes.f_22285_, 10);
    }

    @Override protected void m_8099_() {
        this.f_21346_.m_25352_(2, new net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal<>(this, net.minecraft.world.entity.player.Player.class, true));
        this.f_21346_.m_25352_(3, new net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal(this));
    }

    @Override public void m_8119_() {
        if(!this.m_9236_().f_46443_ && isSummoning())lockSummonControl();
        if(!this.m_9236_().f_46443_ && this.m_6084_())combat.beforeEntityTick();
        super.m_8119_();
        this.m_20095_();
        if (this.m_9236_().f_46443_) {
            if(clientActionSerial!=visualSerial()){clientActionSerial=visualSerial();clientActionTick=this.f_19797_;}
            return;
        }
        healthProtection.tick();
        if (this.m_6084_() && (!initialized || this.f_19797_ % 20 == 0)) applyConfig(!initialized);
        initialized = true;
        if (this.m_6084_()) {
            if(isSummoning()) {
                this.m_20256_(net.minecraft.world.phys.Vec3.f_82478_);
                this.f_19804_.m_135381_(SUMMON_TICKS,summonTicks()-1);
                if(!isSummoning())releaseSummonControl();
            } else combat.tick();
        }
        this.f_19804_.m_135381_(VISUAL_PHASE,combat.phase());
        this.f_19804_.m_135381_(STUNNED,combat.stunned());
        bossBar.m_142711_(this.m_21223_()/this.m_21233_());
    }
    private void applyConfig(boolean full) {
        healthProtection.beginRestore();
        try {
            double max = com.starfantasy.goety.config.AscianKingConfig.HEALTH.get();
            float oldMax = this.m_21233_(), oldHealth = this.m_21223_();
            if (configuredHealth != max) {
                this.m_21051_(Attributes.f_22276_).m_22100_(max);
                configuredHealth = max;
                if (full || oldHealth >= oldMax) this.m_21153_(this.m_21233_());
                else if (oldHealth > this.m_21233_()) this.m_21153_(this.m_21233_());
            }
            this.m_21051_(Attributes.f_22284_).m_22100_(com.starfantasy.goety.config.AscianKingConfig.ARMOR.get());
            this.m_21051_(Attributes.f_22285_).m_22100_(com.starfantasy.goety.config.AscianKingConfig.ARMOR.get());
            this.f_21364_=com.starfantasy.goety.config.AscianKingConfig.EXPERIENCE.get();
        } finally { healthProtection.endRestore(); }
    }
    public void action(int action) {
        this.f_19804_.m_135381_(ACTION, action);
        this.f_19804_.m_135381_(ACTION_SERIAL, this.f_19804_.m_135370_(ACTION_SERIAL)+1);
    }
    public void syncAction() {
        if(this.m_9236_() instanceof net.minecraft.server.level.ServerLevel level) {
            var data=this.f_19804_.m_135378_();
            if(data!=null)level.m_7726_().m_8445_(this,new net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket(this.m_19879_(),data));
        }
    }
    public int visualAction(){return this.f_19804_.m_135370_(ACTION);}
    public int visualSerial(){return this.f_19804_.m_135370_(ACTION_SERIAL);}
    public int visualPhase(){return this.f_19804_.m_135370_(VISUAL_PHASE);}
    public boolean visualStunned(){return this.f_19804_.m_135370_(STUNNED);}
    public int summonTicks(){return this.f_19804_.m_135370_(SUMMON_TICKS);}
    public boolean isSummoning(){return summonTicks()>0;}
    public float summonAge(float partial){return Math.min(SUMMON_DURATION,SUMMON_DURATION-summonTicks()+partial);}
    private void lockSummonControl(){
        if(!summonOwnsControl){summonPreviousAi=this.m_21525_();summonPreviousGravity=this.m_20068_();summonOwnsControl=true;}
        this.m_21557_(true);this.m_20242_(true);this.m_21573_().m_26573_();this.m_20256_(net.minecraft.world.phys.Vec3.f_82478_);
    }
    private void releaseSummonControl(){
        if(summonOwnsControl){this.m_21557_(summonPreviousAi);this.m_20242_(summonPreviousGravity);summonOwnsControl=false;}
    }
    public float visualActionAge(float partial){return Math.max(0,this.f_19797_-clientActionTick+partial);}
    @Override public boolean m_6469_(net.minecraft.world.damagesource.DamageSource source,float amount) {
        if (com.starfantasy.goety.combat.ApollyonDamageRules.commandKill(source,amount)) {
            combat.clear(); bossBar.m_8321_(false); this.m_146870_(); return true;
        }
        if(isSummoning())return false;
        source=com.starfantasy.goety.combat.ApollyonDamageRules.ordinaryKill(this,source,amount);
        if (combat.transitioning() || healthProtection.invulnerabilityTicks()>0 || combat.friendly(source.m_7639_())) return false;
        this.f_19802_=0; // The independent configurable hit window replaces vanilla's larger-hit exception.
        return super.m_6469_(source,amount);
    }
    @Override protected void m_6475_(net.minecraft.world.damagesource.DamageSource source,float amount) {
        if(isSummoning())return;
        if (combat.friendly(source.m_7639_())) return;
        if (combat.transitioning() || healthProtection.invulnerabilityTicks()>0) return;
        super.m_6475_(com.starfantasy.goety.combat.ApollyonDamageRules.ordinaryKill(this,source,amount),amount);
    }
    @Override public boolean m_5825_() { return true; }
    @Override public boolean m_213824_() { return false; } // Ordinary shields must not be disabled by our attacks.
    @Override public boolean m_21222_() { return true; }
    @Override public net.minecraft.world.entity.MobType m_6336_() { return net.minecraft.world.entity.MobType.f_21641_; }
    @Override public boolean m_142535_(float distance,float multiplier,net.minecraft.world.damagesource.DamageSource source) { return false; }
    @Override public boolean m_7301_(net.minecraft.world.effect.MobEffectInstance effect) {
        return effect.m_19544_()!=com.Polarice3.Goety.common.effects.GoetyEffects.BURN_HEX.get()
                && effect.m_19544_()!=net.minecraft.world.effect.MobEffects.f_19615_
                && effect.m_19544_().m_19483_()!=net.minecraft.world.effect.MobEffectCategory.HARMFUL && super.m_7301_(effect);
    }
    @Override public boolean m_147207_(net.minecraft.world.effect.MobEffectInstance effect, @javax.annotation.Nullable net.minecraft.world.entity.Entity source) {
        return (source==this || effect.m_19544_().m_19486_()) && super.m_147207_(effect,source);
    }
    @Override public com.starfantasy.library.combat.CombatHealthProtection combatHealthProtection() { return healthProtection; }
    @Override public double combatDamageCap() { return com.starfantasy.goety.config.AscianKingConfig.DAMAGE_CAP.get(); }
    @Override public int combatHitInvulnerabilityTicks() { return com.starfantasy.goety.config.AscianKingConfig.INVULNERABILITY.get(); }
    @Override public boolean combatHealthLocked() { return isSummoning() || combat.transitioning(); }
    @Override public boolean handleCombatHealthLoss(float loss) { return combat.interceptHealthLoss(loss); }
    public void setPhaseHealth(float value) {
        healthProtection.beginRestore();
        try { this.m_21153_(value); } finally { healthProtection.endRestore(); }
    }
    @Override public void m_6457_(net.minecraft.server.level.ServerPlayer player) { super.m_6457_(player); bossBar.m_6543_(player); combat.syncHud(); }
    @Override public void m_6452_(net.minecraft.server.level.ServerPlayer player) { super.m_6452_(player); bossBar.m_6539_(player); }

    @Override protected void m_8097_() {
        super.m_8097_();
        this.f_19804_.m_135372_(ACTION, 0);
        this.f_19804_.m_135372_(ACTION_SERIAL, 0);
        this.f_19804_.m_135372_(VISUAL_PHASE, 1);
        this.f_19804_.m_135372_(STUNNED, false);
        this.f_19804_.m_135372_(SUMMON_TICKS, 0);
        this.f_19804_.m_135372_(WINGS, true);
        this.f_19804_.m_135372_(WING_SCALE, 1.5F);
    }

    public boolean wingsVisible() { return this.f_19804_.m_135370_(WINGS); }
    public float wingScale() { return this.f_19804_.m_135370_(WING_SCALE); }
    public void setWingsVisible(boolean visible) { this.f_19804_.m_135381_(WINGS, visible); }
    public void setWingScale(float scale) {
        this.f_19804_.m_135381_(WING_SCALE, Float.isFinite(scale) ? Math.max(.6F, Math.min(2F, scale)) : 1.5F);
    }

    @Override public void m_7380_(CompoundTag tag) {
        super.m_7380_(tag);
        tag.m_128379_("AscianWings", wingsVisible());
        tag.m_128350_("AscianWingScale", wingScale());
        combat.save(tag);
        tag.m_128379_("AscianInitialized", initialized);
        tag.m_128405_("AscianHitImmunity", healthProtection.invulnerabilityTicks());
        tag.m_128405_("AscianSummonTicks",summonTicks());
        tag.m_128379_("AscianSummonOwnsControl",summonOwnsControl);
        tag.m_128379_("AscianSummonPreviousAi",summonPreviousAi);
        tag.m_128379_("AscianSummonPreviousGravity",summonPreviousGravity);
    }

    @Override public void m_7378_(CompoundTag tag) {
        healthProtection.beginRestore();
        try { super.m_7378_(tag); } finally { healthProtection.endRestore(); }
        this.m_21530_(); // Mob's reader overwrites the constructor flag from legacy NBT.
        if (tag.m_128441_("AscianWings")) setWingsVisible(tag.m_128471_("AscianWings"));
        if (tag.m_128441_("AscianWingScale")) setWingScale(tag.m_128457_("AscianWingScale"));
        initialized = tag.m_128471_("AscianInitialized");
        configuredHealth = -1;
        healthProtection.setInvulnerabilityTicks(tag.m_128451_("AscianHitImmunity"));
        combat.load(tag);
        this.f_19804_.m_135381_(SUMMON_TICKS,Math.max(0,Math.min(SUMMON_DURATION,tag.m_128451_("AscianSummonTicks"))));
        if(tag.m_128471_("AscianSummonOwnsControl")){
            this.m_21557_(tag.m_128471_("AscianSummonPreviousAi"));
            this.m_20242_(tag.m_128471_("AscianSummonPreviousGravity"));
        }
        summonOwnsControl=false;
        if(isSummoning())lockSummonControl();
    }

    @Override public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "body", 0, state -> {
            int serial=this.f_19804_.m_135370_(ACTION_SERIAL);
            if(serial!=clientAnimationSerial) { state.getController().forceAnimationReset(); clientAnimationSerial=serial; }
            String clip=switch(this.f_19804_.m_135370_(ACTION)) {
                case 1 -> "teleport"; case 2,10 -> "attack1"; case 11 -> "attack2"; case 12 -> "attack3"; case 13 -> "attack4";
                case 3 -> "lightning_call"; case 4 -> "barrage"; case 5 -> "shoot";
                default -> "idle";
            };
            return state.setAndContinue(this.m_21224_()?DEATH:clip.equals("idle")?IDLE:RawAnimation.begin().thenPlay(clip));
        }));
    }
    @Override public void m_6667_(net.minecraft.world.damagesource.DamageSource source) {
        combat.clear(); bossBar.m_8321_(false);
        super.m_6667_(source);
    }
    @Override protected void m_6153_() {
        if (this.f_20919_==0 && !this.m_9236_().f_46443_) { combat.clear(); bossBar.m_8321_(false); }
        ++this.f_20919_;
        if (this.f_20919_ == 1 && !this.m_9236_().f_46443_) {
            this.m_9236_().m_5594_(null, this.m_20183_(), net.minecraft.sounds.SoundEvents.f_12556_,
                    net.minecraft.sounds.SoundSource.HOSTILE, 1F, 1F);
        }
        this.m_21573_().m_26573_();
        this.m_20256_(net.minecraft.world.phys.Vec3.f_82478_);
        if (this.f_20919_ >= DEATH_TICKS && !this.m_9236_().f_46443_) {
            // Normal death tick, never an entity-leave callback. Particles/sound do not mutate entity sections.
            com.starfantasy.goety.combat.ApollyonDeathEffects.explodeAscian(this);
            this.m_9236_().m_7605_(this, (byte)60);
            this.m_142687_(net.minecraft.world.entity.Entity.RemovalReason.KILLED);
        }
    }
    public float effectOpacity(float partial) {
        return this.m_21224_() ? Math.max(0, Math.min(1, (DEATH_TICKS - this.f_20919_ - partial) / 20F)) : 1;
    }
    @Override public AnimatableInstanceCache getAnimatableInstanceCache() { return cache; }
}
