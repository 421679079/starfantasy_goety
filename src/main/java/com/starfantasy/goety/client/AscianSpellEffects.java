package com.starfantasy.goety.client;

import com.google.gson.Gson;
import com.mojang.blaze3d.vertex.*;
import com.starfantasy.goety.entity.AscianKingEntity;
import com.starfantasy.goety.network.ClientboundAscianEffectPacket;
import com.starfantasy.goety.registry.ApollyonSoundRegistry;
import com.starfantasy.library.vfx.client.StarFantasyTranslucentWorldRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.particles.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.*;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Quaternionf;
import java.io.*;
import java.util.*;
import java.lang.Math;

/** Approved preview geometry. All lifetime, sound and particle work is local to one visual wave. */
@Mod.EventBusSubscriber(modid="starfantasy_goety",value=Dist.CLIENT)
public final class AscianSpellEffects {
    private static final ResourceLocation FIRE=tex("fire_pillar"),DARK=tex("dark_pillar"),
            ICE=new ResourceLocation("minecraft","textures/block/ice.png"),BONES=new ResourceLocation("minecraft","textures/entity/skeleton/wither_skeleton.png");
    private static final List<Wave> ACTIVE=new ArrayList<>();
    private static final Map<UUID,StunRings> STUN_RINGS=new HashMap<>();
    private record StunParticle(Particle particle,int expires){}
    private static final class StunRings {
        final AscianKingEntity owner;
        final List<StunParticle> particles=new ArrayList<>();
        int age,ring;
        StunRings(AscianKingEntity owner){this.owner=owner;}
        void clear(){for(var p:particles)p.particle().m_107274_();particles.clear();}
    }
    private static ClientLevel current;
    private static float[][] clawModel;
    private static float clawHeight;
    private static final float[][] PILLARS={{.42F,5.7F,0,3.25F,5.8F,8.35F},{1.55F,2.55F,0,2.5F,5.8F,8.35F},{1.26F,2.9F,1.35F,4.15F,5.8F,7.5F},{.96F,2,3.25F,5,5.8F,7.5F}};
    private record Hand(int side,float[][] vertices){}
    private static final class Wave {
        final ClientboundAscianEffectPacket packet;final float[] handYaw;int age;
        Wave(ClientboundAscianEffectPacket p,int age){
            packet=p;this.age=age;
            handYaw=p.kind()==ClientboundAscianEffectPacket.TRANSITION_HANDS||(p.kind()==8&&p.meleePhase()>=5)?new float[p.centers().size()]:null;
            if(handYaw!=null)for(int i=0;i<handYaw.length;i++)
                handYaw[i]=new Random(p.tick()^p.owner().getLeastSignificantBits()^p.centers().get(i).hashCode()).nextFloat()*(float)(Math.PI*2);
        }
    }
    private static ResourceLocation tex(String s){return new ResourceLocation("starfantasy_goety","textures/effect/ascian/"+s+".png");}
    private static void level(ClientLevel l){if(l!=current){ACTIVE.clear();for(var s:STUN_RINGS.values())s.clear();STUN_RINGS.clear();current=l;}}
    public static void receive(ClientboundAscianEffectPacket p){
        var l=Minecraft.m_91087_().f_91073_;level(l);
        if(l==null||!l.m_46472_().m_135782_().equals(p.dimension()))return;
        if(p.kind()==0){ACTIVE.removeIf(w->w.packet.owner().equals(p.owner()));var s=STUN_RINGS.remove(p.owner());if(s!=null)s.clear();return;}
        int age=(int)Math.max(0,Math.min(100,l.m_46467_()-p.tick()));
        if(age>=life(p))return;
        if(p.kind()==ClientboundAscianEffectPacket.TELEPORT_WINDUP||p.kind()==ClientboundAscianEffectPacket.TELEPORT_ARRIVAL){
            if(age<=2)for(Vec3 c:p.centers())teleportSphere(l,c,p.kind()==ClientboundAscianEffectPacket.TELEPORT_ARRIVAL);
            return;
        }
        if(p.kind()==8){
            ACTIVE.add(new Wave(p,age));
            if(age<=2){
                meleeSound(l,center(p),meleeKind(p,0));
                for(int i=0;i<p.centers().size();i++)if(meleeKind(p,i)==7)
                    ApollyonLightningStrikeEffect.spawn(p.centers().get(i),p.tick()^p.centers().get(i).hashCode(),2,false,.75F);
            }
            return;
        }
        if(p.kind()==7){for(Vec3 c:p.centers())ApollyonLightningStrikeEffect.spawn(c,p.tick()^c.hashCode(),2);return;}
        if(p.kind()==5||p.kind()==6){for(Vec3 c:p.centers())burst(l,c,p.kind()==5,p.tick());return;}
        Wave w=new Wave(p,age);ACTIVE.add(w);
        // Late packets catch up the mesh without playing expired bursts.
        if(age<=2&&(p.kind()==1||p.kind()==2))sound(l,center(p),ApollyonSoundRegistry.FIRE_BURST.get(),1,1);
        if(p.kind()==3&&age>=5&&age<8)sound(l,center(p),ApollyonSoundRegistry.FIRE_BURST.get(),1,1);
        if(isHands(p.kind())&&age>=10&&age<13)sound(l,center(p),SoundEvents.f_11865_,1,1);
    }
    private static boolean isHands(int kind){return kind==4||kind==ClientboundAscianEffectPacket.TRANSITION_HANDS;}
    private static int life(ClientboundAscianEffectPacket p){return switch(p.kind()){
        case 1,2->9;case 3->18;case 8->p.meleePhase()>=5?30:18;
        case 4,ClientboundAscianEffectPacket.TRANSITION_HANDS->30;default->4;};}
    private static int meleeKind(ClientboundAscianEffectPacket p,int index){
        int style=p.meleePhase()==6?1+(p.styleOffset()+index)%5:p.meleePhase();
        return switch(style){case 1->1;case 2->3;case 3->7;case 4->2;default->4;};
    }
    private static void meleeSound(ClientLevel l,Vec3 p,int kind){
        SoundEvent event=kind==7?com.Polarice3.Goety.init.ModSounds.THUNDER_STRIKE_FAST.get()
                :kind==4?net.minecraftforge.registries.ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("minecraft","entity.wither.shoot"))
                :ApollyonSoundRegistry.FIRE_BURST.get();
        sound(l,p,event,1,1);
    }
    private static Vec3 center(ClientboundAscianEffectPacket p){
        Vec3 c=Vec3.f_82478_;for(Vec3 v:p.centers())c=c.m_82549_(v);return c.m_82490_(1D/Math.max(1,p.centers().size()));
    }
    private static void sound(ClientLevel l,Vec3 p,SoundEvent s,float volume,float pitch){l.m_7785_(p.f_82479_,p.f_82480_,p.f_82481_,s,SoundSource.HOSTILE,volume,pitch,false);}
    @SubscribeEvent public static void tick(TickEvent.ClientTickEvent e){
        if(e.phase!=TickEvent.Phase.END)return;var mc=Minecraft.m_91087_();var l=mc.f_91073_;level(l);if(l==null||mc.m_91104_())return;
        for(Iterator<Wave> it=ACTIVE.iterator();it.hasNext();){
            Wave w=it.next();var owner=l.m_6815_(w.packet.ownerId());
            if(w.age>=life(w.packet)||(owner!=null&&(!owner.m_6084_()||!owner.m_20148_().equals(w.packet.owner())))){it.remove();continue;}
            ++w.age;int k=w.packet.kind();
            if(k==8&&w.age==12)for(int i=0;i<w.packet.centers().size();i++)if(meleeKind(w.packet,i)==3)shatter(l,w.packet.centers().get(i),w.packet.tick(),.5F);
            if(k==3&&w.age==5)sound(l,center(w.packet),ApollyonSoundRegistry.FIRE_BURST.get(),1,1);
            if(isHands(k)&&w.age==10)sound(l,center(w.packet),SoundEvents.f_11865_,1,1);
            if(k==3&&w.age==17){sound(l,center(w.packet),SoundEvents.f_11983_,1,1);for(Vec3 p:w.packet.centers())shatter(l,p,w.packet.tick());}
        }
        tickStunRings(l);
    }
    private static void tickStunRings(ClientLevel l){
        var mc=Minecraft.m_91087_();var camera=mc.m_91288_();
        STUN_RINGS.entrySet().removeIf(entry->{
            var s=entry.getValue();var boss=s.owner;
            if(l.m_6815_(boss.m_19879_())!=boss||!boss.m_6084_()||!boss.visualStunned()
                    ||camera==null||camera.m_20238_(boss.m_20182_())>4096){s.clear();return true;}
            return false;
        });
        if(camera==null)return;
        for(var entity:l.m_104735_())if(entity instanceof AscianKingEntity boss&&boss.visualStunned()&&boss.m_6084_()
                &&camera.m_20238_(boss.m_20182_())<=4096){
            var s=STUN_RINGS.computeIfAbsent(boss.m_20148_(),id->new StunRings(boss));
            s.particles.removeIf(p->{if(s.age>=p.expires()){p.particle().m_107274_();return true;}return !p.particle().m_107276_();});
            if(s.age%5==0){
                double rotation=Math.toRadians(s.ring++*10D);
                for(int i=0;i<9;i++){
                    double a=rotation+i*Math.PI*2/9;
                    Vec3 p=boss.m_20182_().m_82520_(Math.cos(a)*1.2,boss.m_20206_()+.45,Math.sin(a)*1.2);
                    var particle=mc.f_91061_.m_107370_(ParticleTypes.f_123797_,p.f_82479_,p.f_82480_,p.f_82481_,0,0,0);
                    if(particle!=null){
                        particle.m_107264_(p.f_82479_,p.f_82480_,p.f_82481_);particle.m_172260_(0,0,0);
                        // Vanilla CritParticle ticks once in its constructor; our bound is 20 full ticks.
                        particle.m_107257_(21);s.particles.add(new StunParticle(particle,s.age+20));
                    }
                }
            }
            ++s.age;
        }
    }
    private static void burst(ClientLevel l,Vec3 p,boolean stun,long seed){
        if(stun){
            sound(l,p,SoundEvents.f_11913_,1,1);p=p.m_82520_(0,3,0);
            l.m_7106_(ParticleTypes.f_123812_,p.f_82479_,p.f_82480_,p.f_82481_,0,0,0);
            return; // Vanilla TNT-style emitter creates a short sequence of explosion sprites.
        }
        Random r=new Random(seed);
        for(int i=0;i<70;i++){
            double x=r.nextGaussian(),y=r.nextGaussian(),z=r.nextGaussian();double n=Math.sqrt(x*x+y*y+z*z);x=x/n*.18;y=y/n*.18;z=z/n*.18;
            var type=i%2==0?ParticleTypes.f_123799_:ParticleTypes.f_123755_;
            l.m_7106_(type,p.f_82479_,p.f_82480_+.4,p.f_82481_,x,y,z);
        }
    }
    private static void teleportSphere(ClientLevel l,Vec3 center,boolean arrival){
        var camera=Minecraft.m_91087_().m_91288_();
        if(camera==null||camera.m_20238_(center)>16384)return;
        double phi=Math.PI*(3-Math.sqrt(5)),speed=arrival?1:6;
        // Preserve the original 1000-point sphere and two dragon-breath layers.
        for(int layer=0;layer<(arrival?2:1);layer++)for(int i=0;i<1000;i++){
            double y=1-i/999D*2,r=Math.sqrt(Math.max(0,1-y*y)),theta=phi*i;
            l.m_7106_(arrival?ParticleTypes.f_123799_:ParticleTypes.f_123760_,center.f_82479_,center.f_82480_,center.f_82481_,
                    Math.cos(theta)*r*speed,y*speed,Math.sin(theta)*r*speed);
        }
    }
    private static void shatter(ClientLevel l,Vec3 p,long seed){
        shatter(l,p,seed,1);
    }
    private static void shatter(ClientLevel l,Vec3 p,long seed,float scale){
        var mc=Minecraft.m_91087_();if(mc.m_91288_()==null||mc.m_91288_().m_20238_(p)>4096)return;
        Random r=new Random(seed^p.hashCode());
        var ice=net.minecraftforge.registries.ForgeRegistries.BLOCKS.getValue(new ResourceLocation("minecraft","ice"));
        var debris=new BlockParticleOption(ParticleTypes.f_123794_,ice.m_49966_());
        for(int i=0;i<82;i++){double h=r.nextDouble()*5.8,a=r.nextDouble()*Math.PI*2,rad=.65*(1-h/5.8);
            l.m_7106_(debris,p.f_82479_+Math.cos(a)*rad*scale,p.f_82480_+h*scale,p.f_82481_+Math.sin(a)*rad*scale,Math.cos(a)*.14*scale,(.07+r.nextDouble()*.08)*scale,Math.sin(a)*.14*scale);}
    }
    @SubscribeEvent public static void render(RenderLevelStageEvent e){
        if(e.getStage()!=RenderLevelStageEvent.Stage.AFTER_PARTICLES||!StarFantasyTranslucentWorldRenderer.shouldDefer())return;
        if(current!=Minecraft.m_91087_().f_91073_)return;
        Vec3 camera=e.getCamera().m_90583_();
        for(Wave w:ACTIVE){float age=w.age+e.getPartialTick();int kind=w.packet.kind();
            for(int i=0;i<w.packet.centers().size();i++){
                Vec3 point=w.packet.centers().get(i);
                if(point.m_82557_(camera)>65536)continue;
                int drawKind=kind==ClientboundAscianEffectPacket.TRANSITION_HANDS?4:kind==8?meleeKind(w.packet,i):kind;
                float drawAge=kind==8&&drawKind==3?age+5:age;
                PoseStack pose=e.getPoseStack();pose.m_85836_();pose.m_85837_(point.f_82479_-camera.f_82479_,point.f_82480_-camera.f_82480_,point.f_82481_-camera.f_82481_);
                if(drawKind==4&&w.handYaw!=null)
                    pose.m_252781_(new Quaternionf().rotationY(w.handYaw[i]));
                if(kind==ClientboundAscianEffectPacket.TRANSITION_HANDS){
                    pose.m_85841_(1.5F,1.5F,1.5F);
                }
                if(kind==8&&drawKind!=4){
                    float scale=drawKind==3?.5F:.75F;
                    pose.m_85841_(scale,scale,scale);
                }
                StarFantasyTranslucentWorldRenderer.defer(pose,0,2,0,(p,b)->draw(p,b,drawKind,drawAge));pose.m_85849_();
            }
        }
    }
    private static void draw(PoseStack p,MultiBufferSource b,int kind,float age){
        if(kind==1||kind==2)pillar(p,b,kind==2,age);
        else if(kind==3&&age>=5&&age<17)ice(p,b,age);
        else if(kind==4&&age<30)claw(p,b,age);
    }
    private static float clamp(float x){return Math.max(0,Math.min(1,x));}
    private static float out(float x){return 1-(float)Math.pow(1-clamp(x),3);}
    private static float env(float age,float[] m){return age<m[3]?clamp(age/m[3]):age<m[4]?1:1-clamp((age-m[4])/(m[5]-m[4]));}
    private static void pillar(PoseStack p,MultiBufferSource b,boolean dark,float age){
        var v=b.m_6299_(AscianEffectMaterials.pillar(dark?DARK:FIRE));float root=env(age,PILLARS[0]);
        for(int i=0;i<4;i++){float[] m=PILLARS[i];float s=i==0?1:env(age,m);if(i==3&&age<m[3])s=(float)Math.floor(s*5)/5;s*=root;if(s<.001)continue;
            p.m_85836_();p.m_85837_(0,m[2]*root,0);p.m_85841_(s,s,s);float w=m[0]/2,h=m[1];
            for(int j=0;j<4;j++){float[] uv=pillarUv(m,j);p.m_85836_();p.m_252781_(new Quaternionf().rotationY(j*(float)Math.PI/2));quad(p,v,new float[][]{{-w,0,w,uv[0],uv[2]},{w,0,w,uv[1],uv[2]},{w,h,w,uv[1],0},{-w,h,w,uv[0],0}},1,1,1,1,false);p.m_85849_();}
            for(int j=0;j<2;j++){float[] uv=pillarUv(m,j+2);p.m_85836_();p.m_252781_(new Quaternionf().rotationY((float)Math.PI*(.25F+j*.5F)).rotateX(j==0?-.12F:.12F));
                float[][] q={{-w*1.05F,0,0,uv[0],uv[2]},{w*1.05F,0,0,uv[1],uv[2]},{w*1.05F,h*.94F,0,uv[1],0},{-w*1.05F,h*.94F,0,uv[0],0}};
                // Only the facing side renders; dim crossed tongues avoid building an opaque interior.
                quad(p,v,q,1,1,1,.35F,false);quad(p,v,new float[][]{q[3],q[2],q[1],q[0]},1,1,1,.35F,false);p.m_85849_();}
            p.m_85849_();
        }
    }
    private static float[] pillarUv(float[] model,int face){
        int pixels=Math.min(24,Math.max(4,Math.round(model[0]*10))),offset=(Math.round(model[1]*3)+face*5)%(25-pixels);
        return new float[]{offset/24F,(offset+pixels)/24F,Math.min(64,Math.max(4,Math.round(model[1]*10)))/64F};
    }
    private static double rnd(int i){double q=Math.sin(i*127.1+41.7)*43758.5453;return q-Math.floor(q);}
    private static void ice(PoseStack p,MultiBufferSource b,float age){
        var v=b.m_6299_(AscianEffectMaterials.get(ICE,false,false));
        for(int j=0;j<7;j++){float radius=j==0?.65F:.28F,h=j==0?5.8F:(float)(1.6+rnd(j)*1.6);double a=j*2.399;
            p.m_85836_();p.m_85837_(j==0?0:Math.cos(a)*.7,-.04,j==0?0:Math.sin(a)*.7);
            if(j>0)p.m_252781_(new Quaternionf().rotationX((float)Math.cos(a)*.2F).rotateZ((float)Math.sin(a)*.2F));
            p.m_85841_(1,out((age-5)/5),1);
            for(int i=0;i<5;i++){double a0=i*Math.PI*2/5,a1=(i+1)*Math.PI*2/5;float x=(float)Math.cos(a0)*radius,z=(float)Math.sin(a0)*radius,x1=(float)Math.cos(a1)*radius,z1=(float)Math.sin(a1)*radius;
                float width=(float)Math.hypot(x1-x,z1-z),tx=radius*.18F,tz=-radius*.1F,u=((tx-x)*(x1-x)+(tz-z)*(z1-z))/width,shade=(float)(.72+.25*(.5+.5*Math.cos((a0+a1)/2-.7)));
                quad(p,v,new float[][]{{x,0,z,0,1},{tx,h,tz,u,1-h},{x1,0,z1,width,1},{x1,0,z1,width,1}},shade,shade,shade,1,false);
            }p.m_85849_();
        }
    }
    private static void claw(PoseStack p,MultiBufferSource b,float age){
        if(clawModel==null)try(var in=Minecraft.m_91087_().m_91098_().m_215593_(new ResourceLocation("starfantasy_goety","effects/ascian_hands.json")).m_215507_()){
            // Reuse one approved hand, centered on the effect point; no lateral closing motion.
            float[][] vertices=new Gson().fromJson(new InputStreamReader(in,java.nio.charset.StandardCharsets.UTF_8),Hand[].class)[0].vertices;
            float minX=Float.POSITIVE_INFINITY,maxX=Float.NEGATIVE_INFINITY,minZ=minX,maxZ=maxX,maxY=maxX;
            for(float[] v:vertices){minX=Math.min(minX,v[0]);maxX=Math.max(maxX,v[0]);minZ=Math.min(minZ,v[2]);maxZ=Math.max(maxZ,v[2]);maxY=Math.max(maxY,v[1]);}
            for(float[] v:vertices){v[0]-=(minX+maxX)/2;v[2]-=(minZ+maxZ)/2;}
            clawHeight=maxY+.01F;clawModel=vertices;
        }catch(IOException ex){throw new IllegalStateException("Missing approved Ascian hands",ex);}
        float visible=age<10?clamp(age/10):age<20?1:1-clamp((age-20)/10);
        visible=visible*visible*(3-2*visible);
        float y=-clawHeight*(1-visible);
        var v=b.m_6299_(AscianEffectMaterials.get(BONES,false,false));
        for(int i=0;i<clawModel.length;i+=4){float[][] q=new float[4][];for(int j=0;j<4;j++){q[j]=clawModel[i+j].clone();q[j][1]+=y;}quad(p,v,q,1,1,1,1,true);}
    }
    // Clip emerging hands to their ground plane, including side faces crossing the plane.
    private static void quad(PoseStack p,VertexConsumer v,float[][] q,float r,float g,float b,float alpha,boolean clip){
        if(!clip){for(float[] a:q)vertex(p,v,a,r,g,b,alpha);return;}
        List<float[]> polygon=new ArrayList<>();
        for(int i=0;i<4;i++){float[] a=q[i],c=q[(i+1)%4];boolean ai=a[1]>=0,ci=c[1]>=0;if(ai)polygon.add(a);if(ai!=ci){float t=-a[1]/(c[1]-a[1]);float[] n=new float[5];for(int j=0;j<5;j++)n[j]=a[j]+(c[j]-a[j])*t;polygon.add(n);}}
        for(int i=1;i+1<polygon.size();i++){vertex(p,v,polygon.get(0),r,g,b,alpha);vertex(p,v,polygon.get(i),r,g,b,alpha);vertex(p,v,polygon.get(i+1),r,g,b,alpha);vertex(p,v,polygon.get(i+1),r,g,b,alpha);}
    }
    static void vertex(PoseStack p,VertexConsumer v,float[] a,float r,float g,float b,float alpha){v.m_252986_(p.m_85850_().m_252922_(),a[0],a[1],a[2]).m_7421_(a[3],a[4]).m_85950_(r,g,b,alpha).m_5752_();}
    private AscianSpellEffects(){}
}
