package com.boostcolony.commands;

import com.boostcolony.config.BoostColonyConfig;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.entity.citizen.Skill;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Tuple;
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
            .then(Commands.literal("maxskills")
                .executes(ctx -> maxSkills(ctx, -1))
                .then(Commands.argument("colonyId", IntegerArgumentType.integer(1))
                    .executes(ctx -> maxSkills(ctx, IntegerArgumentType.getInteger(ctx, "colonyId")))))
            .then(Commands.literal("maxbuildings")
                .executes(ctx -> maxBuildings(ctx, -1))
                .then(Commands.argument("colonyId", IntegerArgumentType.integer(1))
                    .executes(ctx -> maxBuildings(ctx, IntegerArgumentType.getInteger(ctx, "colonyId")))))
            .then(Commands.literal("maxall")
                .executes(ctx -> maxAll(ctx, -1))
                .then(Commands.argument("colonyId", IntegerArgumentType.integer(1))
                    .executes(ctx -> maxAll(ctx, IntegerArgumentType.getInteger(ctx, "colonyId")))))
            .then(Commands.literal("speed")
                .then(Commands.argument("buildDelay", IntegerArgumentType.integer(0, 150))
                    .then(Commands.argument("mineDelay", IntegerArgumentType.integer(0, 40))
                        .executes(BoostColonyCommands::setSpeed))))
            .then(Commands.literal("range")
                .then(Commands.argument("blocks", IntegerArgumentType.integer(5, 128))
                    .executes(BoostColonyCommands::setRange)))
        );
    }

    private static IColony getColony(CommandContext<CommandSourceStack> ctx, int colonyId) {
        CommandSourceStack source = ctx.getSource();
        if (colonyId > 0) {
            return IColonyManager.getInstance().getColonyByDimension(colonyId, source.getLevel().dimension());
        }

        if (source.getEntity() instanceof ServerPlayer player) {
            return IColonyManager.getInstance().getIColony(source.getLevel(), player.blockPosition());
        }

        return null;
    }

    private static int showStatus(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack source = ctx.getSource();
        source.sendSuccess(() -> Component.literal("=== BoostColony Status ===").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD), false);
        source.sendSuccess(() -> Component.literal("Building Delay: ").withStyle(ChatFormatting.YELLOW)
            .append(Component.literal(BoostColonyConfig.SERVER.buildingDelay.get() + " ticks (MineColonies vanilla: 150)").withStyle(ChatFormatting.GREEN)), false);
        source.sendSuccess(() -> Component.literal("Destroying Delay: ").withStyle(ChatFormatting.YELLOW)
            .append(Component.literal(BoostColonyConfig.SERVER.miningDelay.get() + " ticks (MineColonies vanilla: 40)").withStyle(ChatFormatting.GREEN)), false);
        source.sendSuccess(() -> Component.literal("Placing Range: ").withStyle(ChatFormatting.YELLOW)
            .append(Component.literal(BoostColonyConfig.SERVER.placeBlockRange.get() + " blocks (MineColonies vanilla: 5-12)").withStyle(ChatFormatting.GREEN)), false);
        source.sendSuccess(() -> Component.literal("Max Citizen Skills: ").withStyle(ChatFormatting.YELLOW)
            .append(Component.literal(BoostColonyConfig.SERVER.maxCitizenSkills.get() ? "ENABLED (Lvl " + BoostColonyConfig.SERVER.maxSkillLevel.get() + ")" : "DISABLED").withStyle(ChatFormatting.GREEN)), false);
        source.sendSuccess(() -> Component.literal("Auto Max Skills: ").withStyle(ChatFormatting.YELLOW)
            .append(Component.literal(BoostColonyConfig.SERVER.autoMaxSkillsOnUpdate.get() ? "ENABLED" : "DISABLED").withStyle(ChatFormatting.GREEN)), false);
        return 1;
    }

    private static int maxSkills(CommandContext<CommandSourceStack> ctx, int colonyId) {
        CommandSourceStack source = ctx.getSource();
        IColony colony = getColony(ctx, colonyId);
        if (colony == null) {
            source.sendFailure(Component.literal("Could not find colony! Either stand inside a colony or provide colony ID."));
            return 0;
        }

        int targetLevel = BoostColonyConfig.SERVER.maxSkillLevel.get();
        int count = 0;

        for (ICitizenData citizen : colony.getCitizenManager().getCitizens()) {
            var skillHandler = citizen.getCitizenSkillHandler();
            if (skillHandler != null) {
                for (Skill skill : Skill.values()) {
                    int currentLevel = skillHandler.getLevel(skill);
                    if (currentLevel < targetLevel) {
                        skillHandler.incrementLevel(skill, targetLevel - currentLevel);
                    }
                }
                skillHandler.levelUp(citizen);
                citizen.markDirty(10);
                count++;
            }
        }

        final int boostedCount = count;
        source.sendSuccess(() -> Component.literal("Boosted " + boostedCount + " citizen(s) in Colony #" + colony.getID() + " to level " + targetLevel + " for all skills!")
            .withStyle(ChatFormatting.GREEN), true);
        return count;
    }

    private static int maxBuildings(CommandContext<CommandSourceStack> ctx, int colonyId) {
        CommandSourceStack source = ctx.getSource();
        IColony colony = getColony(ctx, colonyId);
        if (colony == null) {
            source.sendFailure(Component.literal("Could not find colony! Either stand inside a colony or provide colony ID."));
            return 0;
        }

        int count = 0;
        for (IBuilding building : colony.getServerBuildingManager().getBuildings().values()) {
            int maxLevel = building.getMaxBuildingLevel();
            if (building.getBuildingLevel() < maxLevel) {
                building.setBuildingLevel(maxLevel);
                building.markDirty();
                count++;
            }
        }

        final int boostedCount = count;
        source.sendSuccess(() -> Component.literal("Boosted " + boostedCount + " building(s) in Colony #" + colony.getID() + " to max level!")
            .withStyle(ChatFormatting.GREEN), true);
        return count;
    }

    private static int maxAll(CommandContext<CommandSourceStack> ctx, int colonyId) {
        maxSkills(ctx, colonyId);
        maxBuildings(ctx, colonyId);
        return 1;
    }

    private static int setSpeed(CommandContext<CommandSourceStack> ctx) {
        int buildDelay = IntegerArgumentType.getInteger(ctx, "buildDelay");
        int mineDelay = IntegerArgumentType.getInteger(ctx, "mineDelay");
        BoostColonyConfig.SERVER.buildingDelay.set(buildDelay);
        BoostColonyConfig.SERVER.miningDelay.set(mineDelay);
        BoostColonyConfig.SERVER_SPEC.save();

        ctx.getSource().sendSuccess(() -> Component.literal("Updated BoostColony speeds: Building delay = " + buildDelay + " ticks, Mining delay = " + mineDelay + " ticks.")
            .withStyle(ChatFormatting.GREEN), true);
        return 1;
    }

    private static int setRange(CommandContext<CommandSourceStack> ctx) {
        int blocks = IntegerArgumentType.getInteger(ctx, "blocks");
        BoostColonyConfig.SERVER.placeBlockRange.set(blocks);
        BoostColonyConfig.SERVER_SPEC.save();

        ctx.getSource().sendSuccess(() -> Component.literal("Updated BoostColony builder placement range to " + blocks + " blocks.")
            .withStyle(ChatFormatting.GREEN), true);
        return 1;
    }
}
