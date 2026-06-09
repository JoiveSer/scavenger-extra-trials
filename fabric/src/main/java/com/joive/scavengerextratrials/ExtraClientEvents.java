package com.joive.scavengerextratrials;

import com.joive.scavengerextratrials.config.ScavengerExtraTrialsConfig;
import com.joive.scavengerextratrials.registry.ExtraModifiers;
import meow.binary.scavenger.client.ClientScavengerData;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;

public final class ExtraClientEvents {
    private static long upsideDownStarted;
    private static float upsideDownRoll;

    private ExtraClientEvents() {
    }

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(ExtraClientEvents::onClientTick);
    }

    private static void onClientTick(Minecraft minecraft) {
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

    public static float upsideDownRoll() {
        return upsideDownRoll;
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
