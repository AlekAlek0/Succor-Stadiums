package net.alek.succorstadiums.mixin.hud;

import net.alek.succorstadiums.config.MagicIndicatorMode;
import net.alek.succorstadiums.config.SuccorStadiumsConfigScreen;
import net.alek.succorstadiums.item.weapons.magic.MagicIndicator;
import net.minecraft.client.AttackIndicatorStatus;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Hud.class)
public abstract class HudMagicCrosshairIndicatorMixin {

    @Shadow @Final private Minecraft minecraft;

    @Unique
    private static final Identifier MAGIC_INDICATOR_BACKGROUND =
            Identifier.fromNamespaceAndPath("succorstadiums", "textures/gui/hud/crosshair_magic_indicator_background.png");
    @Unique
    private static final Identifier MAGIC_INDICATOR_PROGRESS =
            Identifier.fromNamespaceAndPath("succorstadiums", "textures/gui/hud/crosshair_magic_indicator_progress.png");

    @Unique
    private static final int ICON_WIDTH = 16;
    @Unique
    private static final int ICON_HEIGHT = 4;
    @Unique
    private static final int VANILLA_OVERLAP_SHIFT = ICON_HEIGHT + 2;

    @Inject(method = "extractCrosshair", at = @At("TAIL"))
    private void succorstadiums$extractMagicCrosshairIndicator(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        if (SuccorStadiumsConfigScreen.getConfig().magicIndicatorMode != MagicIndicatorMode.CROSSHAIR) {
            return;
        }

        Player player = this.minecraft.player;
        if (player == null) {
            return;
        }

        if (!this.minecraft.options.getCameraType().isFirstPerson()) {
            return;
        }

        ItemStack heldItem = player.getMainHandItem();
        if (!(heldItem.getItem() instanceof MagicIndicator)) {
            return;
        }

        float cooldownPercent = player.getCooldowns()
                .getCooldownPercent(heldItem, deltaTracker.getGameTimeDeltaPartialTick(false));

        if (cooldownPercent <= 0.0F) {
            return;
        }

        int x = graphics.guiWidth() / 2 - ICON_WIDTH / 2;
        int y = graphics.guiHeight() / 2 - 7 + 16;

        if (isVanillaCrosshairIndicatorShowing(player)) {
            y += VANILLA_OVERLAP_SHIFT;
        }

        graphics.blit(RenderPipelines.GUI_TEXTURED, MAGIC_INDICATOR_BACKGROUND,
                x, y, 0.0F, 0.0F, ICON_WIDTH, ICON_HEIGHT, ICON_WIDTH, ICON_HEIGHT, ARGB.opaque(-1));

        int fillWidth = (int) ((1.0F - cooldownPercent) * (float) ICON_WIDTH);

        graphics.blit(RenderPipelines.GUI_TEXTURED, MAGIC_INDICATOR_PROGRESS,
                x, y, 0.0F, 0.0F, fillWidth, ICON_HEIGHT, ICON_WIDTH, ICON_HEIGHT, ARGB.opaque(-1));
    }

    @Unique
    private boolean isVanillaCrosshairIndicatorShowing(Player player) {
        if (this.minecraft.options.attackIndicator().get() != AttackIndicatorStatus.CROSSHAIR) {
            return false;
        }
        return player.getAttackStrengthScale(0.0F) < 1.0F;
    }
}