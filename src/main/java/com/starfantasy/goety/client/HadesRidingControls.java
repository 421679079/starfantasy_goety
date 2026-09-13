package com.starfantasy.goety.client;

import com.starfantasy.goety.StarFantasyGoetyMod;
import com.starfantasy.goety.entity.HadesServantEntity;
import com.starfantasy.goety.network.ServerboundHadesRidePacket;
import com.starfantasy.goety.network.StarFantasyGoetyNetwork;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.client.event.MovementInputUpdateEvent;
import net.minecraftforge.client.event.InputEvent;

@Mod.EventBusSubscriber(modid = StarFantasyGoetyMod.MODID, value = Dist.CLIENT)
public final class HadesRidingControls {
    private static HadesServantEntity previousMount;
    private HadesRidingControls() { }

    @SubscribeEvent public static void attack(InputEvent.InteractionKeyMappingTriggered event) {
        if (!event.isAttack()) return;
        Minecraft mc = Minecraft.m_91087_();
        if (mc.f_91080_ == null && mc.m_91396_() && mc.f_91074_ != null
                && mc.f_91074_.m_20202_() instanceof HadesServantEntity hades) {
            StarFantasyGoetyNetwork.attackWithHades(hades.m_19879_(), mc.f_91074_.m_146908_());
        }
    }

    @SubscribeEvent public static void movementInput(MovementInputUpdateEvent event) {
        if (event.getEntity().m_20202_() instanceof HadesServantEntity) {
            // Read the actual jump/sneak key mappings separately for flight. Do not send vanilla dismount input.
            event.getInput().f_108573_ = false;
            event.getInput().f_108572_ = false;
        }
    }

    @SubscribeEvent public static void tick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.m_91087_();
        boolean stop = false;
        while (HadesKeyMappings.STOP_RIDING.m_90859_()) stop = true;
        if (mc.f_91074_ == null || !(mc.f_91074_.m_20202_() instanceof HadesServantEntity hades)) {
            previousMount = null;
            return;
        }
        if (previousMount != hades) {
            previousMount = hades;
            mc.f_91065_.m_93063_(HadesKeyMappings.hint(), false);
        }
        float forward = 0, strafe = 0, vertical = 0;
        if (mc.f_91080_ == null && mc.m_91396_()) {
            if (stop) {
                StarFantasyGoetyNetwork.dismountHades(hades.m_19879_());
                return;
            }
            forward = (mc.f_91066_.f_92085_.m_90857_() ? 1 : 0) - (mc.f_91066_.f_92087_.m_90857_() ? 1 : 0);
            strafe = (mc.f_91066_.f_92086_.m_90857_() ? 1 : 0) - (mc.f_91066_.f_92088_.m_90857_() ? 1 : 0);
            boolean descend = mc.f_91066_.f_92090_.m_90857_();
            vertical = (mc.f_91066_.f_92089_.m_90857_() ? 1 : 0) - (descend ? 1 : 0);
        }
        StarFantasyGoetyNetwork.sendHadesRideInput(new ServerboundHadesRidePacket(
                hades.m_19879_(), forward, strafe, vertical, mc.f_91074_.m_146908_()));
    }
}
