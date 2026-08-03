package net.alek.succorstadiums.item.foods;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.NonNull;

public class CreeperSalveItem extends Item {
    public CreeperSalveItem(Properties properties) {
        super(properties);
    }

    @Override
    public @NonNull InteractionResult use(@NonNull Level level, Player player, @NonNull InteractionHand hand) {
        // Don't allow eating if health is full
        if (player.getHealth() >= player.getMaxHealth()) {
            return InteractionResult.FAIL;
        }

        return super.use(level, player, hand);
    }
}
