package com.starfantasy.goety.magic.guard;

import com.Polarice3.Goety.api.items.magic.IWand;
import com.Polarice3.Goety.utils.SEHelper;
import com.starfantasy.goety.config.SpellConfig;
import com.starfantasy.library.network.StarFantasyLibraryNetwork;
import com.starfantasy.library.posture.GuardProbeEvent;
import com.starfantasy.library.posture.PerfectGuardEvent;
import com.starfantasy.library.vfx.StarFantasyVfx;
import java.util.Map;
import java.util.ArrayList;
import java.util.WeakHashMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

@Mod.EventBusSubscriber(modid = "starfantasy_goety")
public final class GuardChannel {
    private static final Map<LivingEntity, Session> ACTIVE = new WeakHashMap<>();
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
        ACTIVE.put(player, new Session(staff, new GuardRules.Cast(player.level().getGameTime(), GuardSpell.duration(staff)), shield, player.level()));
    }
    /** Non-player providers own their resource costs and cooldowns, and need not use an item. */
    public static void beginSpell(LivingEntity caster, ItemStack staff, int duration) {
        if (caster instanceof Player || !caster.isAlive() || duration <= 0
                || !(caster.level() instanceof ServerLevel level) || ACTIVE.containsKey(caster)) return;
        GuardShieldEntity shield = GuardFocusContent.SHIELD.get().create(level);
        if (shield != null) { shield.initialize(caster); level.addFreshEntity(shield); }
        ACTIVE.put(caster, new Session(staff, new GuardRules.Cast(level.getGameTime(), duration), shield, level));
    }
    public static void refreshSpell(LivingEntity caster) {
        Session session = ACTIVE.get(caster);
        if (session != null) session.lastRefresh = caster.level().getGameTime();
    }
    public static boolean isActive(LivingEntity caster) {
        Session s = ACTIVE.get(caster);
        if (s == null || !caster.isAlive() || caster.isRemoved() || caster.level() != s.level
                || !s.cast.active(caster.level().getGameTime())) return false;
        return caster instanceof Player
                ? caster.isUsingItem() && caster.getUseItem() == s.staff && isGuardStaff(s.staff)
                : caster.level().getGameTime() - s.lastRefresh <= 2;
    }
    public static void finish(LivingEntity caster) {
        Session session = ACTIVE.remove(caster);
        if (session == null) return;
        if (session.shield != null) session.shield.discard();
        boolean missed = session.cast.finish();
        if (!(caster instanceof ServerPlayer player)) return;
        if (missed) {
            SEHelper.addCooldown(player, GuardFocusContent.GUARD_FOCUS.get(), GuardSpell.INSTANCE.spellCooldown(player));
        } else {
            clearCooldown(player);
        }
    }
    private static void clearCooldown(ServerPlayer player) {
        SEHelper.getFocusCoolDown(player).removeCooldown(player, player.level(), GuardFocusContent.GUARD_FOCUS.get());
    }
    @SubscribeEvent public static void tickSpells(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        for (LivingEntity caster : new ArrayList<>(ACTIVE.keySet())) {
            if (!(caster instanceof Player) && !isActive(caster)) finish(caster);
        }
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
        double reduction = SpellConfig.GUARD_DAMAGE_REDUCTION.get();
        if (reduction < 1.0D && (!(event instanceof GuardProbeEvent) || reduction <= 0.0D)) return;
        LivingEntity player = event.getEntity();
        Session s = eligible(player, event.getSource(), event.getAmount());
        if (s == null) return;
        event.setCanceled(true);
        succeed(player, s, event.getSource());
        int immunity = SpellConfig.GUARD_INVULNERABILITY.get();
        GuardInvulnerability.grant(player, immunity);
    }
    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void hurt(LivingHurtEvent event) {
        double reduction = SpellConfig.GUARD_DAMAGE_REDUCTION.get();
        if (reduction >= 1.0D) return;
        LivingEntity player = event.getEntity();
        Session s = eligible(player, event.getSource(), event.getAmount());
        if (s == null) return;
        event.setAmount((float)(event.getAmount() * (1.0D - reduction)));
        succeed(player, s, event.getSource());
        int immunity = SpellConfig.GUARD_INVULNERABILITY.get();
        GuardInvulnerability.grant(player, immunity);
    }
    private static Session eligible(LivingEntity player, DamageSource source, float amount) {
        if (amount <= 0 || !Float.isFinite(amount)) return null;
        Session s = ACTIVE.get(player);
        if (!isActive(player)) return null;
        Entity direct = source.getDirectEntity(), attacker = source.getEntity();
        if ((direct == null && attacker == null) || direct == player || attacker == player
                || blacklisted(direct) || blacklisted(attacker) || source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) return null;
        Vec3 origin = source.getSourcePosition();
        if (origin == null) return null;
        Vec3 incoming = origin.subtract(player.position()), look = player.getLookAngle();
        return GuardRules.inFront(look.x, look.z, incoming.x, incoming.z) ? s : null;
    }
    private static void succeed(LivingEntity player, Session s, DamageSource source) {
        if (s.cast.succeed() && player instanceof ServerPlayer serverPlayer) {
            SEHelper.increaseSouls(serverPlayer, SpellConfig.GUARD_SOUL_REWARD.get());
            SEHelper.sendSEUpdatePacket(serverPlayer);
            clearCooldown(serverPlayer);
        }
        long now = player.level().getGameTime();
        if (s.feedbackTick != now) {
            s.feedbackTick = now;
            StarFantasyVfx.guardClash(player, clashPosition(player.position(), source.getSourcePosition()));
            if (player instanceof ServerPlayer serverPlayer) StarFantasyLibraryNetwork.sendShake(serverPlayer, 10, 1.0F);
        }
        // Soul recovery is once per cast, but each guarded attack has its own source/reward.
        // A zero-reduction configuration must not award boss guard mechanics.
        if (SpellConfig.GUARD_DAMAGE_REDUCTION.get() > 0.0D && player instanceof ServerPlayer serverPlayer) {
            MinecraftForge.EVENT_BUS.post(new PerfectGuardEvent(serverPlayer, source));
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
        if (event.getEntity() instanceof ServerPlayer player) { finish(player); GuardInvulnerability.clear(player); }
    }
    @SubscribeEvent public static void changedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) { finish(player); GuardInvulnerability.clear(player); }
    }
    @SubscribeEvent public static void stopped(ServerStoppedEvent event) { ACTIVE.clear(); }
    private static final class Session {
        final ItemStack staff;
        final GuardRules.Cast cast;
        final GuardShieldEntity shield;
        final Level level;
        long lastRefresh;
        long feedbackTick = Long.MIN_VALUE;
        Session(ItemStack staff, GuardRules.Cast cast, GuardShieldEntity shield, Level level) {
            this.staff = staff; this.cast = cast; this.shield = shield;
            this.level = level; this.lastRefresh = cast.start;
        }
    }
}
