package net.alek.succorstadiums.mixin.item.equipment;

import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.inventory.MerchantScreen;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.client.gui.components.Button;
import net.minecraft.world.entity.player.Player;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.client.Minecraft;

import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Mixin;
import org.jspecify.annotations.NonNull;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

import net.alek.succorstadiums.network.item.equipment.pouch.WithdrawExactCoinsPayload;
import net.alek.succorstadiums.item.equipment.pouch.PouchContents;
import net.alek.succorstadiums.datagen.ModItemTagProvider;
import net.alek.succorstadiums.component.ModComponents;
import net.alek.succorstadiums.item.ModItems;

@Mixin(MerchantScreen.class)
public abstract class MerchantWithdrawButtonMixin {

    // Get the shopItem from the villager trade
    @Shadow
    private int shopItem;

    // Initialize the button
    @Unique
    private PouchIconButton succorstadiums$withdrawButton;

    // Inject code at the end of the init method of the MerchantScreen class with our custom code to add our custom button
    @Inject(method = "init", at = @At("TAIL"))
    private void succorstadiums$addWithdrawButton(CallbackInfo ci) {

        // Get the merchant screen and calculate the top left corner of the trade screen
        MerchantScreen self = (MerchantScreen) (Object) this;
        int xo = (self.width - 276) / 2;
        int yo = (self.height - 166) / 2;

        // Create the button and set its visibility to false
        this.succorstadiums$withdrawButton = new PouchIconButton(xo + 248, yo + 36, button -> succorstadiums$onWithdrawClicked());
        this.succorstadiums$withdrawButton.visible = false;

        // Add the custom button to the merchants screen list of ui widgets
        ((ScreenInvokerMixin) self).succorstadiums$invokeAddRenderableWidget(this.succorstadiums$withdrawButton);
    }

    // Inject code at the end of the extractContents method to update the visibility of our custom button
    @Inject(method = "extractContents", at = @At("TAIL"))
    private void succorstadiums$updateWithdrawButtonVisibility(CallbackInfo ci) {

        // Get the merchant screen, menu and trade offers
        MerchantScreen self = (MerchantScreen) (Object) this;
        MerchantMenu menu = self.getMenu();
        MerchantOffers offers = menu.getOffers();

        // Initialize variables of if the trade costs coins and how many required coins
        boolean costsCoins = false;
        int requiredCoins = 0;

        // Get the pouch stack and create variables for if the player has a pouch
        // and set initialize a variable for availableCoins to use later
        ItemStack pouchStack = succorstadiums$findPouch();
        boolean hasPouch = !pouchStack.isEmpty();
        int availableCoins = 0;

        // Check to make sure the offer isn't empty, has a valid index of at least 1 or greater
        // and that the selected trade isn't past the end of the list
        if (!offers.isEmpty() && this.shopItem >= 0 && this.shopItem < offers.size()) {

            // Get the trade offer and offer costs
            MerchantOffer offer = offers.get(this.shopItem);
            ItemStack costA = offer.getCostA();
            ItemStack costB = offer.getCostB();

            // Check if cost item has the coins tag if so set costCoins to true and set the required coins to the price
            if (costA.is(ModItemTagProvider.COINS)) {
                costsCoins = true;
                requiredCoins += costA.getCount();
            }
            if (costB.is(ModItemTagProvider.COINS)) {
                costsCoins = true;
                requiredCoins += costB.getCount();
            }
        }

        // If the player has a pouch get the contents of the pouch and set the available coins to the total count of coins in the pouch
        if (hasPouch) {
            PouchContents contents = pouchStack.getOrDefault(ModComponents.POUCH_CONTENTS, PouchContents.EMPTY);
            availableCoins = contents.getTotalCount();
        }

        // Set the visibility of the button based on if the offer costs coins and if we have a pouch
        this.succorstadiums$withdrawButton.visible = costsCoins && hasPouch;

        // Set the activity of the button based on if the offer costs coins and if we have enough coins for the trade cost
        this.succorstadiums$withdrawButton.active = costsCoins && hasPouch && availableCoins >= requiredCoins;

        // Set the icon of our custom button to be the pouch stack
        this.succorstadiums$withdrawButton.setIcon(pouchStack);
    }

    // Helper method to send the payload when user clicks the custom button
    @Unique
    private void succorstadiums$onWithdrawClicked() {
        ClientPlayNetworking.send(new WithdrawExactCoinsPayload(this.shopItem));
    }

    // Iterate through the inventory and find what stack contains the plains coin pouch
    // and return it else return empty
    @Unique
    private ItemStack succorstadiums$findPouch() {
        Player player = Minecraft.getInstance().player;
        if (player == null) {
            return ItemStack.EMPTY;
        }
        for (ItemStack stack : player.getInventory().getNonEquipmentItems()) {
            if (stack.is(ModItems.PLAINS_COIN_POUCH)) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }

    @Unique
    private static class PouchIconButton extends Button {

        // Initialize the size of our button and the icon of our button to be a empty itemstack
        private static final int SIZE = 18;
        private ItemStack icon = ItemStack.EMPTY;

        // Public constructor for our custom button
        PouchIconButton(int x, int y, OnPress onPress) {
            super(x, y, SIZE, SIZE, Component.translatable("button.succorstadiums.withdraw_coins"), onPress, DEFAULT_NARRATION);
        }

        // Set the icon of our custom button
        void setIcon(ItemStack icon) {
            this.icon = icon;
        }

        @Override
        protected void extractContents(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
            int bgColor;

            // Set the bgColor based on if the button is active or not
            if (!this.active) {
                bgColor = 0xFF202020;
            } else {
                bgColor = this.isHoveredOrFocused() ? 0xFF5A5A5A : 0xFF3C3C3C;
            }

            // Create the button fill and outline
            graphics.fill(this.getX(), this.getY(), this.getX() + SIZE, this.getY() + SIZE, bgColor);
            graphics.outline(this.getX(), this.getY(), SIZE, SIZE, 0xFF1E1E1E);

            // If the icon is not empty then set the current icon of the button
            if (!this.icon.isEmpty()) {
                int iconX = this.getX() + (SIZE - 16) / 2;
                int iconY = this.getY() + (SIZE - 16) / 2;

                graphics.item(this.icon.copyWithCount(1), iconX, iconY);
            }

            // If the user is hovering over the button create the tooltip
            if (this.isHovered()) {
                graphics.setTooltipForNextFrame(Minecraft.getInstance().font, this.getMessage(), mouseX, mouseY);
            }
        }

        // Override the on-screen text narration to work for our custom button tooltip
        @Override
        public void updateWidgetNarration(@NonNull NarrationElementOutput output) {
            this.defaultButtonNarrationText(output);
        }
    }
}