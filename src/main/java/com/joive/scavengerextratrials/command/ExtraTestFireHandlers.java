package com.joive.scavengerextratrials.command;

import com.joive.scavengerextratrials.registry.ExtraModifiers;
import com.joive.scavengerextratrials.event.ExtraTrialEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

public final class ExtraTestFireHandlers {
    private ExtraTestFireHandlers() {
    }

    public static boolean fire(ServerPlayer player, Identifier modifierId) {
        if (!ExtraModifiers.isExtraModifier(modifierId)) {
            player.sendSystemMessage(Component.translatable("scavenger_extra_trials.command.testfire.not_extra"));
            return false;
        }

        player.sendSystemMessage(Component.translatable("scavenger_extra_trials.command.testfire.requested", modifierId.toString()));
        if ("closing_border".equals(modifierId.getPath())) {
            return ExtraTrialEvents.testfireClosingBorder(player);
        }
        if ("route_pressure".equals(modifierId.getPath())) {
            return ExtraTrialEvents.testfireRoutePressure(player);
        }
        if ("enderman_blood".equals(modifierId.getPath())) {
            return ExtraTrialEvents.testfireEndermanBlood(player);
        }
        if ("hot_potato".equals(modifierId.getPath())) {
            return ExtraTrialEvents.testfireHotPotato(player);
        }
        if ("repelling_loot".equals(modifierId.getPath())) {
            return ExtraTrialEvents.testfireRepellingLoot(player);
        }
        if ("upside_down".equals(modifierId.getPath())) {
            return ExtraTrialEvents.testfireUpsideDown(player);
        }
        if ("spicy_start".equals(modifierId.getPath())) {
            return ExtraTrialEvents.testfireSpicyStart(player);
        }
        if ("glass_heart".equals(modifierId.getPath())) {
            return ExtraTrialEvents.testfireGlassHeart(player);
        }
        player.sendSystemMessage(Component.literal("No testfire handler for " + modifierId + "."));
        return false;
    }
}
