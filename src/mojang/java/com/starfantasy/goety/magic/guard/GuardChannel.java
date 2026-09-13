package com.starfantasy.goety.magic.guard;

import com.Polarice3.Goety.api.items.magic.IWand;
import com.Polarice3.Goety.utils.SEHelper;
import com.starfantasy.goety.config.SpellConfig;
import com.starfantasy.goety.mixin.GuardHurtMemoryAccessor;
import com.starfantasy.library.network.StarFantasyLibraryNetwork;
import com.starfantasy.library.vfx.StarFantasyVfx;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

@Mod.EventBusSubscriber(modid = "starfantasy_goety")
public final class GuardChannel {
    private static final Map<ServerPlayer, Session> ACTIVE = new WeakHashMap<>();
    private GuardChannel() {}
    public static boolean isGuardStaff(ItemStack stack) {
        return stack.getItem() instanceof IWand && IWand.getFocus(stack).getItem() instanceof GuardFocusItem;
    }
    public static void begin(ServerPlayer player, ItemStack staff) {
        if (ACTIVE.containsKey(player)) return;
        int soulCost = SpellConfig.GUARD_SOUL_COST.get();
        if (!player.isCreative() && !SEHelper.getSoulsAmount(player, soulCost)) {
            player.stopUsingItem(); return;
        }
        if (!player.isCreative()) {
            SEHelper.decreaseSouls(player, soulCost);
            SEHelper.sendSEUpdatePacket(player);
        }
        GuardShieldEntity shield = GuardFocusContent.SHIELD.get().create(player.serverLevel());
        if (shield != null) { shield.initialize(player); player.serverLevel().addFreshEntity(shield); }
        ACTIVE.put(player, new Session(staff, new GuardRules.Cast(player.level().getGameTime(), GuardSpell.duration(staff)), shield));
    }
    public static void finish(ServerPlayer player) {
        Session session = ACTIVE.remove(player);
        if (session == null) return;
        if (session.shield != null) session.shield.discard();
        if (session.cast.finish()) {
            SEHelper.addCooldown(player, GuardFocusContent.GUARD_FOCUS.get(), GuardSpell.INSTANCE.spellCooldown(player));
        } else {
            clearCooldown(player);
        }
    }
    private static void clearCooldown(ServerPlayer player) {
        SEHelper.getFocusCoolDown(player).removeCooldown(player, player.level(), GuardFocusContent.GUARD_FOCUS.get());
    }
    @SubscribeEvent public static void tick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer player)) return;
        Session s = ACTIVE.get(player);
        if (s != null && (!player.isAlive() || !player.isUsingItem() || player.getUseItem() != s.staff
                || !isGuardStaff(s.staff) || !s.cast.active(player.level().getGameTime()))) {
            finish(player);
            if (player.isUsingItem() && player.getUseItem() == s.staff) player.stopUsingItem();
        }
    }
    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void attacked(LivingAttackEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || event.getAmount() <= 0 || !Float.isFinite(event.getAmount())) return;
        Session s = ACTIVE.get(player);
        if (s == null || !s.cast.active(player.level().getGameTime()) || !player.isUsingItem()
                || player.getUseItem() != s.staff || !isGuardStaff(s.staff)) return;
        var source = event.getSource();
        Entity direct = source.getDirectEntity(), attacker = source.getEntity();
        if ((direct == null && attacker == null) || direct == player || attacker == player
                || blacklisted(direct) || blacklisted(attacker) || source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) return;
        Vec3 origin = source.getSourcePosition();
        if (origin == null) return;
        Vec3 incoming = origin.subtract(player.position()), look = player.getLookAngle();
        if (!GuardRules.inFront(look.x, look.z, incoming.x, incoming.z)) return;
        event.setCanceled(true);
        if (s.cast.succeed()) {
            SEHelper.increaseSouls(player, SpellConfig.GUARD_SOUL_REWARD.get());
            SEHelper.sendSEUpdatePacket(player);
            clearCooldown(player);
        }
        int immunity = SpellConfig.GUARD_INVULNERABILITY.get();
        if (immunity > 0) {
            GuardHurtMemoryAccessor memory = (GuardHurtMemoryAccessor)player;
            memory.starfantasy$setLastHurt(player.invulnerableTime > 10
                    ? Math.max(memory.starfantasy$getLastHurt(), event.getAmount()) : event.getAmount());
            player.invulnerableTime = Math.max(player.invulnerableTime, immunity);
        }
        long now = player.level().getGameTime();
        if (s.feedbackTick != now) {
            s.feedbackTick = now;
            StarFantasyVfx.guardClash(player, clashPosition(player.position(), origin));
            StarFantasyLibraryNetwork.sendShake(player, 10, 1.0F);
        }
    }
    public static Vec3 clashPosition(Vec3 player, Vec3 source) {
        // Preserve the 1.5-block spark height; the entire origin remains within 3 blocks of the player.
        Vec3 horizontal = new Vec3((source.x - player.x) * .5D, 0, (source.z - player.z) * .5D);
        double maxHorizontal = Math.sqrt(9.0D - 1.5D * 1.5D);
        if (horizontal.lengthSqr() > maxHorizontal * maxHorizontal) horizontal = horizontal.normalize().scale(maxHorizontal);
        return player.add(horizontal).add(0, 1.5D, 0);
    }
    private static boolean blacklisted(Entity entity) {
        return entity != null && SpellConfig.GUARD_BLACKLIST.get().contains(String.valueOf(ForgeRegistries.ENTITY_TYPES.getKey(entity.getType())));
    }
    @SubscribeEvent public static void logout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) finish(player);
    }
    @SubscribeEvent public static void changedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) finish(player);
    }
    @SubscribeEvent public static void stopped(ServerStoppedEvent event) { ACTIVE.clear(); }
    private static final class Session {
        final ItemStack staff;
        final GuardRules.Cast cast;
        final GuardShieldEntity shield;
        long feedbackTick = Long.MIN_VALUE;
        Session(ItemStack staff, GuardRules.Cast cast, GuardShieldEntity shield) {
            this.staff = staff; this.cast = cast; this.shield = shield;
        }
    }
}
