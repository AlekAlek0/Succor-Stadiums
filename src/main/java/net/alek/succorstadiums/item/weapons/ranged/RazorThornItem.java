package net.alek.succorstadiums.item.weapons.ranged;

import net.alek.succorstadiums.sound.ModSounds;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.Item;

import net.alek.succorstadiums.entity.items.RazorThornEntity;
import net.alek.succorstadiums.entity.ModEntityTypes;

import org.jspecify.annotations.NonNull;

public class RazorThornItem extends Item {

    private static final int PROJECTILE_COUNT = 3;
    private static final int COOLDOWN_TICKS = 40;

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

        // Spawn the given amount of razor thorn entity knives
        for (int i = 0; i < PROJECTILE_COUNT; i++) {

            RazorThornEntity knife = new RazorThornEntity(ModEntityTypes.RAZOR_THORN, player, level, itemStack);
            knife.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0f, 2.5f, 5.0f);
            level.addFreshEntity(knife);
        }

        // Remove 1 item stack when used
        if (!player.getAbilities().instabuild) {
            itemStack.shrink(1);
        }

        // 1% chance to get one razor thorn back on use
        if (level.getRandom().nextFloat() < 0.01f) {
            player.getInventory().add(new ItemStack(this));
        }

        // Set Cooldown, play a sound effect and return a success value for the interaction result
        player.getCooldowns().addCooldown(this.getDefaultInstance(), COOLDOWN_TICKS);
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.CROSSBOW_LOADING_END, SoundSource.PLAYERS, 1.0f, 1.0f);
        return InteractionResult.SUCCESS;
    }
}