package net.alek.succorstadiums.command;

import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.commands.Commands;
import net.minecraft.world.phys.Vec3;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

import net.alek.succorstadiums.util.CustomVillagerSpawner;
import net.alek.succorstadiums.dev.DevCommands;

// Mod commands class
public class ModCommands {

    // Register method for the mod commands
    public static void registerModCommands() {

        // Register the dev tools tick handler
        DevCommands.registerDevTick();

        // Register the succor stadium commands
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                dispatcher.register(
                        Commands.literal("succorstadiums")
                                // Villagers subcommand group
                                .then(Commands.literal("villagers")
                                        .then(Commands.literal("ye")
                                                .executes(ModCommands::executeSummonYe)
                                        )
                                )
                                .then(Commands.literal("villagers")
                                        .then(Commands.literal("ol")
                                                .executes(ModCommands::executeSummonOl)
                                        )
                                )
                                .then(Commands.literal("villagers")
                                        .then(Commands.literal("marvin")
                                                .executes(ModCommands::executeSummonMarvin)
                                        )
                                )
                                .then(Commands.literal("villagers")
                                        .then(Commands.literal("bimbleton")
                                                .executes(ModCommands::executeSummonBimbleton)
                                        )
                                )
                                .then(Commands.literal("villagers")
                                        .then(Commands.literal("bartholomew")
                                                .executes(ModCommands::executeSummonBartholomew)
                                        )
                                )
                                .then(Commands.literal("villagers")
                                        .then(Commands.literal("propung")
                                                .executes(ModCommands::executeSummonPropung)
                                        )
                                )
                                .then(Commands.literal("villagers")
                                        .then(Commands.literal("boe")
                                                .executes(ModCommands::executeSummonBoe)
                                        )
                                )
                                // Dev subcommand group
                                .then(DevCommands.buildDevCommands())
                )
        );
    }

    // Helper method for the villagers ye command
    private static int executeSummonYe(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack source = ctx.getSource();
        ServerLevel level = source.getLevel();
        Vec3 pos = source.getPosition();
        float yaw = source.getEntity() instanceof ServerPlayer player ? player.getYRot() : source.getRotation().x;

        CustomVillagerSpawner.spawnYeBuy(level, pos, yaw);

        source.sendSuccess(() -> Component.literal("Spawned Ye"), true);
        return 1;
    }

    // Helper method for the villagers ol command
    private static int executeSummonOl(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack source = ctx.getSource();
        ServerLevel level = source.getLevel();
        Vec3 pos = source.getPosition();
        float yaw = source.getEntity() instanceof ServerPlayer player ? player.getYRot() : source.getRotation().x;

        CustomVillagerSpawner.spawnOlSell(level, pos, yaw);

        source.sendSuccess(() -> Component.literal("Spawned Ol"), true);
        return 1;
    }

    // Helper method for the villagers marvin command
    private static int executeSummonMarvin(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack source = ctx.getSource();
        ServerLevel level = source.getLevel();
        Vec3 pos = source.getPosition();
        float yaw = source.getEntity() instanceof ServerPlayer player ? player.getYRot() : source.getRotation().x;

        CustomVillagerSpawner.spawnMarvin(level, pos, yaw);

        source.sendSuccess(() -> Component.literal("Spawned Marvin Malarkey"), true);
        return 1;
    }

    // Helper method for the villagers bimbleton command
    private static int executeSummonBimbleton(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack source = ctx.getSource();
        ServerLevel level = source.getLevel();
        Vec3 pos = source.getPosition();
        float yaw = source.getEntity() instanceof ServerPlayer player ? player.getYRot() : source.getRotation().x;

        CustomVillagerSpawner.spawnBimbleton(level, pos, yaw);

        source.sendSuccess(() -> Component.literal("Spawned Ghimple Bimbleton"), true);
        return 1;
    }

    // Helper method for the villagers bartholomew command
    private static int executeSummonBartholomew(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack source = ctx.getSource();
        ServerLevel level = source.getLevel();
        Vec3 pos = source.getPosition();
        float yaw = source.getEntity() instanceof ServerPlayer player ? player.getYRot() : source.getRotation().x;

        CustomVillagerSpawner.spawnBartholomew(level, pos, yaw);

        source.sendSuccess(() -> Component.literal("Spawned Bartholomew Bale"), true);
        return 1;
    }

    // Helper method for the villagers propung command
    private static int executeSummonPropung(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack source = ctx.getSource();
        ServerLevel level = source.getLevel();
        Vec3 pos = source.getPosition();
        float yaw = source.getEntity() instanceof ServerPlayer player ? player.getYRot() : source.getRotation().x;

        CustomVillagerSpawner.spawnPropung(level, pos, yaw);

        source.sendSuccess(() -> Component.literal("Spawned Propung Giewish"), true);
        return 1;
    }

    // Helper method for the villagers boe command
    private static int executeSummonBoe(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack source = ctx.getSource();
        ServerLevel level = source.getLevel();
        Vec3 pos = source.getPosition();
        float yaw = source.getEntity() instanceof ServerPlayer player ? player.getYRot() : source.getRotation().x;

        CustomVillagerSpawner.spawnBoe(level, pos, yaw);

        source.sendSuccess(() -> Component.literal("Spawned Boe Narrow"), true);
        return 1;
    }
}