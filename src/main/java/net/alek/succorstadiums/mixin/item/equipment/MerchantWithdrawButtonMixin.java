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

import org.jspecify.annotations.NonNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

import net.alek.succorstadiums.network.item.equipment.pouch.WithdrawExactCoinsPayload;
import net.alek.succorstadiums.item.ModItems;

@Mixin(MerchantScreen.class)
public abstract class MerchantWithdrawButtonMixin {

    @Unique
    private int shopItem;

    @Unique
    private PouchIconButton succorstadiums$withdrawButton;

    protected MerchantWithdrawButtonMixin(int shopItem) {
        this.shopItem = shopItem;
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void succorstadiums$addWithdrawButton(CallbackInfo ci) {
        System.out.println("[succorstadiums] addWithdrawButton injection fired");
        MerchantScreen self = (MerchantScreen) (Object) this;
        int xo = (self.width - 276) / 2;
        int yo = (self.height - 222) / 2;

        this.succorstadiums$withdrawButton = new PouchIconButton(
                xo + 5, yo + 158,
                button -> succorstadiums$onWithdrawClicked()
        );
        this.succorstadiums$withdrawButton.visible = false;
        ((ScreenInvokerMixin) self).succorstadiums$invokeAddRenderableWidget(this.succorstadiums$withdrawButton);
    }

    @Inject(method = "extractContents", at = @At("TAIL"))
    private void succorstadiums$updateWithdrawButtonVisibility(CallbackInfo ci) {
        MerchantScreen self = (MerchantScreen) (Object) this;
        MerchantMenu menu = self.getMenu();
        MerchantOffers offers = menu.getOffers();

        boolean costsCoins = false;
        if (!offers.isEmpty() && this.shopItem >= 0 && this.shopItem < offers.size()) {
            MerchantOffer offer = offers.get(this.shopItem);
            costsCoins = offer.getCostA().is(ModItems.EMERALD_COIN) || offer.getCostB().is(ModItems.EMERALD_COIN);
        }

        ItemStack pouchStack = succorstadiums$findPouch();
        this.succorstadiums$withdrawButton.visible = costsCoins && !pouchStack.isEmpty();
        this.succorstadiums$withdrawButton.setIcon(pouchStack);
    }

    @Unique
    private void succorstadiums$onWithdrawClicked() {
        ClientPlayNetworking.send(new WithdrawExactCoinsPayload(this.shopItem));
    }

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
        private static final int SIZE = 20;
        private ItemStack icon = ItemStack.EMPTY;

        PouchIconButton(int x, int y, OnPress onPress) {
            super(x, y, SIZE, SIZE, Component.translatable("button.succorstadiums.withdraw_coins"), onPress, DEFAULT_NARRATION);
        }

        void setIcon(ItemStack icon) {
            this.icon = icon;
        }

        @Override
        protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
            int bgColor = this.isHoveredOrFocused() ? 0xFF5A5A5A : 0xFF3C3C3C;
            graphics.fill(this.getX(), this.getY(), this.getX() + SIZE, this.getY() + SIZE, bgColor);
            graphics.outline(this.getX(), this.getY(), SIZE, SIZE, 0xFF1E1E1E);

            if (!this.icon.isEmpty()) {
                int iconX = this.getX() + (SIZE - 16) / 2;
                int iconY = this.getY() + (SIZE - 16) / 2;
                graphics.item(this.icon.copyWithCount(1), iconX, iconY);
            }

            if (this.isHoveredOrFocused()) {
                graphics.setTooltipForNextFrame(Minecraft.getInstance().font, this.getMessage(), mouseX, mouseY);
            }
        }

        @Override
        public void updateWidgetNarration(@NonNull NarrationElementOutput output) {
            this.defaultButtonNarrationText(output);
        }
    }
}