package com.joive.scavengerextratrials.command;

import com.joive.scavengerextratrials.ScavengerExtraTrials;
import com.joive.scavengerextratrials.event.ExtraTrialEvents;
import com.joive.scavengerextratrials.registry.ExtraModifiers;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import dev.architectury.networking.NetworkManager;
import meow.binary.scavenger.data.ScavengerSavedData;
import meow.binary.scavenger.network.SyncScavengerDataPacket;
import meow.binary.scavenger.registry.Modifiers;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public final class ScavengerExtraCommands {
    private static final int MENU_PAGE_SIZE = 6;
    private static final int MENU_PAGE_COUNT = (ExtraModifiers.PATHS.size() + MENU_PAGE_SIZE - 1) / MENU_PAGE_SIZE;
    private static Field hasWonField;

    private ScavengerExtraCommands() {
    }

    public static void register() {
        CommandRegistrationCallback.EVENT.register(ScavengerExtraCommands::registerCommands);
    }

    private static void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext context, Commands.CommandSelection selection) {
        dispatcher.register(Commands.literal("scavengerextra")
                .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .executes(ctx -> info(ctx.getSource()))
                .then(Commands.literal("help").executes(ctx -> info(ctx.getSource())))
                .then(Commands.literal("info").executes(ctx -> info(ctx.getSource())))
                .then(Commands.literal("status").executes(ctx -> status(ctx.getSource())))
                .then(Commands.literal("reload").executes(ctx -> reload(ctx.getSource())))
                .then(Commands.literal("sync").executes(ctx -> sync(ctx.getSource())))
                .then(Commands.literal("win")
                        .then(Commands.literal("reset").executes(ctx -> resetWin(ctx.getSource()))))
                .then(Commands.literal("menu")
                        .executes(ctx -> menu(ctx.getSource(), 1))
                        .then(Commands.argument("page", IntegerArgumentType.integer(1, MENU_PAGE_COUNT))
                                .executes(ctx -> menu(ctx.getSource(), IntegerArgumentType.getInteger(ctx, "page")))))
                .then(Commands.literal("modifier")
                        .then(Commands.literal("list").executes(ctx -> listModifiers(ctx.getSource())))
                        .then(Commands.literal("get").executes(ctx -> getModifier(ctx.getSource())))
                        .then(Commands.literal("random").executes(ctx -> randomModifier(ctx.getSource())))
                        .then(Commands.literal("set")
                                .then(Commands.argument("modifier_id", StringArgumentType.greedyString())
                                        .suggests((ctx, builder) -> SharedSuggestionProvider.suggest(extraModifierSuggestions(), builder))
                                        .executes(ctx -> setModifier(ctx.getSource(), StringArgumentType.getString(ctx, "modifier_id")))))
                        .then(Commands.literal("testfire")
                                .executes(ctx -> testfireActive(ctx.getSource()))
                                .then(Commands.argument("modifier_id", StringArgumentType.greedyString())
                                        .suggests((ctx, builder) -> SharedSuggestionProvider.suggest(extraModifierSuggestions(), builder))
                                        .executes(ctx -> testfire(ctx.getSource(), StringArgumentType.getString(ctx, "modifier_id")))))));
    }

    private static int info(CommandSourceStack source) {
        source.sendSuccess(() -> Component.literal("Scavenger Extra Trials commands:"), false);
        source.sendSuccess(() -> Component.literal("/scavengerextra menu [page]"), false);
        source.sendSuccess(() -> Component.literal("/scavengerextra modifier list"), false);
        source.sendSuccess(() -> Component.literal("/scavengerextra modifier set <modifier_id>"), false);
        source.sendSuccess(() -> Component.literal("/scavengerextra modifier get"), false);
        source.sendSuccess(() -> Component.literal("/scavengerextra modifier random"), false);
        source.sendSuccess(() -> Component.literal("/scavengerextra modifier testfire [modifier_id]"), false);
        source.sendSuccess(() -> Component.literal("/scavengerextra status"), false);
        source.sendSuccess(() -> Component.literal("/scavengerextra reload"), false);
        source.sendSuccess(() -> Component.literal("/scavengerextra sync"), false);
        source.sendSuccess(() -> Component.literal("/scavengerextra win reset"), false);
        return 1;
    }

    private static int menu(CommandSourceStack source, int page) {
        source.sendSuccess(() -> Component.literal("Scavenger Extra Trials menu " + page + "/" + MENU_PAGE_COUNT).withStyle(ChatFormatting.GOLD), false);
        int start = (page - 1) * MENU_PAGE_SIZE;
        int end = Math.min(start + MENU_PAGE_SIZE, ExtraModifiers.PATHS.size());
        for (int index = start; index < end; index++) {
            String path = ExtraModifiers.PATHS.get(index);
            source.sendSuccess(() -> clickableModifierLine(path), false);
        }
        source.sendSuccess(() -> menuNavigation(page), false);
        return 1;
    }

    private static int listModifiers(CommandSourceStack source) {
        source.sendSuccess(() -> Component.literal("Scavenger Extra Trials modifiers (" + ExtraModifiers.PATHS.size() + "):").withStyle(ChatFormatting.GOLD), false);
        for (String path : ExtraModifiers.PATHS) {
            source.sendSuccess(() -> clickableModifierLine(path), false);
        }
        return 1;
    }

    private static MutableComponent clickableModifierLine(String path) {
        Identifier id = ExtraModifiers.id(path);
        boolean registered = Modifiers.getIds().contains(id);
        String command = "/scavengerextra modifier set " + id;
        ChatFormatting color = registered ? ChatFormatting.GREEN : ChatFormatting.RED;
        return Component.literal(" - " + id + " ")
                .withStyle(color)
                .append(Component.translatable("scavenger.modifier." + path).withStyle(ChatFormatting.WHITE))
                .withStyle(style -> style
                        .withClickEvent(new ClickEvent.RunCommand(command))
                        .withHoverEvent(new HoverEvent.ShowText(Component.literal(command))));
    }

    private static MutableComponent menuNavigation(int page) {
        MutableComponent line = Component.literal("");
        if (page > 1) {
            line.append(menuButton("[Prev]", "/scavengerextra menu " + (page - 1))).append(" ");
        }
        if (page < MENU_PAGE_COUNT) {
            line.append(menuButton("[Next]", "/scavengerextra menu " + (page + 1))).append(" ");
        }
        line.append(menuButton("[List]", "/scavengerextra modifier list"));
        return line;
    }

    private static MutableComponent menuButton(String text, String command) {
        return Component.literal(text).withStyle(style -> style
                .withColor(ChatFormatting.YELLOW)
                .withClickEvent(new ClickEvent.RunCommand(command))
                .withHoverEvent(new HoverEvent.ShowText(Component.literal(command))));
    }

    private static int setModifier(CommandSourceStack source, String rawId) {
        Identifier id = parseExtraModifier(source, rawId);
        if (id == null) {
            return 0;
        }
        if (!Modifiers.getIds().contains(id)) {
            source.sendFailure(Component.literal("Modifier is not registered in the Scavenger wheel: " + id));
            return 0;
        }

        ScavengerSavedData data = data(source.getServer());
        Identifier oldId = data.getModifierId();
        ExtraTrialEvents.onModifierChanging(source.getServer(), oldId, id);
        warnIfTargetEmpty(source, data);
        data.setModifierId(id);
        resetWinState(data);
        data.setDirty();
        syncToAll(source.getServer(), data);
        source.sendSuccess(() -> Component.literal("Set Scavenger modifier to " + id), true);
        return 1;
    }

    private static int getModifier(CommandSourceStack source) {
        ScavengerSavedData data = data(source.getServer());
        source.sendSuccess(() -> Component.literal("Scavenger item: " + safeItemId(data)), false);
        source.sendSuccess(() -> Component.literal("Scavenger modifier: " + data.getModifierId()), false);
        source.sendSuccess(() -> Component.literal("hasWon: " + data.hasWon()), false);
        source.sendSuccess(() -> Component.literal("winTimestamp: " + data.getWinTimestamp()), false);
        return 1;
    }

    private static int randomModifier(CommandSourceStack source) {
        List<Identifier> registered = new ArrayList<>();
        for (String path : ExtraModifiers.PATHS) {
            Identifier id = ExtraModifiers.id(path);
            if (Modifiers.getIds().contains(id)) {
                registered.add(id);
            }
        }
        if (registered.isEmpty()) {
            source.sendFailure(Component.literal("No Scavenger Extra Trials modifiers are registered."));
            return 0;
        }
        Identifier chosen = registered.get(source.getLevel().random.nextInt(registered.size()));
        return setModifier(source, chosen.toString());
    }

    private static int testfireActive(CommandSourceStack source) {
        ScavengerSavedData data = data(source.getServer());
        Identifier id = data.getModifierId();
        if (!ExtraModifiers.isExtraModifier(id)) {
            source.sendFailure(Component.literal("Active modifier is not a Scavenger Extra Trials modifier."));
            return 0;
        }
        return runTestfire(source, id);
    }

    private static int testfire(CommandSourceStack source, String rawId) {
        Identifier id = parseExtraModifier(source, rawId);
        return id == null ? 0 : runTestfire(source, id);
    }

    private static int runTestfire(CommandSourceStack source, Identifier id) {
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("This command must be run by a player."));
            return 0;
        }
        return ExtraTestFireHandlers.fire(player, id) ? 1 : 0;
    }

    private static int status(CommandSourceStack source) {
        int registered = 0;
        for (String path : ExtraModifiers.PATHS) {
            if (Modifiers.getIds().contains(ExtraModifiers.id(path))) {
                registered++;
            }
        }
        int registeredCount = registered;
        source.sendSuccess(() -> Component.literal("Scavenger Extra Trials base: loaded"), false);
        source.sendSuccess(() -> Component.literal("Registered modifiers: " + registeredCount + "/" + ExtraModifiers.PATHS.size()), false);
        source.sendSuccess(() -> Component.literal("Active Scavenger modifier: " + data(source.getServer()).getModifierId()), false);
        return 1;
    }

    private static int reload(CommandSourceStack source) {
        syncToAll(source.getServer(), data(source.getServer()));
        source.sendSuccess(() -> Component.literal("Scavenger data synced. Fabric defaults are bundled with the mod."), false);
        return 1;
    }

    private static int sync(CommandSourceStack source) {
        syncToAll(source.getServer(), data(source.getServer()));
        source.sendSuccess(() -> Component.literal("Scavenger data synced to all players."), false);
        return 1;
    }

    private static int resetWin(CommandSourceStack source) {
        ScavengerSavedData data = data(source.getServer());
        resetWinState(data);
        data.setDirty();
        syncToAll(source.getServer(), data);
        source.sendSuccess(() -> Component.literal("Scavenger win state reset."), true);
        return 1;
    }

    private static Identifier parseExtraModifier(CommandSourceStack source, String rawId) {
        String idText = rawId.trim();
        if (!idText.contains(":") && idText.startsWith(ScavengerExtraTrials.MOD_ID)) {
            String suffix = idText.substring(ScavengerExtraTrials.MOD_ID.length());
            if (ExtraModifiers.PATHS.contains(suffix)) {
                source.sendFailure(Component.literal("Invalid modifier id. Did you mean " + ScavengerExtraTrials.MOD_ID + ":" + suffix + "?"));
                return null;
            }
        }
        if (!idText.contains(":")) {
            if (!ExtraModifiers.PATHS.contains(idText)) {
                sendUnknownModifierFailure(source, idText);
                return null;
            }
            idText = ScavengerExtraTrials.MOD_ID + ":" + idText;
        }
        Identifier id = Identifier.tryParse(idText);
        if (id == null) {
            source.sendFailure(Component.literal("Invalid modifier id. Use /scavengerextra modifier list"));
            return null;
        }
        if (!ExtraModifiers.isExtraModifier(id)) {
            sendUnknownModifierFailure(source, id.getPath());
            return null;
        }
        return id;
    }

    private static void sendUnknownModifierFailure(CommandSourceStack source, String path) {
        String suggestion = closestModifierPath(path);
        if (suggestion != null) {
            source.sendFailure(Component.literal("Unknown modifier. Did you mean " + ScavengerExtraTrials.MOD_ID + ":" + suggestion + "?"));
            return;
        }
        source.sendFailure(Component.literal("Unknown modifier. Use /scavengerextra modifier list"));
    }

    private static String closestModifierPath(String path) {
        if (discardedModifierPath("unstable", "goal").equals(path) || discardedModifierPath("butter", "fingers").equals(path)) {
            return null;
        }
        String best = null;
        int bestDistance = Integer.MAX_VALUE;
        for (String candidate : ExtraModifiers.PATHS) {
            int distance = editDistance(path, candidate);
            if (distance < bestDistance) {
                bestDistance = distance;
                best = candidate;
            }
        }
        return bestDistance <= 2 ? best : null;
    }

    private static int editDistance(String left, String right) {
        int[] previous = new int[right.length() + 1];
        int[] current = new int[right.length() + 1];
        for (int i = 0; i <= right.length(); i++) {
            previous[i] = i;
        }
        for (int i = 1; i <= left.length(); i++) {
            current[0] = i;
            for (int j = 1; j <= right.length(); j++) {
                int cost = left.charAt(i - 1) == right.charAt(j - 1) ? 0 : 1;
                current[j] = Math.min(Math.min(current[j - 1] + 1, previous[j] + 1), previous[j - 1] + cost);
            }
            int[] swap = previous;
            previous = current;
            current = swap;
        }
        return previous[right.length()];
    }

    private static String discardedModifierPath(String first, String second) {
        return first + "_" + second;
    }

    private static Iterable<String> extraModifierSuggestions() {
        List<String> ids = new ArrayList<>(ExtraModifiers.PATHS.size() * 2);
        for (String path : ExtraModifiers.PATHS) {
            ids.add(path);
            ids.add(ScavengerExtraTrials.MOD_ID + ":" + path);
        }
        return ids;
    }

    private static ScavengerSavedData data(MinecraftServer server) {
        return ScavengerSavedData.get(server.overworld());
    }

    private static void warnIfTargetEmpty(CommandSourceStack source, ScavengerSavedData data) {
        Item item = data.getItem();
        if (data.isEmpty() || item == null || item == Items.AIR) {
            source.sendFailure(Component.literal("Create or start a Scavenger world first; current target item is empty."));
        }
    }

    private static String safeItemId(ScavengerSavedData data) {
        Item item = data.getItem();
        if (item == null) {
            return "minecraft:air";
        }
        Identifier id = BuiltInRegistries.ITEM.getKey(item);
        return id == null ? "minecraft:air" : id.toString();
    }

    private static void resetWinState(ScavengerSavedData data) {
        data.setWinTimestamp(0L);
        try {
            Field field = hasWonField;
            if (field == null) {
                field = ScavengerSavedData.class.getDeclaredField("hasWon");
                field.setAccessible(true);
                hasWonField = field;
            }
            field.setBoolean(data, false);
        } catch (ReflectiveOperationException exception) {
            ScavengerExtraTrials.LOGGER.warn("Unable to reset Scavenger hasWon state", exception);
        }
    }

    private static void syncToAll(MinecraftServer server, ScavengerSavedData data) {
        Item item = data.getItem();
        if (item == null) {
            item = Items.AIR;
        }
        Identifier modifier = data.getModifierId();
        if (modifier == null) {
            modifier = Identifier.fromNamespaceAndPath("scavenger", "none");
        }
        SyncScavengerDataPacket packet = new SyncScavengerDataPacket(item, modifier, data.getWinTimestamp(), false);
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            NetworkManager.sendToPlayer(player, packet);
        }
    }
}
