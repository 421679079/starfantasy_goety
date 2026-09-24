package com.starfantasy.goety.client;

import com.starfantasy.goety.entity.ApostleServantEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.model.data.EntityModelData;

/** The same twelve Apostle meshes, with owned bow/casting poses and no boss proxy entity. */
public final class ApostleServantGeoModel extends GeoModel<ApostleServantEntity> {
    private final java.util.Map<ApostleServantEntity,float[]> legMotion = new java.util.WeakHashMap<>();
    private final net.minecraft.client.model.geom.ModelPart rightArm = new net.minecraft.client.model.geom.ModelPart(java.util.List.of(),java.util.Map.of());
    private final net.minecraft.client.model.geom.ModelPart leftArm = new net.minecraft.client.model.geom.ModelPart(java.util.List.of(),java.util.Map.of());
    private ResourceLocation resource(ApostleServantEntity e,String folder,String suffix) {
        return new ResourceLocation("starfantasy_goety",folder+"/entity/apostle/apostle_the_"+ApostleServantEntity.TITLES[e.getTitleNumber()]+suffix);
    }
    @Override public ResourceLocation getModelResource(ApostleServantEntity e) { return resource(e,"geo",".geo.json"); }
    @Override public ResourceLocation getTextureResource(ApostleServantEntity e) { return resource(e,"textures",".png"); }
    @Override public ResourceLocation getAnimationResource(ApostleServantEntity e) { return new ResourceLocation("starfantasy_goety","animations/entity/apostle/apostle.animation.json"); }
    @Override public void setCustomAnimations(ApostleServantEntity e,long id,AnimationState<ApostleServantEntity> state) {
        EntityModelData head=state.getData(DataTickets.ENTITY_MODEL_DATA);
        float yaw=head==null?0:-head.netHeadYaw()*((float)Math.PI/180F), pitch=head==null?0:-head.headPitch()*((float)Math.PI/180F);
        Double renderTick=state.getData(DataTickets.TICK);
        float tick=renderTick==null?(float)state.getAnimationTick():renderTick.floatValue();
        float swing=Mth.m_14089_(state.getLimbSwing()*.6662F)*state.getLimbSwingAmount();
        pose("a_head",pitch,yaw,0); pose("a_body",0,yaw*.3F,0);
        pose("longlong",.5F*state.getLimbSwingAmount()-pitch,0,0);
        boolean action=e.isCasting()||e.isSettingUpSecond()||e.isAimingBow();
        float[] motion=legMotion.computeIfAbsent(e,ignored->new float[]{.2F,tick});
        float elapsed=tick-motion[1], target=action?.35F:.2F;
        motion[0]=elapsed<0||elapsed>20?target:Mth.m_14121_(motion[0],target,elapsed*.04F); motion[1]=tick;
        float leftLeg=-swing*.7F, rightLeg=swing*.7F;
        pose("a_legL",Mth.m_14036_(leftLeg*motion[0],-.24F,.24F),0,0);
        pose("a_legR",Mth.m_14036_(rightLeg*motion[0],-.24F,.24F),0,0); pose("a_skirt1",0,0,0);
        if(e.m_21224_()) {
            float sway=Mth.m_14089_(tick*.6662F)*.25F;
            pose("a_head",-(float)Math.toRadians(25),yaw,0);
            pose("longlong",.5F*state.getLimbSwingAmount()+(float)Math.toRadians(25),0,0);
            pose("a_armR",sway,0,2.3561945F+.3F);
            pose("a_armL",-sway,0,-2.3561945F-.3F);
            pose("a_legL",0,0,0); pose("a_legR",0,0,0);
        } else if(e.isCasting()||e.isSettingUpSecond()) {
            float sway=Mth.m_14089_(tick*.6662F)*.25F;
            // The weapon arm uses Goety's SPELL_AND_WEAPON swing, including its
            // Y/Z rotations; the other arm is the raised casting hand.
            net.minecraft.client.model.AnimationUtils.m_102091_(rightArm,leftArm,e,0,tick);
            if(e.m_5737_()==net.minecraft.world.entity.HumanoidArm.RIGHT) {
                pose("a_armR",rightArm.f_104203_,rightArm.f_104204_,rightArm.f_104205_+.3F);
                pose("a_armL",sway,0,-2.3561945F-.3F);
            } else {
                pose("a_armR",sway,0,2.3561945F+.3F);
                pose("a_armL",leftArm.f_104203_,leftArm.f_104204_,leftArm.f_104205_-.3F);
            }
        } else if(e.isAimingBow()) {
            pose("a_armR",-(float)Math.PI/2+pitch,yaw-.1F,.3F);
            pose("a_armL",-(float)Math.PI/2+pitch,yaw+.5F,-.3F);
        } else {
            // Same CROSSED fallback as ApostleGeoModel: arms follow their legs.
            pose("a_armR",rightLeg,-.314F,.3F); pose("a_armL",leftLeg,.314F,-.3F);
        }
        float halo=.4F*(float)Math.sin(tick*.2); pose("c1",0,halo,0); pose("c2",0,-halo,0);
    }
    private void pose(String name,float x,float y,float z) {
        getBone(name).ifPresent(b->{b.setRotX(-x);b.setRotY(-y);b.setRotZ(z);});
    }
}
