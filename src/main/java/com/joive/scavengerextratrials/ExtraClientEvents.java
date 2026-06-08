package com.joive.scavengerextratrials;

import com.joive.scavengerextratrials.config.ScavengerExtraTrialsConfig;
import com.joive.scavengerextratrials.registry.ExtraModifiers;
import meow.binary.scavenger.client.ClientScavengerData;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;

@EventBusSubscriber(modid = ScavengerExtraTrials.MOD_ID, value = Dist.CLIENT)
public final class ExtraClientEvents {
    private static long upsideDownStarted;
    private static float upsideDownRoll;

    private ExtraClientEvents() {
    }

    public static void register(IEventBus modEventBus) {
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || minecraft.player == null || ClientScavengerData.isEmpty()) {
            clearUpsideDown();
            return;
        }

        boolean active = ScavengerExtraTrialsConfig.ENABLE_UPSIDE_DOWN.get()
                && ClientScavengerData.is(ExtraModifiers.UPSIDE_DOWN);
        long now = minecraft.level.getGameTime();
        if (!active) {
            upsideDownStarted = 0L;
            smoothRoll(0.0F);
        } else {
            if (upsideDownStarted == 0L) {
                upsideDownStarted = now;
            }
            boolean ready = now - upsideDownStarted >= ScavengerExtraTrialsConfig.UPSIDE_DOWN_FIRST_DELAY_TICKS.get();
            smoothRoll(ready ? ScavengerExtraTrialsConfig.UPSIDE_DOWN_ROLL_DEGREES.get() : 0.0F);
        }
    }

    @SubscribeEvent
    public static void onComputeCameraAngles(ViewportEvent.ComputeCameraAngles event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || minecraft.screen != null || ClientScavengerData.isEmpty() || Math.abs(upsideDownRoll) < 0.05F) {
            return;
        }
        event.setRoll(event.getRoll() + upsideDownRoll);
    }

    private static void smoothRoll(float target) {
        int smoothTicks = Math.max(1, ScavengerExtraTrialsConfig.UPSIDE_DOWN_SMOOTH_TICKS.get());
        upsideDownRoll += (target - upsideDownRoll) / smoothTicks;
        if (target == 0.0F && Math.abs(upsideDownRoll) < 0.05F) {
            upsideDownRoll = 0.0F;
        }
    }

    private static void clearUpsideDown() {
        upsideDownStarted = 0L;
        upsideDownRoll = 0.0F;
    }
}
