package net.alek.succorstadiums.command;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.alek.succorstadiums.arena.MobArena;
import net.alek.succorstadiums.arena.MobArenaManager;
import net.alek.succorstadiums.arena.ArenaSession;
import net.alek.succorstadiums.arena.Wave;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;

import java.util.Collection;
import java.util.List;
import net.alek.succorstadiums.arena.ArenaSessionManager;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public class ModCommands {

    private static final SuggestionProvider<CommandSourceStack> ARENA_SUGGESTIONS =
            (context, builder) -> {
                MobArenaManager.getAllArenas().forEach(arena -> builder.suggest(arena.getName()));
                return builder.buildFuture();
            };

    // Suggests mob IDs wrapped in quotes so StringArgumentType.string() accepts them
    private static final SuggestionProvider<CommandSourceStack> MOB_SUGGESTIONS =
            (context, builder) -> {
                String remaining = builder.getRemaining().replace("\"", "");
                net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.keySet().stream()
                        .map(key -> "\"" + key.toString() + "\"")
                        .filter(s -> s.contains(remaining))
                        .forEach(builder::suggest);
                return builder.buildFuture();
            };

    private static final SuggestionProvider<CommandSourceStack> WAVE_SUGGESTIONS =
            (context, builder) -> {
                try {
                    String arenaName = StringArgumentType.getString(context, "arena");
                    MobArena arena = MobArenaManager.getArena(arenaName);
                    if (arena != null) {
                        for (int i = 1; i <= arena.getWaveCount(); i++) {
                            builder.suggest(i);
                        }
                    }
                } catch (Exception ignored) {
                }
                return builder.buildFuture();
            };

    public static void registerModCommands() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                dispatcher.register(
                Commands.literal("succorstadiums")
                        // --- Arena commands ---
                        .then(Commands.literal("create_mobArena")
                                .then(Commands.argument("name", StringArgumentType.word())
                                        .then(Commands.argument("center_position", Vec3Argument.vec3())
                                                .then(Commands.argument("radius", IntegerArgumentType.integer(1))
                                                        .then(Commands.argument("delay_seconds", IntegerArgumentType.integer(0))
                                                                .executes(ModCommands::createMobArena)
                                                        )
                                                )
                                        )
                                )
                        )
                        .then(Commands.literal("remove_mobArena")
                                .then(Commands.argument("name", StringArgumentType.word())
                                        .suggests(ARENA_SUGGESTIONS)
                                        .executes(ModCommands::removeMobArena)
                                )
                        )
                        .then(Commands.literal("list_mobArenas")
                                .executes(ModCommands::listMobArenas)
                        )

                        // --- Wave commands ---
                        .then(Commands.literal("add_WaveToMobArena")
                                .then(Commands.argument("arena", StringArgumentType.word())
                                        .suggests(ARENA_SUGGESTIONS)
                                        .executes(ModCommands::addWaveToMobArena)
                                )
                        )
                        .then(Commands.literal("add_MobsToMobArena")
                                .then(Commands.argument("arena", StringArgumentType.word())
                                        .suggests(ARENA_SUGGESTIONS)
                                        .then(Commands.argument("wave_number", IntegerArgumentType.integer(1))
                                                .suggests(WAVE_SUGGESTIONS)
                                                .then(Commands.argument("mob_type", StringArgumentType.string())
                                                        .suggests(MOB_SUGGESTIONS)
                                                        .then(Commands.argument("count", IntegerArgumentType.integer(1))
                                                                .executes(ModCommands::addMobsToWave)
                                                        )
                                                )
                                        )
                                )
                        )
                        .then(Commands.literal("list_Waves")
                                .then(Commands.argument("arena", StringArgumentType.word())
                                        .suggests(ARENA_SUGGESTIONS)
                                        .executes(ModCommands::listWaves)
                                )
                        )
                        .then(Commands.literal("start_MobArena")
                                .then(Commands.argument("arena", StringArgumentType.word())
                                        .suggests(ARENA_SUGGESTIONS)
                                        .then(Commands.argument("players", EntityArgument.players())
                                                .executes(ModCommands::startMobArena)
                                        )
                                )
                        )
                        .then(Commands.literal("stop_MobArena")
                                .then(Commands.argument("arena", StringArgumentType.word())
                                        .suggests(ARENA_SUGGESTIONS)
                                        .executes(ModCommands::stopMobArena)
                                )
                        )
                        .then(Commands.literal("remove_WaveFromMobArena")
                                .then(Commands.argument("arena", StringArgumentType.word())
                                        .suggests(ARENA_SUGGESTIONS)
                                        .then(Commands.argument("wave_number", IntegerArgumentType.integer(1))
                                                .suggests(WAVE_SUGGESTIONS)
                                                .executes(ModCommands::removeWaveFromMobArena)
                                        )
                                )
                        )
        ));
    }

    // --- Arena handlers ---

    private static int createMobArena(CommandContext<CommandSourceStack> context) {
        String name = StringArgumentType.getString(context, "name");
        Vec3 pos = Vec3Argument.getVec3(context, "center_position");
        int radius = IntegerArgumentType.getInteger(context, "radius");
        int delay = IntegerArgumentType.getInteger(context, "delay_seconds");

        boolean created = MobArenaManager.createArena(name, pos.x, pos.y, pos.z, radius, delay);

        if (!created) {
            context.getSource().sendFailure(Component.literal("§cArena §6\"" + name + "\" §calready exists!"));
            return 0;
        }

        context.getSource().sendSuccess(
                () -> Component.literal("§aCreated mob arena.\n§6Name: \"" + name + "\"\n\n§fX: " + (int) pos.x + "\nY: " + (int) pos.y + "\nZ: " + (int) pos.z + "\n\n§eRadius: " + radius + "b\n§dWave Delay: " + delay + "s"),
                false
        );
        return 1;
    }

    private static int removeMobArena(CommandContext<CommandSourceStack> context) {
        String name = StringArgumentType.getString(context, "name");
        boolean removed = MobArenaManager.removeArena(name);

        if (!removed) {
            context.getSource().sendFailure(Component.literal("§cArena §6\"" + name + "\"§c does not exist!"));
            return 0;
        }

        context.getSource().sendSuccess(() -> Component.literal("§aRemoved mob arena §6\"" + name + "\"§a successfully!"), false);
        return 1;
    }

    private static int listMobArenas(CommandContext<CommandSourceStack> context) {
        Collection<MobArena> arenas = MobArenaManager.getAllArenas();

        if (arenas.isEmpty()) {
            context.getSource().sendSuccess(() -> Component.literal("§cNo mob arenas have been created yet."), false);
            return 0;
        }

        StringBuilder sb = new StringBuilder("Mob Arenas (" + arenas.size() + "):\n");
        for (MobArena arena : arenas) {
            sb.append("§f - §6\"").append(arena.getName())
                    .append("\"\n\n§fX: ").append((int) arena.getCenterX())
                    .append("\nY: ").append((int) arena.getCenterY())
                    .append("\nZ: ").append((int) arena.getCenterZ())
                    .append("\n\n§eRadius: ").append(arena.getRadius())
                    .append("b\n§dWave Delay: ").append(arena.getDelayBetweenWaves()).append("s")
                    .append("\n\n§bWaves: ").append(arena.getWaveCount())
                    .append("\n\n");
        }

        String message = sb.toString();
        context.getSource().sendSuccess(() -> Component.literal(message), false);
        return 1;
    }

    // --- Wave handlers ---

    private static int addWaveToMobArena(CommandContext<CommandSourceStack> context) {
        String arenaName = StringArgumentType.getString(context, "arena");

        MobArena arena = MobArenaManager.getArena(arenaName);
        if (arena == null) {
            context.getSource().sendFailure(Component.literal("§cArena §6\"" + arenaName + "\"§c does not exist!"));
            return 0;
        }

        Wave wave = arena.addWave();
        MobArenaManager.save();

        context.getSource().sendSuccess(
                () -> Component.literal("§aAdded §bwave " + wave.getWaveNumber() + "§a to arena §6\"" + arenaName + "\" §asuccesfully!"),
                false
        );
        return 1;
    }

    private static int addMobsToWave(CommandContext<CommandSourceStack> context) {
        String arenaName = StringArgumentType.getString(context, "arena");
        int waveNumber = IntegerArgumentType.getInteger(context, "wave_number");
        String mobType = StringArgumentType.getString(context, "mob_type");
        int count = IntegerArgumentType.getInteger(context, "count");

        MobArena arena = MobArenaManager.getArena(arenaName);
        if (arena == null) {
            context.getSource().sendFailure(Component.literal("§cArena §6\"" + arenaName + "\"§c does not exist!"));
            return 0;
        }

        Wave wave = arena.getWave(waveNumber);
        if (wave == null) {
            context.getSource().sendFailure(Component.literal("§bWave " + waveNumber + " §cdoes not exist in arena §6\"" + arenaName + "\"§c!"));
            return 0;
        }

        wave.addMob(mobType, count);
        MobArenaManager.save();

        context.getSource().sendSuccess(
                () -> Component.literal("§aAdded §e" + count + "x §2" + mobType + " §ato §bwave " + waveNumber + " §aof arena §6\"" + arenaName + "\" §asuccesfully!"),
                false
        );
        return 1;
    }

    private static int removeWaveFromMobArena(CommandContext<CommandSourceStack> context) {
        String arenaName = StringArgumentType.getString(context, "arena");
        int waveNumber = IntegerArgumentType.getInteger(context, "wave_number");

        MobArena arena = MobArenaManager.getArena(arenaName);
        if (arena == null) {
            context.getSource().sendFailure(Component.literal("§cArena §6\"" + arenaName + "\" §cdoes not exist!"));
            return 0;
        }

        boolean removed = arena.removeWave(waveNumber);
        if (!removed) {
            context.getSource().sendFailure(Component.literal("§bWave " + waveNumber + " §cdoes not exist in arena §6\"" + arenaName + "\"§c!"));
            return 0;
        }

        MobArenaManager.save();

        context.getSource().sendSuccess(
                () -> Component.literal("§aRemoved §bwave " + waveNumber + " §afrom arena §6\"" + arenaName + "\" §aremaining waves have been re-numbered successfully!"),
                false
        );
        return 1;
    }

    private static int listWaves(CommandContext<CommandSourceStack> context) {
        String arenaName = StringArgumentType.getString(context, "arena");

        MobArena arena = MobArenaManager.getArena(arenaName);
        if (arena == null) {
            context.getSource().sendFailure(Component.literal("§cArena §6\"" + arenaName + "\" §cdoes not exist!"));
            return 0;
        }

        if (arena.getWaves().isEmpty()) {
            context.getSource().sendSuccess(() -> Component.literal("§cArena §6\"" + arenaName + "\" §chas no waves yet."), false);
            return 0;
        }

        StringBuilder sb = new StringBuilder("Mob Arena: \n - §6\"" + arenaName + "\"\n§f - §dWave Delay: " + arena.getDelayBetweenWaves() + "s\n\n§f");
        for (Wave wave : arena.getWaves()) {
            sb.append(" §f- §bWave ").append(wave.getWaveNumber())
                    .append(" §f| §eTotal wave mob count: ").append(wave.getTotalMobCount()).append("\n");

            wave.getMobs().forEach(mob ->
                    sb.append("       §f- §e").append(mob.getCount()).append("x §2").append(mob.getMobType()).append("\n")
            );
        }

        String message = sb.toString();
        context.getSource().sendSuccess(() -> Component.literal(message), false);
        return 1;
    }


