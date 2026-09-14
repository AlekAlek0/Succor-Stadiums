package net.alek.succorstadiums.mixin;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.world.entity.player.Player;
import net.minecraft.resources.Identifier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Hud;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;

import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;

import net.alek.succorstadiums.attachments.ModAttachments;
import net.alek.succorstadiums.mana.ManaData;

@Mixin(Hud.class)
public abstract class HudManaMixin {

    @Shadow @Final private Minecraft minecraft;

    @Unique
    private static final Identifier MANA_FULL =
            Identifier.fromNamespaceAndPath("succorstadiums", "textures/gui/hud/mana_star_full.png");
    @Unique
    private static final Identifier MANA_EMPTY =
            Identifier.fromNamespaceAndPath("succorstadiums", "textures/gui/hud/mana_star_empty.png");
    @Unique
    private static final Identifier MANA_HALF =
            Identifier.fromNamespaceAndPath("succorstadiums", "textures/gui/hud/mana_star_half.png");

    @Unique
    private static final int ICON_SIZE = 9;
    @Unique
    private static final int ICON_SPACING = 8;
    @Unique
    private static final int POINTS_PER_ICON = 2;

    @Inject(method = "extractPlayerHealth", at = @At("TAIL"))
    private void succorstadiums$extractMana(GuiGraphicsExtractor graphics, CallbackInfo ci) {
        Player player = this.minecraft.player;
        if (player == null) {
            return;
        }

        ManaData data = player.getAttachedOrCreate(ModAttachments.MANA, ManaData::new);
        int mana = data.getMana();
        int maxMana = data.getMaxMana();
        int icons = Mth.ceil((float) maxMana / POINTS_PER_ICON);

        int xRight = graphics.guiWidth() / 2 + 91;
        int yLineBase = graphics.guiHeight() - 39;
        int yLine = yLineBase - 10;

        for (int i = 0; i < icons; i++) {
            int xo = xRight - i * ICON_SPACING - 9;
            int pointsForThisIcon = (i + 1) * POINTS_PER_ICON;

            graphics.blit(RenderPipelines.GUI_TEXTURED, MANA_EMPTY, xo, yLine,
                    0.0F, 0.0F, ICON_SIZE, ICON_SIZE, ICON_SIZE, ICON_SIZE, ARGB.opaque(-1));

            if (pointsForThisIcon <= mana) {
                graphics.blit(RenderPipelines.GUI_TEXTURED, MANA_FULL, xo, yLine,
                        0.0F, 0.0F, ICON_SIZE, ICON_SIZE, ICON_SIZE, ICON_SIZE, ARGB.opaque(-1));
            } else if (pointsForThisIcon - 1 <= mana) {
                graphics.blit(RenderPipelines.GUI_TEXTURED, MANA_HALF, xo, yLine,
                        0.0F, 0.0F, ICON_SIZE, ICON_SIZE, ICON_SIZE, ICON_SIZE, ARGB.opaque(-1));
            }
        }
    }
}