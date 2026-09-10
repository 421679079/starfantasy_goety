package com.starfantasy.goety.mixin;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.Util;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootTable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Keep the church reward stacks intact after normal loot generation and Forge modifiers. */
@Mixin(value = LootTable.class, remap = false)
public abstract class ChurchLootStackMixin {
    private static final ResourceLocation STARFANTASY_CHURCH_SECRET =
            new ResourceLocation("starfantasy_goety", "chests/church_secret");

    // Explicit SRG selector matches this project's production mixin convention.
    @Inject(method = "m_230924_(Lit/unimi/dsi/fastutil/objects/ObjectArrayList;ILnet/minecraft/util/RandomSource;)V",
            at = @At("HEAD"), cancellable = true, remap = false)
    private void starfantasy$keepRewardStacks(ObjectArrayList<ItemStack> stacks, int slots,
                                             RandomSource random, CallbackInfo callback) {
        if (STARFANTASY_CHURCH_SECRET.equals(((LootTable)(Object)this).getLootTableId())) {
            Util.shuffle(stacks, random);
            callback.cancel();
        }
    }
}
