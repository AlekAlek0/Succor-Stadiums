package net.alek.succorstadiums.arena;

import net.alek.succorstadiums.SuccorStadiumsConstants; // Import the new constants class
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.BossEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

import static net.alek.succorstadiums.SuccorStadiums.MOD_ID;

// Arena session class
public class ArenaSession {

    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private final MobArena arena;
    private final ServerLevel level;
    private final List<ServerPlayer> initialPlayers; // All players who started the arena
    private final Set<UUID> activePlayerUUIDs;       // Players currently alive in the arena

    private int currentWaveIndex = 0;
    private int totalMobsInWave = 0;
    private final List<UUID> activeMobUUIDs = new ArrayList<>();
    private boolean waitingForNextWave = false;
    private int delayTicksRemaining = 0;

    // Replaced 'finished' and 'playerLost' with ArenaState
    private enum ArenaState { RUNNING, WIN, LOSS }
    private ArenaState state = ArenaState.RUNNING;

    private ServerBossEvent bossBar;

    // Removed the local CUSTOM_MOB_HEALTH map and its static initializer

    // Constructor to create an arena session
    public ArenaSession(MobArena arena, ServerLevel level, List<ServerPlayer> players) {
        this.arena = arena;
        this.level = level;
        this.initialPlayers = new ArrayList<>(players);
        this.activePlayerUUIDs = players.stream().map(ServerPlayer::getUUID).collect(Collectors.toSet());
    }

    // Arena start method
    public void start() {
        bossBar = new ServerBossEvent(
                java.util.UUID.randomUUID(),
                Component.literal("Starting..."),
                BossEvent.BossBarColor.RED,
                BossEvent.BossBarOverlay.PROGRESS
        );
        initialPlayers.forEach(bossBar::addPlayer);

        broadcast("§6--- " + arena.getName() + " has begun! ---");
        spawnCurrentWave();
    }

