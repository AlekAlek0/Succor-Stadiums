package net.alek.succorstadiums.item.foods;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.InteractionResult;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.Item;

import org.jspecify.annotations.NonNull;

import net.alek.succorstadiums.mana.ManaHelper;

public class ManaPasteItem extends Item {
    public ManaPasteItem(Properties properties) {
        super(properties);
    }

    private static final Component MANA_FULL_MESSAGE =
            Component.translatable("message.succorstadiums.mana_full");

    @Override
    public @NonNull InteractionResult use(@NonNull Level level, @NonNull Player player, @NonNull InteractionHand hand) {
        // Don't allow eating if mana is full
        if (!level.isClientSide() && ManaHelper.get(player).getMana() >= ManaHelper.get(player).getMaxMana()) {
            player.sendOverlayMessage(MANA_FULL_MESSAGE);
            return InteractionResult.FAIL;
        }

        return super.use(level, player, hand);
    }
}