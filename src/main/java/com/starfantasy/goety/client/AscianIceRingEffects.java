package com.starfantasy.goety.client;

import com.mojang.blaze3d.vertex.*;
import com.starfantasy.goety.combat.ascian.AscianIceRingPattern.Ring;
import com.starfantasy.goety.network.ClientboundAscianIceRingPacket;
import com.starfantasy.library.vfx.client.StarFantasyTranslucentWorldRenderer;
import com.starfantasy.library.vfx.particle.*;
import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.particles.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Quaternionf;
import static com.starfantasy.goety.combat.ascian.AscianIceRingPattern.*;

/** Twenty-six analytic meshes at most per cast; no synced projectile entities or collisions. */
@Mod.EventBusSubscriber(modid="starfantasy_goety",value=Dist.CLIENT)
public final class AscianIceRingEffects {
    private static final ResourceLocation ICE=new ResourceLocation("minecraft","textures/block/ice.png");
    private static final class Visual {
        Ring ring;Vec3 locked;Particle warning;boolean warningMade,broken;
        Visual(Ring r){ring=r;}
        void clear(){if(warning!=null)warning.m_107274_();}
    }
    private static final class State {
        final UUID owner;final int ownerId;final long revision,received;
        final Map<Long,Visual> rings=new LinkedHashMap<>();final List<Particle> particles=new ArrayList<>();
        State(ClientboundAscianIceRingPacket p,long now){owner=p.owner();ownerId=p.ownerId();revision=p.revision();received=now;}
        void clear(){rings.values().forEach(Visual::clear);particles.forEach(Particle::m_107274_);rings.clear();particles.clear();}
    }
    private static ClientLevel current;
    private static final Map<UUID,State> ACTIVE=new HashMap<>();
    private static void level(ClientLevel next){if(current!=next){ACTIVE.values().forEach(State::clear);ACTIVE.clear();current=next;}}
    public static void receive(ClientboundAscianIceRingPacket packet){
        ClientLevel level=Minecraft.m_91087_().f_91073_;level(level);
        if(level==null||!level.m_46472_().m_135782_().equals(packet.dimension()))return;
        long now=level.m_46467_();State s=ACTIVE.get(packet.owner());
        if(s!=null&&s.revision>packet.revision())return;
        if(s==null||s.revision!=packet.revision()||s.ownerId!=packet.ownerId()){
            if(s!=null)s.clear();s=new State(packet,now);ACTIVE.put(packet.owner(),s);
        }
        Set<Long> keep=new HashSet<>();
        for(Ring r:packet.rings()){
            keep.add(r.id());Visual v=s.rings.computeIfAbsent(r.id(),id->new Visual(r));v.ring=r;if(r.frozen())v.locked=r.center();
        }
        // Explicit terminal records avoid losing the burst when the server is a tick ahead.
        for(Ring r:packet.shattered()){
            Visual v=s.rings.get(r.id());if(v==null)v=new Visual(r);v.ring=r;v.locked=r.center();if(!v.broken)shatter(s,v);
        }
        for(var it=s.rings.entrySet().iterator();it.hasNext();){var entry=it.next();if(keep.contains(entry.getKey()))continue;
            Visual v=entry.getValue();
            v.clear();it.remove();
        }
        if(packet.rings().isEmpty()&&packet.shattered().isEmpty()){s.particles.forEach(Particle::m_107274_);s.particles.clear();}
    }
    private static LivingEntity target(Visual v){var e=current.m_6815_(v.ring.targetId());return e instanceof LivingEntity t&&t.m_20148_().equals(v.ring.target())&&t.m_6084_()?t:null;}
    private static Vec3 center(Visual v,float partial){
        if(v.ring.frozen())return v.ring.center();
        if(v.locked!=null)return v.locked;
        LivingEntity target=target(v);if(target==null)return v.ring.center();
        Vec3 p=target.m_20318_(partial);return new Vec3(p.f_82479_,v.ring.center().f_82480_,p.f_82481_);
    }
    private static Particle particle(State s,ParticleOptions options,Vec3 p,double dx,double dy,double dz,int life){
        var mc=Minecraft.m_91087_();if(mc.m_91288_()==null||mc.m_91288_().m_20238_(p)>9216)return null;
        Particle result=mc.f_91061_.m_107370_(options,p.f_82479_,p.f_82480_,p.f_82481_,dx,dy,dz);
        if(result!=null){result.m_107257_(life);s.particles.add(result);}return result;
    }
    private static void shatter(State s,Visual v){
        v.broken=true;Ring r=v.ring;Vec3 anchor=center(v,1);Random random=new Random(r.id());
        var debris=new BlockParticleOption(ParticleTypes.f_123794_,Blocks.f_50354_.m_49966_());
        for(int slot=0;slot<r.count();slot++){
            Vec3 p=r.position(slot,LAUNCH+FLIGHT,anchor);
            for(int i=0;i<12;i++)particle(s,debris,p.m_82520_(random.nextDouble()*.5-.25,random.nextDouble()*.3,random.nextDouble()*.5-.25),
                    random.nextGaussian()*.1,random.nextDouble()*.12,random.nextGaussian()*.1,14);
        }
    }
    @SubscribeEvent public static void tick(TickEvent.ClientTickEvent event){
        if(event.phase!=TickEvent.Phase.END)return;var level=Minecraft.m_91087_().f_91073_;level(level);if(level==null)return;
        long now=level.m_46467_();
        for(var it=ACTIVE.values().iterator();it.hasNext();){State s=it.next();var owner=level.m_6815_(s.ownerId);
            if(owner!=null&&(!owner.m_20148_().equals(s.owner)||!owner.m_6084_())||owner==null&&now-s.received>20){s.clear();it.remove();continue;}
            s.particles.removeIf(p->!p.m_107276_());
            for(Visual v:s.rings.values()){
                Ring r=v.ring;long age=now-r.startTick();LivingEntity target=target(v);
                if(target==null){v.clear();continue;}
                if(age>=LAUNCH&&v.locked==null)v.locked=center(v,1);
                Vec3 anchor=center(v,1);
                if(age>=ORBIT_END&&age<r.damageAge()){
                    if(!v.warningMade){v.warningMade=true;
                        v.warning=Minecraft.m_91087_().f_91061_.m_107370_(new GroundWarningCircleOptions((int)(r.damageAge()-age),(float)r.radius(),r.hard()?WarningColor.PURPLE:WarningColor.RED,s.ownerId,GroundWarningTrackingMode.NONE,0,0),
                                anchor.f_82479_,anchor.f_82480_+.06,anchor.f_82481_,0,0,0);
                    }
                    if(v.warning!=null)v.warning.m_107264_(anchor.f_82479_,anchor.f_82480_+.06,anchor.f_82481_);
                }else if(v.warning!=null)v.warning.m_107274_();
                if(age>=0&&age<LAUNCH+FLIGHT&&age%4==0){
                    Random random=new Random(r.id()*1000003+age);
                    for(int slot=0;slot<r.count();slot++)for(int i=0;i<2;i++){
                        Vec3 p=r.position(slot,age,anchor).m_82520_(random.nextDouble()*.55-.275,random.nextDouble()*.5-.25,random.nextDouble()*.55-.275);
                        particle(s,ParticleTypes.f_175821_,p,0,0,0,14);
                    }
                }
                if(age>=LAUNCH+FLIGHT&&!v.broken)shatter(s,v);
            }
            if(s.rings.isEmpty()&&s.particles.isEmpty()&&now-s.received>40)it.remove();
        }
    }
    @SubscribeEvent public static void render(RenderLevelStageEvent event){
        if(event.getStage()!=RenderLevelStageEvent.Stage.AFTER_PARTICLES||!StarFantasyTranslucentWorldRenderer.shouldDefer())return;
        if(current==null||current!=Minecraft.m_91087_().f_91073_)return;
        Vec3 camera=event.getCamera().m_90583_();float partial=event.getPartialTick();double now=current.m_46467_()+partial;
        for(State s:ACTIVE.values())for(Visual v:s.rings.values()){
            Ring r=v.ring;double age=now-r.startTick();if(age<0||age>=LAUNCH+FLIGHT||target(v)==null)continue;
            Vec3 anchor=center(v,partial);
            for(int slot=0;slot<r.count();slot++){
                Vec3 p=r.position(slot,age,anchor);if(p.m_82557_(camera)>65536)continue;
                double a=slot*Math.PI*2/r.count()+r.angle(age);float yaw=(float)Math.atan2(-Math.cos(a),-Math.sin(a));
                float scale=(float)Math.min(1,age/8);PoseStack pose=event.getPoseStack();pose.m_85836_();pose.m_85837_(p.f_82479_-camera.f_82479_,p.f_82480_-camera.f_82480_,p.f_82481_-camera.f_82481_);
                StarFantasyTranslucentWorldRenderer.defer(pose,(stack,buffers)->cone(stack,buffers,yaw,scale));pose.m_85849_();
            }
        }
    }
    private static final float[][] BELT={{.33F,0,-.25F},{0,.27F,-.25F},{-.33F,0,-.25F},{0,-.27F,-.25F}};
    private static final float[] TIP={0,0,2.1F},BACK={0,0,-.6F},SHADE={.98F,.78F,.66F,.88F};
    private static void cone(PoseStack pose,MultiBufferSource buffers,float yaw,float scale){
        pose.m_85836_();pose.m_252781_(new Quaternionf().rotationY(yaw));pose.m_85841_(scale,scale,scale);
        VertexConsumer out=buffers.m_6299_(AscianMeteorMaterials.rock(ICE));
        for(int i=0;i<4;i++){triangle(pose,out,TIP,BELT[i],BELT[(i+1)%4],SHADE[i]);triangle(pose,out,BACK,BELT[(i+1)%4],BELT[i],SHADE[i]);}
        pose.m_85849_();
    }
    private static void triangle(PoseStack p,VertexConsumer out,float[] a,float[] b,float[] c,float shade){vertex(p,out,a,.5F,0,shade);vertex(p,out,b,0,1,shade);vertex(p,out,c,1,1,shade);vertex(p,out,c,1,1,shade);}
    private static void vertex(PoseStack p,VertexConsumer out,float[] v,float u,float w,float shade){out.m_252986_(p.m_85850_().m_252922_(),v[0],v[1],v[2]).m_85950_(shade,shade,shade,1).m_7421_(u,w).m_5752_();}
    private AscianIceRingEffects(){}
}
