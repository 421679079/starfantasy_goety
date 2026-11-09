package com.starfantasy.goety.combat.ascian;

import com.starfantasy.goety.combat.ApollyonPageantController;
import com.starfantasy.goety.config.AscianKingConfig;
import com.starfantasy.goety.entity.AscianKingEntity;
import com.starfantasy.goety.network.ClientboundAscianMeteorPacket;
import com.starfantasy.goety.network.StarFantasyGoetyNetwork;
import com.starfantasy.goety.registry.ApollyonSoundRegistry;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.EntityLeaveLevelEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import static com.starfantasy.goety.combat.ascian.AscianMeteorPattern.*;

/** Server-only gameplay records, not world entities. Damage callbacks may invalidate a batch. */
@Mod.EventBusSubscriber(modid="starfantasy_goety")
public final class AscianMeteorSpell {
    private static final class Meteor {final Path path;int next;Meteor(Path p){path=p;}}
    private static final class PendingTrap {final Trap trap;boolean fired;PendingTrap(Trap t){trap=t;}}
    private final AscianKingEntity boss;
    private final AscianSpells spells;
    private final List<Meteor> meteors=new ArrayList<>();
    private final List<PendingTrap> traps=new ArrayList<>();
    private long nextId,revision;
    public AscianMeteorSpell(AscianKingEntity boss,AscianSpells spells){this.boss=boss;this.spells=spells;}
    public void cast(LivingEntity target){
        if(!(boss.m_9236_() instanceof ServerLevel level))return;
        double y=Math.max(boss.m_20186_(),target.m_20186_())+8;
        List<Path> paths=create(boss.m_20182_(),target.m_20182_(),boss.m_20154_(),level.m_46467_(),()->++nextId,
                (x,z)->AscianEffects.ground(level,x,y,z));
        for(Path path:paths){meteors.add(new Meteor(path));spells.warn(path.points().get(0),WARNING_TICKS,RADIUS,AscianKingConfig.HARD_MODE.get());}
        Vec3 middle=paths.get(2).position(0);
        level.m_6263_(null,middle.f_82479_,middle.f_82480_,middle.f_82481_,ApollyonSoundRegistry.CHAOS_METEOR.get(),SoundSource.HOSTILE,2F,1F);
        publish();
    }
    private boolean valid(long run){return revision==run&&boss.m_6084_();}
    public void tick(){
        if(!(boss.m_9236_() instanceof ServerLevel level))return;
        long run=revision,now=level.m_46467_();boolean changed=false;
        for(Meteor meteor:List.copyOf(meteors)){
            if(!valid(run))return;
            long age=now-meteor.path.startTick();
            if(age>=METEOR_TICKS){meteors.remove(meteor);changed=true;continue;}
            while(meteor.next<EXPLOSIONS.size()&&age>=EXPLOSIONS.get(meteor.next)){
                int index=meteor.next++;Vec3 center=meteor.path.points().get(index);
                ApollyonPageantController.playPyroclastExplosion(level,center,METEOR_VISUAL_SIZE,3F);
                spells.meteorArea(center,RADIUS);
                if(!valid(run))return;
                if(AscianKingConfig.HARD_MODE.get()){
                    traps.add(new PendingTrap(new Trap(++nextId,now,center)));
                    spells.warn(center,WARNING_TICKS,TRAP_RADIUS,true);
                    changed=true;
                }
                if(meteor.next<EXPLOSIONS.size())spells.warn(meteor.path.points().get(meteor.next),WARNING_TICKS,RADIUS,AscianKingConfig.HARD_MODE.get());
            }
        }
        for(PendingTrap pending:List.copyOf(traps)){
            if(!valid(run))return;
            long age=now-pending.trap.startTick();
            if(!pending.fired&&age>=TRAP_DAMAGE_TICKS){
                pending.fired=true;
                spells.meteorArea(pending.trap.center(),TRAP_RADIUS);
                if(!valid(run))return;
            }
            if(age>=TRAP_TICKS){traps.remove(pending);changed=true;}
        }
        if(changed)publish();
    }
    public long revision(){return revision;}
    public ClientboundAscianMeteorPacket snapshot(){return new ClientboundAscianMeteorPacket(boss.m_9236_().m_46472_().m_135782_(),boss.m_20148_(),boss.m_19879_(),revision,meteors.stream().map(m->m.path).toList(),traps.stream().map(t->t.trap).toList());}
    private void publish(){if(boss.m_9236_() instanceof ServerLevel)StarFantasyGoetyNetwork.sendAscianMeteorState(boss,snapshot());}
    public void clear(){++revision;meteors.clear();traps.clear();publish();StarFantasyGoetyNetwork.clearFireTraps(boss);}
    @SubscribeEvent public static void tracking(PlayerEvent.StartTracking event){
        if(event.getTarget() instanceof AscianKingEntity boss&&event.getEntity() instanceof ServerPlayer player)
        {
            StarFantasyGoetyNetwork.sendAscianMeteorState(player,boss.combat.spells.meteors.snapshot());
            StarFantasyGoetyNetwork.sendAscianIceRings(player,boss.combat.spells.iceRings.snapshot());
        }
    }
    @SubscribeEvent public static void leave(EntityLeaveLevelEvent event){
        if(!event.getLevel().f_46443_&&event.getEntity() instanceof AscianKingEntity boss){
            boss.combat.spells.stopCasting();
            boss.combat.spells.meteors.clear();
            boss.combat.spells.iceRings.clear();
            com.starfantasy.library.vfx.StarFantasyVfx.clearWarningsForOwner(boss);
        }
    }
}
