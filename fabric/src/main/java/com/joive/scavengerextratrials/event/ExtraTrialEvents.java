package com.joive.scavengerextratrials.event;

import com.joive.scavengerextratrials.ScavengerExtraTrials;
import com.joive.scavengerextratrials.config.ScavengerExtraTrialsConfig;
import com.joive.scavengerextratrials.data.ExtraTrialsSavedData;
import com.joive.scavengerextratrials.registry.ExtraModifiers;
import com.joive.scavengerextratrials.util.ExtraModifierChecks;
import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.EntityEvent;
import dev.architectury.event.events.common.PlayerEvent;
import dev.architectury.event.events.common.TickEvent;
import meow.binary.scavenger.data.ScavengerSavedData;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Relative;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Set;

public final class ExtraTrialEvents {
    private static final int ROUTE_NO_CHUNK = Integer.MIN_VALUE;
    private static final Identifier GLASS_HEART_MODIFIER_ID = Identifier.fromNamespaceAndPath(ScavengerExtraTrials.MOD_ID, "glass_heart_health_penalty");

    private ExtraTrialEvents() {
    }

    public static void register() {
        TickEvent.PLAYER_POST.register(ExtraTrialEvents::onPlayerPostTick);
        EntityEvent.LIVING_HURT.register(ExtraTrialEvents::onLivingHurt);
        EntityEvent.LIVING_DEATH.register(ExtraTrialEvents::onLivingDeath);
        PlayerEvent.PLAYER_QUIT.register(ExtraTrialEvents::onPlayerQuit);
        ServerLivingEntityEvents.ALLOW_DAMAGE.register(ExtraTrialEvents::allowDamage);
        ScavengerExtraTrials.LOGGER.info("Scavenger Extra Trials event hooks ready");
    }

    public static void onModifierChanging(MinecraftServer server, Identifier oldModifier, Identifier newModifier) {
        if (server == null) {
            return;
        }
        if (ExtraModifiers.id("closing_border").equals(oldModifier) && !ExtraModifiers.id("closing_border").equals(newModifier)) {
            stopClosingBorderControl(server, true);
        }
        if (ExtraModifiers.id("route_pressure").equals(oldModifier)
                || ExtraModifiers.id("enderman_blood").equals(oldModifier)
                || ExtraModifiers.id("hot_potato").equals(oldModifier)
                || ExtraModifiers.id("glass_heart").equals(oldModifier)
                || ExtraModifiers.id("repelling_loot").equals(oldModifier)
                || ExtraModifiers.id("spicy_start").equals(oldModifier)
                || ExtraModifiers.id("upside_down").equals(oldModifier)) {
            if (!oldModifier.equals(newModifier)) {
                resetRuntimeForAllPlayers(server);
            }
        }
    }

    public static void startClosingBorder(ServerLevel level) {
        if (level != null) {
            setupClosingBorder(level.getServer(), null, false);
        }
    }

    public static void tickRoutePressure(ServerPlayer player) {
        if (player != null) {
            tickRoutePressure(player, false);
        }
    }

    public static boolean testfireClosingBorder(ServerPlayer player) {
        if (!ScavengerExtraTrialsConfig.ENABLE_CLOSING_BORDER.get()) {
            return false;
        }
        setupClosingBorder(player.level().getServer(), player, true);
        WorldBorder border = player.level().getServer().overworld().getWorldBorder();
        player.sendSystemMessage(Component.literal("closing_border testfire: start=" + closingBorderStartSize()
                + ", min=" + closingBorderMinSize()
                + ", shrinkSeconds=" + closingBorderShrinkSeconds()
                + ", current=" + Math.round(border.getSize())));
        return true;
    }

    public static boolean testfireRoutePressure(ServerPlayer player) {
        if (!ScavengerExtraTrialsConfig.ENABLE_ROUTE_PRESSURE.get()) {
            return false;
        }
        ExtraTrialsSavedData data = ExtraTrialsSavedData.get(player.level());
        data.setCooldown(player.getUUID(), ExtraTrialsSavedData.ROUTE_CHUNK_X, player.chunkPosition().x);
        data.setCooldown(player.getUUID(), ExtraTrialsSavedData.ROUTE_CHUNK_Z, player.chunkPosition().z);
        data.setCooldown(player.getUUID(), ExtraTrialsSavedData.ROUTE_TICKS, Math.max(0, routePressureMaxTicks() - 40));
        data.setCooldown(player.getUUID(), ExtraTrialsSavedData.ROUTE_WARNING_STATE, 1L);
        data.setCooldown(player.getUUID(), ExtraTrialsSavedData.ROUTE_GRACE_UNTIL, 0L);
        data.setCooldown(player.getUUID(), ExtraTrialsSavedData.ROUTE_LAST_MESSAGE_TICK, player.level().getGameTime());
        player.displayClientMessage(Component.translatable("scavenger_extra_trials.actionbar.route_pressure.warning"), true);
        player.sendSystemMessage(Component.literal("route_pressure testfire: chunk timer primed"));
        return true;
    }

    public static boolean testfireEndermanBlood(ServerPlayer player) {
        boolean triggered = triggerEndermanBlood(player, true);
        player.sendSystemMessage(Component.literal(triggered
                ? "enderman_blood testfire: teleport triggered"
                : "enderman_blood testfire: teleport failed"));
        return triggered;
    }

    public static boolean testfireHotPotato(ServerPlayer player) {
        boolean primed = false;
        if (player.getMainHandItem().isEmpty() && player.getOffhandItem().isEmpty()) {
            player.sendSystemMessage(Component.translatable("scavenger_extra_trials.actionbar.hot_potato.empty"));
            return false;
        }
        ExtraTrialsSavedData data = ExtraTrialsSavedData.get(player.level());
        if (!player.getMainHandItem().isEmpty()) {
            primeHotPotatoHand(player, data, EquipmentSlot.MAINHAND);
            primed = true;
        }
        if (!player.getOffhandItem().isEmpty()) {
            primeHotPotatoHand(player, data, EquipmentSlot.OFFHAND);
            primed = true;
        }
        data.setCooldown(player.getUUID(), ExtraTrialsSavedData.HOT_POTATO_LAST_MESSAGE_TICK, player.level().getGameTime());
        player.displayClientMessage(Component.translatable("scavenger_extra_trials.actionbar.hot_potato.switch_item"), true);
        player.sendSystemMessage(Component.literal("hot_potato testfire: held item overheated"));
        return primed;
    }

