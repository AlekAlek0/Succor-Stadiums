package net.alek.succorstadiums.item.equipment.pouch;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.InteractionResult;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.Item;

import org.jspecify.annotations.NonNull;

import net.alek.succorstadiums.components.ModComponents;

// PlainsCoinPouchItem class
public class PlainsCoinPouchItem extends Item {
    public PlainsCoinPouchItem(Properties properties) {
        super(properties);
    }

    @Override
    public @NonNull InteractionResult use(Level level, Player player, @NonNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        // Only do something if level is server sided
        if (!level.isClientSide()) {

            // Get the pouchs current contents and create a new pouch container with the contents
            PouchContents contents = stack.getOrDefault(ModComponents.POUCH_CONTENTS, PouchContents.EMPTY);
            PouchContainer container = new PouchContainer(contents, updated -> stack.set(ModComponents.POUCH_CONTENTS, updated));

            // Open the pouch
            player.openMenu(new SimpleMenuProvider(
                    (syncId, inventory, p) -> ChestMenu.threeRows(syncId, inventory, container),
                    Component.translatable("container.succorstadiums.plains_coin_pouch")
            ));

            // Play a sound effect and return a interaction result success
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.BUNDLE_INSERT, SoundSource.PLAYERS, 1.0F, 1.0F);
        }

        return InteractionResult.SUCCESS;
    }
}