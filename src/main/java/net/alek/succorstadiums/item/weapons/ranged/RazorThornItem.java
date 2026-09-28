package net.alek.succorstadiums.item.weapons.ranged;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.Item;

import java.util.concurrent.atomic.AtomicBoolean;
import org.jspecify.annotations.NonNull;

import net.alek.succorstadiums.entity.items.RazorThornEntity;
import net.alek.succorstadiums.entity.ModEntityTypes;
import net.alek.succorstadiums.sound.ModSounds;

// RazorThornItem class
public class RazorThornItem extends Item {

    // Initialize ints for the projectile count and cooldown
    private static final int BASE_PROJECTILE_COUNT = 3;
    private static final int COOLDOWN_TICKS = 40; // 2 Seconds

    // Degrees each side knife is offset
    private static final float SPREAD_ANGLE_DEGREES = 10.0f;

    // Initialize floats for the knives velocity to be 50% more than the bownana velocity
    private static final float PROJECTILE_VELOCITY = 0.80F * 1.5f;

    // Initialize floats for the base damage of each knife to be 2 or 3 for special knife
    private static final float NORMAL_DAMAGE = 4.0f; // 2 Hearts
    private static final float BONUS_DAMAGE = 6.0f; // 3 Hearts

    // 1% chance for an extra projectile
    private static final float BONUS_PROJECTILE_CHANCE = 0.01f;

    public RazorThornItem(Properties properties) {
        super(properties);
    }

    @Override
    public @NonNull InteractionResult use(final Level level, final @NonNull Player player, final @NonNull InteractionHand hand) {

        // Check to see if level is client sided if so return a pass value for the interaction result
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        // Get the item in player hand as itemStack
        ItemStack itemStack = player.getItemInHand(hand);

        // Roll for the bonus projectile
        boolean bonusProjectile = level.getRandom().nextFloat() < BONUS_PROJECTILE_CHANCE;
        int projectileCount = BASE_PROJECTILE_COUNT + (bonusProjectile ? 1 : 0);

        // Shared between every knife in this throw: the first knife to hit the ground claims the pickup
        AtomicBoolean pickupClaim = new AtomicBoolean(false);

        // Spawn the razor thorn entity knives in an evenly spaced cone centered on where the player is aiming
        for (int i = 0; i < projectileCount; i++) {

            RazorThornEntity knife = new RazorThornEntity(ModEntityTypes.RAZOR_THORN, player, level, itemStack);

            // The last knife of a bonus throw is the 3H one
            boolean isBonus = bonusProjectile && i == projectileCount - 1;

            // Divide by the launch velocity to land on the exact damage we want
            knife.setBaseDamage((isBonus ? BONUS_DAMAGE : NORMAL_DAMAGE) / PROJECTILE_VELOCITY);
            knife.setPickupClaim(pickupClaim);

            // Center the cone: 3 knives gives -10, 0, +10 and 4 knives gives -15, -5, +5, +15
            float yawOffset = (i - (projectileCount - 1) / 2.0F) * SPREAD_ANGLE_DEGREES;
            float yaw = player.getYRot() + yawOffset;

            // Shoot the knives and add the entity to the level
            knife.shootFromRotation(player, player.getXRot(), yaw, 0.0F, PROJECTILE_VELOCITY, 0.0F);
            level.addFreshEntity(knife);
        }

        // Remove 1 item stack when used
        if (!player.getAbilities().instabuild) {
            itemStack.shrink(1);
        }

        // Set Cooldown, play a sound effect and return a success value for the interaction result
        player.getCooldowns().addCooldown(this.getDefaultInstance(), COOLDOWN_TICKS);
        level.playSound(null, player.getX(), player.getY(), player.getZ(), ModSounds.RAZOR_THORN_USE, SoundSource.PLAYERS, 1F, 1.0F);
        return InteractionResult.SUCCESS;
    }
}