    // Arena tick method
    public void tick() {
        if (state != ArenaState.RUNNING) return; // Only tick if running

        // Check if all players are dead
        if (activePlayerUUIDs.isEmpty()) {
            endArena(ArenaState.LOSS); // Players lost
            return;
        }

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
                endArena(ArenaState.WIN); // Players won
            } else {
                int delaySecs = arena.getDelayBetweenWaves();
                broadcast("§eWave " + currentWaveIndex + " cleared! Next wave in " + delaySecs + " seconds...");
                delayTicksRemaining = delaySecs * 20;
                waitingForNextWave = true;
            }
        }
    }

    // Helper method to spawn current wave in arena
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
                                level,
                                level.getCurrentDifficultyAt(mob.blockPosition()),
                                EntitySpawnReason.COMMAND,
                                null
                        );

                        // Apply custom health from shared constants if applicable, AFTER finalizeSpawn
                        if (SuccorStadiumsConstants.MOB_HEALTH_OVERRIDES.containsKey(entityType)) {
                            AttributeInstance maxHealthAttribute = mob.getAttribute(Attributes.MAX_HEALTH);
                            if (maxHealthAttribute != null) {
                                double customHealth = SuccorStadiumsConstants.MOB_HEALTH_OVERRIDES.get(entityType);
                                maxHealthAttribute.setBaseValue(customHealth);
                                mob.setHealth((float) customHealth); // Set current health to new max
                            }
                        }

                        // Apply size/age for slimes and zombies/zombie villagers
                        if (waveMob.getSize() != null) {
                            if (entityType == EntityType.SLIME) {
                                // Slime size: 1 (small), 2 (medium), 4 (large)
                                ((net.minecraft.world.entity.monster.Slime) mob).setSize(waveMob.getSize(), true);
                            } else if (entityType == EntityType.ZOMBIE || entityType == EntityType.ZOMBIE_VILLAGER) {
                                // Zombie age: -1 (baby), 0 (adult)
                                if (waveMob.getSize() == -1) {
                                    mob.setBaby(true);
                                } else if (waveMob.getSize() == 0) {
                                    mob.setBaby(false);
                                }
                            }
                        }

                        // ── Riding mob ────────────────────────────────────────
                        if (waveMob.getRidingMob() != null && !waveMob.getRidingMob().isEmpty()) {
                            Optional<EntityType<?>> ridingEntityTypeOpt = BuiltInRegistries.ENTITY_TYPE
                                    .getOptional(Identifier.parse(waveMob.getRidingMob()));
                            if (ridingEntityTypeOpt.isPresent()) {
                                Entity ridingEntity = ridingEntityTypeOpt.get().create(level, EntitySpawnReason.COMMAND);
                                if (ridingEntity != null) {
                                    ridingEntity.snapTo(spawnPos.x, spawnPos.y, spawnPos.z,
                                            level.getRandom().nextFloat() * 360f, 0f);
                                    level.addFreshEntity(ridingEntity);
                                    entity.startRiding(ridingEntity);
                                    activeMobUUIDs.add(ridingEntity.getUUID());
                                } else {
                                    broadcast("§cCould not create riding entity for '" + waveMob.getRidingMob() + "'");
                                }
                            } else {
                                broadcast("§cUnknown riding mob type '" + waveMob.getRidingMob() + "'");
                            }
                        }

                        // ── Main hand item ────────────────────────────────────
                        if (waveMob.getMainHandItem() != null && !waveMob.getMainHandItem().isEmpty()) {
                            BuiltInRegistries.ITEM.getOptional(Identifier.parse(waveMob.getMainHandItem()))
                                    .ifPresentOrElse(
                                            item -> mob.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(item)),
                                            () -> broadcast("§cUnknown main hand item '" + waveMob.getMainHandItem() + "'")
                                    );
                        }

                        // ── Offhand item ─────────────────────────────────────
                        if (waveMob.getOffHandItem() != null && !waveMob.getOffHandItem().isEmpty()) {
                            BuiltInRegistries.ITEM.getOptional(Identifier.parse(waveMob.getOffHandItem()))
                                    .ifPresentOrElse(
                                            item -> mob.setItemSlot(EquipmentSlot.OFFHAND, new ItemStack(item)),
                                            () -> broadcast("§cUnknown off hand item '" + waveMob.getOffHandItem() + "'")
                                    );
                        }

                        // ── Armor items ───────────────────────────────────────
                        if (waveMob.getArmorItems() != null && !waveMob.getArmorItems().isEmpty()) {
                            for (String armorItemId : waveMob.getArmorItems()) {
                                BuiltInRegistries.ITEM.getOptional(Identifier.parse(armorItemId))
                                        .ifPresentOrElse(item -> {
                                            ItemStack stack = new ItemStack(item);
                                            String id = armorItemId.toLowerCase();
                                            if (id.contains("helmet")) {
                                                mob.setItemSlot(EquipmentSlot.HEAD, stack);
                                            } else if (id.contains("chestplate")) {
                                                mob.setItemSlot(EquipmentSlot.CHEST, stack);
                                            } else if (id.contains("leggings")) {
                                                mob.setItemSlot(EquipmentSlot.LEGS, stack);
                                            } else if (id.contains("boots")) {
                                                mob.setItemSlot(EquipmentSlot.FEET, stack);
                                            } else {
                                                broadcast("§cUnknown armor slot for '" + armorItemId + "'");
                                            }
                                        }, () -> broadcast("§cUnknown armor item '" + armorItemId + "'"));
                            }
                        }

                        // ── Potion effects ────────────────────────────────────
                        // Format: "effectId:durationSeconds:amplifier,..."
                        if (waveMob.getPotionEffects() != null && !waveMob.getPotionEffects().isEmpty()) {
                            for (String entry : waveMob.getPotionEffects().split(",")) {
                                String[] parts = entry.trim().split(":");
                                // Expected format from client: "effectId:duration:amplifier"
                                // effectId can be "strength" or "minecraft:strength"
                                if (parts.length < 3) {
                                    broadcast("§cInvalid potion effect entry '" + entry + "' (expected effectId:duration:amplifier)");
                                    continue;
                                }

                                // The last two parts are always duration and amplifier
                                String ampStr = parts[parts.length - 1];
                                String durStr = parts[parts.length - 2];

                                // The effectId is everything before the last two parts
                                String effectId = String.join(":", Arrays.copyOfRange(parts, 0, parts.length - 2));

                                try {
                                    int durationTicks = Integer.parseInt(durStr) * 20;
                                    int amplifier     = Integer.parseInt(ampStr);
                                    BuiltInRegistries.MOB_EFFECT
                                            .getOptional(Identifier.parse(effectId))
                                            .ifPresentOrElse(
                                                    effect -> {
                                                        var registry = level.registryAccess()
                                                                .lookupOrThrow(Registries.MOB_EFFECT);

                                                        var key = net.minecraft.resources.ResourceKey.create(
                                                                Registries.MOB_EFFECT,
                                                                Identifier.parse(effectId)
                                                        );

                                                        var holder = registry.get(key);

                                                        if (holder.isPresent()) {
                                                            mob.addEffect(new MobEffectInstance(
                                                                    holder.get(),
                                                                    durationTicks,
                                                                    amplifier
                                                            ));
                                                        } else {
                                                            broadcast("§cUnknown potion effect '" + effectId + "'");
                                                        }
                                                    },
                                                    () -> broadcast("§cUnknown potion effect '" + effectId + "'")
                                            );
                                } catch (NumberFormatException e) {
                                    broadcast("§cInvalid potion effect numbers in '" + entry + "'");
                                }
                            }
                        }

                        // ── Enchantments ──────────────────────────────────────
                        // Format: "target:namespace:enchantId:level,..."
                        // e.g. "mainhand:minecraft:sharpness:5"
                        if (waveMob.getEnchantments() != null && !waveMob.getEnchantments().isEmpty()) {
                            for (String entry : waveMob.getEnchantments().split(",")) {
                                String[] parts = entry.trim().split(":");
                                // parts[0]=target, parts[1]=namespace, parts[2]=enchant, parts[3]=level
                                if (parts.length < 3) { // Changed from < 4 to < 3, as target:enchantId:level is 3 parts
                                    broadcast("§cInvalid enchantment entry '" + entry + "' (expected target:enchantId:level)");
                                    continue;
                                }
                                String target    = parts[0];
                                String lvlStr    = parts[parts.length - 1]; // Last part is level
                                String enchantId = String.join(":", Arrays.copyOfRange(parts, 1, parts.length - 1)); // Middle parts form enchantId

                                EquipmentSlot slot = switch (target.toLowerCase()) {
                                    case "mainhand"   -> EquipmentSlot.MAINHAND;
                                    case "offhand"    -> EquipmentSlot.OFFHAND;
                                    case "helmet"     -> EquipmentSlot.HEAD;
                                    case "chestplate" -> EquipmentSlot.CHEST;
                                    case "leggings"   -> EquipmentSlot.LEGS;
                                    case "boots"      -> EquipmentSlot.FEET;
                                    default           -> null;
                                };

                                if (slot == null) {
                                    broadcast("§cUnknown enchantment target '" + target + "'");
                                    continue;
                                }

                                try {
                                    int level = Integer.parseInt(lvlStr);
                                    var enchantRegistry = this.level.registryAccess()
                                            .lookupOrThrow(Registries.ENCHANTMENT);
                                    Optional<Holder.Reference<Enchantment>> enchantOpt =
                                            enchantRegistry.get(net.minecraft.resources.ResourceKey.create(
                                                    Registries.ENCHANTMENT,
                                                    Identifier.parse(enchantId)));

                                    if (enchantOpt.isEmpty()) {
                                        broadcast("§cUnknown enchantment '" + enchantId + "'");
                                        continue;
                                    }

                                    ItemStack stack = mob.getItemBySlot(slot);
                                    if (stack.isEmpty()) {
                                        // Create a dummy item so the enchant has somewhere to live.
                                        // Using a book as fallback; adjust if needed.
                                        stack = new ItemStack(net.minecraft.world.item.Items.ENCHANTED_BOOK);
                                        mob.setItemSlot(slot, stack);
                                    }
                                    stack.enchant(enchantOpt.get(), level);

                                } catch (NumberFormatException e) {
                                    broadcast("§cInvalid enchantment level in '" + entry + "'");
                                }
                            }
                        }
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

    // Helper method to kill current wave in arena
    public void KillCurrentWave() {
        for (UUID uuid : activeMobUUIDs) {
            Entity entity = level.getEntity(uuid);
            if (entity != null) entity.discard();
        }

        activeMobUUIDs.clear();
        endArena(ArenaState.LOSS); // Force end as a loss if current wave is killed
    }

    /**
     * Called when a player dies in the arena.
     * @param player The player who died.
     */
    public void onPlayerDeath(ServerPlayer player) {
        if (activePlayerUUIDs.remove(player.getUUID())) {
            broadcast("§e" + player.getName().getString() + " has been eliminated!");
            if (activePlayerUUIDs.isEmpty()) {
                endArena(ArenaState.LOSS); // All players are dead, arena ends in loss
            }
        }
    }

    /**
     * Ends the arena session.
     * @param newState The state the arena should transition to (WIN or LOSS).
     */
    private void endArena(ArenaState newState) {
        // If already in a terminal state (LOSS), or if already WIN and trying to WIN again, do nothing.
        // A LOSS can always override a WIN.
        if (this.state == ArenaState.LOSS) {
            return; // Already lost, cannot change outcome
        }
        if (this.state == ArenaState.WIN && newState == ArenaState.WIN) {
            return; // Already won, and trying to win again, do nothing
        }

        this.state = newState; // Update the state

        // Clear any remaining mobs
        for (UUID uuid : activeMobUUIDs) {
            Entity entity = level.getEntity(uuid);
            if (entity != null) entity.discard();
        }
        activeMobUUIDs.clear();

        if (bossBar != null) {
            initialPlayers.forEach(bossBar::removePlayer);
            if (this.state == ArenaState.LOSS) {
                bossBar.setName(Component.literal("§cYou lost! Arena failed."));
                bossBar.setProgress(0f);
                bossBar.setColor(BossEvent.BossBarColor.RED);
                broadcast("§c--- " + arena.getName() + " failed! All players eliminated. ---");
            } else if (this.state == ArenaState.WIN) {
                bossBar.setName(Component.literal("§aAll waves complete! You win!"));
                bossBar.setProgress(1f);
                bossBar.setColor(BossEvent.BossBarColor.GREEN);
                broadcast("§a--- All waves complete! You win! ---");
            }
        }
        // Deregister this session
        ArenaSessionManager.stopSession(arena.getName());
    }


    // Helper method to generate a random position in the arena radius
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

    // Helper method to broadcast a given message to all players in arena
    private void broadcast(String message) {
        initialPlayers.forEach(p -> p.sendSystemMessage(Component.literal(message)));
    }

    // Accessor methods to get arena and finished state
    public MobArena getArena() { return arena; }
    public boolean isFinished() { return state != ArenaState.RUNNING; }
    public boolean hasPlayer(UUID playerUUID) { return activePlayerUUIDs.contains(playerUUID); }
}