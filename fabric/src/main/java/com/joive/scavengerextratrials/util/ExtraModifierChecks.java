package com.joive.scavengerextratrials.util;

import com.joive.scavengerextratrials.registry.ExtraModifiers;
import meow.binary.scavenger.data.ScavengerSavedData;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;

public final class ExtraModifierChecks {
    private ExtraModifierChecks() {
    }

    public static boolean isActive(ServerLevel level, Identifier modifierId) {
        ScavengerSavedData data = getScavengerData(level);
        return data != null
                && !data.isEmpty()
                && !data.hasWon()
                && ExtraModifiers.isExtraModifier(data.getModifierId())
                && data.getModifierId().equals(modifierId);
    }

    public static ScavengerSavedData getScavengerData(ServerLevel level) {
        try {
            return level == null || level.getServer() == null ? null : ScavengerSavedData.get(level.getServer().overworld());
        } catch (RuntimeException exception) {
            return null;
        }
    }
}
