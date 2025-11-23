package com.majorbonghits.moderncompanions.command;

import com.majorbonghits.moderncompanions.ModernCompanions;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

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
    }

    private static int forwardLocate(CommandSourceStack source) {
        // Delegate to vanilla locate to keep output/formatting consistent.
        source.getServer().getCommands().performPrefixedCommand(source, LOCATE_CMD);
        return 1;
    }
}
