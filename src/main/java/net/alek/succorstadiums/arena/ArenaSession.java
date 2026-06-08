package net.alek.succorstadiums.arena;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.BossEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.levelgen.Heightmap;
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
    private int totalMobsInWave = 0;
    private final List<UUID> activeMobUUIDs = new ArrayList<>();
    private boolean waitingForNextWave = false;
    private int delayTicksRemaining = 0;
    private boolean finished = false;

    private ServerBossEvent bossBar;

    public ArenaSession(MobArena arena, ServerLevel level, List<ServerPlayer> players) {
        this.arena = arena;
        this.level = level;
        this.players = players;
    }

    public void start() {
        bossBar = new ServerBossEvent(
                java.util.UUID.randomUUID(),
                Component.literal("Starting..."),
                BossEvent.BossBarColor.RED,
                BossEvent.BossBarOverlay.PROGRESS
        );
        players.forEach(bossBar::addPlayer);

        broadcast("§6--- " + arena.getName() + " has begun! ---");
        spawnCurrentWave();
    }

    public void tick() {
        if (finished) return;

        if (waitingForNextWave) {
            int secsLeft = (delayTicksRemaining / 20) + 1;
            bossBar.setName(Component.literal("§eNext wave in " + secsLeft + "s..."));
            bossBar.setProgress((float) delayTicksRemaining / (arena.getDelayBetweenWaves() * 20));
            bossBar.setColor(BossEvent.BossBarColor.YELLOW);

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

        // Update boss bar
        int remaining = activeMobUUIDs.size();
        int waveNum = currentWaveIndex + 1;
        int totalWaves = arena.getWaveCount();
        bossBar.setName(Component.literal(
                "§6" + arena.getName() + " §f- §bWave: " + waveNum + "/" + totalWaves + "§f - §cEnemies Remaining: " + remaining
        ));
        float progress = totalMobsInWave > 0 ? (float) remaining / totalMobsInWave : 0f;
        bossBar.setProgress(Math.clamp(progress, 0f, 1f));
        bossBar.setColor(BossEvent.BossBarColor.RED);

        if (remaining == 0) {
            currentWaveIndex++;

            if (currentWaveIndex >= arena.getWaves().size()) {
                finished = true;
                bossBar.setName(Component.literal("§a All waves complete! You win!"));
                bossBar.setProgress(1f);
                bossBar.setColor(BossEvent.BossBarColor.GREEN);
                players.forEach(bossBar::removePlayer);
                broadcast("§a-- All waves complete! You win! ---");
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
        broadcast("§c--- Wave " + wave.getWaveNumber() + " / " + arena.getWaveCount() + " ---");

        try {
            for (WaveMob waveMob : wave.getMobs()) {
                Optional<EntityType<?>> entityTypeOpt = BuiltInRegistries.ENTITY_TYPE
                        .getOptional(Identifier.parse(waveMob.getMobType()));

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

                    if (entity instanceof Mob mob) {
                        mob.finalizeSpawn(
                                (ServerLevelAccessor) level,
                                level.getCurrentDifficultyAt(mob.blockPosition()),
                                EntitySpawnReason.COMMAND,
                                null
                        );
                    }

                    level.addFreshEntity(entity);
                    activeMobUUIDs.add(entity.getUUID());
                }
            }

            totalMobsInWave = activeMobUUIDs.size();

            bossBar.setColor(BossEvent.BossBarColor.RED);
            bossBar.setProgress(1f);
            bossBar.setName(Component.literal(
                    "Wave " + wave.getWaveNumber() + "/" + arena.getWaveCount() + " — " + totalMobsInWave + " mobs remaining"
            ));

            broadcast("§cSurvive! " + totalMobsInWave + " mobs spawned.");

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
        finished = true;

        if (bossBar != null) {
            players.forEach(bossBar::removePlayer);
        }

        broadcast("§aCurrent wave has been discarded and the arena stopped!");
    }

    private Vec3 randomPositionInRadius() {
        double angle = level.getRandom().nextDouble() * 2 * Math.PI;
        double actualRadius = arena.getRadius() / 2.0;
        double distance = Math.sqrt(level.getRandom().nextDouble()) * actualRadius;
        double x = arena.getCenterX() + distance * Math.cos(angle);
        double z = arena.getCenterZ() + distance * Math.sin(angle);

        BlockPos pos = new BlockPos((int) x, (int) arena.getCenterY(), (int) z);
        int surfaceY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, pos.getX(), pos.getZ());

        return new Vec3(x, surfaceY, z);
    }

    private void broadcast(String message) {
        players.forEach(p -> p.sendSystemMessage(Component.literal(message)));
    }

    public boolean isFinished() { return finished; }
    public MobArena getArena() { return arena; }
}