package com.starfantasy.goety.item;

import com.Polarice3.Goety.common.items.revive.ReviveServantItem;
import com.starfantasy.goety.entity.HadesServantEntity;
import com.starfantasy.goety.registry.HaloItemRegistry;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

/** Goety-compatible saved servant data; revival is only available through the altar. */
public final class FadedHaloItem extends ReviveServantItem {
    public FadedHaloItem() {
        super(new Item.Properties().m_41487_(1).m_41497_(Rarity.RARE).m_41486_());
    }

    public static ItemStack capture(HadesServantEntity servant) {
        ItemStack stack = new ItemStack(HaloItemRegistry.FADED_HALO.get());
        setSummon(servant, stack);
        setOwnerName(servant.getTrueOwner(), stack);
        if (servant.getOwnerId() != null) stack.m_41784_().m_128362_("HadesOwner", servant.getOwnerId());
        return stack;
    }

    public static boolean belongsTo(ItemStack stack, Player player) {
        CompoundTag tag = stack.m_41783_();
        return player != null && stack.m_150930_(HaloItemRegistry.FADED_HALO.get())
                && tag != null && tag.m_128403_("HadesOwner")
                && player.m_20148_().equals(tag.m_128342_("HadesOwner"))
                && tag.m_128469_("entity").m_128403_("Owner")
                && player.m_20148_().equals(tag.m_128469_("entity").m_128342_("Owner"))
                && "starfantasy_goety:hades_servant".equals(tag.m_128469_("entity").m_128461_("entity"));
    }

    @Override public void m_7373_(ItemStack stack, @Nullable Level level,
                                  List<Component> tooltip, TooltipFlag flag) {
        CompoundTag tag = stack.m_41783_();
        if (tag != null && tag.m_128441_("owner_name")) {
            tooltip.add(Component.m_237115_("tooltip.goety.arcaPlayer")
                    .m_130946_(tag.m_128461_("owner_name")).m_130940_(ChatFormatting.GRAY));
        }
        tooltip.add(Component.m_237115_("tooltip.starfantasy_goety.faded_halo").m_130940_(ChatFormatting.GRAY));
    }
}
