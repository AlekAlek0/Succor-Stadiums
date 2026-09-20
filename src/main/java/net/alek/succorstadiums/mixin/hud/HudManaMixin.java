package net.alek.succorstadiums.mixin.hud;

import net.alek.succorstadiums.attachments.ModAttachments;
import net.alek.succorstadiums.mana.HypermanaData;
import net.alek.succorstadiums.mana.ManaData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Hud.class)
public abstract class HudManaMixin {

    @Shadow @Final private Minecraft minecraft;
    @Shadow private int tickCount;

    // Mana textures
    @Unique
    private static final Identifier MANA_FULL =
            Identifier.fromNamespaceAndPath("succorstadiums", "textures/gui/hud/mana_full.png");
    @Unique
    private static final Identifier MANA_EMPTY =
            Identifier.fromNamespaceAndPath("succorstadiums", "textures/gui/hud/mana_empty.png");
    @Unique
    private static final Identifier MANA_HALF =
            Identifier.fromNamespaceAndPath("succorstadiums", "textures/gui/hud/mana_half.png");
    @Unique
    private static final Identifier MANA_FULL_BLINKING =
            Identifier.fromNamespaceAndPath("succorstadiums", "textures/gui/hud/mana_star_full_blinking.png");
    @Unique
    private static final Identifier MANA_EMPTY_BLINKING =
            Identifier.fromNamespaceAndPath("succorstadiums", "textures/gui/hud/mana_star_empty_blinking.png");
    @Unique
    private static final Identifier MANA_HALF_BLINKING =
            Identifier.fromNamespaceAndPath("succorstadiums", "textures/gui/hud/mana_star_half_blinking.png");

    // Hypermana textures
    @Unique
    private static final Identifier HYPERMANA_FULL =
            Identifier.fromNamespaceAndPath("succorstadiums", "textures/gui/hud/hypermana_star_full.png");
    @Unique
    private static final Identifier HYPERMANA_HALF =
            Identifier.fromNamespaceAndPath("succorstadiums", "textures/gui/hud/hypermana_star_half.png");

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

        ManaData manaData = player.getAttachedOrCreate(ModAttachments.MANA, ManaData::new);
        HypermanaData hyperData = player.getAttachedOrCreate(ModAttachments.HYPERMANA, HypermanaData::new);

        int mana = manaData.getMana();
        int maxMana = manaData.getMaxMana();
        int hypermana = hyperData.getHypermana();

        if (this.succorstadiums$lastMana != -1 && mana != this.succorstadiums$lastMana) {
            this.succorstadiums$manaBlinkEndTick = this.tickCount + BLINK_DURATION_TICKS;
        }
        this.succorstadiums$lastMana = mana;

        boolean blink = this.succorstadiums$manaBlinkEndTick > this.tickCount
                && (this.succorstadiums$manaBlinkEndTick - this.tickCount) / 3 % 2 == 1;

        int manaIcons = Mth.ceil((float) maxMana / POINTS_PER_ICON);

        int xRight = graphics.guiWidth() / 2 + 91;
        int yLineBase = graphics.guiHeight() - 39;
        int yLine = yLineBase - 10;

        // Regular mana row
        for (int i = 0; i < manaIcons; i++) {
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

        if (hypermana > 0) {
            int hyperIcons = Mth.ceil((float) hypermana / POINTS_PER_ICON);
            for (int i = 0; i < hyperIcons; i++) {
                int xo = xRight - (manaIcons + i) * ICON_SPACING - 9;
                int pointsForThisIcon = (i + 1) * POINTS_PER_ICON;

                if (pointsForThisIcon <= hypermana) {
                    graphics.blit(RenderPipelines.GUI_TEXTURED, HYPERMANA_FULL, xo, yLine,
                            0.0F, 0.0F, ICON_SIZE, ICON_SIZE, ICON_SIZE, ICON_SIZE, ARGB.opaque(-1));
                } else if (pointsForThisIcon - 1 <= hypermana) {
                    graphics.blit(RenderPipelines.GUI_TEXTURED, HYPERMANA_HALF, xo, yLine,
                            0.0F, 0.0F, ICON_SIZE, ICON_SIZE, ICON_SIZE, ICON_SIZE, ARGB.opaque(-1));
                }
            }
        }
    }
}