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

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DogWhistleItem extends Item {
    public DogWhistleItem(Properties properties) {
        super(properties);
    }

    private static final int COOLDOWN_TICKS = 3900;
    private static final int DESPAWN_TICKS = 900;

    // Tracks wolves and the game time they should despawn at
    public static final Map<UUID, Long> SUMMONED_WOLVES = new HashMap<>();

    @Override
    public InteractionResult use(Level level, Player user, InteractionHand usedHand) {
        if (!level.isClientSide()) {
            ServerLevel serverLevel = (ServerLevel) level;
            Vec3 userPos = user.position();

            for (int i = 0; i < 5; i++) {
                Wolf wolf = EntityType.WOLF.create(serverLevel, EntitySpawnReason.MOB_SUMMONED);
                if (wolf != null) {
                    double offsetX = (serverLevel.getRandom().nextDouble() - 0.5) * 3;
                    double offsetZ = (serverLevel.getRandom().nextDouble() - 0.5) * 3;
                    wolf.setPos(userPos.x + offsetX, userPos.y, userPos.z + offsetZ);
                    wolf.setOwner(user);
                    wolf.setTame(true, false);
                    wolf.setTarget(null);

                    serverLevel.addFreshEntity(wolf);

                    // Store after adding so the UUID is finalized
                    SUMMONED_WOLVES.put(wolf.getUUID(), serverLevel.getGameTime() + DESPAWN_TICKS);
                }
            }

            level.playSound(null, userPos.x, userPos.y, userPos.z, SoundEvents.WOLF_GROWL_BABY, SoundSource.PLAYERS, 1.0F, 1.0F);
            user.getCooldowns().addCooldown(this.getDefaultInstance(), COOLDOWN_TICKS);
        }

        return InteractionResult.SUCCESS;
    }
}