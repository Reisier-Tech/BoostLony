package com.boostcolony.commands;

import com.boostcolony.config.BoostColonyConfig;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraftforge.event.RegisterCommandsEvent;

public class BoostColonyCommands {

    public static void onRegisterCommands(RegisterCommandsEvent event) {
        register(event.getDispatcher());
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("boostcolony")
            .requires(source -> source.hasPermission(2))
            .then(Commands.literal("status")
                .executes(BoostColonyCommands::showStatus))
            .then(Commands.literal("placespeed")
                .then(Commands.argument("multiplier", DoubleArgumentType.doubleArg(1.0, 100.0))
                    .executes(BoostColonyCommands::setPlaceSpeed)))
            .then(Commands.literal("breakspeed")
                .then(Commands.argument("multiplier", DoubleArgumentType.doubleArg(1.0, 100.0))
                    .executes(BoostColonyCommands::setBreakSpeed)))
            .then(Commands.literal("range")
                .then(Commands.argument("blocks", IntegerArgumentType.integer(5, 64))
                    .executes(BoostColonyCommands::setRange)))
            .then(Commands.literal("reset")
                .executes(BoostColonyCommands::resetDefaults))
        );
    }

    private static int showStatus(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack source = ctx.getSource();
        int placeBurst = (int) Math.min(100.0, Math.max(1.0, BoostColonyConfig.SERVER.placeSpeed.get()));
        int breakBurst = (int) Math.min(100.0, Math.max(1.0, BoostColonyConfig.SERVER.breakSpeed.get()));

        source.sendSuccess(() -> Component.literal("=== BoostColony Status ===").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD), false);
        source.sendSuccess(() -> Component.literal("Block Place Speed: ").withStyle(ChatFormatting.YELLOW)
            .append(Component.literal(BoostColonyConfig.SERVER.placeSpeed.get() + "x (Rapid burst: up to " + placeBurst + " blocks/tick)").withStyle(ChatFormatting.GREEN)), false);
        source.sendSuccess(() -> Component.literal("Block Break Speed: ").withStyle(ChatFormatting.YELLOW)
            .append(Component.literal(BoostColonyConfig.SERVER.breakSpeed.get() + "x (Rapid burst: up to " + breakBurst + " blocks/tick)").withStyle(ChatFormatting.GREEN)), false);
        source.sendSuccess(() -> Component.literal("Worker Reach Range: ").withStyle(ChatFormatting.YELLOW)
            .append(Component.literal(BoostColonyConfig.SERVER.placeBlockRange.get() + " blocks").withStyle(ChatFormatting.GREEN)), false);
        return 1;
    }

    private static int setPlaceSpeed(CommandContext<CommandSourceStack> ctx) {
        double multiplier = DoubleArgumentType.getDouble(ctx, "multiplier");
        BoostColonyConfig.SERVER.placeSpeed.set(multiplier);
        BoostColonyConfig.SERVER_SPEC.save();

        int burst = (int) Math.min(100.0, Math.max(1.0, multiplier));
        ctx.getSource().sendSuccess(() -> Component.literal("Updated BoostColony place speed to " + multiplier + "x (Rapid burst: up to " + burst + " blocks/tick).")
            .withStyle(ChatFormatting.GREEN), true);
        return 1;
    }

    private static int setBreakSpeed(CommandContext<CommandSourceStack> ctx) {
        double multiplier = DoubleArgumentType.getDouble(ctx, "multiplier");
        BoostColonyConfig.SERVER.breakSpeed.set(multiplier);
        BoostColonyConfig.SERVER_SPEC.save();

        int burst = (int) Math.min(100.0, Math.max(1.0, multiplier));
        ctx.getSource().sendSuccess(() -> Component.literal("Updated BoostColony break/mine speed to " + multiplier + "x (Rapid burst: up to " + burst + " blocks/tick).")
            .withStyle(ChatFormatting.GREEN), true);
        return 1;
    }

    private static int setRange(CommandContext<CommandSourceStack> ctx) {
        int blocks = IntegerArgumentType.getInteger(ctx, "blocks");
        BoostColonyConfig.SERVER.placeBlockRange.set(blocks);
        BoostColonyConfig.SERVER_SPEC.save();

        ctx.getSource().sendSuccess(() -> Component.literal("Updated BoostColony reach range to " + blocks + " blocks.")
            .withStyle(ChatFormatting.GREEN), true);
        return 1;
    }

    private static int resetDefaults(CommandContext<CommandSourceStack> ctx) {
        BoostColonyConfig.SERVER.placeSpeed.set(5.0D);
        BoostColonyConfig.SERVER.breakSpeed.set(5.0D);
        BoostColonyConfig.SERVER.placeBlockRange.set(16);
        BoostColonyConfig.SERVER_SPEC.save();

        ctx.getSource().sendSuccess(() -> Component.literal("Reset BoostColony settings to defaults (PlaceSpeed: 5.0x, BreakSpeed: 5.0x, Range: 16 blocks).")
            .withStyle(ChatFormatting.GREEN), true);
        return 1;
    }
}
