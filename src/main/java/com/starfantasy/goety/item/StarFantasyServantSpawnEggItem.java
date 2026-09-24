package com.starfantasy.goety.item;

import com.Polarice3.Goety.common.items.ServantSpawnEggItem;
import com.starfantasy.goety.servant.ServantOwnershipData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.RegistryObject;

public final class StarFantasyServantSpawnEggItem extends ServantSpawnEggItem {
    private final String servantType;
    private final int title;

    public StarFantasyServantSpawnEggItem(RegistryObject<? extends EntityType<? extends Mob>> type,
                                          int baseColor, int spotColor, int title) {
        super(type, baseColor, spotColor, new Item.Properties());
        servantType = type.getId().toString();
        this.title = title;
    }

    @Override public ItemStack m_7968_() {
        ItemStack stack = super.m_7968_();
        prepareTitle(stack);
        return stack;
    }

    @Override public InteractionResult m_6225_(UseOnContext context) {
        if (context.m_43725_() instanceof ServerLevel) {
            Player player = context.m_43723_();
            if (!canSpawnFor(player)) return InteractionResult.FAIL;
            prepareTitle(context.m_43722_());
        }
        return super.m_6225_(context);
    }

    @Override public InteractionResultHolder<ItemStack> m_7203_(Level level, Player player, InteractionHand hand) {
        if (level instanceof ServerLevel) {
            ItemStack stack = player.m_21120_(hand);
            if (!canSpawnFor(player)) return InteractionResultHolder.m_19100_(stack);
            prepareTitle(stack);
        }
        return super.m_7203_(level, player, hand);
    }

    private boolean canSpawnFor(Player player) {
        if (player == null || !player.m_6047_() || ServantOwnershipData.canOwnAnother(servantType, player))
            return true;
        player.m_5661_(Component.m_237115_("message.starfantasy_goety.spawn_egg.limit"), true);
        return false;
    }

    private void prepareTitle(ItemStack stack) {
        if (title < 0) return;
        CompoundTag root = stack.m_41784_();
        if (!root.m_128441_("EntityTag")) root.m_128365_("EntityTag", new CompoundTag());
        root.m_128469_("EntityTag").m_128405_("TitleNumber", title);
    }
}
