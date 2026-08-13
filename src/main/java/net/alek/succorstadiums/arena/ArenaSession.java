package net.alek.succorstadiums.arena;

import net.alek.succorstadiums.entity.ModEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.BossEvent;
import net.minecraft.world.entity.monster.cubemob.Slime;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
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

    // All players who started the arena and players currently alive in the arena
    private final List<ServerPlayer> initialPlayers;
    private final Set<UUID> activePlayerUUIDs;

    private int currentWaveIndex = 0;
    private int totalMobsInWave = 0;
    private final List<UUID> activeMobUUIDs = new ArrayList<>();
    private boolean firstWave = true;
    private boolean waitingForNextWave = false;
    private int delayTicksRemaining = 0;
    private int currentDelayDurationTicks = 0;

    private enum ArenaState { RUNNING, WIN, LOSS }
    private ArenaState state = ArenaState.RUNNING;

    private ServerBossEvent bossBar;

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
                BossEvent.BossBarColor.YELLOW,
                BossEvent.BossBarOverlay.PROGRESS
        );

        // Teleport all selected players to the arena position
        initialPlayers.forEach(player -> {
            bossBar.addPlayer(player);

            player.teleportTo(
                    arena.getCenterX(),
                    arena.getCenterY(),
                    arena.getCenterZ()
            );
        });

        // Broadcast chat message that arena has begun
        broadcast("§6--- " + arena.getName() + " has begun! ---");

        //  First wave sown delay (falling back to the arena's default) determines the start delay
        int startDelaySecs = arena.getWaves().isEmpty()
                ? arena.getDelayBetweenWaves()
                : arena.getWaves().getFirst().getEffectiveDelay(arena.getDelayBetweenWaves());

        currentDelayDurationTicks = startDelaySecs * 20;
        delayTicksRemaining = currentDelayDurationTicks;
        waitingForNextWave = true;
    }

    // Arena tick method
    public void tick() {
        Wave wave = arena.getWaves().get(currentWaveIndex);

        // If arena is not running just return
        if (state != ArenaState.RUNNING) return;

        // Create vars to get remaining mobs in wave, current wave num,
        // total waves in arena, and create progress based on mobs remaining for boss bar
        int remaining = activeMobUUIDs.size();
        int waveNum = currentWaveIndex + 1;
        int totalWaves = arena.getWaveCount();
        float progress = totalMobsInWave > 0 ? (float) remaining / totalMobsInWave : 0f;

        // Check if all players are dead if true send arena state as loss and end
        if (activePlayerUUIDs.isEmpty()) {
            endArena(ArenaState.LOSS);
            return;
        }

        // If arena state is in middle of waves waiting for next wave setup and configure boss bar
        if (waitingForNextWave) {
            int secsLeft = (delayTicksRemaining / 20) + 1;

            bossBar.setName(Component.literal((firstWave
                            ? "§eFirst wave in " + secsLeft + "s..."
                            : "§eNext wave in " + secsLeft + "s...")
                            + " §f| §aPlayers: " + activePlayerUUIDs.size()
            ));

            bossBar.setProgress(currentDelayDurationTicks > 0
                            ? (float) delayTicksRemaining / currentDelayDurationTicks
                            : 0f
            );

            bossBar.setColor(BossEvent.BossBarColor.YELLOW);

            // Tick down on wave delay time
            delayTicksRemaining--;

            // If wave delay hits zero spawn next wave
            if (delayTicksRemaining <= 0) {
                waitingForNextWave = false;
                firstWave = false;
                spawnCurrentWave();
            }
            return;
        }

        // Remove mob from active wave mob list if mob is killed
        activeMobUUIDs.removeIf(uuid -> {
            Entity entity = level.getEntity(uuid);
            return entity == null || !entity.isAlive();
        });

        // Set up and configure boss bar for current wave
        bossBar.setName(Component.literal(
                "§6" + arena.getName()
                        + " §f- §b" + wave.getName() + " " + wave.getWaveNumber() + "/" + arena.getWaveCount()
                        + " §f- §cEnemies Remaining: " + remaining
                        + " §f- §aPlayers: " + activePlayerUUIDs.size()
        ));

        bossBar.setProgress(Math.clamp(progress, 0f, 1f));
        bossBar.setColor(BossEvent.BossBarColor.RED);

        // If remaining mobs in wave is zero start set up for next
        if (remaining == 0) {
            Wave clearedWave = arena.getWaves().get(currentWaveIndex);
            grantRewards(activePlayerUUIDs, clearedWave.getRewards());

            // Increment current wave by 1
            currentWaveIndex++;

            // If current wave is the same as total wave count then arena must be won and send arena state win
            if (currentWaveIndex >= arena.getWaves().size()) {
                grantRewards(activePlayerUUIDs, arena.getRewards());
                grantParticipationRewards();
                endArena(ArenaState.WIN);

            // if arena isn't won start setup for next
            } else {
                Wave nextWave = arena.getWaves().get(currentWaveIndex);
                int delaySecs = nextWave.getEffectiveDelay(arena.getDelayBetweenWaves());
                broadcast("§eWave " + currentWaveIndex + " cleared! Next wave in " + delaySecs + " seconds...");
                currentDelayDurationTicks = delaySecs * 20;
                delayTicksRemaining = currentDelayDurationTicks;
                waitingForNextWave = true;
            }
        }
    }

    // Helper method to spawn current wave in arena
    private void spawnCurrentWave() {
        Wave wave = arena.getWaves().get(currentWaveIndex);
        broadcast("§c-- " + wave.getName() + " -- " + wave.getWaveNumber() + " / " + arena.getWaveCount() + " --");

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

                        // Remove any vanilla equipment minecraft gives on mobs and set drop chance to 0%
                        for (EquipmentSlot slot : EquipmentSlot.values()) {
                            mob.setItemSlot(slot, ItemStack.EMPTY);
                            mob.setDropChance(slot, 0.0F);
                        }

                        if (entityType == EntityTypes.SLIME || entityType == ModEntityTypes.BANANA_SLIME) {
                            if (waveMob.getSize() != null) {
                                ((Slime) mob).setSize(waveMob.getSize(), true);
                            }
                        } else if (entityType == EntityTypes.ZOMBIE || entityType == EntityTypes.ZOMBIE_VILLAGER || entityType == ModEntityTypes.ZOMBIE_FARMER) {
                            mob.setBaby(waveMob.getSize() != null && waveMob.getSize() == -1);
                        }

                        // ── Riding mob ────────────────────────────────────────
                        if (waveMob.getRidingMob() != null && !waveMob.getRidingMob().isEmpty()) {
                            Optional<EntityType<?>> ridingEntityTypeOpt = BuiltInRegistries.ENTITY_TYPE
                                    .getOptional(Identifier.parse(waveMob.getRidingMob()));
                            if (ridingEntityTypeOpt.isPresent()) {Entity ridingEntity = ridingEntityTypeOpt.get().create(level, EntitySpawnReason.COMMAND);
                                if (ridingEntity != null) {ridingEntity.snapTo(spawnPos.x, spawnPos.y, spawnPos.z, level.getRandom().nextFloat() * 360f, 0f);
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
                                if (parts.length < 3) {
                                    broadcast("§cInvalid potion effect entry '" + entry + "' (expected effectId:duration:amplifier)");
                                    continue;
                                }

                                String ampStr = parts[parts.length - 1];
                                String durStr = parts[parts.length - 2];
                                String effectId = String.join(":", Arrays.copyOfRange(parts, 0, parts.length - 2));

                                try {
                                    int durationTicks = durStr.equals("-1")
                                            ? Integer.MAX_VALUE
                                            : Integer.parseInt(durStr) * 20;
                                    int amplifier     = Math.max(0, Integer.parseInt(ampStr) - 1);
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
                        if (waveMob.getEnchantments() != null && !waveMob.getEnchantments().isEmpty()) {
                            for (String entry : waveMob.getEnchantments().split(",")) {
                                String[] parts = entry.trim().split(":");
                                if (parts.length < 3) {
                                    broadcast("§cInvalid enchantment entry '" + entry + "' (expected target:enchantId:level)");
                                    continue;
                                }
                                String target    = parts[0];
                                String lvlStr    = parts[parts.length - 1];
                                String enchantId = String.join(":", Arrays.copyOfRange(parts, 1, parts.length - 1));

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
        endArena(ArenaState.LOSS);
    }

    // Helper method to handle a player death
    public void onPlayerDeath(ServerPlayer player) {
        if (activePlayerUUIDs.remove(player.getUUID())) {
            broadcast("§e" + player.getName().getString() + " has been eliminated!");
            if (activePlayerUUIDs.isEmpty()) {
                endArena(ArenaState.LOSS);
            }
        }
    }

    private void endArena(ArenaState newState) {
        if (this.state == ArenaState.LOSS) {
            return;
        }
        if (this.state == ArenaState.WIN && newState == ArenaState.WIN) {
            return;
        }

        this.state = newState;

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
        ArenaSessionManager.stopSession(arena.getName());
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

    // Accessor methods to get arena, arena state, and if a player is in the arena
    public MobArena getArena() { return arena; }
    public boolean isFinished() { return state != ArenaState.RUNNING; }
    public boolean hasPlayer(UUID playerUUID) {return activePlayerUUIDs.contains(playerUUID);}

    private void grantRewards(Collection<UUID> uuids, List<RewardItem> rewards) {
        if (rewards == null || rewards.isEmpty()) return;
        for (UUID uuid : uuids) {
            ServerPlayer player = level.getServer().getPlayerList().getPlayer(uuid);
            if (player != null) grantRewardsToPlayer(player, rewards);
        }
    }

    private void grantRewardsToPlayer(ServerPlayer player, List<RewardItem> rewards) {
        for (RewardItem reward : rewards) {
            if (reward.isXp()) {
                int amount = Math.max(0, reward.getCount());
                if (reward.isXpLevels()) {
                    player.giveExperienceLevels(amount);
                } else {
                    player.giveExperiencePoints(amount);
                }
                continue;
            }
            try {
                var itemOpt = BuiltInRegistries.ITEM.getOptional(Identifier.parse(reward.getItemId()));
                if (itemOpt.isEmpty()) {
                    broadcast("§cUnknown reward item '" + reward.getItemId() + "', skipping.");
                    continue;
                }
                ItemStack stack = new ItemStack(itemOpt.get(), reward.getCount());
                if (!player.getInventory().add(stack)) {
                    player.drop(stack, false);
                }
            } catch (Exception e) {
                broadcast("§cFailed to grant reward '" + reward.getItemId() + "': " + e.getMessage());
            }
        }
    }

    private void grantParticipationRewards() {
        List<RewardItem> rewards = arena.getParticipationRewards();
        if (rewards.isEmpty()) return;
        for (ServerPlayer player : initialPlayers) {
            if (!activePlayerUUIDs.contains(player.getUUID())) {
                grantRewardsToPlayer(player, rewards);
            }
        }
    }

    // Helper method that sends a chat message to each player in the arena
    private void broadcast(String message) {
        initialPlayers.forEach(p -> p.sendSystemMessage(Component.literal(message)));
    }
}