    public static boolean testfireRepellingLoot(ServerPlayer player) {
        for (int index = 0; index < 5; index++) {
            double angle = Math.PI * 2.0D * index / 5.0D;
            double distance = 2.0D + player.level().random.nextDouble() * 3.0D;
            double height = index < 2 ? 1.65D : 0.25D;
            double dirX = Math.cos(angle);
            double dirZ = Math.sin(angle);
            ItemStack stack = new ItemStack(List.of(Items.COBBLESTONE, Items.APPLE, Items.COPPER_INGOT, Items.STICK, Items.IRON_NUGGET).get(index));
            ItemEntity item = new ItemEntity(player.level(), player.getX() + dirX * distance, player.getY() + height,
                    player.getZ() + dirZ * distance, stack);
            item.setDeltaMovement(dirX * 1.60D, item.getDeltaMovement().y, dirZ * 1.60D);
            item.hurtMarked = true;
            player.level().addFreshEntity(item);
            ((ServerLevel) player.level()).sendParticles(ParticleTypes.END_ROD, item.getX(), item.getY() + 0.15D, item.getZ(),
                    3, 0.08D, 0.05D, 0.08D, 0.02D);
        }
        player.displayClientMessage(Component.translatable("scavenger_extra_trials.actionbar.repelling_loot.testfire"), true);
        player.sendSystemMessage(Component.literal("repelling_loot testfire: 5 items spawned with immediate X/Z velocity"));
        return true;
    }

    public static boolean testfireUpsideDown(ServerPlayer player) {
        player.displayClientMessage(Component.translatable("scavenger_extra_trials.actionbar.upside_down.testfire"), true);
        player.sendSystemMessage(Component.literal("upside_down testfire: camera roll is client-side and follows the active modifier"));
        return true;
    }

    public static boolean testfireSpicyStart(ServerPlayer player) {
        if (!ScavengerExtraTrialsConfig.ENABLE_SPICY_START.get()) {
            return false;
        }
        boolean teleported = tryStartSpicyStart(player, true);
        if (!teleported && player.level().dimension() == Level.NETHER) {
            primeSpicyStartProtection(player, ExtraTrialsSavedData.get(player.level()), player.level().getGameTime());
            player.displayClientMessage(Component.translatable("scavenger_extra_trials.actionbar.spicy_start.protection"), true);
        }
        player.sendSystemMessage(Component.literal("spicy_start testfire: " + (teleported ? "nether teleport attempted" : "protection refreshed")));
        return true;
    }

    public static boolean testfireGlassHeart(ServerPlayer player) {
        ExtraTrialsSavedData data = ExtraTrialsSavedData.get(player.level());
        int stacks = addGlassHeartStack(player, data);
        player.sendSystemMessage(Component.literal("glass_heart testfire: stacks=" + stacks));
        return stacks > 0;
    }

