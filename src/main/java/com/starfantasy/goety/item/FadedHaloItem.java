package com.starfantasy.goety.item;

import com.Polarice3.Goety.common.items.revive.ReviveServantItem;
import com.Polarice3.Goety.common.entities.ally.Summoned;
import com.Polarice3.Goety.common.entities.ModEntityType;
import com.Polarice3.Goety.common.entities.projectiles.FlyingItem;
import com.starfantasy.goety.registry.HaloItemRegistry;
import com.starfantasy.goety.entity.ApollyonServantEntity;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

/** Shared saved-servant behavior for the faded halo and faded crown; revival requires an altar. */
public final class FadedHaloItem extends ReviveServantItem {
    public FadedHaloItem() {
        super(new Item.Properties().m_41487_(1).m_41497_(Rarity.RARE).m_41486_());
    }

    public static ItemStack capture(Summoned servant) {
        ItemStack stack = new ItemStack(servant instanceof ApollyonServantEntity
                ? HaloItemRegistry.FADED_CROWN.get() : HaloItemRegistry.FADED_HALO.get());
        setSummon(servant, stack);
        setOwnerName(servant.getTrueOwner(), stack);
        // Retain the original key so previously issued Hades halos remain valid.
        if (servant.getOwnerId() != null) stack.m_41784_().m_128362_("HadesOwner", servant.getOwnerId());
        return stack;
    }

    public static void returnToOwner(Summoned servant) {
        if (servant.m_9236_().f_46443_ || servant.getOwnerId() == null) return;
        ItemStack halo = capture(servant);
        if (servant.getTrueOwner() != null) {
            FlyingItem flying = new FlyingItem(ModEntityType.FLYING_ITEM.get(), servant.m_9236_(),
                    servant.m_20185_(), servant.m_20186_() + 2, servant.m_20189_());
            flying.setOwner(servant.getTrueOwner());
            flying.setItem(halo);
            flying.setSecondsCool(0);
            servant.m_9236_().m_7967_(flying);
        } else {
            ItemEntity drop = new ItemEntity(servant.m_9236_(), servant.m_20185_(),
                    servant.m_20186_() + 2, servant.m_20189_(), halo);
            drop.m_266426_(servant.getOwnerId());
            servant.m_9236_().m_7967_(drop);
        }
    }

    public static boolean isApollyon(ItemStack stack) {
        CompoundTag tag = stack.m_41783_();
        return tag != null && "starfantasy_goety:apollyon_servant".equals(
                tag.m_128469_("entity").m_128461_("entity"));
    }

    public static boolean belongsTo(ItemStack stack, Player player) {
        CompoundTag tag = stack.m_41783_();
        boolean crown = stack.m_150930_(HaloItemRegistry.FADED_CROWN.get());
        return player != null && (stack.m_150930_(HaloItemRegistry.FADED_HALO.get()) || crown)
                && tag != null && tag.m_128403_("HadesOwner")
                && player.m_20148_().equals(tag.m_128342_("HadesOwner"))
                && tag.m_128469_("entity").m_128403_("Owner")
                && player.m_20148_().equals(tag.m_128469_("entity").m_128342_("Owner"))
                && (isApollyon(stack) || !crown && "starfantasy_goety:hades_servant".equals(
                        tag.m_128469_("entity").m_128461_("entity")));
    }

    @Override public void m_7373_(ItemStack stack, @Nullable Level level,
                                  List<Component> tooltip, TooltipFlag flag) {
        CompoundTag tag = stack.m_41783_();
        if (tag != null && tag.m_128441_("owner_name")) {
            tooltip.add(Component.m_237115_("tooltip.goety.arcaPlayer")
                    .m_130946_(tag.m_128461_("owner_name")).m_130940_(ChatFormatting.GRAY));
        }
        boolean crown = stack.m_150930_(HaloItemRegistry.FADED_CROWN.get());
        tooltip.add(Component.m_237115_(crown
                ? "tooltip.starfantasy_goety.faded_crown"
                : "tooltip.starfantasy_goety.faded_halo").m_130940_(ChatFormatting.GRAY));
        if (!crown) tooltip.add(Component.m_237115_("tooltip.starfantasy_goety.faded_halo.summon")
                .m_130940_(ChatFormatting.GRAY));
    }
}