// --- Session handlers ---

    private static int startMobArena(CommandContext<CommandSourceStack> context) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        String arenaName = StringArgumentType.getString(context, "arena");
        MobArena arena = MobArenaManager.getArena(arenaName);

        if (arena == null) {
            context.getSource().sendFailure(Component.literal("§cArena §6\"" + arenaName + "\" §cdoes not exist!"));
            return 0;
        }

        if (arena.getWaves().isEmpty()) {
            context.getSource().sendFailure(Component.literal("§cArena §6\"" + arenaName + "\" §chas no waves configured!"));
            return 0;
        }

        if (ArenaSessionManager.isRunning(arenaName)) {
            context.getSource().sendFailure(Component.literal("§cArena §6\"" + arenaName + "\" §cis already running!"));
            return 0;
        }

        List<ServerPlayer> players = new java.util.ArrayList<>(EntityArgument.getPlayers(context, "players"));
        ServerLevel level = context.getSource().getLevel();

        ArenaSession session = new ArenaSession(arena, level, players);
        ArenaSessionManager.startSession(session);

        context.getSource().sendSuccess(
                () -> Component.literal("§aStarted arena §6\"" + arenaName + "\" §awith §e" + players.size() + " §aplayer(s)!"),
                false
        );
        return 1;
    }

    private static int stopMobArena(CommandContext<CommandSourceStack> context) {
        String arenaName = StringArgumentType.getString(context, "arena");

        if (!ArenaSessionManager.isRunning(arenaName)) {
            context.getSource().sendFailure(Component.literal("§cArena §6\"" + arenaName + "\" §cis not currently running!"));
            return 0;
        }

        // 1. Get the active session instance from your manager before stopping it
        // (Replace 'ArenaSession' with whatever your session object class name actually is)
        ArenaSession session = ArenaSessionManager.getSession(arenaName);

        if (session != null) {
            // 2. Call the method on the actual instance variable (lowercase 'session')
            session.KillCurrentWave();
        }

        // 3. Stop the session afterward
        ArenaSessionManager.stopSession(arenaName);

        context.getSource().sendSuccess(() -> Component.literal("§aStopped arena §6\"" + arenaName + "\" §asuccessfully!"), false);
        return 1;
    }
}