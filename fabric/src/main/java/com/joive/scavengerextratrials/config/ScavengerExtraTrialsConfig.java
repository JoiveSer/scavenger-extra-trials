package com.joive.scavengerextratrials.config;

public final class ScavengerExtraTrialsConfig {
    public static final Value<Boolean> FAST_TESTING_DEFAULTS = new Value<>(false);

    public static final Value<Boolean> ENABLE_CLOSING_BORDER = new Value<>(true);
    public static final Value<Boolean> ENABLE_ROUTE_PRESSURE = new Value<>(true);
    public static final Value<Boolean> ENABLE_GLASS_HEART = new Value<>(true);
    public static final Value<Boolean> ENABLE_UPSIDE_DOWN = new Value<>(true);
    public static final Value<Boolean> ENABLE_ENDERMAN_BLOOD = new Value<>(true);
    public static final Value<Boolean> ENABLE_HOT_POTATO = new Value<>(true);
    public static final Value<Boolean> ENABLE_REPELLING_LOOT = new Value<>(true);
    public static final Value<Boolean> ENABLE_SPICY_START = new Value<>(true);

    public static final Value<Integer> CLOSING_BORDER_START_SIZE = new Value<>(2048);
    public static final Value<Integer> CLOSING_BORDER_MIN_SIZE = new Value<>(1024);
    public static final Value<Integer> CLOSING_BORDER_FIRST_PULSE_TICKS = new Value<>(80);
    public static final Value<Integer> CLOSING_BORDER_SHRINK_DELAY_TICKS = new Value<>(300);
    public static final Value<Integer> CLOSING_BORDER_SHRINK_SECONDS = new Value<>(3000);
    public static final Value<Boolean> CLOSING_BORDER_RESTORE_AFTER_WIN = new Value<>(true);

    public static final Value<Integer> ROUTE_PRESSURE_MAX_TICKS_IN_CHUNK = new Value<>(300);
    public static final Value<Integer> ROUTE_PRESSURE_FIRST_WARNING_TICKS = new Value<>(160);
    public static final Value<Integer> ROUTE_PRESSURE_FINAL_WARNING_TICKS = new Value<>(240);
    public static final Value<Boolean> ROUTE_PRESSURE_LETHAL = new Value<>(true);
    public static final Value<Double> ROUTE_PRESSURE_DAMAGE_AMOUNT = new Value<>(1000.0D);
    public static final Value<Boolean> ROUTE_PRESSURE_RESET_ON_CHUNK_CHANGE = new Value<>(true);

    public static final Value<Integer> GLASS_HEART_MAX_STACKS = new Value<>(3);
    public static final Value<Integer> GLASS_HEART_DURATION_TICKS = new Value<>(600);
    public static final Value<Double> GLASS_HEART_DAMAGE_THRESHOLD = new Value<>(6.0D);
    public static final Value<Double> GLASS_HEART_HEALTH_PENALTY_PER_STACK = new Value<>(2.0D);
    public static final Value<Boolean> GLASS_HEART_RECOVER_ONE_STACK_AT_TIME = new Value<>(true);

    public static final Value<Integer> UPSIDE_DOWN_ROLL_DEGREES = new Value<>(180);
    public static final Value<Integer> UPSIDE_DOWN_SMOOTH_TICKS = new Value<>(10);
    public static final Value<Integer> UPSIDE_DOWN_FIRST_DELAY_TICKS = new Value<>(60);

    public static final Value<Double> ENDERMAN_BLOOD_TELEPORT_CHANCE = new Value<>(1.0D);
    public static final Value<Integer> ENDERMAN_BLOOD_COOLDOWN_TICKS = new Value<>(60);
    public static final Value<Integer> ENDERMAN_BLOOD_MIN_TELEPORT_DISTANCE = new Value<>(20);
    public static final Value<Integer> ENDERMAN_BLOOD_MAX_TELEPORT_DISTANCE = new Value<>(48);
    public static final Value<Boolean> ENDERMAN_BLOOD_DROP_ARMOR = new Value<>(true);
    public static final Value<Boolean> ENDERMAN_BLOOD_DROP_MAIN_HAND = new Value<>(true);
    public static final Value<Boolean> ENDERMAN_BLOOD_DROP_OFFHAND = new Value<>(true);

    public static final Value<Integer> HOT_POTATO_HEAT_TICKS = new Value<>(60);
    public static final Value<Integer> HOT_POTATO_WARNING_TICKS = new Value<>(25);
    public static final Value<Integer> HOT_POTATO_FIRE_SECONDS = new Value<>(5);
    public static final Value<Boolean> HOT_POTATO_LAVA_STYLE_BURN = new Value<>(true);
    public static final Value<Boolean> HOT_POTATO_TRACK_MAIN_HAND = new Value<>(true);
    public static final Value<Boolean> HOT_POTATO_TRACK_OFFHAND = new Value<>(true);
    public static final Value<Boolean> HOT_POTATO_RESET_ON_HAND_ITEM_CHANGE = new Value<>(true);

    public static final Value<Double> REPELLING_LOOT_INNER_RADIUS = new Value<>(1.05D);
    public static final Value<Double> REPELLING_LOOT_OUTER_RADIUS = new Value<>(12.0D);
    public static final Value<Double> REPELLING_LOOT_STRONG_RADIUS = new Value<>(6.0D);
    public static final Value<Double> REPELLING_LOOT_STRENGTH = new Value<>(6.0D);
    public static final Value<Double> REPELLING_LOOT_CLOSE_RANGE_BOOST = new Value<>(4.20D);
    public static final Value<Double> REPELLING_LOOT_MAX_HORIZONTAL_VELOCITY = new Value<>(3.60D);
    public static final Value<Double> REPELLING_LOOT_VERTICAL_BOOST = new Value<>(0.03D);
    public static final Value<Integer> REPELLING_LOOT_TICK_INTERVAL = new Value<>(1);
    public static final Value<Boolean> REPELLING_LOOT_PARTICLES = new Value<>(true);
    public static final Value<Integer> REPELLING_LOOT_PARTICLE_INTERVAL_TICKS = new Value<>(2);
    public static final Value<Integer> REPELLING_LOOT_MAX_ITEMS_PER_PLAYER = new Value<>(32);

    public static final Value<Boolean> SPICY_START_TELEPORT_ONCE_PER_RUN = new Value<>(true);
    public static final Value<Integer> SPICY_START_FIRE_PROTECTION_TICKS = new Value<>(600);
    public static final Value<Integer> SPICY_START_DAMAGE_PROTECTION_TICKS = new Value<>(200);
    public static final Value<Integer> SPICY_START_SAFE_SEARCH_RADIUS = new Value<>(96);
    public static final Value<Integer> SPICY_START_MAX_SAFE_POSITION_ATTEMPTS = new Value<>(96);
    public static final Value<Integer> SPICY_START_PORTAL_SICKNESS_TICKS = new Value<>(220);
    public static final Value<Boolean> SPICY_START_PORTAL_GLITCH_ON_LEAVING_NETHER = new Value<>(true);

    private ScavengerExtraTrialsConfig() {
    }

    public record Value<T>(T get) {
    }
}
