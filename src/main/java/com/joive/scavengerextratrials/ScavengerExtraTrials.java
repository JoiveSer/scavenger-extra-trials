package com.joive.scavengerextratrials;

import com.joive.scavengerextratrials.command.ScavengerExtraCommands;
import com.joive.scavengerextratrials.config.ScavengerExtraTrialsConfig;
import com.joive.scavengerextratrials.event.ExtraTrialEvents;
import com.joive.scavengerextratrials.registry.ExtraModifiers;
import com.mojang.logging.LogUtils;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;

@Mod(ScavengerExtraTrials.MOD_ID)
public final class ScavengerExtraTrials {
    public static final String MOD_ID = "scavenger_extra_trials";
    public static final Logger LOGGER = LogUtils.getLogger();

    public ScavengerExtraTrials(IEventBus modEventBus, ModContainer modContainer) {
        ExtraModifiers.register();
        if (FMLEnvironment.getDist() == Dist.CLIENT) {
            registerClientEvents(modEventBus);
        }
        ExtraTrialEvents.register();
        ScavengerExtraCommands.register(NeoForge.EVENT_BUS);
        modContainer.registerConfig(ModConfig.Type.COMMON, ScavengerExtraTrialsConfig.SPEC);
    }

    private static void registerClientEvents(IEventBus modEventBus) {
        try {
            Class.forName("com.joive.scavengerextratrials.ExtraClientEvents")
                    .getMethod("register", IEventBus.class)
                    .invoke(null, modEventBus);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Unable to register Scavenger Extra Trials client events", exception);
        }
    }
}
