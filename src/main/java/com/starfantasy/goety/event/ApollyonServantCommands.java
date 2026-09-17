package com.starfantasy.goety.event;

import com.starfantasy.goety.registry.ApollyonEntityRegistry;
import com.Polarice3.Goety.common.ritual.RitualRequirements;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Operator-only preview entrance; respects the same ownership limit as the rituals. */
@Mod.EventBusSubscriber(modid = "starfantasy_goety")
public final class ApollyonServantCommands {
    private ApollyonServantCommands() {}
    @SubscribeEvent public static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.m_82127_("apollyon_servant")
                .requires(source -> source.m_6761_(2)).executes(context -> {
                    ServerPlayer player = context.getSource().m_81375_();
                    var level = context.getSource().m_81372_();
                    if (!RitualRequirements.canSummon(level, player,
                            ApollyonEntityRegistry.APOLLYON_SERVANT.get())) return 0;
                    var servant = ApollyonEntityRegistry.APOLLYON_SERVANT.get().m_20615_(level);
                    if (servant == null) return 0;
                    servant.setTrueOwner(player);
                    servant.m_6034_(player.m_20185_(), player.m_20186_(), player.m_20189_());
                    return level.m_7967_(servant) ? 1 : 0;
                }));
    }
}
