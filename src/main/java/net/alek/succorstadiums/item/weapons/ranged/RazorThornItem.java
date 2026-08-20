package net.alek.succorstadiums.item.weapons.ranged;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.Item;

import net.alek.succorstadiums.entity.ModEntityTypes;
import net.alek.succorstadiums.entity.items.RazorThornEntity;

import org.jspecify.annotations.NonNull;

public class RazorThornItem extends Item {

    private static final int PROJECTILE_COUNT = 3;
    private static final int COOLDOWN_TICKS = 40;

    public RazorThornItem(Properties properties) {
        super(properties);
    }

    @Override
    public @NonNull InteractionResult use(final Level level, final Player player, final @NonNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        for (int i = 0; i < PROJECTILE_COUNT; i++) {

            RazorThornEntity knife = new RazorThornEntity(ModEntityTypes.RAZOR_THORN, player, level, stack);
            knife.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0f, 2.5f, 5.0f);
            level.addFreshEntity(knife);
        }

        player.getCooldowns().addCooldown(this.getDefaultInstance(), COOLDOWN_TICKS);
        return InteractionResult.SUCCESS;
    }
}