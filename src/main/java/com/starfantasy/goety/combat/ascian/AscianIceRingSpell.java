package com.starfantasy.goety.combat.ascian;

import com.starfantasy.goety.config.AscianKingConfig;
import com.starfantasy.goety.entity.AscianKingEntity;
import com.starfantasy.goety.network.*;
import com.starfantasy.goety.registry.ApollyonSoundRegistry;
import java.util.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.*;
import static com.starfantasy.goety.combat.ascian.AscianIceRingPattern.*;

/** No projectile entities: the server owns only frozen centers and one hit per circle. */
public final class AscianIceRingSpell {
    private static final class Cast {
        final UUID target;final boolean hard;final List<Pending> rings=new ArrayList<>();
        Cast(UUID target,boolean hard){this.target=target;this.hard=hard;}
    }
    private static final class Pending {Ring ring;boolean hit,ended,started;Pending(Ring r){ring=r;started=r.index()==0;}}
    private final AscianKingEntity boss;
    private final AscianSpells spells;
    private final List<Cast> casts=new ArrayList<>();
    private long nextId,revision;
    public AscianIceRingSpell(AscianKingEntity boss,AscianSpells spells){this.boss=boss;this.spells=spells;}
    private LivingEntity target(Cast cast){
        LivingEntity t=boss.m_5448_();
        return boss.m_6084_()&&t!=null&&t.m_9236_()==boss.m_9236_()&&t.m_20148_().equals(cast.target)&&!boss.combat.skip(t)?t:null;
    }
    public static Vec3 ground(LivingEntity target){
        Vec3 p=target.m_20182_();
        var hit=target.m_9236_().m_45547_(new ClipContext(p.m_82520_(0,.1,0),new Vec3(p.f_82479_,target.m_9236_().m_141937_(),p.f_82481_),ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,target));
        return hit.m_6662_()==HitResult.Type.MISS?p:hit.m_82450_();
    }
    public void cast(LivingEntity target){
        if(!(boss.m_9236_() instanceof ServerLevel level)||target!=boss.m_5448_()||boss.combat.skip(target)||casts.size()>=8)return;
        boolean hard=AscianKingConfig.HARD_MODE.get();Cast cast=new Cast(target.m_20148_(),hard);Vec3 center=ground(target);long now=level.m_46467_();
        for(int index=0;index<(hard?2:1);index++)cast.rings.add(new Pending(new Ring(++nextId,cast.target,target.m_19879_(),now+index*20,index,hard,center,false)));
        casts.add(cast);sound(target,ApollyonSoundRegistry.ICE_BOLT_START.get());publish();
    }
    private boolean valid(Cast cast,long run){return revision==run&&casts.contains(cast)&&target(cast)!=null;}
    public void tick(){
        if(!(boss.m_9236_() instanceof ServerLevel level))return;
        long now=level.m_46467_(),run=revision;boolean changed=false;List<Ring> shattered=new ArrayList<>();
        for(Cast cast:List.copyOf(casts)){
            if(revision!=run)return;
            LivingEntity target=target(cast);
            if(target==null){casts.remove(cast);changed=true;continue;}
            for(Pending pending:cast.rings){
                Ring r=pending.ring;long age=now-r.startTick();
                if(!pending.started&&age>=0){pending.started=true;sound(target,ApollyonSoundRegistry.ICE_BOLT_START.get());if(revision!=run)return;}
                if(!r.frozen()){
                    pending.ring=r=r.at(ground(target),age>=LAUNCH);
                    if(age==LAUNCH)changed=true;
                }
                if(!pending.hit&&age>=r.damageAge()){
                    pending.hit=true;
                    spells.iceRingArea(r.center(),r.radius(),r.damageScale(),cast.hard,()->valid(cast,run));
                    if(revision!=run)return;
                    if(!valid(cast,run)){casts.remove(cast);changed=true;break;}
                }
                if(age>=LAUNCH+FLIGHT&&!pending.ended){pending.ended=true;shattered.add(r);changed=true;sound(target,ApollyonSoundRegistry.ICE_BOLT_HIT.get());if(revision!=run)return;}
            }
            long end=cast.rings.get(cast.rings.size()-1).ring.startTick()+LAUNCH+FLIGHT;
            if(casts.contains(cast)&&now>=end){casts.remove(cast);changed=true;}
        }
        // Sparse ground anchors, not 26 entity updates; clients track rendered target X/Z locally.
        if(revision!=run)return;
        if(changed||!casts.isEmpty()&&now%5==0){var p=snapshot();StarFantasyGoetyNetwork.sendAscianIceRings(boss,new ClientboundAscianIceRingPacket(p.dimension(),p.owner(),p.ownerId(),p.revision(),p.rings(),shattered));}
    }
    private void sound(LivingEntity target,SoundEvent event){
        ((ServerLevel)boss.m_9236_()).m_6263_(null,target.m_20185_(),target.m_20186_(),target.m_20189_(),event,SoundSource.HOSTILE,1F,1F);
    }
    public ClientboundAscianIceRingPacket snapshot(){return new ClientboundAscianIceRingPacket(boss.m_9236_().m_46472_().m_135782_(),boss.m_20148_(),boss.m_19879_(),revision,casts.stream().flatMap(c->c.rings.stream()).filter(p->!p.ended).map(p->p.ring).toList());}
    private void publish(){StarFantasyGoetyNetwork.sendAscianIceRings(boss,snapshot());}
    public void clear(){++revision;casts.clear();publish();}
}