    private static void onPlayerPostTick(Player player) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }

        tickClosingBorder(serverPlayer);
        tickRoutePressure(serverPlayer, false);
        tickGlassHeart(serverPlayer);
        tickHotPotato(serverPlayer);
        tickRepellingLoot(serverPlayer);
        tickSpicyStart(serverPlayer);
    }

    private static boolean allowDamage(LivingEntity entity, DamageSource source, float amount) {
        if (!(entity instanceof ServerPlayer player)) {
            return true;
        }
        ExtraTrialsSavedData data = ExtraTrialsSavedData.get(player.level());
        long now = player.level().getGameTime();
        if (now < data.getCooldown(player.getUUID(), ExtraTrialsSavedData.SPICY_START_FIRE_PROTECTION_END)
                && (source.is(DamageTypeTags.IS_FIRE) || player.isInLava())) {
            player.clearFire();
            return false;
        }
        return true;
    }

    public static float adjustIncomingDamage(ServerPlayer player, DamageSource source, float amount) {
        ExtraTrialsSavedData data = ExtraTrialsSavedData.get(player.level());
        long now = player.level().getGameTime();
        if (now < data.getCooldown(player.getUUID(), ExtraTrialsSavedData.SPICY_START_DAMAGE_PROTECTION_END)
                && !source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            return Math.max(0.0F, amount * 0.35F);
        }
        return amount;
    }

    private static EventResult onLivingHurt(LivingEntity entity, DamageSource source, float amount) {
        if (!(entity instanceof ServerPlayer player) || amount <= 0.0F) {
            return EventResult.pass();
        }
        triggerGlassHeart(player, amount);
        triggerEndermanBlood(player, false);
        return EventResult.pass();
    }

    private static EventResult onLivingDeath(LivingEntity entity, DamageSource source) {
        if (entity instanceof ServerPlayer player) {
            clearPlayerRuntime(player);
        }
        return EventResult.pass();
    }

    private static void onPlayerQuit(ServerPlayer player) {
        clearPlayerRuntime(player);
    }

    private static void tickClosingBorder(ServerPlayer player) {
        MinecraftServer server = player.level().getServer();
        ScavengerSavedData scavengerData = ExtraModifierChecks.getScavengerData(player.level());
        if (scavengerData == null) {
            return;
        }

        if (scavengerData.hasWon()) {
            stopClosingBorderControl(server, ScavengerExtraTrialsConfig.CLOSING_BORDER_RESTORE_AFTER_WIN.get());
            resetRuntimeForAllPlayers(server);
            return;
        }

        if (!ScavengerExtraTrialsConfig.ENABLE_CLOSING_BORDER.get()
                || !ExtraModifiers.id("closing_border").equals(scavengerData.getModifierId())) {
            return;
        }

        setupClosingBorder(server, player, false);
    }

    private static void setupClosingBorder(MinecraftServer server, ServerPlayer anchorPlayer, boolean forceShrinkNow) {
        ServerLevel overworld = server.overworld();
        ExtraTrialsSavedData data = ExtraTrialsSavedData.get(overworld);
        WorldBorder border = overworld.getWorldBorder();
        long now = overworld.getGameTime();

        if (!data.isClosingBorderControlled()) {
            data.saveOriginalClosingBorder(border.getCenterX(), border.getCenterZ(), border.getSize(),
                    border.getWarningBlocks(), border.getWarningTime());
            BlockPos center = anchorPlayer != null && anchorPlayer.level().dimension() == overworld.dimension()
                    ? anchorPlayer.blockPosition()
                    : overworld.getRespawnData().pos();
            border.setCenter(center.getX() + 0.5D, center.getZ() + 0.5D);
            border.setWarningBlocks(24);
            border.setWarningTime(12);
            border.setSize(closingBorderStartSize());
            data.setClosingBorderControlled(true);
            data.setClosingBorderStartGameTime(now);
            data.setClosingBorderFirstPulseSent(false);
            data.setClosingBorderShrinkStarted(false);
            broadcastActionbar(server, Component.translatable("scavenger_extra_trials.actionbar.closing_border.started"));
        }

        long elapsed = now - data.getClosingBorderStartGameTime();
        if (!data.isClosingBorderFirstPulseSent() && elapsed >= closingBorderFirstPulseTicks()) {
            broadcastActionbar(server, Component.translatable("scavenger_extra_trials.actionbar.closing_border.warning"));
            playWarningSound(server);
            data.setClosingBorderFirstPulseSent(true);
        }

        if (forceShrinkNow || (!data.isClosingBorderShrinkStarted() && elapsed >= closingBorderShrinkDelayTicks())) {
            double current = Math.max(border.getSize(), closingBorderMinSize());
            border.lerpSizeBetween(current, closingBorderMinSize(), closingBorderShrinkSeconds() * 20L, now);
            data.setClosingBorderShrinkStarted(true);
            broadcastActionbar(server, Component.translatable("scavenger_extra_trials.actionbar.closing_border.warning"));
            playWarningSound(server);
        }
    }

    public static void stopClosingBorderControl(MinecraftServer server, boolean restoreOriginal) {
        ServerLevel overworld = server.overworld();
        ExtraTrialsSavedData data = ExtraTrialsSavedData.get(overworld);
        if (!data.isClosingBorderControlled()) {
            return;
        }

        WorldBorder border = overworld.getWorldBorder();
        border.setSize(border.getSize());
        if (restoreOriginal && data.isClosingBorderOriginalSaved()) {
            border.setCenter(data.getClosingBorderOriginalCenterX(), data.getClosingBorderOriginalCenterZ());
            border.setWarningBlocks(data.getClosingBorderOriginalWarningBlocks());
            border.setWarningTime(data.getClosingBorderOriginalWarningTime());
            border.setSize(data.getClosingBorderOriginalSize());
        }
        data.clearClosingBorderControl();
    }

    public static void resetRoutePressureForAllPlayers(MinecraftServer server) {
        ExtraTrialsSavedData data = ExtraTrialsSavedData.get(server.overworld());
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            data.resetRoutePressure(player.getUUID());
        }
    }

    public static void resetRuntimeForAllPlayers(MinecraftServer server) {
        ExtraTrialsSavedData data = ExtraTrialsSavedData.get(server.overworld());
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            data.resetPlayerRuntime(player.getUUID());
            removeGlassHeartModifier(player);
        }
    }

    private static void clearPlayerRuntime(ServerPlayer player) {
        ExtraTrialsSavedData.get(player.level()).resetPlayerRuntime(player.getUUID());
        removeGlassHeartModifier(player);
    }

    private static void tickRoutePressure(ServerPlayer player, boolean forced) {
        if (!forced && (!ScavengerExtraTrialsConfig.ENABLE_ROUTE_PRESSURE.get()
                || !ExtraModifierChecks.isActive(player.level(), ExtraModifiers.id("route_pressure")))) {
            return;
        }
        if (!player.isAlive() || player.isDeadOrDying() || player.isChangingDimension()) {
            ExtraTrialsSavedData.get(player.level()).resetRoutePressure(player.getUUID());
            return;
        }

        ExtraTrialsSavedData data = ExtraTrialsSavedData.get(player.level());
        long now = player.level().getGameTime();
        long graceUntil = data.getCooldown(player.getUUID(), ExtraTrialsSavedData.ROUTE_GRACE_UNTIL);
        if (graceUntil == 0L) {
            data.setCooldown(player.getUUID(), ExtraTrialsSavedData.ROUTE_GRACE_UNTIL, now + 40L);
            return;
        }
        if (now < graceUntil) {
            return;
        }

        int chunkX = player.chunkPosition().x;
        int chunkZ = player.chunkPosition().z;
        int storedX = (int) data.getCooldown(player.getUUID(), ExtraTrialsSavedData.ROUTE_CHUNK_X);
        int storedZ = (int) data.getCooldown(player.getUUID(), ExtraTrialsSavedData.ROUTE_CHUNK_Z);
        long ticks = data.getCooldown(player.getUUID(), ExtraTrialsSavedData.ROUTE_TICKS);
        long warningState = data.getCooldown(player.getUUID(), ExtraTrialsSavedData.ROUTE_WARNING_STATE);

        if (storedX == 0L && storedZ == 0L && ticks == 0L) {
            storedX = ROUTE_NO_CHUNK;
        }

        if (storedX == ROUTE_NO_CHUNK || chunkX != storedX || chunkZ != storedZ) {
            if (ScavengerExtraTrialsConfig.ROUTE_PRESSURE_RESET_ON_CHUNK_CHANGE.get()) {
                data.setCooldown(player.getUUID(), ExtraTrialsSavedData.ROUTE_CHUNK_X, chunkX);
                data.setCooldown(player.getUUID(), ExtraTrialsSavedData.ROUTE_CHUNK_Z, chunkZ);
                data.setCooldown(player.getUUID(), ExtraTrialsSavedData.ROUTE_TICKS, 1L);
                data.setCooldown(player.getUUID(), ExtraTrialsSavedData.ROUTE_WARNING_STATE, 0L);
                data.setCooldown(player.getUUID(), ExtraTrialsSavedData.ROUTE_LAST_MESSAGE_TICK, 0L);
                return;
            }
        }

        ticks++;
        data.setCooldown(player.getUUID(), ExtraTrialsSavedData.ROUTE_TICKS, ticks);

        if (ticks >= routePressureMaxTicks()) {
            punishRoutePressure(player, data);
            return;
        }

        if (ticks >= routePressureFinalWarningTicks()) {
            sendThrottledRouteWarning(player, data, now, 2L, Component.translatable("scavenger_extra_trials.actionbar.route_pressure.final_warning"));
        } else if (ticks >= routePressureFirstWarningTicks()) {
            sendThrottledRouteWarning(player, data, now, 1L, Component.translatable("scavenger_extra_trials.actionbar.route_pressure.warning"));
        }
    }

    private static void sendThrottledRouteWarning(ServerPlayer player, ExtraTrialsSavedData data, long now, long stage, Component message) {
        long oldStage = data.getCooldown(player.getUUID(), ExtraTrialsSavedData.ROUTE_WARNING_STATE);
        long lastMessage = data.getCooldown(player.getUUID(), ExtraTrialsSavedData.ROUTE_LAST_MESSAGE_TICK);
        boolean stageChanged = oldStage < stage;
        if (stageChanged || now - lastMessage >= 60L) {
            player.displayClientMessage(message, true);
            data.setCooldown(player.getUUID(), ExtraTrialsSavedData.ROUTE_LAST_MESSAGE_TICK, now);
        }
        if (stageChanged) {
            player.playSound(stage >= 2L ? SoundEvents.WITHER_SPAWN : SoundEvents.PLAYER_LEVELUP, stage >= 2L ? 0.5F : 0.45F, stage >= 2L ? 1.6F : 0.7F);
            data.setCooldown(player.getUUID(), ExtraTrialsSavedData.ROUTE_WARNING_STATE, stage);
        }
    }

    private static void punishRoutePressure(ServerPlayer player, ExtraTrialsSavedData data) {
        player.displayClientMessage(Component.translatable("scavenger_extra_trials.actionbar.route_pressure.punished"), true);
        player.sendSystemMessage(Component.translatable("scavenger_extra_trials.actionbar.route_pressure.punished"));
        if (ScavengerExtraTrialsConfig.ROUTE_PRESSURE_LETHAL.get()) {
            player.hurtServer(player.level(), player.damageSources().genericKill(), Float.MAX_VALUE);
        } else {
            player.hurtServer(player.level(), player.damageSources().magic(), (float) ScavengerExtraTrialsConfig.ROUTE_PRESSURE_DAMAGE_AMOUNT.get().doubleValue());
        }
        data.resetRoutePressure(player.getUUID());
    }

    private static boolean triggerEndermanBlood(ServerPlayer player, boolean forced) {
        if (!forced && (!ScavengerExtraTrialsConfig.ENABLE_ENDERMAN_BLOOD.get()
                || !ExtraModifierChecks.isActive(player.level(), ExtraModifiers.id("enderman_blood")))) {
            return false;
        }
        if (!player.isAlive() || player.isDeadOrDying() || player.isChangingDimension()) {
            return false;
        }
        ExtraTrialsSavedData data = ExtraTrialsSavedData.get(player.level());
        long now = player.level().getGameTime();
        if (!forced && now < data.getCooldown(player.getUUID(), ExtraTrialsSavedData.ENDERMAN_BLOOD_COOLDOWN)) {
            return false;
        }
        if (!forced && player.level().random.nextDouble() > ScavengerExtraTrialsConfig.ENDERMAN_BLOOD_TELEPORT_CHANCE.get()) {
            return false;
        }

        BlockPos safe = findSafeTeleportPosition(player);
        if (safe == null) {
            player.level().playSound(null, player.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 0.35F, 0.45F);
            player.level().sendParticles(ParticleTypes.SMOKE, player.getX(), player.getY() + 1.0D, player.getZ(), 16, 0.25D, 0.45D, 0.25D, 0.02D);
            player.displayClientMessage(Component.translatable("scavenger_extra_trials.actionbar.enderman_blood.failed"), true);
            data.setCooldown(player.getUUID(), ExtraTrialsSavedData.ENDERMAN_BLOOD_COOLDOWN, now + 20L);
            return false;
        }

        Vec3 old = player.position();
        player.level().sendParticles(ParticleTypes.PORTAL, old.x, old.y + 1.0D, old.z, 42, 0.45D, 0.7D, 0.45D, 0.08D);
        player.level().playSound(null, player.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 0.8F, 0.75F);
        dropEndermanBloodEquipment(player);
        boolean teleported = player.teleportTo(player.level(), safe.getX() + 0.5D, safe.getY(), safe.getZ() + 0.5D, Set.<Relative>of(), player.getYRot(), player.getXRot(), true);
        if (teleported) {
            player.level().sendParticles(ParticleTypes.PORTAL, safe.getX() + 0.5D, safe.getY() + 1.0D, safe.getZ() + 0.5D, 42, 0.45D, 0.7D, 0.45D, 0.08D);
            player.level().playSound(null, safe, SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 0.8F, 1.0F);
            player.displayClientMessage(Component.translatable("scavenger_extra_trials.actionbar.enderman_blood.triggered"), true);
        }
        data.setCooldown(player.getUUID(), ExtraTrialsSavedData.ENDERMAN_BLOOD_COOLDOWN,
                now + ScavengerExtraTrialsConfig.ENDERMAN_BLOOD_COOLDOWN_TICKS.get());
        return teleported;
    }

    private static void dropEndermanBloodEquipment(ServerPlayer player) {
        if (ScavengerExtraTrialsConfig.ENDERMAN_BLOOD_DROP_ARMOR.get()) {
            for (EquipmentSlot slot : List.of(EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET)) {
                dropSlot(player, slot);
            }
        }
        if (ScavengerExtraTrialsConfig.ENDERMAN_BLOOD_DROP_MAIN_HAND.get()) {
            dropSlot(player, EquipmentSlot.MAINHAND);
        }
        if (ScavengerExtraTrialsConfig.ENDERMAN_BLOOD_DROP_OFFHAND.get()) {
            dropSlot(player, EquipmentSlot.OFFHAND);
        }
        player.getInventory().setChanged();
    }

    private static void tickHotPotato(ServerPlayer player) {
        if (!ScavengerExtraTrialsConfig.ENABLE_HOT_POTATO.get()
                || !ExtraModifierChecks.isActive(player.level(), ExtraModifiers.id("hot_potato"))
                || !player.isAlive()
                || player.isDeadOrDying()) {
            ExtraTrialsSavedData.get(player.level()).resetHotPotato(player.getUUID());
            return;
        }

        ExtraTrialsSavedData data = ExtraTrialsSavedData.get(player.level());
        boolean trackedAny = false;
        if (ScavengerExtraTrialsConfig.HOT_POTATO_TRACK_MAIN_HAND.get()) {
            trackedAny |= tickHotPotatoHand(player, data, EquipmentSlot.MAINHAND);
        }
        if (ScavengerExtraTrialsConfig.HOT_POTATO_TRACK_OFFHAND.get()) {
            trackedAny |= tickHotPotatoHand(player, data, EquipmentSlot.OFFHAND);
        }
        if (!trackedAny) {
            data.resetHotPotato(player.getUUID());
        }
    }

    private static boolean tickHotPotatoHand(ServerPlayer player, ExtraTrialsSavedData data, EquipmentSlot slot) {
        ItemStack held = player.getItemBySlot(slot);
        String hashKey = slot == EquipmentSlot.MAINHAND ? ExtraTrialsSavedData.HOT_POTATO_MAIN_ITEM_HASH : ExtraTrialsSavedData.HOT_POTATO_OFFHAND_ITEM_HASH;
        String heatKey = slot == EquipmentSlot.MAINHAND ? ExtraTrialsSavedData.HOT_POTATO_MAIN_HEAT_TICKS : ExtraTrialsSavedData.HOT_POTATO_OFFHAND_HEAT_TICKS;
        String warnedKey = slot == EquipmentSlot.MAINHAND ? ExtraTrialsSavedData.HOT_POTATO_MAIN_WARNED : ExtraTrialsSavedData.HOT_POTATO_OFFHAND_WARNED;

        if (held.isEmpty()) {
            data.setCooldown(player.getUUID(), hashKey, 0L);
            data.setCooldown(player.getUUID(), heatKey, 0L);
            data.setCooldown(player.getUUID(), warnedKey, 0L);
            return false;
        }

        long hash = itemHash(held);
        long storedHash = data.getCooldown(player.getUUID(), hashKey);
        long heat = data.getCooldown(player.getUUID(), heatKey);
        if (storedHash != hash && ScavengerExtraTrialsConfig.HOT_POTATO_RESET_ON_HAND_ITEM_CHANGE.get()) {
            data.setCooldown(player.getUUID(), hashKey, hash);
            data.setCooldown(player.getUUID(), heatKey, 1L);
            data.setCooldown(player.getUUID(), warnedKey, 0L);
            return true;
        }
        if (storedHash != hash) {
            data.setCooldown(player.getUUID(), hashKey, hash);
        }

        heat++;
        data.setCooldown(player.getUUID(), heatKey, heat);
        long now = player.level().getGameTime();
        long warned = data.getCooldown(player.getUUID(), warnedKey);
        long lastMessage = data.getCooldown(player.getUUID(), ExtraTrialsSavedData.HOT_POTATO_LAST_MESSAGE_TICK);
        if (heat >= hotPotatoWarningTicks() && warned == 0L && now - lastMessage >= 40L) {
            player.displayClientMessage(Component.translatable(slot == EquipmentSlot.MAINHAND
                    ? "scavenger_extra_trials.actionbar.hot_potato.main_warning"
                    : "scavenger_extra_trials.actionbar.hot_potato.offhand_warning"), true);
            player.playSound(SoundEvents.FIRECHARGE_USE, 0.35F, 1.8F);
            data.setCooldown(player.getUUID(), warnedKey, 1L);
            data.setCooldown(player.getUUID(), ExtraTrialsSavedData.HOT_POTATO_LAST_MESSAGE_TICK, now);
        }

        if (heat >= hotPotatoHeatTicks()) {
            triggerHotPotatoBurn(player, data, slot);
        }
        return true;
    }

    private static void primeHotPotatoHand(ServerPlayer player, ExtraTrialsSavedData data, EquipmentSlot slot) {
        String hashKey = slot == EquipmentSlot.MAINHAND ? ExtraTrialsSavedData.HOT_POTATO_MAIN_ITEM_HASH : ExtraTrialsSavedData.HOT_POTATO_OFFHAND_ITEM_HASH;
        String heatKey = slot == EquipmentSlot.MAINHAND ? ExtraTrialsSavedData.HOT_POTATO_MAIN_HEAT_TICKS : ExtraTrialsSavedData.HOT_POTATO_OFFHAND_HEAT_TICKS;
        String warnedKey = slot == EquipmentSlot.MAINHAND ? ExtraTrialsSavedData.HOT_POTATO_MAIN_WARNED : ExtraTrialsSavedData.HOT_POTATO_OFFHAND_WARNED;
        data.setCooldown(player.getUUID(), hashKey, itemHash(player.getItemBySlot(slot)));
        data.setCooldown(player.getUUID(), heatKey, Math.max(0, hotPotatoHeatTicks() - 20));
        data.setCooldown(player.getUUID(), warnedKey, 1L);
    }

    private static void triggerHotPotatoBurn(ServerPlayer player, ExtraTrialsSavedData data, EquipmentSlot slot) {
        if (player.isInWater()) {
            player.level().sendParticles(ParticleTypes.LARGE_SMOKE, player.getX(), player.getY() + 0.8D, player.getZ(), 24, 0.35D, 0.4D, 0.35D, 0.03D);
            player.level().playSound(null, player.blockPosition(), SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS, 0.8F, 1.2F);
            player.hurtServer(player.level(), player.damageSources().magic(), 2.0F);
        } else if (ScavengerExtraTrialsConfig.HOT_POTATO_LAVA_STYLE_BURN.get()) {
            player.igniteForSeconds(ScavengerExtraTrialsConfig.HOT_POTATO_FIRE_SECONDS.get());
            player.level().sendParticles(ParticleTypes.FLAME, player.getX(), player.getY() + 0.8D, player.getZ(), 28, 0.35D, 0.5D, 0.35D, 0.04D);
            player.level().playSound(null, player.blockPosition(), SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 0.8F, 0.75F);
        } else {
            player.level().sendParticles(ParticleTypes.LARGE_SMOKE, player.getX(), player.getY() + 0.8D, player.getZ(), 18, 0.3D, 0.35D, 0.3D, 0.02D);
            player.level().playSound(null, player.blockPosition(), SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 0.5F, 1.4F);
            player.hurtServer(player.level(), player.damageSources().magic(), 3.0F);
        }
        player.displayClientMessage(Component.translatable("scavenger_extra_trials.actionbar.hot_potato.overheated"), true);
        player.sendSystemMessage(Component.translatable(slot == EquipmentSlot.MAINHAND
                ? "scavenger_extra_trials.actionbar.hot_potato.main_overheated"
                : "scavenger_extra_trials.actionbar.hot_potato.offhand_overheated"));
        data.resetHotPotato(player.getUUID());
    }

    private static void triggerGlassHeart(ServerPlayer player, float amount) {
        if (!ScavengerExtraTrialsConfig.ENABLE_GLASS_HEART.get()
                || !ExtraModifierChecks.isActive(player.level(), ExtraModifiers.id("glass_heart"))
                || amount < ScavengerExtraTrialsConfig.GLASS_HEART_DAMAGE_THRESHOLD.get()) {
            return;
        }
        addGlassHeartStack(player, ExtraTrialsSavedData.get(player.level()));
    }

    private static void tickGlassHeart(ServerPlayer player) {
        ExtraTrialsSavedData data = ExtraTrialsSavedData.get(player.level());
        if (!ScavengerExtraTrialsConfig.ENABLE_GLASS_HEART.get()
                || !ExtraModifierChecks.isActive(player.level(), ExtraModifiers.id("glass_heart"))
                || !player.isAlive()
                || player.isDeadOrDying()) {
            clearGlassHeart(player, data);
            return;
        }

        long stacks = data.getCooldown(player.getUUID(), ExtraTrialsSavedData.GLASS_HEART_STACKS);
        if (stacks <= 0L) {
            removeGlassHeartModifier(player);
            return;
        }

        long now = player.level().getGameTime();
        long expiresAt = data.getCooldown(player.getUUID(), ExtraTrialsSavedData.GLASS_HEART_EXPIRES_AT);
        if (expiresAt > 0L && now >= expiresAt) {
            if (ScavengerExtraTrialsConfig.GLASS_HEART_RECOVER_ONE_STACK_AT_TIME.get() && stacks > 1L) {
                stacks--;
                data.setCooldown(player.getUUID(), ExtraTrialsSavedData.GLASS_HEART_STACKS, stacks);
                data.setCooldown(player.getUUID(), ExtraTrialsSavedData.GLASS_HEART_EXPIRES_AT, now + ScavengerExtraTrialsConfig.GLASS_HEART_DURATION_TICKS.get());
                player.displayClientMessage(Component.translatable("scavenger_extra_trials.actionbar.glass_heart.restored"), true);
            } else {
                clearGlassHeart(player, data);
                player.displayClientMessage(Component.translatable("scavenger_extra_trials.actionbar.glass_heart.restored"), true);
                return;
            }
        }
        applyGlassHeartModifier(player, (int) stacks);
    }

    private static int addGlassHeartStack(ServerPlayer player, ExtraTrialsSavedData data) {
        int maxStacks = ScavengerExtraTrialsConfig.GLASS_HEART_MAX_STACKS.get();
        int stacks = (int) Math.min(maxStacks, data.getCooldown(player.getUUID(), ExtraTrialsSavedData.GLASS_HEART_STACKS) + 1L);
        data.setCooldown(player.getUUID(), ExtraTrialsSavedData.GLASS_HEART_STACKS, stacks);
        data.setCooldown(player.getUUID(), ExtraTrialsSavedData.GLASS_HEART_EXPIRES_AT,
                player.level().getGameTime() + ScavengerExtraTrialsConfig.GLASS_HEART_DURATION_TICKS.get());
        applyGlassHeartModifier(player, stacks);
        player.displayClientMessage(Component.translatable("scavenger_extra_trials.actionbar.glass_heart.cracked"), true);
        player.playSound(SoundEvents.GLASS_BREAK, 0.7F, 0.8F + player.level().random.nextFloat() * 0.4F);
        player.level().sendParticles(ParticleTypes.DAMAGE_INDICATOR, player.getX(), player.getY() + 1.0D, player.getZ(), 10, 0.35D, 0.35D, 0.35D, 0.02D);
        return stacks;
    }

    private static void clearGlassHeart(ServerPlayer player, ExtraTrialsSavedData data) {
        data.resetGlassHeart(player.getUUID());
        removeGlassHeartModifier(player);
    }

    private static void applyGlassHeartModifier(ServerPlayer player, int stacks) {
        AttributeInstance maxHealth = player.getAttribute(Attributes.MAX_HEALTH);
        if (maxHealth == null) {
            return;
        }
        maxHealth.removeModifier(GLASS_HEART_MODIFIER_ID);
        double penalty = -ScavengerExtraTrialsConfig.GLASS_HEART_HEALTH_PENALTY_PER_STACK.get() * Math.max(0, stacks);
        if (penalty != 0.0D) {
            maxHealth.addTransientModifier(new AttributeModifier(GLASS_HEART_MODIFIER_ID, penalty, AttributeModifier.Operation.ADD_VALUE));
        }
        if (player.getHealth() > player.getMaxHealth()) {
            player.setHealth(player.getMaxHealth());
        }
    }

    private static void removeGlassHeartModifier(ServerPlayer player) {
        AttributeInstance maxHealth = player.getAttribute(Attributes.MAX_HEALTH);
        if (maxHealth != null) {
            maxHealth.removeModifier(GLASS_HEART_MODIFIER_ID);
        }
    }

    private static void tickRepellingLoot(ServerPlayer player) {
        if (!ScavengerExtraTrialsConfig.ENABLE_REPELLING_LOOT.get()
                || !ExtraModifierChecks.isActive(player.level(), ExtraModifiers.id("repelling_loot"))
                || !player.isAlive()
                || player.isDeadOrDying()) {
            return;
        }
        double inner = ScavengerExtraTrialsConfig.REPELLING_LOOT_INNER_RADIUS.get();
        double outer = Math.max(inner + 0.1D, ScavengerExtraTrialsConfig.REPELLING_LOOT_OUTER_RADIUS.get());
        AABB area = player.getBoundingBox().inflate(outer);
        int maxItems = Math.max(1, ScavengerExtraTrialsConfig.REPELLING_LOOT_MAX_ITEMS_PER_PLAYER.get());
        int handled = 0;
        int particleBudget = Math.min(12, maxItems);
        for (ItemEntity item : player.level().getEntitiesOfClass(ItemEntity.class, area, ItemEntity::isAlive)) {
            if (handled++ >= maxItems) {
                break;
            }
            if (repelItemFromPlayer(player, item, inner, outer, particleBudget > 0)) {
                particleBudget--;
            }
        }
    }

    private static boolean repelItemFromPlayer(ServerPlayer player, ItemEntity item, double inner, double outer, boolean canSpawnParticles) {
        double dx = item.getX() - player.getX();
        double dz = item.getZ() - player.getZ();
        double distance = Math.sqrt(dx * dx + dz * dz);
        if (distance <= inner || distance > outer) {
            return false;
        }
        double nx = dx / distance;
        double nz = dz / distance;
        if (Math.abs(nx) + Math.abs(nz) < 1.0E-6D) {
            double angle = player.level().random.nextDouble() * Math.PI * 2.0D;
            nx = Math.cos(angle);
            nz = Math.sin(angle);
        }
        double strongRadius = Math.max(Math.max(inner, ScavengerExtraTrialsConfig.REPELLING_LOOT_STRONG_RADIUS.get()), 6.0D);
        boolean close = distance <= strongRadius;
        double t = 1.0D - ((distance - inner) / Math.max(0.1D, outer - inner));
        t = Math.max(0.0D, Math.min(1.0D, t));
        double push = Math.max(ScavengerExtraTrialsConfig.REPELLING_LOOT_STRENGTH.get(), 6.0D) * t * t;
        if (close) {
            push *= Math.max(ScavengerExtraTrialsConfig.REPELLING_LOOT_CLOSE_RANGE_BOOST.get(), 4.20D);
        }
        Vec3 current = item.getDeltaMovement();
        double x = current.x + nx * push * 0.20D;
        double z = current.z + nz * push * 0.20D;
        double currentY = current.y;
        double maxHorizontal = Math.max(ScavengerExtraTrialsConfig.REPELLING_LOOT_MAX_HORIZONTAL_VELOCITY.get(), 3.60D);
        double horizontal = Math.sqrt(x * x + z * z);
        if (maxHorizontal > 0.0D && horizontal > maxHorizontal) {
            double scale = maxHorizontal / horizontal;
            x *= scale;
            z *= scale;
        }
        double y = currentY;
        double verticalBoost = ScavengerExtraTrialsConfig.REPELLING_LOOT_VERTICAL_BOOST.get();
        if (verticalBoost > 0.0D && shouldNudgeRepellingLootUp(item, current)) {
            y = currentY + Math.min(verticalBoost, 0.03D);
        }
        item.setDeltaMovement(x, y, z);
        item.hurtMarked = true;
        if (canSpawnParticles && shouldShowRepellingLootTrail(player, item, close)) {
            double trailX = item.getX() - nx * 0.35D;
            double trailZ = item.getZ() - nz * 0.35D;
            ((ServerLevel) player.level()).sendParticles(close ? ParticleTypes.END_ROD : ParticleTypes.PORTAL,
                    trailX, item.getY() + 0.12D, trailZ, close ? 2 : 1,
                    0.08D, 0.035D, 0.08D, close ? 0.018D : 0.01D);
            return true;
        }
        return false;
    }

    private static boolean shouldNudgeRepellingLootUp(ItemEntity item, Vec3 current) {
        double horizontal = Math.sqrt(current.x * current.x + current.z * current.z);
        return (item.onGround() && horizontal < 0.04D && current.y >= -0.02D)
                || (Math.abs(current.y) < 0.003D && horizontal < 0.01D);
    }

    private static boolean shouldShowRepellingLootTrail(ServerPlayer player, ItemEntity item, boolean close) {
        if (!ScavengerExtraTrialsConfig.REPELLING_LOOT_PARTICLES.get()) {
            return false;
        }
        long baseInterval = Math.max(1L, ScavengerExtraTrialsConfig.REPELLING_LOOT_PARTICLE_INTERVAL_TICKS.get());
        long interval = close ? baseInterval : baseInterval + 3L;
        return (player.level().getGameTime() + item.getId()) % interval == 0L;
    }

    private static void tickSpicyStart(ServerPlayer player) {
        ExtraTrialsSavedData data = ExtraTrialsSavedData.get(player.level());
        if (!ScavengerExtraTrialsConfig.ENABLE_SPICY_START.get()
                || !ExtraModifierChecks.isActive(player.level(), ExtraModifiers.id("spicy_start"))
                || !player.isAlive()
                || player.isDeadOrDying()) {
            data.setCooldown(player.getUUID(), ExtraTrialsSavedData.SPICY_START_LAST_DIMENSION, dimensionCode(player.level()));
            return;
        }

        long lastDimension = data.getCooldown(player.getUUID(), ExtraTrialsSavedData.SPICY_START_LAST_DIMENSION);
        if (lastDimension == 2L && player.level().dimension() != Level.NETHER
                && ScavengerExtraTrialsConfig.SPICY_START_PORTAL_GLITCH_ON_LEAVING_NETHER.get()) {
            startPortalSickness(player, data, false);
        }

        if (!ScavengerExtraTrialsConfig.SPICY_START_TELEPORT_ONCE_PER_RUN.get()
                || data.getCooldown(player.getUUID(), ExtraTrialsSavedData.SPICY_START_TELEPORTED) == 0L) {
            tryStartSpicyStart(player, false);
        }

        tickPortalSickness(player, data);
        data.setCooldown(player.getUUID(), ExtraTrialsSavedData.SPICY_START_LAST_DIMENSION, dimensionCode(player.level()));
    }

    private static boolean tryStartSpicyStart(ServerPlayer player, boolean forced) {
        if (!forced && player.level().dimension() == Level.NETHER) {
            ExtraTrialsSavedData data = ExtraTrialsSavedData.get(player.level());
            if (data.getCooldown(player.getUUID(), ExtraTrialsSavedData.SPICY_START_TELEPORTED) == 0L) {
                data.setCooldown(player.getUUID(), ExtraTrialsSavedData.SPICY_START_TELEPORTED, 1L);
                primeSpicyStartProtection(player, data, player.level().getGameTime());
            }
            return false;
        }

        ServerLevel nether = player.level().getServer().getLevel(Level.NETHER);
        if (nether == null) {
            player.sendSystemMessage(Component.translatable("scavenger_extra_trials.actionbar.spicy_start.failed"));
            return false;
        }

        BlockPos center = new BlockPos(player.getBlockX() / 8, 64, player.getBlockZ() / 8);
        BlockPos safe = findSafeTeleportPosition(nether, center,
                ScavengerExtraTrialsConfig.SPICY_START_SAFE_SEARCH_RADIUS.get(),
                ScavengerExtraTrialsConfig.SPICY_START_MAX_SAFE_POSITION_ATTEMPTS.get());
        if (safe == null) {
            player.sendSystemMessage(Component.translatable("scavenger_extra_trials.actionbar.spicy_start.failed"));
            return false;
        }

        ExtraTrialsSavedData data = ExtraTrialsSavedData.get(player.level());
        data.setCooldown(player.getUUID(), ExtraTrialsSavedData.SPICY_START_TELEPORTED, 1L);
        boolean teleported = player.teleportTo(nether, safe.getX() + 0.5D, safe.getY(), safe.getZ() + 0.5D,
                Set.<Relative>of(), player.getYRot(), player.getXRot(), true);
        if (teleported) {
            ExtraTrialsSavedData netherData = ExtraTrialsSavedData.get(nether);
            primeSpicyStartProtection(player, netherData, nether.getGameTime());
            netherData.setCooldown(player.getUUID(), ExtraTrialsSavedData.SPICY_START_LAST_DIMENSION, 2L);
            nether.sendParticles(ParticleTypes.FLAME, safe.getX() + 0.5D, safe.getY() + 0.6D, safe.getZ() + 0.5D,
                    24, 0.35D, 0.4D, 0.35D, 0.02D);
            nether.playSound(null, safe, SoundEvents.PORTAL_TRAVEL, SoundSource.PLAYERS, 0.55F, 1.25F);
            player.displayClientMessage(Component.translatable("scavenger_extra_trials.actionbar.spicy_start.started"), true);
        }
        return teleported;
    }

    private static void primeSpicyStartProtection(ServerPlayer player, ExtraTrialsSavedData data, long now) {
        data.setCooldown(player.getUUID(), ExtraTrialsSavedData.SPICY_START_FIRE_PROTECTION_END,
                now + ScavengerExtraTrialsConfig.SPICY_START_FIRE_PROTECTION_TICKS.get());
        data.setCooldown(player.getUUID(), ExtraTrialsSavedData.SPICY_START_DAMAGE_PROTECTION_END,
                now + ScavengerExtraTrialsConfig.SPICY_START_DAMAGE_PROTECTION_TICKS.get());
        data.setCooldown(player.getUUID(), ExtraTrialsSavedData.SPICY_START_LAST_MESSAGE_TICK, now);
    }

    private static void startPortalSickness(ServerPlayer player, ExtraTrialsSavedData data, boolean forced) {
        long now = player.level().getGameTime();
        if (!forced && now < data.getCooldown(player.getUUID(), ExtraTrialsSavedData.SPICY_START_PORTAL_GLITCH_COOLDOWN)) {
            return;
        }
        data.setCooldown(player.getUUID(), ExtraTrialsSavedData.SPICY_START_PORTAL_SICKNESS_END,
                now + ScavengerExtraTrialsConfig.SPICY_START_PORTAL_SICKNESS_TICKS.get());
        data.setCooldown(player.getUUID(), ExtraTrialsSavedData.SPICY_START_PORTAL_GLITCH_COOLDOWN, now + 300L);
        data.setCooldown(player.getUUID(), ExtraTrialsSavedData.SPICY_START_LAST_MESSAGE_TICK, now);
        player.level().playSound(null, player.blockPosition(), SoundEvents.PORTAL_TRIGGER, SoundSource.PLAYERS, 0.7F, 0.65F);
        player.level().sendParticles(ParticleTypes.PORTAL, player.getX(), player.getY() + 1.0D, player.getZ(),
                36, 0.35D, 0.55D, 0.35D, 0.07D);
        player.displayClientMessage(Component.translatable("scavenger_extra_trials.actionbar.spicy_start.portal_sickness"), true);
    }

    private static void tickPortalSickness(ServerPlayer player, ExtraTrialsSavedData data) {
        long now = player.level().getGameTime();
        if (now >= data.getCooldown(player.getUUID(), ExtraTrialsSavedData.SPICY_START_PORTAL_SICKNESS_END)) {
            return;
        }
        if (now % 8L == 0L) {
            double sideways = (player.level().random.nextDouble() - 0.5D) * 0.16D;
            double forward = (player.level().random.nextDouble() - 0.5D) * 0.08D;
            player.push(sideways, 0.0D, forward);
        }
        if (now % 12L == 0L) {
            player.level().sendParticles(ParticleTypes.REVERSE_PORTAL, player.getX(), player.getY() + 0.9D, player.getZ(),
                    5, 0.25D, 0.35D, 0.25D, 0.02D);
        }
    }

    private static long dimensionCode(ServerLevel level) {
        if (level.dimension() == Level.NETHER) {
            return 2L;
        }
        if (level.dimension() == Level.END) {
            return 3L;
        }
        return 1L;
    }

    private static boolean dropSlot(ServerPlayer player, EquipmentSlot slot) {
        ItemStack stack = player.getItemBySlot(slot);
        if (stack.isEmpty()) {
            return false;
        }
        ItemStack dropped = stack.copy();
        player.setItemSlot(slot, ItemStack.EMPTY);
        ItemEntity entity = player.drop(dropped, false, false);
        if (entity != null) {
            entity.setDeltaMovement((player.level().random.nextDouble() - 0.5D) * 0.22D, 0.16D,
                    (player.level().random.nextDouble() - 0.5D) * 0.22D);
        }
        player.getInventory().setChanged();
        return true;
    }

    private static BlockPos findSafeTeleportPosition(ServerPlayer player) {
        ServerLevel level = player.level();
        int min = Math.max(20, ScavengerExtraTrialsConfig.ENDERMAN_BLOOD_MIN_TELEPORT_DISTANCE.get());
        int max = Math.max(min + 1, Math.max(48, ScavengerExtraTrialsConfig.ENDERMAN_BLOOD_MAX_TELEPORT_DISTANCE.get()));
        BlockPos origin = player.blockPosition();
        for (int attempt = 0; attempt < 36; attempt++) {
            double angle = level.random.nextDouble() * Math.PI * 2.0D;
            int distance = min + level.random.nextInt(max - min + 1);
            int x = origin.getX() + (int) Math.round(Math.cos(angle) * distance);
            int z = origin.getZ() + (int) Math.round(Math.sin(angle) * distance);
            int yStart = origin.getY() + 8 - level.random.nextInt(6);
            for (int dy = 0; dy < 18; dy++) {
                BlockPos pos = new BlockPos(x, yStart - dy, z);
                if (isSafeStandPosition(level, pos)) {
                    return pos;
                }
            }
        }
        return null;
    }

    private static BlockPos findSafeTeleportPosition(ServerLevel level, BlockPos center, int radius, int attempts) {
        int safeRadius = Math.max(1, radius);
        int safeAttempts = Math.max(1, attempts);
        for (int attempt = 0; attempt < safeAttempts; attempt++) {
            int dx = level.random.nextInt(safeRadius * 2 + 1) - safeRadius;
            int dz = level.random.nextInt(safeRadius * 2 + 1) - safeRadius;
            BlockPos probe = center.offset(dx, 0, dz);
            BlockPos pos = level.dimension() == Level.NETHER
                    ? findNetherFloor(level, probe)
                    : level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, probe);
            if (pos != null && isSafeStandPosition(level, pos)) {
                return pos;
            }
        }
        return null;
    }

    private static BlockPos findNetherFloor(ServerLevel level, BlockPos probe) {
        int min = Math.max(level.getMinY() + 4, 32);
        int max = Math.min(level.getMaxY() - 3, 118);
        for (int y = max; y >= min; y--) {
            BlockPos pos = new BlockPos(probe.getX(), y, probe.getZ());
            if (isSafeStandPosition(level, pos)) {
                return pos;
            }
        }
        return null;
    }

    private static boolean isSafeStandPosition(ServerLevel level, BlockPos pos) {
        if (pos.getY() <= level.getMinY() + 3 || pos.getY() >= level.getMaxY() - 2) {
            return false;
        }
        BlockPos below = pos.below();
        return level.getBlockState(below).isSolid()
                && !level.getFluidState(below).is(FluidTags.LAVA)
                && level.getFluidState(pos).isEmpty()
                && level.getFluidState(pos.above()).isEmpty()
                && level.noCollision(new AABB(pos))
                && level.noCollision(new AABB(pos.above()));
    }

    private static long itemHash(ItemStack stack) {
        Identifier id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        return id == null ? 0L : (long) id.hashCode() * 31L + stack.getCount();
    }

    private static void broadcastActionbar(MinecraftServer server, Component message) {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            player.displayClientMessage(message, true);
        }
    }

    private static void playWarningSound(MinecraftServer server) {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            player.playSound(SoundEvents.WITHER_SPAWN, 0.5F, 1.8F);
        }
    }

    private static int closingBorderStartSize() {
        return ScavengerExtraTrialsConfig.FAST_TESTING_DEFAULTS.get() ? 1536 : Math.max(2048, ScavengerExtraTrialsConfig.CLOSING_BORDER_START_SIZE.get());
    }

    private static int closingBorderMinSize() {
        return ScavengerExtraTrialsConfig.FAST_TESTING_DEFAULTS.get() ? 1024 : Math.max(1024, ScavengerExtraTrialsConfig.CLOSING_BORDER_MIN_SIZE.get());
    }

    private static int closingBorderFirstPulseTicks() {
        return ScavengerExtraTrialsConfig.CLOSING_BORDER_FIRST_PULSE_TICKS.get();
    }

    private static int closingBorderShrinkDelayTicks() {
        return ScavengerExtraTrialsConfig.FAST_TESTING_DEFAULTS.get() ? 180 : Math.max(300, ScavengerExtraTrialsConfig.CLOSING_BORDER_SHRINK_DELAY_TICKS.get());
    }

    private static int closingBorderShrinkSeconds() {
        return ScavengerExtraTrialsConfig.FAST_TESTING_DEFAULTS.get() ? 1800 : Math.max(3000, ScavengerExtraTrialsConfig.CLOSING_BORDER_SHRINK_SECONDS.get());
    }

    private static int routePressureMaxTicks() {
        return ScavengerExtraTrialsConfig.ROUTE_PRESSURE_MAX_TICKS_IN_CHUNK.get();
    }

    private static int routePressureFirstWarningTicks() {
        return ScavengerExtraTrialsConfig.ROUTE_PRESSURE_FIRST_WARNING_TICKS.get();
    }

    private static int routePressureFinalWarningTicks() {
        return ScavengerExtraTrialsConfig.ROUTE_PRESSURE_FINAL_WARNING_TICKS.get();
    }

    private static int hotPotatoWarningTicks() {
        return ScavengerExtraTrialsConfig.HOT_POTATO_WARNING_TICKS.get();
    }

    private static int hotPotatoHeatTicks() {
        return ScavengerExtraTrialsConfig.HOT_POTATO_HEAT_TICKS.get();
    }
}
