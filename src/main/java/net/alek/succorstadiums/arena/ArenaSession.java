package net.alek.succorstadiums.arena;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static net.alek.succorstadiums.SuccorStadiums.MOD_ID;

// Arena session class
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

    // Constructor to create an arena session
    public ArenaSession(MobArena arena, ServerLevel level, List<ServerPlayer> players) {
        this.arena = arena;
        this.level = level;
        this.players = players;
    }

    // Arena start method
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

    // Arena tick method
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
                                            } else if (id.contains("skull") || id.contains("head") || id.contains("pumpkin")) {
                                                mob.setItemSlot(EquipmentSlot.HEAD, stack);
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
                                // parts[0]=namespace, parts[1]=path, parts[2]=duration, parts[3]=amplifier
                                // e.g. "minecraft:strength:60:1" → 4 parts
                                if (parts.length < 4) {
                                    broadcast("§cInvalid potion effect entry '" + entry + "' (expected namespace:id:duration:amplifier)");
                                    continue;
                                }
                                String effectId  = parts[0] + ":" + parts[1];
                                String durStr    = parts[2];
                                String ampStr    = parts[3];
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
                                if (parts.length < 4) {
                                    broadcast("§cInvalid enchantment entry '" + entry + "' (expected target:namespace:id:level)");
                                    continue;
                                }
                                String target    = parts[0];
                                String enchantId = parts[1] + ":" + parts[2];
                                String lvlStr    = parts[3];

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
        finished = true;

        if (bossBar != null) players.forEach(bossBar::removePlayer);

        broadcast("§aCurrent wave has been discarded and the arena stopped!");
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
        players.forEach(p -> p.sendSystemMessage(Component.literal(message)));
    }

    // Accessor methods to get arena and finished state
    public MobArena getArena() { return arena; }
    public boolean isFinished() { return finished; }
}