package com.joive.scavengerextratrials.registry;

import com.joive.scavengerextratrials.ScavengerExtraTrials;
import com.joive.scavengerextratrials.event.ExtraTrialEvents;
import dev.architectury.registry.registries.Registrar;
import dev.architectury.registry.registries.RegistrySupplier;
import meow.binary.scavenger.data.modifier.ScavengerModifier;
import net.minecraft.resources.Identifier;

import java.lang.reflect.Field;
import java.util.List;
import java.util.function.Supplier;

public final class ExtraModifiers {
    private static final Registrar<ScavengerModifier> MODIFIERS = findModifiersRegistrar();

    public static final RegistrySupplier<ScavengerModifier> CLOSING_BORDER = register("closing_border",
            () -> new ScavengerModifier(null, ExtraTrialEvents::startClosingBorder));
    public static final RegistrySupplier<ScavengerModifier> ROUTE_PRESSURE = register("route_pressure",
            () -> new ScavengerModifier(ExtraTrialEvents::tickRoutePressure, null));
    public static final RegistrySupplier<ScavengerModifier> GLASS_HEART = register("glass_heart");
    public static final RegistrySupplier<ScavengerModifier> UPSIDE_DOWN = register("upside_down");
    public static final RegistrySupplier<ScavengerModifier> ENDERMAN_BLOOD = register("enderman_blood");
    public static final RegistrySupplier<ScavengerModifier> HOT_POTATO = register("hot_potato");
    public static final RegistrySupplier<ScavengerModifier> REPELLING_LOOT = register("repelling_loot");
    public static final RegistrySupplier<ScavengerModifier> SPICY_START = register("spicy_start");

    public static final List<String> PATHS = List.of(
            "closing_border",
            "route_pressure",
            "glass_heart",
            "upside_down",
            "enderman_blood",
            "hot_potato",
            "repelling_loot",
            "spicy_start"
    );

    private ExtraModifiers() {
    }

    public static void register() {
        ScavengerExtraTrials.LOGGER.info("Registered {} Scavenger Extra Trials modifiers", PATHS.size());
    }

    public static boolean isExtraModifier(Identifier id) {
        return id != null && ScavengerExtraTrials.MOD_ID.equals(id.getNamespace()) && PATHS.contains(id.getPath());
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(ScavengerExtraTrials.MOD_ID, path);
    }

    private static RegistrySupplier<ScavengerModifier> register(String path) {
        return register(path, () -> new ScavengerModifier(null, null));
    }

    private static RegistrySupplier<ScavengerModifier> register(String path, Supplier<ScavengerModifier> supplier) {
        return MODIFIERS.register(id(path), supplier);
    }

    @SuppressWarnings("unchecked")
    private static Registrar<ScavengerModifier> findModifiersRegistrar() {
        try {
            Class<?> registries = Class.forName("meow.binary.scavenger.registry.ScavengerRegistries");
            Field field = registries.getDeclaredField("MODIFIERS");
            field.setAccessible(true);
            return (Registrar<ScavengerModifier>) field.get(null);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Unable to access Scavenger modifier registry", exception);
        }
    }
}
