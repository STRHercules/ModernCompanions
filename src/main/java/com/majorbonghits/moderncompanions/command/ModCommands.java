package com.majorbonghits.moderncompanions.command;

import com.majorbonghits.moderncompanions.ModernCompanions;
import com.majorbonghits.moderncompanions.entity.AbstractHumanCompanionEntity;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;

/**
 * Adds small helper commands to locate the closest companion house, mirroring the
 * original Human Companions `/locate humancompanions:companion_house`.
 */
@EventBusSubscriber(modid = ModernCompanions.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public final class ModCommands {
    private static final String LOCATE_CMD = "locate structure #modern_companions:companion_houses";

    private ModCommands() {}

    @SubscribeEvent
    public static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(
                Commands.literal("locatecompanionhouse")
                        .requires(src -> src.hasPermission(2))
                        .executes(ctx -> forwardLocate(ctx.getSource()))
        );
        event.getDispatcher().register(
                Commands.literal("locatecompanions")
                        .requires(src -> src.hasPermission(2))
                        .executes(ctx -> forwardLocate(ctx.getSource()))
        );

        event.getDispatcher().register(
                Commands.literal("companionskin")
                        .requires(src -> src.getEntity() instanceof ServerPlayer)
                        .then(Commands.argument("name", StringArgumentType.string())
                                .then(Commands.argument("url", StringArgumentType.greedyString())
                                        .executes(ctx -> setCompanionSkin(
                                                ctx.getSource(),
                                                StringArgumentType.getString(ctx, "name"),
                                                StringArgumentType.getString(ctx, "url")))))
        );
    }

    private static int forwardLocate(CommandSourceStack source) {
        // Delegate to vanilla locate to keep output/formatting consistent.
        source.getServer().getCommands().performPrefixedCommand(source, LOCATE_CMD);
        return 1;
    }

    private static int setCompanionSkin(CommandSourceStack source, String name, String url) {
        ServerPlayer player = Objects.requireNonNull(source.getPlayer());
        if (!isHttpUrl(url)) {
            source.sendFailure(Component.literal("Skin URL must start with http:// or https://"));
            return 0;
        }

        AbstractHumanCompanionEntity companion = findOwnedCompanion(source, player, name);
        if (companion == null) {
            source.sendFailure(Component.literal("No owned companion named \"" + name + "\" was found."));
            return 0;
        }

        companion.setCustomSkinUrl(url);
        source.sendSuccess(() -> Component.literal(
                "Updated skin for " + companion.getName().getString() + " using " + url), false);
        return 1;
    }

    private static AbstractHumanCompanionEntity findOwnedCompanion(CommandSourceStack source, ServerPlayer player, String name) {
        String targetName = name.trim();
        // Scan all loaded levels so the command works even if the companion is parked in another dimension.
        for (ServerLevel level : source.getServer().getAllLevels()) {
            for (var entity : level.getEntities().getAll()) {
                if (entity instanceof AbstractHumanCompanionEntity companion
                        && targetName.equalsIgnoreCase(companion.getName().getString())
                        && (companion.isOwnedBy(player) || source.hasPermission(2))) {
                    return companion;
                }
            }
        }
        return null;
    }

    private static boolean isHttpUrl(String raw) {
        try {
            URL parsed = new URL(raw);
            String protocol = parsed.getProtocol();
            return protocol.equals("http") || protocol.equals("https");
        } catch (MalformedURLException e) {
            return false;
        }
    }
}
