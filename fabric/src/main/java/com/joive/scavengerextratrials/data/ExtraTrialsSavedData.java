package com.joive.scavengerextratrials.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class ExtraTrialsSavedData extends SavedData {
    public static final String ROUTE_CHUNK_X = "route_chunk_x";
    public static final String ROUTE_CHUNK_Z = "route_chunk_z";
    public static final String ROUTE_TICKS = "route_ticks";
    public static final String ROUTE_WARNING_STATE = "route_warning_state";
    public static final String ROUTE_GRACE_UNTIL = "route_grace_until";
    public static final String ROUTE_LAST_MESSAGE_TICK = "route_last_message_tick";
    public static final String ENDERMAN_BLOOD_COOLDOWN = "enderman_blood_cooldown";
    public static final String HOT_POTATO_MAIN_ITEM_HASH = "hot_potato_main_item_hash";
    public static final String HOT_POTATO_MAIN_HEAT_TICKS = "hot_potato_main_heat_ticks";
    public static final String HOT_POTATO_MAIN_WARNED = "hot_potato_main_warned";
    public static final String HOT_POTATO_OFFHAND_ITEM_HASH = "hot_potato_offhand_item_hash";
    public static final String HOT_POTATO_OFFHAND_HEAT_TICKS = "hot_potato_offhand_heat_ticks";
    public static final String HOT_POTATO_OFFHAND_WARNED = "hot_potato_offhand_warned";
    public static final String HOT_POTATO_LAST_MESSAGE_TICK = "hot_potato_last_message_tick";
    public static final String GLASS_HEART_STACKS = "glass_heart_stacks";
    public static final String GLASS_HEART_EXPIRES_AT = "glass_heart_expires_at";
    public static final String SPICY_START_TELEPORTED = "spicy_start_teleported";
    public static final String SPICY_START_FIRE_PROTECTION_END = "spicy_start_fire_protection_end";
    public static final String SPICY_START_DAMAGE_PROTECTION_END = "spicy_start_damage_protection_end";
    public static final String SPICY_START_PORTAL_SICKNESS_END = "spicy_start_portal_sickness_end";
    public static final String SPICY_START_LAST_DIMENSION = "spicy_start_last_dimension";
    public static final String SPICY_START_PORTAL_GLITCH_COOLDOWN = "spicy_start_portal_glitch_cooldown";
    public static final String SPICY_START_LAST_MESSAGE_TICK = "spicy_start_last_message_tick";
    public static final Codec<ExtraTrialsSavedData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.unboundedMap(Codec.STRING, Codec.LONG).optionalFieldOf("cooldowns", Map.of()).forGetter(data -> data.cooldowns),
            Codec.BOOL.optionalFieldOf("closingBorderOriginalSaved", false).forGetter(ExtraTrialsSavedData::isClosingBorderOriginalSaved),
            Codec.BOOL.optionalFieldOf("closingBorderControlled", false).forGetter(ExtraTrialsSavedData::isClosingBorderControlled),
            Codec.DOUBLE.optionalFieldOf("closingBorderOriginalCenterX", 0.0D).forGetter(ExtraTrialsSavedData::getClosingBorderOriginalCenterX),
            Codec.DOUBLE.optionalFieldOf("closingBorderOriginalCenterZ", 0.0D).forGetter(ExtraTrialsSavedData::getClosingBorderOriginalCenterZ),
            Codec.DOUBLE.optionalFieldOf("closingBorderOriginalSize", 5.999997E7D).forGetter(ExtraTrialsSavedData::getClosingBorderOriginalSize),
            Codec.INT.optionalFieldOf("closingBorderOriginalWarningBlocks", 5).forGetter(ExtraTrialsSavedData::getClosingBorderOriginalWarningBlocks),
            Codec.INT.optionalFieldOf("closingBorderOriginalWarningTime", 15).forGetter(ExtraTrialsSavedData::getClosingBorderOriginalWarningTime),
            Codec.LONG.optionalFieldOf("closingBorderStartGameTime", 0L).forGetter(ExtraTrialsSavedData::getClosingBorderStartGameTime),
            Codec.BOOL.optionalFieldOf("closingBorderFirstPulseSent", false).forGetter(ExtraTrialsSavedData::isClosingBorderFirstPulseSent),
            Codec.BOOL.optionalFieldOf("closingBorderShrinkStarted", false).forGetter(ExtraTrialsSavedData::isClosingBorderShrinkStarted)
    ).apply(instance, ExtraTrialsSavedData::new));

    public static final SavedDataType<ExtraTrialsSavedData> TYPE = new SavedDataType<>(
            "scavenger_extra_trials_data",
            ExtraTrialsSavedData::new,
            CODEC,
            DataFixTypes.LEVEL
    );

    private final Map<String, Long> cooldowns = new HashMap<>();
    private boolean closingBorderOriginalSaved;
    private boolean closingBorderControlled;
    private double closingBorderOriginalCenterX;
    private double closingBorderOriginalCenterZ;
    private double closingBorderOriginalSize = 5.999997E7D;
    private int closingBorderOriginalWarningBlocks = 5;
    private int closingBorderOriginalWarningTime = 15;
    private long closingBorderStartGameTime;
    private boolean closingBorderFirstPulseSent;
    private boolean closingBorderShrinkStarted;

    public ExtraTrialsSavedData() {
    }

    private ExtraTrialsSavedData(Map<String, Long> cooldowns, boolean closingBorderOriginalSaved, boolean closingBorderControlled,
                                 double closingBorderOriginalCenterX, double closingBorderOriginalCenterZ, double closingBorderOriginalSize,
                                 int closingBorderOriginalWarningBlocks, int closingBorderOriginalWarningTime,
                                 long closingBorderStartGameTime, boolean closingBorderFirstPulseSent, boolean closingBorderShrinkStarted) {
        this.cooldowns.putAll(cooldowns);
        this.closingBorderOriginalSaved = closingBorderOriginalSaved;
        this.closingBorderControlled = closingBorderControlled;
        this.closingBorderOriginalCenterX = closingBorderOriginalCenterX;
        this.closingBorderOriginalCenterZ = closingBorderOriginalCenterZ;
        this.closingBorderOriginalSize = closingBorderOriginalSize;
        this.closingBorderOriginalWarningBlocks = closingBorderOriginalWarningBlocks;
        this.closingBorderOriginalWarningTime = closingBorderOriginalWarningTime;
        this.closingBorderStartGameTime = closingBorderStartGameTime;
        this.closingBorderFirstPulseSent = closingBorderFirstPulseSent;
        this.closingBorderShrinkStarted = closingBorderShrinkStarted;
    }

    public static ExtraTrialsSavedData get(ServerLevel level) {
        return level.getServer().overworld().getDataStorage().computeIfAbsent(TYPE);
    }

    public long getCooldown(UUID uuid, String key) {
        return cooldowns.getOrDefault(key + ":" + uuid, 0L);
    }

    public void setCooldown(UUID uuid, String key, long value) {
        String mapKey = key + ":" + uuid;
        if (value == 0L) {
            cooldowns.remove(mapKey);
        } else {
            cooldowns.put(mapKey, value);
        }
        setDirty();
    }

    public void resetRoutePressure(UUID uuid) {
        setCooldown(uuid, ROUTE_CHUNK_X, 0L);
        setCooldown(uuid, ROUTE_CHUNK_Z, 0L);
        setCooldown(uuid, ROUTE_TICKS, 0L);
        setCooldown(uuid, ROUTE_WARNING_STATE, 0L);
        setCooldown(uuid, ROUTE_GRACE_UNTIL, 0L);
        setCooldown(uuid, ROUTE_LAST_MESSAGE_TICK, 0L);
    }

    public void resetHotPotato(UUID uuid) {
        setCooldown(uuid, HOT_POTATO_MAIN_ITEM_HASH, 0L);
        setCooldown(uuid, HOT_POTATO_MAIN_HEAT_TICKS, 0L);
        setCooldown(uuid, HOT_POTATO_MAIN_WARNED, 0L);
        setCooldown(uuid, HOT_POTATO_OFFHAND_ITEM_HASH, 0L);
        setCooldown(uuid, HOT_POTATO_OFFHAND_HEAT_TICKS, 0L);
        setCooldown(uuid, HOT_POTATO_OFFHAND_WARNED, 0L);
        setCooldown(uuid, HOT_POTATO_LAST_MESSAGE_TICK, 0L);
    }

    public void resetGlassHeart(UUID uuid) {
        setCooldown(uuid, GLASS_HEART_STACKS, 0L);
        setCooldown(uuid, GLASS_HEART_EXPIRES_AT, 0L);
    }

    public void resetSpicyStart(UUID uuid) {
        setCooldown(uuid, SPICY_START_TELEPORTED, 0L);
        setCooldown(uuid, SPICY_START_FIRE_PROTECTION_END, 0L);
        setCooldown(uuid, SPICY_START_DAMAGE_PROTECTION_END, 0L);
        setCooldown(uuid, SPICY_START_PORTAL_SICKNESS_END, 0L);
        setCooldown(uuid, SPICY_START_LAST_DIMENSION, 0L);
        setCooldown(uuid, SPICY_START_PORTAL_GLITCH_COOLDOWN, 0L);
        setCooldown(uuid, SPICY_START_LAST_MESSAGE_TICK, 0L);
    }

    public void resetPlayerRuntime(UUID uuid) {
        resetRoutePressure(uuid);
        resetHotPotato(uuid);
        resetGlassHeart(uuid);
        resetSpicyStart(uuid);
        setCooldown(uuid, ENDERMAN_BLOOD_COOLDOWN, 0L);
    }

    public boolean isClosingBorderOriginalSaved() {
        return closingBorderOriginalSaved;
    }

    public void saveOriginalClosingBorder(double centerX, double centerZ, double size, int warningBlocks, int warningTime) {
        if (closingBorderOriginalSaved) {
            return;
        }
        closingBorderOriginalSaved = true;
        closingBorderOriginalCenterX = centerX;
        closingBorderOriginalCenterZ = centerZ;
        closingBorderOriginalSize = size;
        closingBorderOriginalWarningBlocks = warningBlocks;
        closingBorderOriginalWarningTime = warningTime;
        setDirty();
    }

    public boolean isClosingBorderControlled() {
        return closingBorderControlled;
    }

    public void setClosingBorderControlled(boolean closingBorderControlled) {
        this.closingBorderControlled = closingBorderControlled;
        setDirty();
    }

    public double getClosingBorderOriginalCenterX() {
        return closingBorderOriginalCenterX;
    }

    public double getClosingBorderOriginalCenterZ() {
        return closingBorderOriginalCenterZ;
    }

    public double getClosingBorderOriginalSize() {
        return closingBorderOriginalSize;
    }

    public int getClosingBorderOriginalWarningBlocks() {
        return closingBorderOriginalWarningBlocks;
    }

    public int getClosingBorderOriginalWarningTime() {
        return closingBorderOriginalWarningTime;
    }

    public long getClosingBorderStartGameTime() {
        return closingBorderStartGameTime;
    }

    public void setClosingBorderStartGameTime(long closingBorderStartGameTime) {
        this.closingBorderStartGameTime = closingBorderStartGameTime;
        setDirty();
    }

    public boolean isClosingBorderShrinkStarted() {
        return closingBorderShrinkStarted;
    }

    public boolean isClosingBorderFirstPulseSent() {
        return closingBorderFirstPulseSent;
    }

    public void setClosingBorderFirstPulseSent(boolean closingBorderFirstPulseSent) {
        this.closingBorderFirstPulseSent = closingBorderFirstPulseSent;
        setDirty();
    }

    public void setClosingBorderShrinkStarted(boolean closingBorderShrinkStarted) {
        this.closingBorderShrinkStarted = closingBorderShrinkStarted;
        setDirty();
    }

    public void clearClosingBorderControl() {
        closingBorderControlled = false;
        closingBorderStartGameTime = 0L;
        closingBorderFirstPulseSent = false;
        closingBorderShrinkStarted = false;
        setDirty();
    }
}
