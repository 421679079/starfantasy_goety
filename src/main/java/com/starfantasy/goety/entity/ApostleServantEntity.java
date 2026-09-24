package com.starfantasy.goety.entity;

import com.Polarice3.Goety.api.entities.IOwned;
import com.Polarice3.Goety.common.entities.ally.Summoned;
import com.Polarice3.Goety.common.entities.ModEntityType;
import com.Polarice3.Goety.common.entities.neutral.Owned;
import com.Polarice3.Goety.common.entities.hostile.servants.Inferno;
import com.Polarice3.Goety.common.entities.hostile.servants.ObsidianMonolith;
import com.Polarice3.Goety.common.entities.projectiles.DeathArrow;
import com.Polarice3.Goety.common.entities.projectiles.NetherMeteor;
import com.Polarice3.Goety.common.entities.util.LightningTrap;
import com.Polarice3.Goety.config.AttributesConfig;
import com.Polarice3.Goety.config.MobsConfig;
import com.Polarice3.Goety.init.ModSounds;
import com.Polarice3.Goety.common.effects.GoetyEffects;
import com.Polarice3.Goety.utils.BlockFinder;
import com.starfantasy.goety.api.ApostleAppearanceAccess;
import com.starfantasy.goety.combat.apostle.*;
import com.starfantasy.goety.combat.ApollyonDeathEffects;
import com.starfantasy.goety.combat.ServantMobility;
import com.starfantasy.goety.config.ServantConfig;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import com.Polarice3.Goety.common.items.ModItems;
import net.minecraft.world.damagesource.*;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;
import java.util.*;

/** Independent servant AI without the hostile Apostle superclass. */
public final class ApostleServantEntity extends Summoned implements GeoEntity, ApostleAppearanceAccess, ApostleCastAccess, com.starfantasy.goety.api.HitCooldownAccess {
    public static final String[] TITLES = {"risen", "abhorrent", "defiler", "dark", "great_shadow", "witch_king",
            "pyre_lord", "profane", "cruel", "terrible", "glorious", "atrocious"};
    private static final EntityDataAccessor<Integer> TITLE = SynchedEntityData.m_135353_(ApostleServantEntity.class, EntityDataSerializers.f_135028_);
    private static final EntityDataAccessor<Boolean> SECOND = SynchedEntityData.m_135353_(ApostleServantEntity.class, EntityDataSerializers.f_135035_);
    private static final EntityDataAccessor<Boolean> CASTING = SynchedEntityData.m_135353_(ApostleServantEntity.class, EntityDataSerializers.f_135035_);
    private static final EntityDataAccessor<Boolean> SHIELD = SynchedEntityData.m_135353_(ApostleServantEntity.class, EntityDataSerializers.f_135035_);
    private static final EntityDataAccessor<Boolean> TRANSITION = SynchedEntityData.m_135353_(ApostleServantEntity.class, EntityDataSerializers.f_135035_);
    private static final EntityDataAccessor<Boolean> MONOLITH = SynchedEntityData.m_135353_(ApostleServantEntity.class, EntityDataSerializers.f_135035_);
    private static final EntityDataAccessor<Integer> DEATH_AGE = SynchedEntityData.m_135353_(ApostleServantEntity.class, EntityDataSerializers.f_135028_);
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private final ApostleServantTeleport teleport = new ApostleServantTeleport(this);
    private final ApostleServantSpell spell = new ApostleServantSpell(this);
    private int invulnerable, antiRegen, shotCooldown=40, spellCooldown=60, monolithCooldown, infernoCooldown;
    private int utilityCast, utilityTicks, transitionAge;
    private int idleSecondTicks, roarCooldown, receivedHits, utilityCooldown, tornadoCooldown;
    private float transitionStart;
    private UUID pillar;
    private final Map<UUID,Long> infernos = new HashMap<>();
    private long pillarExpires;
    private boolean utilityNext;
    private double deathGroundY;
    private static final AttributeModifier MONOLITH_SLOW = new AttributeModifier(
            UUID.fromString("1d73cebd-9b39-49d9-84a1-f160dfc86c10"),"Servant monolith recovery",-.25,AttributeModifier.Operation.ADDITION);

