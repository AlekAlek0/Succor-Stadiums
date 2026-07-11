package net.alek.succorstadiums.item.weapons.ranged;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.List;

public class BownanaItem extends BowItem {

    // Vanilla velocity is 3.0F
    private static final float VELOCITY_MULTIPLIER = 0.80F;

    public BownanaItem(Item.Properties properties) {
        super(properties);
    }

    @Override
    public boolean releaseUsing(final ItemStack itemStack, final Level level, final LivingEntity entity, final int remainingTime) {
        if (!(entity instanceof Player player)) {
            return false;
        } else {
            ItemStack projectile = player.getProjectile(itemStack);
            if (projectile.isEmpty()) {
                return false;
            } else {
                int timeHeld = this.getUseDuration(itemStack, entity) - remainingTime;
                float pow = getPowerForTime(timeHeld);
                if ((double)pow < 0.1) {
                    return false;
                } else {
                    List<ItemStack> firedProjectiles = draw(itemStack, projectile, player);
                    if (level instanceof ServerLevel) {
                        ServerLevel serverLevel = (ServerLevel)level;
                        if (!firedProjectiles.isEmpty()) {
                            this.shoot(serverLevel, player, player.getUsedItemHand(), itemStack, firedProjectiles, pow * VELOCITY_MULTIPLIER, 1.0F, pow == 1.0F, (LivingEntity)null);
                        }
                    }

                    level.playSound((Entity)null, player.getX(), player.getY(), player.getZ(), SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS, 1.0F, 1.0F / (level.getRandom().nextFloat() * 0.4F + 1.2F) + pow * 0.5F);
                    player.awardStat(Stats.ITEM_USED.get(this));
                    return true;
                }
            }
        }
    }
}