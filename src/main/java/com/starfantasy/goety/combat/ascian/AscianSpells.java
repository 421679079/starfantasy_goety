package com.starfantasy.goety.combat.ascian;

import com.starfantasy.goety.config.AscianKingConfig;
import com.starfantasy.goety.combat.ApollyonDamageSources;
import com.Polarice3.Goety.utils.ModDamageSource;
import com.starfantasy.goety.entity.*;
import com.starfantasy.goety.network.StarFantasyGoetyNetwork;
import com.starfantasy.library.vfx.StarFantasyVfx;
import com.starfantasy.library.vfx.particle.WarningColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.*;
import net.minecraftforge.registries.ForgeRegistries;
import java.util.*;

/** Per-caster delayed actions; no global pending queues or Souls Weaponry entities. */
public final class AscianSpells {
    public static final String OWNER="AscianSpellOwner", KIND="AscianSpellKind", NO_LOOT="AscianNoLoot";
    private final AscianCombatController combat;
    private final AscianKingEntity boss;
    public final AscianMeteorSpell meteors;
    public final AscianIceRingSpell iceRings;
    private long revision;
    private long castRevision;
    private UUID mixedTarget;
    private int mixedTargetId=-1;
    private final List<Pending> pending=new ArrayList<>();
    private final Set<UUID> children=new HashSet<>();
    private final Map<UUID,Integer> childGrace=new HashMap<>();
    private record Pending(int tick,Runnable action) {}
    public long epoch(){return revision+meteors.revision();}
    public AscianSpells(AscianCombatController combat) { this.combat=combat; boss=combat.boss;meteors=new AscianMeteorSpell(boss,this);iceRings=new AscianIceRingSpell(boss,this); }
    private void later(int ticks,Runnable action) { pending.add(new Pending(boss.f_19797_+Math.max(1,ticks),action)); }
    public void tick() {
        if(mixedTarget!=null){LivingEntity target=boss.m_5448_();if(target==null||combat.skip(target)||target.m_19879_()!=mixedTargetId||!mixedTarget.equals(target.m_20148_()))stopCasting();}
        long run=revision,hazardRun=meteors.revision(),castRun=castRevision;
        List<Pending> due=new ArrayList<>();
        pending.removeIf(p->{if(p.tick<=boss.f_19797_) { due.add(p); return true; } return false;});
        for(Pending p:due) {
            if(revision!=run||meteors.revision()!=hazardRun||castRevision!=castRun||!boss.m_6084_())return;
            if(mixedTarget!=null){LivingEntity target=boss.m_5448_();if(target==null||combat.skip(target)||target.m_19879_()!=mixedTargetId||!mixedTarget.equals(target.m_20148_())){stopCasting();return;}}
            p.action.run();
        }
        if(castRevision!=castRun)return;
        if(pending.isEmpty()){mixedTarget=null;mixedTargetId=-1;}
        if(revision!=run||meteors.revision()!=hazardRun)return;
        meteors.tick();
        if(revision!=run||meteors.revision()!=hazardRun)return;
        iceRings.tick();
        if(revision!=run||meteors.revision()!=hazardRun)return;
        if(boss.f_19797_%100==0)children.removeIf(id->{
            if(boss.f_19797_<childGrace.getOrDefault(id,0)||((ServerLevel)boss.m_9236_()).m_8791_(id)!=null)return false;
            childGrace.remove(id);return true;
        });
    }
    public void own(Entity entity) { if(entity!=null){children.add(entity.m_20148_());childGrace.put(entity.m_20148_(),boss.f_19797_+100);} }
    /** Cancel future casting only. Never removes world entities or changes their lifetime epoch. */
    public void stopCasting(){
        ++castRevision;pending.clear();mixedTarget=null;mixedTargetId=-1;
    }
    public void clear() {
        ++revision;
        ++castRevision;mixedTarget=null;mixedTargetId=-1;
        meteors.clear();
        iceRings.clear();
        pending.clear();
        if(boss.m_9236_() instanceof ServerLevel level)for(UUID id:children) { Entity e=level.m_8791_(id); if(e!=null)e.m_146870_(); }
        children.clear();childGrace.clear();
    }
    private LivingEntity target(UUID id) { Entity e=((ServerLevel)boss.m_9236_()).m_8791_(id); return e instanceof LivingEntity living&&!combat.skip(living)?living:null; }
    public void cast(int phase,LivingEntity target) {
        UUID id=target.m_20148_();
        switch(phase) {
            case 1 -> fire(target);
            case 2 -> iceRings.cast(target);
            case 3 -> lightning(target);
            case 4 -> rift(80,20);
            case 5 -> { for(int i=0;i<16;i++)later(i*2,()-> { LivingEntity t=target(id); if(t!=null)edge(AscianEffects.ground(boss,t)); }); }
            case 6 -> { mixedTarget=id;mixedTargetId=target.m_19879_();rift(200,60); fire(target); later(40,()->{LivingEntity t=target(id);if(t!=null)cast(2,t);});
                later(145,()->{LivingEntity t=target(id);if(t!=null){cast(3,t);cast(5,t);}}); }
        }
    }
    private void fire(LivingEntity target) {
        meteors.cast(target);
    }
    private void lightning(LivingEntity target) {
        Vec3 center=AscianEffects.ground(boss,target);
        boolean returnWave=AscianKingConfig.HARD_MODE.get();
        for(int i=0;i<3;i++){final int ring=i;later(i*20,()->lightningRing(center,ring,returnWave&&ring==2));}
    }
    private void lightningRing(Vec3 center,int ring) {
        lightningRing(center,ring,false);
    }
    private void lightningRing(Vec3 center,int ring,boolean returnAfterImpact) {
        int count=new int[]{1,10,20}[ring]; double radius=ring*12;
        for(int i=0;i<count;i++) {
            double a=i*Math.PI*2/count;
            Vec3 p=AscianEffects.ground(boss.m_9236_(),center.f_82479_+Math.cos(a)*radius,center.f_82480_+8,center.f_82481_+Math.sin(a)*radius);
            warn(p,60,4,AscianKingConfig.HARD_MODE.get());
            boolean last=returnAfterImpact&&i==count-1;
            later(61,()->{
                long run=epoch();
                StarFantasyGoetyNetwork.sendApollyonLightningStrike((ServerLevel)boss.m_9236_(),p,4);
                lightningArea(p,4,AscianDamage.spell(3));
                // Arm the return only after every first-wave outer strike has resolved.
                if(last&&epoch()==run&&boss.m_6084_())later(10,()->{
                    lightningRing(center,2);
                    later(20,()->lightningRing(center,1));
                    later(40,()->lightningRing(center,0));
                });
            });
        }
    }
    private void edge(Vec3 center) {
        warn(center,20,1.6,true);
        // Our visual-only fangs close eight ticks after spawning; this controller owns the hit.
        later(12,()->fang(center));
        later(20,()->{
            long run=epoch();
            // Original blade: 0.75 x 2.0 body expanded by 0.2, independent of the 1.6 warning radius.
            AABB box=new AABB(center.f_82479_-.575,center.f_82480_-.2,center.f_82481_-.575,
                    center.f_82479_+.575,center.f_82480_+2.2,center.f_82481_+.575);
            for(LivingEntity victim:boss.m_9236_().m_45976_(LivingEntity.class,box)){
                if(epoch()!=run||!boss.m_6084_())return;
                if(!combat.skip(victim))fangHit(victim,AscianDamage.spell(5));
            }
        });
    }
    private boolean fangHit(LivingEntity victim,AscianDamage.Profile damage){
        boolean hard=AscianKingConfig.HARD_MODE.get();long run=epoch();int frames=victim.f_19802_;boolean hit=false;
        if(hard)victim.f_19802_=0;
        try{hit=hurt(victim,damage,boss,false,true);}
        finally{if(hard&&!hit)victim.f_19802_=Math.max(frames,victim.f_19802_);}
        if(hit&&epoch()==run&&boss.m_6084_()){
            AscianEffects.hit(victim,5);
            if(hard&&epoch()==run&&boss.m_6084_()){
                var effect=ForgeRegistries.MOB_EFFECTS.getValue(new ResourceLocation("star_fantasy","broken_heart"));
                if(effect!=null)victim.m_7292_(new net.minecraft.world.effect.MobEffectInstance(effect,400,7));
            }
        }
        return hit;
    }
    private void rift(int duration,int bladeDelay) {
        AscianVoidRiftEntity entity=AscianVoidRiftEntity.spawn(boss,duration);own(entity);
        if(AscianKingConfig.HARD_MODE.get())for(int i=0;i<4;i++){
            final float roll=i%2==0?45:-45;
            later(bladeDelay+i*10,()->{
                LivingEntity target=boss.m_5448_();
                if(entity.m_6084_()&&target!=null&&!combat.skip(target)&&target.m_9236_()==boss.m_9236_()){
                    entity.followOwner();own(AscianPhantomBladeEntity.spawn(boss,entity.m_20182_(),target,roll));
                }
            });
        }
    }
    private Entity create(String id) { EntityType<?> type=ForgeRegistries.ENTITY_TYPES.getValue(new ResourceLocation(id)); return type==null?null:type.m_20615_(boss.m_9236_()); }
    public static void invoke(Object target,String name,Class<?> parameter,Object value) {
        try { target.getClass().getMethod(name,parameter).invoke(target,value); }
        catch(ReflectiveOperationException e) { throw new IllegalStateException("Goety spell API mismatch: "+name,e); }
    }
    public void area(Vec3 center,double radius,AscianDamage.Profile damage,int effectPhase,boolean fire,double knockback) {
        area(center,radius,damage,effectPhase,fire,knockback,false,false,effectPhase==5);
    }
    public void barrageArea(Vec3 center,double radius,AscianDamage.Profile damage,int effectPhase,boolean fire) {
        area(center,radius,damage,effectPhase,fire,0,AscianKingConfig.HARD_MODE.get(),true,false);
    }
    public void meteorArea(Vec3 center,double radius) {
        boolean hard=AscianKingConfig.HARD_MODE.get();
        area(center,radius,AscianDamage.spell(1),1,true,0,hard,!hard,hard);
    }
    public void lightningArea(Vec3 center,double radius,AscianDamage.Profile damage) {
        boolean hard=AscianKingConfig.HARD_MODE.get();
        area(center,radius,damage,3,false,0,hard,!hard,hard);
    }
    public void arenaLightningArea(Vec3 center,double radius,AscianDamage.Profile damage) {
        area(center,radius,damage,3,false,0,AscianKingConfig.HARD_MODE.get(),true,false);
    }
    public void transitionArea(Vec3 center,int phase,boolean rear){
        area(center,9,AscianDamage.transition(phase),0,false,10,false,!rear,rear);
    }
    private void area(Vec3 center,double radius,AscianDamage.Profile damage,int effectPhase,boolean fire,double knockback,boolean ignoreFrames,boolean front,boolean rear) {
        long run=revision,hazardRun=meteors.revision();
        for(LivingEntity victim:boss.m_9236_().m_45976_(LivingEntity.class,new AABB(center,center).m_82400_(radius))) {
            if(revision!=run||meteors.revision()!=hazardRun||!boss.m_6084_())return;
            if(combat.skip(victim)||!AscianEffects.sphere(victim.m_20191_(),center,radius))continue;
            if(effectPhase==5&&rear){fangHit(victim,damage);continue;}
            if(knockback>0)victim.m_147240_(knockback,center.f_82479_-victim.m_20185_(),center.f_82481_-victim.m_20189_());
            int oldFrames=victim.f_19802_;boolean hit=false;
            if(ignoreFrames)victim.f_19802_=0;
            try { hit=hurt(victim,damage,boss,front,rear); }
            finally {
                if(ignoreFrames&&!hit)victim.f_19802_=Math.max(oldFrames,victim.f_19802_);
            }
            if(revision!=run||meteors.revision()!=hazardRun)return;
            if(hit) {
                if(effectPhase>0)AscianEffects.hit(victim,effectPhase);
                if(fire&&victim.m_20094_()<=0)victim.m_20254_(20);
            }
        }
    }
    public boolean hurt(LivingEntity victim,AscianDamage.Profile damage,Entity direct) {
        return hurt(victim,damage,direct,false,false);
    }
    public boolean phantomBladeHit(LivingEntity victim,Entity direct){
        long run=epoch();int frames=victim.f_19802_;boolean hit=false;
        victim.f_19802_=0;
        try{hit=hurt(victim,AscianDamage.phantomBlade(),direct);}
        finally{if(!hit)victim.f_19802_=Math.max(frames,victim.f_19802_);}
        if(hit&&epoch()==run&&boss.m_6084_()&&!direct.m_213877_())AscianEffects.hit(victim,4);
        return hit;
    }
    public void iceRingArea(Vec3 center,double radius,double scale,boolean hard,java.util.function.BooleanSupplier valid){
        long run=epoch();
        AABB area=new AABB(center.f_82479_-radius,center.f_82480_,center.f_82481_-radius,
                center.f_82479_+radius,center.f_82480_+AscianIceRingPattern.HIT_HEIGHT,center.f_82481_+radius);
        for(LivingEntity victim:boss.m_9236_().m_45976_(LivingEntity.class,area)){
            if(epoch()!=run||!valid.getAsBoolean())return;
            if(combat.skip(victim))continue;
            AABB b=victim.m_20191_();
            double x=Math.max(b.f_82288_-center.f_82479_,Math.max(0,center.f_82479_-b.f_82291_));
            double z=Math.max(b.f_82290_-center.f_82481_,Math.max(0,center.f_82481_-b.f_82293_));
            if(x*x+z*z>radius*radius)continue;
            int oldFrames=victim.f_19802_;boolean hit=false;
            if(hard)victim.f_19802_=0;
            try{hit=victim.m_6469_((hard?ApollyonDamageSources.rear(victim,source(AscianDamage.Element.FROST,boss)):ApollyonDamageSources.front(victim,source(AscianDamage.Element.FROST,boss))),
                    (float)(AscianDamage.spell(2).amount(victim.m_21233_(),AscianKingConfig.DAMAGE_MULTIPLIER.get())*scale));}
            finally{if(hard&&!hit)victim.f_19802_=Math.max(oldFrames,victim.f_19802_);}
            if(epoch()!=run||!valid.getAsBoolean())return;
            if(hit){
                AscianEffects.hit(victim,2);
                if(epoch()!=run||!valid.getAsBoolean())return;
                victim.m_7292_(new net.minecraft.world.effect.MobEffectInstance(com.Polarice3.Goety.common.effects.GoetyEffects.STUNNED.get(),AscianIceRingPattern.STUN));
                if(epoch()!=run||!valid.getAsBoolean())return;
                if(combat.phase()==6)AscianEffects.hit(victim,6);
            }
        }
    }
    private boolean hurt(LivingEntity victim,AscianDamage.Profile damage,Entity direct,boolean front,boolean rear) {
        long run=revision,hazardRun=meteors.revision();
        DamageSource attack=source(damage.element(),direct);
        if(front)attack=ApollyonDamageSources.front(victim,attack);
        else if(rear)attack=ApollyonDamageSources.rear(victim,attack);
        boolean hit=victim.m_6469_(attack,
                damage.amount(victim.m_21233_(),AscianKingConfig.DAMAGE_MULTIPLIER.get()));
        // Phase six is an extra on every successful attack, independent of the spell's visual style.
        if(hit&&revision==run&&meteors.revision()==hazardRun&&combat.phase()==6)AscianEffects.hit(victim,6);
        return hit;
    }
    public DamageSource source(AscianDamage.Element element,Entity direct) {
        return switch(element) {
            case HELLFIRE -> ModDamageSource.hellfire(direct,boss);
            case FROST -> ModDamageSource.indirectFreeze(direct,boss);
            case LIGHTNING -> ModDamageSource.lightning(direct,boss);
            case MAGIC -> boss.m_269291_().m_269104_(direct,boss);
            case WITHER -> new DamageSource(boss.m_269291_().m_269251_().m_269150_(),direct,boss);
        };
    }
    public void warn(Vec3 p,int ticks,double radius,boolean purple) {
        StarFantasyVfx.groundWarningCircleOwnedStatic(boss,p.m_82520_(0,.06,0),ticks,radius,purple?WarningColor.PURPLE:WarningColor.RED);
    }
    public void barrage(LivingEntity target,int phase,boolean fan) {
        combat.warnBody(20,WarningColor.RED);
        UUID id=target.m_20148_(); int serial=boss.visualSerial();
        List<Integer> styles=new ArrayList<>(List.of(1,2,3,4,5));
        if(!fan)styles.addAll(List.of(1,2,3,4,5));
        for(int i=styles.size()-1;i>0;i--)Collections.swap(styles,i,boss.m_217043_().m_188503_(i+1));
        for(int i=0;i<(fan?5:10);i++) {
            int style=phase==6?styles.get(i):phase;
            int count=fan?new int[]{5,4,5,4,5}[i]:1;
            later(20+i*(fan?5:2),()-> {
                LivingEntity t=target(id);
                if(t==null||combat.transitioning()||combat.stunned()||boss.visualSerial()!=serial||combat.action()!=(fan?5:4))return;
                boss.m_5496_(net.minecraft.sounds.SoundEvents.f_11705_,1.2F,1F);
                for(int k=0;k<count;k++)shoot(t,style,phase,fan?(count==4?-60+k*40:-80+k*40):0,fan?.82:1.64);
            });
        }
    }
    private void shoot(LivingEntity target,int style,int phase,double yaw,double speed) {
        Vec3 origin=boss.m_146892_().m_82520_(0,-.1,0);
        Vec3 delta=target.m_20182_().m_82546_(origin), flat=delta.m_82542_(1,0,1);
        double length=Math.max(.0001,flat.m_82553_()), angle=Math.toRadians(yaw); Vec3 f=flat.m_82541_();
        Vec3 direction=new Vec3(f.f_82479_*Math.cos(angle)-f.f_82481_*Math.sin(angle),delta.f_82480_/length,f.f_82479_*Math.sin(angle)+f.f_82481_*Math.cos(angle)).m_82541_();
        own(AscianProjectileEntity.spawn(boss,origin,direction.m_82490_(speed),style,phase,null,0));
    }
    /** Snapshot positions/phase at the hit frame. These waves never create damage-bearing entities. */
    public void meleeVisuals(AscianMeleeShape shape,int phase,double targetY) {
        for(var wave:shape.waves()){
            List<Vec3> centers=new ArrayList<>();
            for(var point:wave.points())centers.add(AscianEffects.ground(boss.m_9236_(),point.x(),Math.max(targetY,boss.m_20186_())+8,point.z()));
            Runnable emit=()->StarFantasyGoetyNetwork.sendAscianMeleeEffects(boss,phase,wave.styleOffset(),centers);
            if(wave.delay()==0)emit.run();else later(wave.delay(),emit);
        }
    }
    /** Preserve the four outward rings; these are presentation only, never extra damage. */
    public void transitionRing(int next,int ring) {
        int count=6+ring*2; double radius=2+ring*2;
        int kind=next==6?com.starfantasy.goety.network.ClientboundAscianEffectPacket.TRANSITION_HANDS
                :switch(next){case 2->1;case 3->3;case 4->7;default->2;};
        List<Vec3> centers=new ArrayList<>();
        for(int i=0;i<count;i++){
            double a=i*Math.PI*2/count;
            centers.add(AscianEffects.ground(boss.m_9236_(),boss.m_20185_()+Math.cos(a)*radius,boss.m_20186_()+8,boss.m_20189_()+Math.sin(a)*radius));
        }
        StarFantasyGoetyNetwork.sendAscianEffects(boss,kind,centers);
    }
    public void ambience(int style) {
        double a=boss.m_217043_().m_188500_()*Math.PI*2,r=12+boss.m_217043_().m_188500_()*6;
        Vec3 p=AscianEffects.ground(boss.m_9236_(),boss.m_20185_()+Math.cos(a)*r,boss.m_20186_()+8,boss.m_20189_()+Math.sin(a)*r);
        int delay=AscianCombatTuning.METEOR_WARNING; warn(p,delay,2,style==5);
        if(style==5) {
            later(delay-8,()->fang(p));
        }
        if(style==1||style==2||style==4) {
            Vec3 from=p.m_82520_(0,30,0);
            own(AscianProjectileEntity.spawn(boss,from,new Vec3(0,-30D/delay,0),style,style,p,delay));
        } else later(delay,()-> {
            if(style==3)StarFantasyGoetyNetwork.sendApollyonLightningStrike((ServerLevel)boss.m_9236_(),p,2);
            if(style==3)arenaLightningArea(p,2,AscianDamage.ambience(style));
            else area(p,2,AscianDamage.ambience(style),style,false,0);
        });
    }
    private void fang(Vec3 position) {
        var fang=ApollyonFangEntity.spawn(boss,position,0);
        if(fang==null)return;
        own(fang);
        // Shared visual fangs are silent: each caster supplies its own sound.
        boss.m_9236_().m_5594_(null,net.minecraft.core.BlockPos.m_274446_(position),
                net.minecraft.sounds.SoundEvents.f_11865_,net.minecraft.sounds.SoundSource.HOSTILE,1F,1F);
    }
    public void summon() {
        int phase=combat.phase();
        int buffLevel=AscianKingConfig.SERVANT_BUFF_LEVEL.get();
        String id=new String[]{"blaze_servant","ice_golem","blackguard_servant","watchling_servant","reaper_servant","inferno"}[phase-1];
        int count=new int[]{2,1,3,3,3,3}[phase-1];
        for(int i=0;i<count;i++) {
            Entity servant=create("goety:"+id);if(servant==null)continue;
            double a=i*Math.PI*2/count;
            Vec3 pos=AscianEffects.ground(boss.m_9236_(),boss.m_20185_()+(count==1?0:Math.cos(a)*1.5),boss.m_20186_()+4,boss.m_20189_()+(count==1?0:Math.sin(a)*1.5));
            servant.m_6034_(pos.f_82479_,pos.f_82480_,pos.f_82481_);
            servant.getPersistentData().m_128379_(NO_LOOT,true);
            invoke(servant,"setTrueOwner",LivingEntity.class,boss); invoke(servant,"setLimitedLife",int.class,600);
            if(buffLevel>0 && servant instanceof LivingEntity living)
                living.m_7292_(new net.minecraft.world.effect.MobEffectInstance(
                        com.Polarice3.Goety.common.effects.GoetyEffects.BUFF.get(),-1,buffLevel-1));
            try {
                Entity circle=(Entity)Class.forName("com.Polarice3.Goety.common.entities.util.SummonCircle")
                        .getConstructor(Level.class,Vec3.class,Entity.class,boolean.class,boolean.class,LivingEntity.class)
                        .newInstance(boss.m_9236_(),pos,servant,true,true,boss);
                boss.m_9236_().m_7967_(circle); own(circle); own(servant);
            } catch(ReflectiveOperationException e) { throw new IllegalStateException("Goety summon API mismatch",e); }
        }
    }
}
