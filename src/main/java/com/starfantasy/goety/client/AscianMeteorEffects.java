package com.starfantasy.goety.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.starfantasy.goety.combat.ascian.AscianMeteorPattern.*;
import com.starfantasy.goety.network.ClientboundAscianMeteorPacket;
import com.starfantasy.goety.network.ClientboundFireTrapWavePacket;
import com.starfantasy.library.vfx.client.StarFantasyTranslucentWorldRenderer;
import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Quaternionf;
import static com.starfantasy.goety.combat.ascian.AscianMeteorPattern.*;

/** Analytic meteor meshes and the existing visual-only fire trap effect. */
@Mod.EventBusSubscriber(modid="starfantasy_goety",value=Dist.CLIENT)
public final class AscianMeteorEffects {
    private static final ResourceLocation ROCK=new ResourceLocation("goety","textures/entity/projectiles/pyroclast.png"),
            FLAME=new ResourceLocation("minecraft","textures/particle/flame.png");
    private static final class State {
        final UUID owner;final int ownerId;final long revision,received;
        final Map<Long,Path> paths=new LinkedHashMap<>();final Map<Long,Trap> traps=new LinkedHashMap<>();
        State(ClientboundAscianMeteorPacket p,long now){owner=p.owner();ownerId=p.ownerId();revision=p.revision();received=now;}
    }
    private static final Map<UUID,State> ACTIVE=new HashMap<>();
    private static ClientLevel current;
    private static void level(ClientLevel next){if(current!=next){ACTIVE.clear();current=next;}}
    public static void receive(ClientboundAscianMeteorPacket packet){
        ClientLevel l=Minecraft.m_91087_().f_91073_;level(l);
        if(l==null||!l.m_46472_().m_135782_().equals(packet.dimension()))return;
        State state=ACTIVE.get(packet.owner());
        if(state!=null&&state.revision>packet.revision())return;
        if(state==null||state.revision!=packet.revision()||state.ownerId!=packet.ownerId()){
            FireTrapEffect.receive(new ClientboundFireTrapWavePacket(packet.dimension(),packet.owner(),packet.ownerId(),l.m_46467_(),0,List.of()));
            state=new State(packet,l.m_46467_());
        }
        for(Path path:packet.meteors())state.paths.put(path.id(),path);
        for(Trap trap:packet.traps())if(state.traps.putIfAbsent(trap.id(),trap)==null)
            FireTrapEffect.receiveScaled(new ClientboundFireTrapWavePacket(packet.dimension(),packet.owner(),packet.ownerId(),trap.startTick(),trap.id(),List.of(trap.center())),TRAP_VISUAL_SCALE);
        // A normal 85-tick mesh expiry retains its detached 18-tick trail; a new revision clears it.
        if(state.paths.isEmpty()&&state.traps.isEmpty())ACTIVE.remove(packet.owner());else ACTIVE.put(packet.owner(),state);
    }
    @SubscribeEvent public static void tick(TickEvent.ClientTickEvent event){
        if(event.phase!=TickEvent.Phase.END)return;ClientLevel l=Minecraft.m_91087_().f_91073_;level(l);if(l==null)return;
        long now=l.m_46467_();
        ACTIVE.values().removeIf(state->{
            var owner=l.m_6815_(state.ownerId);
            if(owner!=null&&(!owner.m_20148_().equals(state.owner)||!owner.m_6084_()))return true;
            if(owner==null&&now-state.received>20)return true;
            state.paths.values().removeIf(path->now-path.startTick()>=METEOR_TICKS+18);
            state.traps.values().removeIf(trap->now-trap.startTick()>=TRAP_TICKS);
            return state.paths.isEmpty()&&state.traps.isEmpty();
        });
    }
    @SubscribeEvent public static void render(RenderLevelStageEvent event){
        if(event.getStage()!=RenderLevelStageEvent.Stage.AFTER_PARTICLES||!StarFantasyTranslucentWorldRenderer.shouldDefer())return;
        if(current==null||current!=Minecraft.m_91087_().f_91073_)return;
        Vec3 camera=event.getCamera().m_90583_();double now=current.m_46467_()+event.getPartialTick();
        Quaternionf billboard=new Quaternionf(event.getCamera().m_253121_());
        for(State state:ACTIVE.values()){
            for(Path path:state.paths.values()){
                float age=(float)(now-path.startTick());if(age<0||age>=METEOR_TICKS+18)continue;
                Vec3 center=path.position(age);if(center.m_82557_(camera)>65536)continue;
                PoseStack pose=event.getPoseStack();pose.m_85836_();pose.m_85837_(center.f_82479_-camera.f_82479_,center.f_82480_-camera.f_82480_,center.f_82481_-camera.f_82481_);
                StarFantasyTranslucentWorldRenderer.defer(pose,0,1.3F,0,(p,b)->meteor(p,b,path,age,center,billboard));pose.m_85849_();
            }
        }
    }
    private static void meteor(PoseStack p,MultiBufferSource buffers,Path path,float age,Vec3 center,Quaternionf billboard){
        if(age<METEOR_TICKS){
            p.m_85836_();p.m_85837_(0,1.3,0);p.m_252781_(new Quaternionf().rotationY(path.yaw()).rotateX(age*.65F).rotateZ(.18F));
            VertexConsumer rock=buffers.m_6299_(AscianMeteorMaterials.rock(ROCK));cube(p,rock,2.55F);
            p.m_252781_(new Quaternionf().rotationXYZ(.52F,.78F,.38F));cube(p,rock,2.2F);p.m_85849_();
        }
        VertexConsumer fire=buffers.m_6299_(AscianMeteorMaterials.flame(FLAME));
        int oldest=Math.max(0,(int)Math.floor(age)-18),newest=Math.min(84,(int)Math.floor(age));
        for(int birth=oldest;birth<=newest;birth++)for(int i=0;i<6;i++){
            long seed=path.id()*100003+birth*97L+i*17;double life=14+4*random(seed+1),elapsed=age-birth;if(elapsed>=life)continue;
            Vec3 origin=path.position(birth);double a=random(seed+2)*Math.PI*2,r=.25+.95*random(seed+3);
            double dx=Math.sin(path.yaw()),dz=Math.cos(path.yaw());
            p.m_85836_();p.m_85837_(origin.f_82479_+Math.cos(a)*r-dx*.4-center.f_82479_,origin.f_82480_+.55+1.7*random(seed+4)-center.f_82480_,origin.f_82481_+Math.sin(a)*r-dz*.4-center.f_82481_);
            p.m_252781_(billboard);sheet(p,fire,1.7F,1.7F,(float)(.95*Math.pow(1-elapsed/life,1.15)));p.m_85849_();
        }
    }
    private static double random(long seed){double n=Math.sin(seed*12.9898+78.233)*43758.5453;return n-Math.floor(n);}
    private static void cube(PoseStack p,VertexConsumer out,float size){
        float h=size/2;
        float[][][] faces={{{h,-h,h},{h,-h,-h},{h,h,-h},{h,h,h}},{{-h,-h,-h},{-h,-h,h},{-h,h,h},{-h,h,-h}},
                {{-h,h,h},{h,h,h},{h,h,-h},{-h,h,-h}},{{-h,-h,-h},{h,-h,-h},{h,-h,h},{-h,-h,h}},
                {{-h,-h,h},{h,-h,h},{h,h,h},{-h,h,h}},{{h,-h,-h},{-h,-h,-h},{-h,h,-h},{h,h,-h}}};
        int[][] uv={{16,24,24,32},{0,24,8,32},{8,16,16,24},{16,16,24,24},{24,24,32,32},{8,24,16,32}};
        float[] shades={.85F,.68F,1,.58F,.9F,.72F};
        for(int i=0;i<6;i++){float[][] q=faces[i];int[] t=uv[i];vertex(p,out,q[0][0],q[0][1],q[0][2],t[0]/32F,t[3]/32F,shades[i],1);vertex(p,out,q[1][0],q[1][1],q[1][2],t[2]/32F,t[3]/32F,shades[i],1);vertex(p,out,q[2][0],q[2][1],q[2][2],t[2]/32F,t[1]/32F,shades[i],1);vertex(p,out,q[3][0],q[3][1],q[3][2],t[0]/32F,t[1]/32F,shades[i],1);}
    }
    private static void sheet(PoseStack p,VertexConsumer out,float width,float height,float alpha){
        flameVertex(p,out,-width/2,-height/2,0,0,1,1,alpha);flameVertex(p,out,width/2,-height/2,0,1,1,1,alpha);flameVertex(p,out,width/2,height/2,0,1,0,1,alpha);flameVertex(p,out,-width/2,height/2,0,0,0,1,alpha);
    }
    private static void flameVertex(PoseStack p,VertexConsumer out,float x,float y,float z,float u,float v,float shade,float alpha){
        out.m_252986_(p.m_85850_().m_252922_(),x,y,z).m_85950_(shade,shade,shade,alpha).m_7421_(u,v)
                .m_86008_(net.minecraft.client.renderer.texture.OverlayTexture.f_118083_)
                .m_85969_(15728880).m_252939_(p.m_85850_().m_252943_(),0,0,1).m_5752_();
    }
    private static void vertex(PoseStack p,VertexConsumer out,float x,float y,float z,float u,float v,float shade,float alpha){out.m_252986_(p.m_85850_().m_252922_(),x,y,z).m_85950_(shade,shade,shade,alpha).m_7421_(u,v).m_5752_();}
    private AscianMeteorEffects() {}
}
