package com.joive.scavengerextratrials;

import com.joive.scavengerextratrials.command.ScavengerExtraCommands;
import com.joive.scavengerextratrials.event.ExtraTrialEvents;
import com.joive.scavengerextratrials.registry.ExtraModifiers;
import com.mojang.logging.LogUtils;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;

public final class ScavengerExtraTrials implements ModInitializer {
    public static final String MOD_ID = "scavenger_extra_trials";
    public static final Logger LOGGER = LogUtils.getLogger();

    @Override
    public void onInitialize() {
        ExtraModifiers.register();
        ExtraTrialEvents.register();
        ScavengerExtraCommands.register();
    }
}
