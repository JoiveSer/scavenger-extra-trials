package com.joive.scavengerextratrials;

import net.fabricmc.api.ClientModInitializer;

public final class ScavengerExtraTrialsClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ExtraClientEvents.register();
    }
}