    public ApostleServantEntity(EntityType<? extends ApostleServantEntity> type, Level level) {
        super(type, level); setConfigurableAttributes(); m_21153_(m_21233_()); setHasLifespan(false); m_21530_();
        ServantMobility.configure(this);
        m_8061_(EquipmentSlot.MAINHAND, new ItemStack(Items.f_42411_)); m_21409_(EquipmentSlot.MAINHAND, 0); f_21364_=0;
    }
    @Override public int getSummonLimit(LivingEntity owner) {
        return com.starfantasy.goety.servant.ServantOwnershipData.summonLimit(this,owner);
    }
    @Override public void onAddedToWorld() {
        super.onAddedToWorld();
        com.starfantasy.goety.servant.ServantOwnershipData.track(this);
    }
    @Override public void setOwnerId(UUID owner) {
        super.setOwnerId(owner);
        com.starfantasy.goety.servant.ServantOwnershipData.track(this);
    }
    public static AttributeSupplier.Builder createAttributes() {
        return Mob.m_21552_().m_22268_(Attributes.f_22276_,320).m_22268_(Attributes.f_22284_,12)
                .m_22268_(Attributes.f_22285_,12).m_22268_(Attributes.f_22278_,.75)
                .m_22268_(Attributes.f_22279_,.3).m_22268_(Attributes.f_22277_,32).m_22268_(Attributes.f_22281_,10);
    }
    @Override public void setConfigurableAttributes() {
        m_21051_(Attributes.f_22276_).m_22100_(ServantConfig.APOSTLE_HEALTH.get());
        m_21051_(Attributes.f_22284_).m_22100_(ServantConfig.APOSTLE_ARMOR.get());
        m_21051_(Attributes.f_22285_).m_22100_(ServantConfig.APOSTLE_ARMOR.get());
    }
    @Override protected void m_8097_() {
        super.m_8097_(); f_19804_.m_135372_(TITLE,0); f_19804_.m_135372_(SECOND,false);
        f_19804_.m_135372_(CASTING,false); f_19804_.m_135372_(SHIELD,false); f_19804_.m_135372_(TRANSITION,false); f_19804_.m_135372_(MONOLITH,false);
        f_19804_.m_135372_(DEATH_AGE,0);
    }
    @Override protected void m_8099_() {
        super.m_8099_();
        f_21345_.m_25352_(0,new Goal() {
            { m_7021_(EnumSet.of(Flag.MOVE,Flag.LOOK,Flag.JUMP)); }
            public boolean m_8036_() { return isStaying() || isSettingUpSecond(); }
            public void m_8037_() { m_21573_().m_26573_(); if(isStaying()) m_6710_(null); }
        });
        f_21345_.m_25352_(2,new Goal() {
            { m_7021_(EnumSet.of(Flag.MOVE,Flag.LOOK)); }
            public boolean m_8036_() { return !isStaying() && !isSettingUpSecond() && m_5448_()!=null && m_5448_().m_6084_(); }
            public boolean m_183429_() { return true; }
            public void m_8056_() { m_21561_(true); }
            public void m_8037_() { combatTick(m_5448_()); }
            public void m_8041_() { cancelCasting(); m_5810_(); m_21561_(false); }
        });
        f_21345_.m_25352_(8,new Summoned.WanderGoal<>(this,1));
    }
    public void initializeTitle(int title) {
        f_19804_.m_135381_(TITLE,Math.max(0,Math.min(11,title)));
        m_6593_(Component.m_237115_("entity.starfantasy_goety.apostle.title."+TITLES[getTitleNumber()]));
    }
    public int getTitleNumber() { return f_19804_.m_135370_(TITLE); }
    @Override public int starfantasy$visualTitle() { return getTitleNumber(); }
    public boolean isSecondPhase() { return f_19804_.m_135370_(SECOND); }
    public boolean isSettingUpSecond() { return f_19804_.m_135370_(TRANSITION); }
    public boolean isCasting() { return f_19804_.m_135370_(CASTING) || isSettingUpSecond(); }
    public boolean isAimingBow() { return m_6084_() && !isCasting() && (m_5912_() || m_6117_()); }
    public void setCasting(boolean value) { f_19804_.m_135381_(CASTING,value); }
    public float damageMultiplier() { return ServantConfig.APOSTLE_DAMAGE_MULTIPLIER.get().floatValue(); }
    @Override public void starfantasy$clearHitCooldown() { invulnerable = 0; }
    public boolean isInNether() { return m_9236_().m_46472_()==Level.f_46429_; }
    public boolean isFriendlyEntity(Entity other) { return m_7307_(other); }
    @Override public boolean m_7307_(Entity other) {
        if(other==null) return false;
        if(other==this) return true;
        UUID owner=getOwnerId();
        if(owner!=null) {
            if(owner.equals(other.m_20148_())) return true;
            Entity root=other; Set<Entity> visited=new HashSet<>();
            while(root instanceof IOwned owned && visited.add(root)) {
                if(root==this || owner.equals(owned.getOwnerId())) return true;
                root=owned.getTrueOwner();
            }
            if(other instanceof OwnableEntity pet && owner.equals(pet.m_21805_())) return true;
        }
        return super.m_7307_(other);
    }
    @Override public boolean m_6779_(LivingEntity target) { return target!=null && !isStaying() && !m_7307_(target) && super.m_6779_(target); }
    @Override public void overrideSetTarget(LivingEntity target) { super.overrideSetTarget(isStaying()?null:target); }
    @Override public void setPriorityTarget(LivingEntity target) { super.setPriorityTarget(isStaying()?null:target); }
    @Override public MobType m_6336_() { return MobType.f_21641_; }
    @Override public boolean m_6040_() { return true; }
    @Override public void m_7311_(int ticks) { super.m_7311_(Math.min(0,ticks)); }
    @Override public boolean m_203441_(net.minecraft.world.level.material.FluidState fluid) {
        return ServantMobility.canStandOn(fluid);
    }
    @Override public boolean m_142535_(float distance,float multiplier,DamageSource source) { return false; }
    @Override protected float m_6431_(Pose pose,EntityDimensions dimensions) { return 1.55F; }
    @Override public boolean m_7301_(MobEffectInstance effect) {
        return effect.m_19544_()!=GoetyEffects.BURN_HEX.get() && effect.m_19544_()!=MobEffects.f_19615_
                && effect.m_19544_().m_19483_()!=MobEffectCategory.HARMFUL && super.m_7301_(effect);
    }
    @Override public boolean m_147207_(MobEffectInstance effect, @javax.annotation.Nullable Entity source) {
        // External effects must be beneficial. Apply accepted effects normally;
        // do not copy the native Apostle's return-only branch.
        return (source==this || effect.m_19544_().m_19486_()) && super.m_147207_(effect,source);
    }
    @Override public void m_5634_(float amount) { if(antiRegen<=0) super.m_5634_(amount); }
    @Override public void tryKill(Player player) { if(getKillChance()<=0) warnKill(player); else super.tryKill(player); }
    @Override public boolean m_6469_(DamageSource source,float amount) {
        boolean bypass=source.m_269533_(DamageTypeTags.f_268738_);
        boolean shieldHit=ApostleShield.get(this)>0;
        if(shieldHit) { invulnerable=0; f_19802_=0; }
        if(source.m_7640_() instanceof LivingEntity attacker) {
            int smite=EnchantmentHelper.m_44836_(Enchantments.f_44978_,attacker); if(smite>0) antiRegen=Math.min(5,smite)*20;
        }
        if(isSettingUpSecond() || (!bypass && (invulnerable>0 || hasMonolithProtection()))) return false;
        if(source.m_7639_()!=null && m_7307_(source.m_7639_())) return false;
        if(source.m_7640_() instanceof NetherMeteor || source.m_7639_() instanceof NetherMeteor) return false;
        if(source.m_276093_(DamageTypes.f_268450_) || source.m_276093_(DamageTypes.f_268671_) || source.m_276093_(DamageTypes.f_268612_)) return false;
        if(!bypass) {
            if(isInNether()) amount*=1-ServantConfig.APOSTLE_NETHER_REDUCTION.get();
            if(source.m_269533_(DamageTypeTags.f_268731_)) amount*=1-ServantConfig.APOSTLE_MAGIC_RESISTANCE.get();
        }
        boolean hurt=super.m_6469_(source,amount);
        if(hurt && !bypass) { ++receivedHits; invulnerable=shieldHit?0:MobsConfig.BossInvulnerabilityTime.get(); teleport.onHurt(); }
        if(shieldHit) f_19802_=0;
        return hurt;
    }
    @Override protected void m_6475_(DamageSource source,float amount) {
        if(!source.m_269533_(DamageTypeTags.f_268738_)) amount=Math.min(amount,AttributesConfig.ApostleDamageCap.get().floatValue());
        float shield=ApostleShield.get(this);
        if(shield>0) { float used=Math.min(shield,amount); ApostleShield.set(this,shield-used); amount-=used; }
        if(amount>0) super.m_6475_(source,amount);
    }
    private ObsidianMonolith monolith() {
        return pillar!=null && m_9236_() instanceof ServerLevel level && level.m_8791_(pillar) instanceof ObsidianMonolith p && p.m_6084_()?p:null;
    }
    public boolean monolithVisible() { return f_19804_.m_135370_(MONOLITH); }
    public boolean hasMonolithProtection() { ObsidianMonolith p=monolith(); return p!=null && !p.isEmerging(); }
    public boolean monolithWeakened() { return hasMonolithProtection() || monolithCooldown>900; }
    @Override public void m_8119_() {
        super.m_8119_();
        ServantMobility.floatInLava(this);
        if(m_9236_().f_46443_ && isSettingUpSecond()) {
            for(int i=0;i<40;i++) m_9236_().m_7107_(net.minecraft.core.particles.ParticleTypes.f_123755_,
                    m_20185_(),m_20186_()+.5,m_20189_(),
                    m_217043_().m_188583_()*.2,m_217043_().m_188583_()*.2,m_217043_().m_188583_()*.2);
        }
        if(m_9236_().f_46443_ || !m_6084_()) return;
        if(tornadoCooldown>0) --tornadoCooldown;
        if(roarCooldown>0) --roarCooldown; if(utilityCooldown>0) --utilityCooldown;
        if(isSecondPhase()) {
            if(!ServantConfig.APOSTLE_AUTO_RESET_PHASE.get() || m_5448_()!=null && m_5448_().m_6084_()) idleSecondTicks=0;
            else if(++idleSecondTicks>=1200) resetCombatPhase();
            if(isSecondPhase() && f_19797_%20==0) ApostleServantNativeSpells.meteor(this);
            if(isSecondPhase() && f_19797_%100==0) com.starfantasy.goety.combat.ServantPhaseWeather.thunder(this);
        } else idleSecondTicks=0;
        if(f_19797_%10==0) redirectMonolithAggro();
        if(invulnerable>0) --invulnerable; if(antiRegen>0) --antiRegen; if(shotCooldown>0) --shotCooldown;
        if(spellCooldown>0) --spellCooldown; if(monolithCooldown>0) --monolithCooldown; if(infernoCooldown>0) --infernoCooldown;
        long now=m_9236_().m_46467_(); infernos.entrySet().removeIf(e->m_9236_() instanceof ServerLevel level && level.m_8791_(e.getKey()) instanceof LivingEntity minion && !minion.m_6084_());
        if(pillar!=null && now>=pillarExpires) minionRemoved(pillar);
        AttributeInstance speed=m_21051_(Attributes.f_22279_);
        if(speed!=null) {
            if(monolithWeakened() && !speed.m_22109_(MONOLITH_SLOW)) speed.m_22118_(MONOLITH_SLOW);
            else if(!monolithWeakened() && speed.m_22109_(MONOLITH_SLOW)) speed.m_22130_(MONOLITH_SLOW);
        }
        if(!isSecondPhase() && !isSettingUpSecond() && m_5448_()!=null && m_21223_()<=m_21233_()/2) {
            cancelCasting(); teleport.clear(); m_5810_(); transitionStart=m_21223_(); transitionAge=0; antiRegen=0;
            f_19804_.m_135381_(TRANSITION,true);
        }
        if(isSettingUpSecond()) {
            ++transitionAge; antiRegen=0;
            ServerLevel level=(ServerLevel)m_9236_();
            com.Polarice3.Goety.utils.ServerParticleUtil.windParticle(level,com.Polarice3.Goety.utils.ColorUtil.BLACK,2,1.5F,m_19879_(),m_20182_());
            com.Polarice3.Goety.utils.ServerParticleUtil.windParticle(level,com.Polarice3.Goety.utils.ColorUtil.BLACK,4,.5F,m_19879_(),m_20182_());
            m_21153_(Math.max(m_21223_(),transitionHealth(transitionStart,m_21233_(),transitionAge)));
            for(LivingEntity e:m_9236_().m_45976_(LivingEntity.class,m_20191_().m_82400_(3))) {
                if(!m_7307_(e)) { Vec3 d=e.m_20182_().m_82546_(m_20182_()).m_82541_(); e.m_20256_(new Vec3(d.f_82479_*.3,.1,d.f_82481_*.3)); }
            }
            if(transitionAge>=100) {
                m_21153_(m_21233_()); f_19804_.m_135381_(SECOND,true); f_19804_.m_135381_(TRANSITION,false);
                com.starfantasy.goety.combat.ServantPhaseWeather.thunder(this);
            }
        } else {
            if(f_19797_%20==0) m_5634_(ServantConfig.APOSTLE_REGENERATION.get().floatValue()*(getTitleNumber()==0?4:1));
            if(hasMonolithProtection() && f_19797_% (isSecondPhase()?40:80)==0) m_5634_(1);
            teleport.tick(m_5448_());
        }
        f_19804_.m_135381_(MONOLITH,hasMonolithProtection());
        if(getTitleNumber()==9) m_147207_(new MobEffectInstance(MobEffects.f_19596_,60,1,false,false),this);
        if(getTitleNumber()==10) m_147207_(new MobEffectInstance(MobEffects.f_19606_,60,0,false,false),this);
        if(isStaying()) { m_6710_(null); teleport.clear(); cancelCasting(); }
        if(isSecondPhase() && m_9236_().m_46472_()==Level.f_46428_ && f_19797_%100==0 && m_5448_()!=null && m_9236_().m_46758_(m_5448_().m_20183_())) {
            LivingEntity t=m_5448_(); LightningTrap trap=new LightningTrap(m_9236_(),t.m_20185_(),t.m_20186_(),t.m_20189_());
            trap.setOwner(this); trap.setDuration(50); m_9236_().m_7967_(trap);
        }
    }
    public static float transitionHealth(float start,float maximum,int tick) { return start+(maximum-start)*Math.min(1,Math.max(0,tick)/100F); }

