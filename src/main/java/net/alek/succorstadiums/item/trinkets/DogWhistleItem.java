package net.alek.succorstadiums.item.trinkets;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import org.jspecify.annotations.NonNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

// Dog Whistle Item
public class DogWhistleItem extends Item {
    public DogWhistleItem(Properties properties) {
        super(properties);
    }

    // Ticks for cooldown and despawn timers
    private static final int COOLDOWN_TICKS = 3900;
    private static final int DESPAWN_TICKS = 900;

    // Tracks wolves and the game time they should despawn at
    public static final Map<UUID, Long> SUMMONED_WOLVES = new HashMap<>();

    // Override the use method in Item
    @Override
    @NonNull
    public InteractionResult use(Level level, @NonNull Player user, @NonNull InteractionHand usedHand) {

        // Check to see if level is not client sided
        if (!level.isClientSide()) {

            // Create a new server level and get user position as a vector
            ServerLevel serverLevel = (ServerLevel) level;
            Vec3 userPos = user.position();

            for (int i = 0; i < 5; i++) {
                // Create a new wolf
                Wolf wolf = EntityType.WOLF.create(serverLevel, EntitySpawnReason.MOB_SUMMONED);
                
                // If wolf is not null then set properties for wolf
                if (wolf != null) {

                    // Create offsets for the users x and z position
                    double offsetX = (serverLevel.getRandom().nextDouble() - 0.5) * 3;
                    double offsetZ = (serverLevel.getRandom().nextDouble() - 0.5) * 3;

                    // Set wolf position to the user x, y and z, with offsets
                    wolf.setPos(userPos.x + offsetX, userPos.y, userPos.z + offsetZ);

                    // Set the wolf owner, tamed state, and target to null
                    wolf.setOwner(user);
                    wolf.setTame(true, false);
                    wolf.setTarget(null);

                    // Add the wolf to the level
                    serverLevel.addFreshEntity(wolf);

                    // Store after adding so the UUID is finalized
                    SUMMONED_WOLVES.put(wolf.getUUID(), serverLevel.getGameTime() + DESPAWN_TICKS);
                }
            }

            // Play the wolf growl sound and set a cooldown
            level.playSound(null, userPos.x, userPos.y, userPos.z, SoundEvents.WOLF_GROWL_BABY, SoundSource.PLAYERS, 1.0F, 1.0F);
            user.getCooldowns().addCooldown(this.getDefaultInstance(), COOLDOWN_TICKS);
        }

        // Return interaction result success
        return InteractionResult.SUCCESS;
    }
}