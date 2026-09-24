package com.starfantasy.goety.mixin;

import com.Polarice3.Goety.client.render.ObsidianMonolithRenderer;
import com.Polarice3.Goety.common.entities.hostile.servants.ObsidianMonolith;
import com.Polarice3.Goety.common.entities.neutral.AbstractMonolith;
import com.mojang.blaze3d.vertex.PoseStack;
import com.starfantasy.goety.client.HadesSeatInterpolation;
import com.starfantasy.goety.entity.ApollyonServantEntity;
import com.starfantasy.goety.entity.HadesServantEntity;
import com.starfantasy.goety.entity.riding.HadesRiderSeat;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = ObsidianMonolithRenderer.class, remap = false)
public abstract class ApollyonServantMonolithChainMixin {
    @Redirect(method = "render(Lcom/Polarice3/Goety/common/entities/neutral/AbstractMonolith;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at = @At(value = "INVOKE", target = "Lcom/Polarice3/Goety/common/entities/neutral/AbstractMonolith;getTrueOwner()Lnet/minecraft/world/entity/LivingEntity;"), remap = false)
    private LivingEntity starfantasy$chainOwner(AbstractMonolith monolith) {
        LivingEntity owner = monolith.getTrueOwner();
        if (monolith instanceof ObsidianMonolith && owner instanceof ApollyonServantEntity servant
                && servant.m_20202_() instanceof HadesServantEntity hades
                && hades.mountedApollyonServant() == servant) return hades;
        return owner;
    }

    @ModifyVariable(method = "render(Lcom/Polarice3/Goety/common/entities/neutral/AbstractMonolith;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at = @At("STORE"), index = 10, remap = false)
    private Vec3 starfantasy$chainEndpoint(Vec3 original, AbstractMonolith monolith,
                                           float yaw, float partial, PoseStack pose, MultiBufferSource buffers, int light) {
        if (!(monolith instanceof ObsidianMonolith)
                || !(monolith.getTrueOwner() instanceof ApollyonServantEntity servant)
                || !(servant.m_20202_() instanceof HadesServantEntity hades)
                || hades.mountedApollyonServant() != servant) return original;
        Vec3 origin = new Vec3(Mth.m_14139_(partial, hades.f_19854_, hades.m_20185_()),
                Mth.m_14139_(partial, hades.f_19855_, hades.m_20186_()),
                Mth.m_14139_(partial, hades.f_19856_, hades.m_20189_()));
        float bodyYaw = Mth.m_14189_(partial, hades.f_20884_, hades.f_20883_);
        return HadesRiderSeat.chainPosition(HadesSeatInterpolation.pose(hades, partial), origin, bodyYaw);
    }
}