    private void resetCombatPhase() {
        this.f_19804_.m_135381_(SECOND, false);
        this.idleSecondTicks = 0;
    }

    @Override
    public InteractionResult m_6071_(Player player, InteractionHand hand) {
        ItemStack stack = player.m_21120_(hand);
        if (this.m_6084_() && stack.m_150930_(ModItems.UNHOLY_BLOOD.get())) {
            boolean fullHealthBeforeFeeding = this.m_21223_() >= this.m_21233_();
            if (!this.isSecondPhase() && fullHealthBeforeFeeding) {
                if (!this.m_9236_().f_46443_) {
                    player.m_5661_(net.minecraft.network.chat.Component.m_237110_(
                            "message.starfantasy_goety.servant.best_condition", this.m_7755_())
                            .m_130940_(net.minecraft.ChatFormatting.RED), true);
                }
                return InteractionResult.FAIL;
            }
            if (!this.m_9236_().f_46443_) {
                // Blood restores health immediately, even while passive regeneration is blocked.
                this.m_21153_(Math.min(this.m_21233_(), this.m_21223_() + this.m_21233_() * 0.5F));
                if (!ServantConfig.APOSTLE_AUTO_RESET_PHASE.get() && this.isSecondPhase()
                        && fullHealthBeforeFeeding) {
                    this.resetCombatPhase();
                    player.m_5661_(net.minecraft.network.chat.Component.m_237110_(
                            "message.starfantasy_goety.servant.restored_condition", this.m_7755_())
                            .m_130940_(net.minecraft.ChatFormatting.GREEN), true);
                }
                if (!player.m_150110_().f_35937_) stack.m_41774_(1);
                this.m_5496_(net.minecraft.sounds.SoundEvents.f_11911_, 1.0F, 1.0F);
            }
            return InteractionResult.m_19078_(this.m_9236_().f_46443_);
        }
        return super.m_6071_(player, hand);
    }
    public void startTitleSpellCooldown(int ticks) { spellCooldown=ticks; }
    private void combatTick(LivingEntity target) {
        if(target==null || isFriendlyEntity(target)) { m_6710_(null); return; }
        m_21563_().m_24960_(target,30,30);
        if(teleport.isPending()) { m_21573_().m_26573_(); m_5810_(); return; }
        if(spell.active()) { spell.tick(); return; }
        if(utilityCast!=0) {
            m_21573_().m_26573_();
            ApostleServantNativeSpells.particles(this,utilityCast);
            --utilityTicks;
            if(utilityCast==5 && utilityTicks==10 && isSecondPhase()) ApostleServantNativeSpells.roarTrap(this);
            if(utilityTicks<=0) {
                switch(utilityCast) {
                    case 1 -> summonMonolith(); case 2 -> summonInfernos();
                    case 3 -> ApostleServantNativeSpells.cloud(this,target);
                    case 4 -> { ApostleServantNativeSpells.tornado(this,target); receivedHits=0; }
                    case 5 -> { ApostleServantNativeSpells.roar(this); receivedHits=0; }
                }
                utilityCast=0; utilityCooldown=isSecondPhase()?(m_21223_()<=m_21233_()/4?20:25):50; setCasting(false);
            }
            return;
        }
        if(roarCooldown==0 && m_20280_(target)<16) { startUtility(5); roarCooldown=120; return; }
        if(receivedHits>=6 && ApostleServantNativeSpells.canTornado(this)) { startUtility(4); return; }
        if(utilityCooldown==0 && utilityNext) {
            utilityNext=false;
            java.util.List<Integer> available=new java.util.ArrayList<>();
            if(monolithCooldown==0 && pillar==null) available.add(1);
            if(infernoCooldown==0 && infernos.size()<4) available.add(2);
            if(ApostleServantNativeSpells.canCloud(this)) available.add(3);
            if(tornadoCooldown==0 && infernos.size()>=2 && ApostleServantNativeSpells.canTornado(this)) available.add(4);
            if(!available.isEmpty()) { startUtility(available.get(m_217043_().m_188503_(available.size()))); return; }
        }
        if(spellCooldown==0 && m_21574_().m_148306_(target)) { spell.start(); utilityNext=true; return; }
        if(m_20280_(target)>144 || !m_21574_().m_148306_(target)) m_21573_().m_5624_(target,1);
        else { m_21573_().m_26573_(); m_21566_().m_24988_(0,f_19797_/40%2==0?.4F:-.4F); }
        int draw=20;
        if(shotCooldown<=draw && !m_6117_()) m_6672_(InteractionHand.MAIN_HAND);
        if(shotCooldown<=0 && m_6117_() && m_21252_()>=20 && m_21574_().m_148306_(target)) {
            shoot(target); m_5810_(); shotCooldown=monolithWeakened()?60:(isSecondPhase()?(m_21223_()<=m_21233_()/4?25:30):40); teleport.onShot(target);
        }
    }
    private void startUtility(int kind) {
        utilityCast=kind; utilityTicks=kind==1?40:kind==2?60:20;
        m_5810_(); setCasting(true); m_21573_().m_26573_();
        m_5496_(ModSounds.APOSTLE_PREPARE_SUMMON.get(),2,1);
    }
    public void onTornadoExpired() { tornadoCooldown+=900; }
    public ObsidianMonolith aggroMonolith() {
        ObsidianMonolith p=monolith(); return p!=null && !p.isEmerging()?p:null;
    }
    private void redirectMonolithAggro() {
        ObsidianMonolith p=aggroMonolith(); if(p==null) return;
        for(Mob mob:m_9236_().m_45976_(Mob.class,m_20191_().m_82400_(64)))
            if(mob.m_5448_()==this && !isFriendlyEntity(mob)) mob.m_6710_(p);
    }
    private void shoot(LivingEntity target) {
        DeathArrow arrow=new DeathArrow(m_9236_(),this); arrow.m_36745_(this,AttributesConfig.ApostleBowDamage.get().floatValue()); arrow.m_36762_(false);
        arrow.m_6034_(m_20185_(),m_20186_()+1.35,m_20189_());
        MobEffect effect=switch(getTitleNumber()) {
            case 1 -> target.m_21222_()?MobEffects.f_19601_:MobEffects.f_19602_;
            case 2 -> isSecondPhase()?GoetyEffects.ACID_VENOM.get():MobEffects.f_19614_;
            case 3 -> MobEffects.f_19615_; case 4 -> isSecondPhase()?MobEffects.f_19610_:MobEffects.f_216964_;
            case 5 -> MobEffects.f_19613_; case 6 -> GoetyEffects.BURN_HEX.get(); case 7 -> MobEffects.f_19612_;
            case 8 -> MobEffects.f_19597_; case 11 -> GoetyEffects.SAPPED.get(); default -> null;
        };
        if(effect!=null) arrow.m_36870_(new MobEffectInstance(effect,effect.m_8093_()?1:200,isSecondPhase()&&getTitleNumber()!=1?1:0));
        if(getTitleNumber()==6) arrow.m_7311_(100);
        arrow.m_6686_(target.m_20185_()-arrow.m_20185_(),target.m_20227_(.5)-arrow.m_20186_(),target.m_20189_()-arrow.m_20189_(),3.2F,1);
        m_9236_().m_7967_(arrow); m_5496_(ModSounds.APOSTLE_SHOOT.get(),1,1);
    }
    private void summonMonolith() {
        if(!(m_9236_() instanceof ServerLevel level) || pillar!=null) return;
        ObsidianMonolith p=new ObsidianMonolith(ModEntityType.OBSIDIAN_MONOLITH.get(),level);
        BlockPos pos=BlockFinder.SummonRadius(m_20183_().m_7918_(6,0,6),p,level,5);
        p.m_20035_(pos,0,0); p.setTrueOwner(this); p.shouldSpawnHeretics=false;
        p.m_6518_(level,level.m_6436_(pos),MobSpawnType.MOB_SUMMONED,null,null); markSummon(p);
        p.m_21051_(Attributes.f_22276_).m_22100_(20); p.m_21153_(20);
        if(level.m_7967_(p)) { pillar=p.m_20148_(); pillarExpires=level.m_46467_()+600; monolithCooldown=1200; }
    }
    private void summonInfernos() {
        if(!(m_9236_() instanceof ServerLevel level)) return;
        int count=Math.min(isSecondPhase()?2:1,4-infernos.size());
        for(int i=0;i<count;i++) {
            Inferno minion=new Inferno(ModEntityType.INFERNO.get(),level);
            BlockPos pos=BlockFinder.SummonRadius(m_20183_(),minion,level,5); minion.m_20035_(pos,0,0); minion.setTrueOwner(this);
            minion.m_6518_(level,level.m_6436_(pos),MobSpawnType.MOB_SUMMONED,null,null);
            minion.setLimitedLife(600); minion.m_6710_(m_5448_()); markSummon(minion);
            if(level.m_7967_(minion)) infernos.put(minion.m_20148_(),level.m_46467_()+600);
        }
        infernoCooldown=900;
    }
    private void markSummon(Owned summon) {
        if(summon instanceof ObsidianMonolith) summon.getPersistentData().m_128356_("StarFantasyServantExpires",m_9236_().m_46467_()+600);
        summon.getPersistentData().m_128362_("StarFantasyServantOwner",m_20148_()); summon.setHostile(false);
    }
    public void minionRemoved(UUID id) { if(id.equals(pillar)) { pillar=null; monolithCooldown=1200; } infernos.remove(id); }
    private void cancelCasting() { if(spell!=null) spell.stop(); utilityCast=0; setCasting(false); }
    public int deathAge() { return f_19804_.m_135370_(DEATH_AGE); }
    // A temporary health-read override must not interrupt an already started finale.
    @Override public boolean m_21224_() { return deathAge()>0 || super.m_21224_(); }
    @Override public boolean m_6084_() { return deathAge()==0 && super.m_6084_(); }
    @Override protected net.minecraft.sounds.SoundEvent m_7975_(DamageSource source) { return com.starfantasy.goety.registry.ApostleAppearanceSounds.MOE_HURT.get(); }
    @Override protected void m_6677_(DamageSource source) {
        net.minecraft.sounds.SoundEvent sound=m_7975_(source);
        if(sound!=null) m_5496_(sound,m_6121_(),1.0F);
    }
    @Override protected net.minecraft.sounds.SoundEvent m_5592_() { return ModSounds.APOSTLE_PREDEATH.get(); }
    @Override public void m_6667_(DamageSource source) {
        if(deathAge()>0) return;
        deathGroundY=m_20186_();
        cancelCasting(); teleport.clear(); m_5810_(); m_21561_(false);
        setStaying(false); f_19812_=true;
        setPriorityTarget(null); m_6710_(null); m_21573_().m_26573_();
        m_21008_(InteractionHand.MAIN_HAND,ItemStack.f_41583_);
        ApostleShield.set(this,0); f_19804_.m_135381_(MONOLITH,false);
        m_20256_(Vec3.f_82478_); m_20242_(true);
        super.m_6667_(source);
    }
    @Override protected void m_6153_() {
        m_20256_(Vec3.f_82478_); m_20242_(true); f_19812_=true;
        if(m_9236_().f_46443_ || m_213877_()) return;
        if(deathAge()==0) deathGroundY=m_20186_();
        int age=deathAge()+1; f_20919_=age; f_19804_.m_135381_(DEATH_AGE,age);
        if(ApollyonDeathEffects.tickApollyon(this,age,deathGroundY)) {
            com.starfantasy.goety.combat.ServantPhaseWeather.death(this);
            ApollyonDeathEffects.explodeApollyon(this,net.minecraft.core.particles.ParticleTypes.f_123744_);
            m_9236_().m_7605_(this,(byte)60);
            m_142687_(Entity.RemovalReason.KILLED);
        }
    }
    @Override public void m_7380_(CompoundTag tag) {
        super.m_7380_(tag); tag.m_128405_("TitleNumber",getTitleNumber()); tag.m_128379_("secondPhase",isSecondPhase());
        tag.m_128405_("ServantIdleSecondTicks",idleSecondTicks);
        tag.m_128405_("ServantRoarCooldown",roarCooldown);
        tag.m_128405_("ServantTornadoCooldown",tornadoCooldown);
        tag.m_128405_("ServantUtilityCooldown",utilityCooldown);
        tag.m_128405_("ServantDeathAge",deathAge());
        tag.m_128347_("ServantDeathGroundY",deathGroundY);
        tag.m_128405_("TransitionAge",isSettingUpSecond()?transitionAge:-1); tag.m_128350_("TransitionStart",transitionStart);
        tag.m_128405_("MonolithCooldown",monolithCooldown); tag.m_128405_("InfernoCooldown",infernoCooldown);
        tag.m_128405_("SpellCooldown",spellCooldown); tag.m_128405_("AntiRegen",antiRegen);
        if(pillar!=null)tag.m_128362_("ServantPillar",pillar); tag.m_128356_("PillarExpires",pillarExpires);
        CompoundTag ids=new CompoundTag(); infernos.forEach((id,end)->ids.m_128356_(id.toString(),end)); tag.m_128365_("ServantInfernos",ids);
    }
    @Override public void m_7378_(CompoundTag tag) {
        super.m_7378_(tag);
        if (tag.m_128441_("TitleNumber") && !tag.m_128441_("CustomName")) initializeTitle(tag.m_128451_("TitleNumber"));
        else f_19804_.m_135381_(TITLE,Math.max(0,Math.min(11,tag.m_128451_("TitleNumber"))));
        f_19804_.m_135381_(SECOND,tag.m_128471_("secondPhase"));
        f_19804_.m_135381_(DEATH_AGE,Math.max(0,tag.m_128451_("ServantDeathAge")));
        deathGroundY=tag.m_128441_("ServantDeathGroundY")?tag.m_128459_("ServantDeathGroundY"):m_20186_();
        idleSecondTicks=Math.max(0,tag.m_128451_("ServantIdleSecondTicks"));
        roarCooldown=Math.max(0,tag.m_128451_("ServantRoarCooldown"));
        tornadoCooldown=Math.max(0,tag.m_128451_("ServantTornadoCooldown"));
        utilityCooldown=Math.max(0,tag.m_128451_("ServantUtilityCooldown"));
        transitionAge=tag.m_128441_("TransitionAge")?tag.m_128451_("TransitionAge"):-1;
        transitionStart=tag.m_128457_("TransitionStart"); f_19804_.m_135381_(TRANSITION,transitionAge>=0);
        monolithCooldown=tag.m_128451_("MonolithCooldown"); infernoCooldown=tag.m_128451_("InfernoCooldown");
        spellCooldown=tag.m_128451_("SpellCooldown"); antiRegen=tag.m_128451_("AntiRegen"); pillarExpires=tag.m_128454_("PillarExpires");
        if(tag.m_128403_("ServantPillar"))pillar=tag.m_128342_("ServantPillar");
        infernos.clear(); CompoundTag ids=tag.m_128469_("ServantInfernos");
        for(String key:ids.m_128431_())try{infernos.put(UUID.fromString(key),ids.m_128454_(key));}catch(IllegalArgumentException ignored){}
    }
    @Override public void registerControllers(AnimatableManager.ControllerRegistrar controllers) { }
    @Override public AnimatableInstanceCache getAnimatableInstanceCache() { return cache; }
    @Override public void starfantasy$castTimer(int ticks) { }
    @Override public boolean starfantasy$teleportPending() { return teleport.isPending(); }
    @Override public boolean starfantasy$shieldVisible() { return f_19804_.m_135370_(SHIELD); }
    @Override public void starfantasy$shieldVisible(boolean visible) { f_19804_.m_135381_(SHIELD,visible); }
}
