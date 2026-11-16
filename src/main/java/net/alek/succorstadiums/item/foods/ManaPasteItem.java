package net.alek.succorstadiums.item.foods;

import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.InteractionResult;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.Item;

import org.jspecify.annotations.NonNull;

import net.alek.succorstadiums.mana.ManaHelper;

// ManaPasteItem class
public class ManaPasteItem extends Item {
    public ManaPasteItem(Properties properties) {
        super(properties);
    }

    private static final Component MANA_FULL_MESSAGE =
            Component.translatable("message.succorstadiums.mana_full");

    @Override
    public @NonNull InteractionResult use(@NonNull Level level, @NonNull Player player, @NonNull InteractionHand hand) {

        // Check to not allow eating if mana is full
        if (!level.isClientSide() && ManaHelper.get(player).getMana() >= ManaHelper.get(player).getMaxMana()) {
            level.playSound(null, player, SoundEvents.NOTE_BLOCK_BELL.value(), SoundSource.UI, 1.0f, 0.7f);
            player.sendOverlayMessage(MANA_FULL_MESSAGE);
            return InteractionResult.FAIL;
        }
        return super.use(level, player, hand);
    }
}