package net.alek.succorstadiums.mixin.hud;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.world.entity.player.Player;
import net.minecraft.resources.Identifier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Hud;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;

import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;

import net.alek.succorstadiums.attachments.ModAttachments;
import net.alek.succorstadiums.mana.ManaData;

@Mixin(Hud.class)
public abstract class HudManaMixin {

    @Shadow @Final private Minecraft minecraft;
    @Shadow private int tickCount;

    @Unique
    private static final Identifier MANA_FULL =
            Identifier.fromNamespaceAndPath("succorstadiums", "textures/gui/hud/manastar/mana_star_full.png");
    @Unique
    private static final Identifier MANA_EMPTY =
            Identifier.fromNamespaceAndPath("succorstadiums", "textures/gui/hud/manastar/mana_star_empty.png");
    @Unique
    private static final Identifier MANA_HALF =
            Identifier.fromNamespaceAndPath("succorstadiums", "textures/gui/hud/manastar/mana_star_half.png");
    @Unique
    private static final Identifier MANA_FULL_BLINKING =
            Identifier.fromNamespaceAndPath("succorstadiums", "textures/gui/hud/manastar/mana_star_full_blinking.png");
    @Unique
    private static final Identifier MANA_EMPTY_BLINKING =
            Identifier.fromNamespaceAndPath("succorstadiums", "textures/gui/hud/manastar/mana_star_empty_blinking.png");
    @Unique
    private static final Identifier MANA_HALF_BLINKING =
            Identifier.fromNamespaceAndPath("succorstadiums", "textures/gui/hud/manastar/mana_star_half_blinking.png");

    @Unique
    private static final int ICON_SIZE = 9;
    @Unique
    private static final int ICON_SPACING = 8;
    @Unique
    private static final int POINTS_PER_ICON = 2;
    @Unique
    private static final int BLINK_DURATION_TICKS = 20;

    @Unique
    private int succorstadiums$lastMana = -1;
    @Unique
    private int succorstadiums$manaBlinkEndTick = 0;

    @Inject(method = "extractPlayerHealth", at = @At("TAIL"))
    private void succorstadiums$extractMana(GuiGraphicsExtractor graphics, CallbackInfo ci) {
        Player player = this.minecraft.player;
        if (player == null) {
            return;
        }

        ManaData data = player.getAttachedOrCreate(ModAttachments.MANA, ManaData::new);
        int mana = data.getMana();
        int maxMana = data.getMaxMana();

        if (this.succorstadiums$lastMana != -1 && mana != this.succorstadiums$lastMana) {
            this.succorstadiums$manaBlinkEndTick = this.tickCount + BLINK_DURATION_TICKS;
        }
        this.succorstadiums$lastMana = mana;

        boolean blink = this.succorstadiums$manaBlinkEndTick > this.tickCount
                && (this.succorstadiums$manaBlinkEndTick - this.tickCount) / 3 % 2 == 1;

        int icons = Mth.ceil((float) maxMana / POINTS_PER_ICON);

        int xRight = graphics.guiWidth() / 2 + 91;
        int yLineBase = graphics.guiHeight() - 39;
        int yLine = yLineBase - 10;

        for (int i = 0; i < icons; i++) {
            int xo = xRight - i * ICON_SPACING - 9;
            int pointsForThisIcon = (i + 1) * POINTS_PER_ICON;

            Identifier empty = blink ? MANA_EMPTY_BLINKING : MANA_EMPTY;
            Identifier full = blink ? MANA_FULL_BLINKING : MANA_FULL;
            Identifier half = blink ? MANA_HALF_BLINKING : MANA_HALF;

            graphics.blit(RenderPipelines.GUI_TEXTURED, empty, xo, yLine,
                    0.0F, 0.0F, ICON_SIZE, ICON_SIZE, ICON_SIZE, ICON_SIZE, ARGB.opaque(-1));

            if (pointsForThisIcon <= mana) {
                graphics.blit(RenderPipelines.GUI_TEXTURED, full, xo, yLine,
                        0.0F, 0.0F, ICON_SIZE, ICON_SIZE, ICON_SIZE, ICON_SIZE, ARGB.opaque(-1));
            } else if (pointsForThisIcon - 1 <= mana) {
                graphics.blit(RenderPipelines.GUI_TEXTURED, half, xo, yLine,
                        0.0F, 0.0F, ICON_SIZE, ICON_SIZE, ICON_SIZE, ICON_SIZE, ARGB.opaque(-1));
            }
        }
    }
}