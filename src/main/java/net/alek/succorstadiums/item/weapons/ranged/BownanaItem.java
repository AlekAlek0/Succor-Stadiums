package net.alek.succorstadiums.item.weapons.ranged;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.Item;

import org.jspecify.annotations.NonNull;
import java.util.List;

// BownanaItem class
public class BownanaItem extends BowItem {
    public BownanaItem(Item.Properties properties) {
        super(properties);
    }

    // Vanilla velocity is 3.0F
    private static final float VELOCITY_MULTIPLIER = 0.80F;

    @Override
    public boolean releaseUsing(final @NonNull ItemStack itemStack, final @NonNull Level level, final @NonNull LivingEntity entity, final int remainingTime) {

        // Return false if instance is not a player
        if (!(entity instanceof Player player)) {
            return false;
        } else {

            // Get player ammo and if projectile is empty return false
            ItemStack projectile = player.getProjectile(itemStack);
            if (projectile.isEmpty()) {
                return false;
            } else {

                // Get how long the bow was drawn for and convert that to draw power
                int timeHeld = this.getUseDuration(itemStack, entity) - remainingTime;
                float pow = getPowerForTime(timeHeld);

                // Check power for a minimum power before firing
                if ((double)pow < 0.1) {
                    return false;
                } else {

                    // Resolve which projectile actually gets fired
                    List<ItemStack> firedProjectiles = draw(itemStack, projectile, player);

                    // Check if level is instanceof server level
                    if (level instanceof ServerLevel serverLevel) {

                        // Fire the projectile if it is not empty
                        if (!firedProjectiles.isEmpty()) {
                            this.shoot(serverLevel, player, player.getUsedItemHand(), itemStack, firedProjectiles, pow * VELOCITY_MULTIPLIER, 1.0F, pow == 1.0F, null);
                        }
                    }

                    // Play sound effect and return true
                    level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS, 1.0F, 1.0F / (level.getRandom().nextFloat() * 0.4F + 1.2F) + pow * 0.5F);
                    return true;
                }
            }
        }
    }
}