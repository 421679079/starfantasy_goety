package com.starfantasy.goety.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.starfantasy.goety.config.StaffConfig;
import com.starfantasy.goety.item.AscianStaffItem;
import com.starfantasy.goety.magic.AscianStaffSchool;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public final class AscianStaffItemRenderer extends GeoItemRenderer<AscianStaffItem> {
    private AscianStaffItemRenderer(){super(new AscianItemModel<>("ascian_staff"));}

    @Override public void actuallyRender(PoseStack pose,AscianStaffItem item,BakedGeoModel model,RenderType type,
            MultiBufferSource buffers,VertexConsumer out,boolean reRender,float partial,int light,int overlay,
            float red,float green,float blue,float alpha){
        super.actuallyRender(pose,item,model,type,buffers,out,reRender,partial,light,overlay,red,green,blue,alpha);
        if(!reRender){
            var mc=Minecraft.m_91087_();float time=mc.f_91073_==null?0:(mc.f_91073_.m_46467_()%12000)+partial;
            int color=StaffConfig.ASCIAN_CUBE_COLORS.get(AscianStaffSchool.get(getCurrentItemStack()).type).get();
            com.starfantasy.goety.halo.client.AscianStaffCube.render(pose,buffers,time,color,overlay);
        }
    }

    public static void initialize(java.util.function.Consumer<IClientItemExtensions> consumer,IClientItemExtensions wand){
        consumer.accept(new IClientItemExtensions(){
            private AscianStaffItemRenderer renderer;
            @Override public HumanoidModel.ArmPose getArmPose(LivingEntity entity,InteractionHand hand,ItemStack stack){return wand.getArmPose(entity,hand,stack);}
            @Override public boolean applyForgeHandTransform(PoseStack pose,LocalPlayer player,HumanoidArm arm,ItemStack stack,
                    float partial,float equip,float swing){return wand.applyForgeHandTransform(pose,player,arm,stack,partial,equip,swing);}
            @Override public BlockEntityWithoutLevelRenderer getCustomRenderer(){if(renderer==null)renderer=new AscianStaffItemRenderer();return renderer;}
        });
    }
}
