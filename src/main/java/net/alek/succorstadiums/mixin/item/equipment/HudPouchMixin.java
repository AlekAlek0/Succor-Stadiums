package net.alek.succorstadiums.mixin.item.equipment;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;

import net.alek.succorstadiums.item.equipment.pouch.PouchContents;
import net.alek.succorstadiums.component.ModComponents;
import net.alek.succorstadiums.item.ModItems;

// HudPouchMixin
@Mixin(Hud.class)
public abstract class HudPouchMixin {

    @Final
    @Shadow
    private Minecraft minecraft;

    // Set icon size, padding, and bottom offset
    @Unique
    private static final int ICON_SIZE = 16;
    @Unique
    private static final int PADDING_FROM_HOTBAR = 4;
    @Unique
    private static final int BOTTOM_OFFSET = 4;

    @Inject(method = "extractItemHotbar", at = @At("TAIL"))
    private void succorstadiums$extractPouchCounter(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, CallbackInfo ci) {

        // Get the player and total coins count in pouch
        Player player = this.minecraft.player;
        assert player != null;
        int total = succorstadiums$getTotalCoins(player);

        // If total is less than or equal to 0 do nothing and return
        if (total <= 0) {
            return;
        }

        // Convert the total into a string
        String text = String.valueOf(total);

        // Initialize variables for locations of the hotbar right edge, icon, text
        int hotbarRightEdge = graphics.guiWidth() / 2 + 91;
        int iconX = hotbarRightEdge + PADDING_FROM_HOTBAR;
        int iconY = graphics.guiHeight() - BOTTOM_OFFSET - ICON_SIZE;
        int textX = iconX + ICON_SIZE + 2;
        int textY = iconY + (ICON_SIZE - 8) / 2;

        // Create the new graphics item and text
        graphics.item(new ItemStack(ModItems.EMERALD_COIN), iconX, iconY);
        graphics.text(this.minecraft.font, text, textX, textY, 0xFFFFFFFF, true);
    }

    // Get the total coins in the pouch contents
    @Unique
    private int succorstadiums$getTotalCoins(Player player) {
        int total = 0;
        for (ItemStack stack : player.getInventory().getNonEquipmentItems()) {
            if (stack.is(ModItems.PLAINS_COIN_POUCH)) {
                PouchContents contents = stack.getOrDefault(ModComponents.POUCH_CONTENTS, PouchContents.EMPTY);
                total += contents.getTotalCount();
            }
        }
        return total;
    }
}