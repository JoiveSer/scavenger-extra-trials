package com.joive.scavengerextratrials.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class ScavengerExtraTrialsConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.BooleanValue FAST_TESTING_DEFAULTS = BUILDER.define("fastTestingDefaults", false);

    public static final ModConfigSpec.BooleanValue ENABLE_CLOSING_BORDER = BUILDER.define("enableClosingBorder", true);
    public static final ModConfigSpec.BooleanValue ENABLE_ROUTE_PRESSURE = BUILDER.define("enableRoutePressure", true);
    public static final ModConfigSpec.BooleanValue ENABLE_GLASS_HEART = BUILDER.define("enableGlassHeart", true);
    public static final ModConfigSpec.BooleanValue ENABLE_UPSIDE_DOWN = BUILDER.define("enableUpsideDown", true);
    public static final ModConfigSpec.BooleanValue ENABLE_ENDERMAN_BLOOD = BUILDER.define("enableEndermanBlood", true);
    public static final ModConfigSpec.BooleanValue ENABLE_HOT_POTATO = BUILDER.define("enableHotPotato", true);
    public static final ModConfigSpec.BooleanValue ENABLE_REPELLING_LOOT = BUILDER.define("enableRepellingLoot", true);
    public static final ModConfigSpec.BooleanValue ENABLE_SPICY_START = BUILDER.define("enableSpicyStart", true);

    public static final ModConfigSpec.IntValue CLOSING_BORDER_START_SIZE = BUILDER.defineInRange("closingBorderStartSize", 2048, 1, 60000000);
    public static final ModConfigSpec.IntValue CLOSING_BORDER_MIN_SIZE = BUILDER.defineInRange("closingBorderMinSize", 1024, 1, 60000000);
    public static final ModConfigSpec.IntValue CLOSING_BORDER_FIRST_PULSE_TICKS = BUILDER.defineInRange("closingBorderFirstPulseTicks", 80, 0, Integer.MAX_VALUE);
    public static final ModConfigSpec.IntValue CLOSING_BORDER_SHRINK_DELAY_TICKS = BUILDER.defineInRange("closingBorderShrinkDelayTicks", 300, 0, Integer.MAX_VALUE);
    public static final ModConfigSpec.IntValue CLOSING_BORDER_SHRINK_SECONDS = BUILDER.defineInRange("closingBorderShrinkSeconds", 3000, 1, Integer.MAX_VALUE);
    public static final ModConfigSpec.BooleanValue CLOSING_BORDER_RESTORE_AFTER_WIN = BUILDER.define("closingBorderRestoreAfterWin", true);

    public static final ModConfigSpec.IntValue ROUTE_PRESSURE_MAX_TICKS_IN_CHUNK = BUILDER.defineInRange("routePressureMaxTicksInChunk", 300, 1, Integer.MAX_VALUE);
    public static final ModConfigSpec.IntValue ROUTE_PRESSURE_FIRST_WARNING_TICKS = BUILDER.defineInRange("routePressureFirstWarningTicks", 160, 1, Integer.MAX_VALUE);
    public static final ModConfigSpec.IntValue ROUTE_PRESSURE_FINAL_WARNING_TICKS = BUILDER.defineInRange("routePressureFinalWarningTicks", 240, 1, Integer.MAX_VALUE);
    public static final ModConfigSpec.BooleanValue ROUTE_PRESSURE_LETHAL = BUILDER.define("routePressureLethal", true);
    public static final ModConfigSpec.DoubleValue ROUTE_PRESSURE_DAMAGE_AMOUNT = BUILDER.defineInRange("routePressureDamageAmount", 1000.0D, 0.0D, Double.MAX_VALUE);
    public static final ModConfigSpec.BooleanValue ROUTE_PRESSURE_RESET_ON_CHUNK_CHANGE = BUILDER.define("routePressureResetOnChunkChange", true);

    public static final ModConfigSpec.IntValue GLASS_HEART_MAX_STACKS = BUILDER.defineInRange("glassHeartMaxStacks", 3, 1, 64);
    public static final ModConfigSpec.IntValue GLASS_HEART_DURATION_TICKS = BUILDER.defineInRange("glassHeartDurationTicks", 600, 1, Integer.MAX_VALUE);
    public static final ModConfigSpec.DoubleValue GLASS_HEART_DAMAGE_THRESHOLD = BUILDER.defineInRange("glassHeartDamageThreshold", 6.0D, 0.0D, Double.MAX_VALUE);
    public static final ModConfigSpec.DoubleValue GLASS_HEART_HEALTH_PENALTY_PER_STACK = BUILDER.defineInRange("glassHeartHealthPenaltyPerStack", 2.0D, 0.0D, 1024.0D);
    public static final ModConfigSpec.BooleanValue GLASS_HEART_RECOVER_ONE_STACK_AT_TIME = BUILDER.define("glassHeartRecoverOneStackAtTime", true);

    public static final ModConfigSpec.IntValue UPSIDE_DOWN_ROLL_DEGREES = BUILDER.defineInRange("upsideDownRollDegrees", 180, 0, 180);
    public static final ModConfigSpec.IntValue UPSIDE_DOWN_SMOOTH_TICKS = BUILDER.defineInRange("upsideDownSmoothTicks", 10, 1, Integer.MAX_VALUE);
    public static final ModConfigSpec.IntValue UPSIDE_DOWN_FIRST_DELAY_TICKS = BUILDER.defineInRange("upsideDownFirstDelayTicks", 60, 0, Integer.MAX_VALUE);

    public static final ModConfigSpec.DoubleValue ENDERMAN_BLOOD_TELEPORT_CHANCE = BUILDER.defineInRange("endermanBloodTeleportChance", 1.0D, 0.0D, 1.0D);
    public static final ModConfigSpec.IntValue ENDERMAN_BLOOD_COOLDOWN_TICKS = BUILDER.defineInRange("endermanBloodCooldownTicks", 60, 0, Integer.MAX_VALUE);
    public static final ModConfigSpec.IntValue ENDERMAN_BLOOD_MIN_TELEPORT_DISTANCE = BUILDER.defineInRange("endermanBloodMinTeleportDistance", 20, 1, 256);
    public static final ModConfigSpec.IntValue ENDERMAN_BLOOD_MAX_TELEPORT_DISTANCE = BUILDER.defineInRange("endermanBloodMaxTeleportDistance", 48, 1, 512);
    public static final ModConfigSpec.BooleanValue ENDERMAN_BLOOD_DROP_ARMOR = BUILDER.define("endermanBloodDropArmor", true);
    public static final ModConfigSpec.BooleanValue ENDERMAN_BLOOD_DROP_MAIN_HAND = BUILDER.define("endermanBloodDropMainHand", true);
    public static final ModConfigSpec.BooleanValue ENDERMAN_BLOOD_DROP_OFFHAND = BUILDER.define("endermanBloodDropOffhand", true);

    public static final ModConfigSpec.IntValue HOT_POTATO_HEAT_TICKS = BUILDER.defineInRange("hotPotatoHeatTicks", 60, 1, Integer.MAX_VALUE);
    public static final ModConfigSpec.IntValue HOT_POTATO_WARNING_TICKS = BUILDER.defineInRange("hotPotatoWarningTicks", 25, 1, Integer.MAX_VALUE);
    public static final ModConfigSpec.IntValue HOT_POTATO_FIRE_SECONDS = BUILDER.defineInRange("hotPotatoFireSeconds", 5, 0, Integer.MAX_VALUE);
    public static final ModConfigSpec.BooleanValue HOT_POTATO_LAVA_STYLE_BURN = BUILDER.define("hotPotatoLavaStyleBurn", true);
    public static final ModConfigSpec.BooleanValue HOT_POTATO_TRACK_MAIN_HAND = BUILDER.define("hotPotatoTrackMainHand", true);
    public static final ModConfigSpec.BooleanValue HOT_POTATO_TRACK_OFFHAND = BUILDER.define("hotPotatoTrackOffhand", true);
    public static final ModConfigSpec.BooleanValue HOT_POTATO_RESET_ON_HAND_ITEM_CHANGE = BUILDER.define("hotPotatoResetOnHandItemChange", true);

    public static final ModConfigSpec.DoubleValue REPELLING_LOOT_INNER_RADIUS = BUILDER.defineInRange("repellingLootInnerRadius", 1.05D, 0.0D, Double.MAX_VALUE);
    public static final ModConfigSpec.DoubleValue REPELLING_LOOT_OUTER_RADIUS = BUILDER.defineInRange("repellingLootOuterRadius", 12.0D, 0.0D, Double.MAX_VALUE);
    public static final ModConfigSpec.DoubleValue REPELLING_LOOT_STRONG_RADIUS = BUILDER.defineInRange("repellingLootStrongRadius", 6.0D, 0.0D, Double.MAX_VALUE);
    public static final ModConfigSpec.DoubleValue REPELLING_LOOT_STRENGTH = BUILDER.defineInRange("repellingLootStrength", 6.0D, 0.0D, Double.MAX_VALUE);
    public static final ModConfigSpec.DoubleValue REPELLING_LOOT_CLOSE_RANGE_BOOST = BUILDER.defineInRange("repellingLootCloseRangeBoost", 4.20D, 0.0D, Double.MAX_VALUE);
    public static final ModConfigSpec.DoubleValue REPELLING_LOOT_MAX_HORIZONTAL_VELOCITY = BUILDER.defineInRange("repellingLootMaxHorizontalVelocity", 3.60D, 0.0D, Double.MAX_VALUE);
    public static final ModConfigSpec.DoubleValue REPELLING_LOOT_VERTICAL_BOOST = BUILDER.defineInRange("repellingLootVerticalBoost", 0.03D, 0.0D, Double.MAX_VALUE);
    public static final ModConfigSpec.IntValue REPELLING_LOOT_TICK_INTERVAL = BUILDER.defineInRange("repellingLootTickInterval", 1, 1, Integer.MAX_VALUE);
    public static final ModConfigSpec.BooleanValue REPELLING_LOOT_PARTICLES = BUILDER.define("repellingLootParticles", true);
    public static final ModConfigSpec.IntValue REPELLING_LOOT_PARTICLE_INTERVAL_TICKS = BUILDER.defineInRange("repellingLootParticleIntervalTicks", 2, 1, Integer.MAX_VALUE);
    public static final ModConfigSpec.IntValue REPELLING_LOOT_MAX_ITEMS_PER_PLAYER = BUILDER.defineInRange("repellingLootMaxItemsPerPlayer", 32, 1, Integer.MAX_VALUE);

    public static final ModConfigSpec.BooleanValue SPICY_START_TELEPORT_ONCE_PER_RUN = BUILDER.define("spicyStartTeleportOncePerRun", true);
    public static final ModConfigSpec.IntValue SPICY_START_FIRE_PROTECTION_TICKS = BUILDER.defineInRange("spicyStartFireProtectionTicks", 600, 0, Integer.MAX_VALUE);
    public static final ModConfigSpec.IntValue SPICY_START_DAMAGE_PROTECTION_TICKS = BUILDER.defineInRange("spicyStartDamageProtectionTicks", 200, 0, Integer.MAX_VALUE);
    public static final ModConfigSpec.IntValue SPICY_START_SAFE_SEARCH_RADIUS = BUILDER.defineInRange("spicyStartSafeSearchRadius", 96, 1, 4096);
    public static final ModConfigSpec.IntValue SPICY_START_MAX_SAFE_POSITION_ATTEMPTS = BUILDER.defineInRange("spicyStartMaxSafePositionAttempts", 96, 1, 4096);
    public static final ModConfigSpec.IntValue SPICY_START_PORTAL_SICKNESS_TICKS = BUILDER.defineInRange("spicyStartPortalSicknessTicks", 220, 0, Integer.MAX_VALUE);
    public static final ModConfigSpec.BooleanValue SPICY_START_PORTAL_GLITCH_ON_LEAVING_NETHER = BUILDER.define("spicyStartPortalGlitchOnLeavingNether", true);

    public static final ModConfigSpec SPEC = BUILDER.build();

    private ScavengerExtraTrialsConfig() {
    }
}
