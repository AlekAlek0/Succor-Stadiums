package net.alek.succorstadiums.arena;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static net.alek.succorstadiums.SuccorStadiums.MOD_ID;

public class ArenaSession {

    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private final MobArena arena;
    private final ServerLevel level;
    private final List<ServerPlayer> players;

    private int currentWaveIndex = 0;
    private final List<UUID> activeMobUUIDs = new ArrayList<>();
    private boolean waitingForNextWave = false;
    private int delayTicksRemaining = 0;
    private boolean finished = false;

    public ArenaSession(MobArena arena, ServerLevel level, List<ServerPlayer> players) {
        this.arena = arena;
        this.level = level;
        this.players = players;
    }

    public void start() {
        broadcast("§6=== " + arena.getName() + " has begun! ===");
        spawnCurrentWave();
    }

    public void tick() {
        if (finished) return;

        if (waitingForNextWave) {
            delayTicksRemaining--;
            if (delayTicksRemaining <= 0) {
                waitingForNextWave = false;
                spawnCurrentWave();
            }
            return;
        }

        activeMobUUIDs.removeIf(uuid -> {
            Entity entity = level.getEntity(uuid);
            return entity == null || !entity.isAlive();
        });

        if (activeMobUUIDs.isEmpty()) {
            currentWaveIndex++;

            if (currentWaveIndex >= arena.getWaves().size()) {
                finished = true;
                broadcast("§a=== All waves complete! You win! ===");
            } else {
                int delaySecs = arena.getDelayBetweenWaves();
                broadcast("§eWave " + currentWaveIndex + " cleared! Next wave in " + delaySecs + " seconds...");
                delayTicksRemaining = delaySecs * 20;
                waitingForNextWave = true;
            }
        }
    }

    private void spawnCurrentWave() {

        Wave wave = arena.getWaves().get(currentWaveIndex);
        broadcast("§c=== Wave " + wave.getWaveNumber() + " / " + arena.getWaveCount() + " ===");

        try {
            for (WaveMob waveMob : wave.getMobs()) {
                Optional<EntityType<?>> entityTypeOpt = BuiltInRegistries.ENTITY_TYPE
                        .getOptional(Identifier.parse(waveMob.getMobType()));

                // Send error if mob type is unknown, skip to next
                if (entityTypeOpt.isEmpty()) {
                    broadcast("§cUnknown mob type '" + waveMob.getMobType() + "', skipping mob!");
                    continue;
                }

                EntityType<?> entityType = entityTypeOpt.get();

                for (int i = 0; i < waveMob.getCount(); i++) {
                    Vec3 spawnPos = randomPositionInRadius();

                    Entity entity = entityType.create(level, EntitySpawnReason.COMMAND);
                    if (entity == null) {
                        broadcast("§cCould not create entity for '" + waveMob.getMobType() + "'");
                        continue;
                    }

                    entity.snapTo(spawnPos.x, spawnPos.y, spawnPos.z,
                            level.getRandom().nextFloat() * 360f, 0f);

                    level.addFreshEntity(entity);
                    activeMobUUIDs.add(entity.getUUID());
                }
            }

            broadcast("§cSurvive! " + activeMobUUIDs.size() + " mobs spawned.");

        } catch (Exception e) {
            broadcast("§4Spawn error: " + e.getClass().getSimpleName() + ": " + e.getMessage());
            LOGGER.error("", e);
        }
    }

    public void KillCurrentWave() {

        for (UUID uuid : activeMobUUIDs) {
            Entity entity = level.getEntity(uuid);
            if (entity != null) {
                entity.discard();
            }
        }

        activeMobUUIDs.clear();
        this.finished = true;

        broadcast("§aCurrent wave has been discarded and the arena stopped!");
    }

    private Vec3 randomPositionInRadius() {

        // Get a random angle around the circle
        double angle = level.getRandom().nextDouble() * 2 * Math.PI;

        // Treat the configured 'radius' as the total diameter (the full middle line).
        // Divide it by 2 to get the actual mathematical radius from the center point.
        double actualRadius = arena.getRadius() / 2.0;

        // Evenly distribute the spawns across this area
        double distance = Math.sqrt(level.getRandom().nextDouble()) * actualRadius;

        // Calculate final positions relative to your center point
        double x = arena.getCenterX() + distance * Math.cos(angle);
        double z = arena.getCenterZ() + distance * Math.sin(angle);

        // Align perfectly with the first floor
        net.minecraft.core.BlockPos pos = new net.minecraft.core.BlockPos((int) x, (int) arena.getCenterY(), (int) z);
        int surfaceY = level.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, pos.getX(), pos.getZ());

        return new Vec3(x, surfaceY, z);
    }

    private void broadcast(String message) {
        players.forEach(p -> p.sendSystemMessage(Component.literal(message)));
    }

    public boolean isFinished() { return finished; }
    public MobArena getArena() { return arena; }
}