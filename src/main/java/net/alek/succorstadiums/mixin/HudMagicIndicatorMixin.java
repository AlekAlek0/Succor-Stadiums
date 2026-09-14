package net.alek.succorstadiums.mixin;

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
import net.minecraft.world.entity.HumanoidArm;
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
public abstract class HudMagicIndicatorMixin {

    @Shadow @Final private Minecraft minecraft;

    @Unique
    private static final Identifier MAGIC_INDICATOR_BACKGROUND =
            Identifier.fromNamespaceAndPath("succorstadiums", "textures/gui/hud/hotbar_magic_indicator_background.png");
    @Unique
    private static final Identifier MAGIC_INDICATOR_PROGRESS =
            Identifier.fromNamespaceAndPath("succorstadiums", "textures/gui/hud/hotbar_magic_indicator_progress.png");

    @Unique
    private static final int ICON_SIZE = 18;
    @Unique
    private static final int OVERLAP_SHIFT = ICON_SIZE + 2;

    @Inject(method = "extractItemHotbar", at = @At("TAIL"))
    private void succorstadiums$extractMagicIndicator(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        if (SuccorStadiumsConfigScreen.getConfig().magicIndicatorMode != MagicIndicatorMode.HOTBAR) {
            return;
        }

        Player player = this.minecraft.player;
        if (player == null) {
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

        int screenCenter = graphics.guiWidth() / 2;
        HumanoidArm offhandArm = player.getMainArm().getOpposite();

        int x = screenCenter + 91 + 6;
        if (offhandArm == HumanoidArm.RIGHT) {
            x = screenCenter - 91 - 22;
        }
        int y = graphics.guiHeight() - 20;

        if (isVanillaAttackIndicatorShowing(player)) {
            if (offhandArm == HumanoidArm.RIGHT) {
                x -= OVERLAP_SHIFT;
            } else {
                x += OVERLAP_SHIFT;
            }
        }

        graphics.blit(RenderPipelines.GUI_TEXTURED, MAGIC_INDICATOR_BACKGROUND,
                x, y, 0.0F, 0.0F, ICON_SIZE, ICON_SIZE, ICON_SIZE, ICON_SIZE, ARGB.opaque(-1));

        int fillHeight = (int) ((1.0F - cooldownPercent) * (float) ICON_SIZE);
        int fillY = ICON_SIZE - fillHeight;

        graphics.blit(RenderPipelines.GUI_TEXTURED, MAGIC_INDICATOR_PROGRESS,
                x, y + fillY, 0.0F, (float) fillY, ICON_SIZE, fillHeight, ICON_SIZE, ICON_SIZE, ARGB.opaque(-1));
    }

    @Unique
    private boolean isVanillaAttackIndicatorShowing(Player player) {
        if (this.minecraft.options.attackIndicator().get() != AttackIndicatorStatus.HOTBAR) {
            return false;
        }
        return player.getAttackStrengthScale(0.0F) < 1.0F;
    }
}