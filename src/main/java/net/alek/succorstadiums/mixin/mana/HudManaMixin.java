package net.alek.succorstadiums.mixin.mana;

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
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;

import net.alek.succorstadiums.attachments.ModAttachments;
import net.alek.succorstadiums.effect.ModEffects;
import net.alek.succorstadiums.mana.HypermanaData;
import net.alek.succorstadiums.mana.ManaData;

@Mixin(Hud.class)
public abstract class HudManaMixin {

    @Shadow @Final private Minecraft minecraft;
    @Shadow private int tickCount;

    // Mana textures
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

    // Hypermana textures
    @Unique
    private static final Identifier HYPERMANA_FULL =
            Identifier.fromNamespaceAndPath("succorstadiums", "textures/gui/hud/manastar/hypermana/hypermana_star_full.png");
    @Unique
    private static final Identifier HYPERMANA_HALF =
            Identifier.fromNamespaceAndPath("succorstadiums", "textures/gui/hud/manastar/hypermana/hypermana_star_half.png");
    @Unique
    private static final Identifier HYPERMANA_FULL_BLINKING =
            Identifier.fromNamespaceAndPath("succorstadiums", "textures/gui/hud/manastar/hypermana/hypermana_star_full_blinking.png");
    @Unique
    private static final Identifier HYPERMANA_HALF_BLINKING =
            Identifier.fromNamespaceAndPath("succorstadiums", "textures/gui/hud/manastar/hypermana/hypermana_star_half_blinking.png");

    // Mana Sickness textures
    @Unique
    private static final Identifier MANASICK_FULL =
            Identifier.fromNamespaceAndPath("succorstadiums", "textures/gui/hud/manastar/manasick/manasick_star_full.png");
    @Unique
    private static final Identifier MANASICK_HALF =
            Identifier.fromNamespaceAndPath("succorstadiums", "textures/gui/hud/manastar/manasick/manasick_star_half.png");
    @Unique
    private static final Identifier MANASICK_FULL_BLINKING =
            Identifier.fromNamespaceAndPath("succorstadiums", "textures/gui/hud/manastar/manasick/manasick_star_full_blinking.png");
    @Unique
    private static final Identifier MANASICK_HALF_BLINKING =
            Identifier.fromNamespaceAndPath("succorstadiums", "textures/gui/hud/manastar/manasick/manasick_star_half_blinking.png");

    @Unique
    private static final int ICON_SIZE = 9;
    @Unique
    private static final int ICON_SPACING = 8;
    @Unique
    private static final int POINTS_PER_ICON = 2; // 2 Mana per star
    @Unique
    private static final int BLINK_DURATION_TICKS = 20; // 1 Second
    @Unique
    private static final int ROW_CAPACITY = 10; // 10 Stars

    @Unique
    private int succorstadiums$lastMana = -1;
    @Unique
    private int succorstadiums$manaBlinkEndTick = 0;

    @Unique
    private int succorstadiums$lastHypermana = -1;
    @Unique
    private int succorstadiums$hyperBlinkEndTick = 0;

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
        int maxHypermana = hyperData.getMaxHypermana();

        boolean sick = player.hasEffect(ModEffects.MANA_SICKNESS);

        // Mana change blink (only used while NOT sick)
        if (this.succorstadiums$lastMana != -1 && mana != this.succorstadiums$lastMana) {
            this.succorstadiums$manaBlinkEndTick = this.tickCount + BLINK_DURATION_TICKS;
        }
        this.succorstadiums$lastMana = mana;

        boolean manaChangeBlink = this.succorstadiums$manaBlinkEndTick > this.tickCount
                && (this.succorstadiums$manaBlinkEndTick - this.tickCount) / 3 % 2 == 1;

        // Hypermana change blink
        if (this.succorstadiums$lastHypermana != -1 && hypermana != this.succorstadiums$lastHypermana) {
            this.succorstadiums$hyperBlinkEndTick = this.tickCount + BLINK_DURATION_TICKS;
        }
        this.succorstadiums$lastHypermana = hypermana;

        boolean hyperChangeBlink = this.succorstadiums$hyperBlinkEndTick > this.tickCount
                && (this.succorstadiums$hyperBlinkEndTick - this.tickCount) / 3 % 2 == 1;

        int manaIcons = Mth.ceil((float) maxMana / POINTS_PER_ICON);
        int hyperIcons = Mth.ceil((float) maxHypermana / POINTS_PER_ICON);

        int xRight = graphics.guiWidth() / 2 + 91;
        int yLineBase = graphics.guiHeight() - 39;
        int yLine = yLineBase - 10;

        Identifier manaEmpty;
        Identifier manaFull;
        Identifier manaHalf;

        manaEmpty = manaChangeBlink ? MANA_EMPTY_BLINKING : MANA_EMPTY;
        if (sick) {
            manaFull = manaChangeBlink ? MANASICK_FULL_BLINKING : MANASICK_FULL;
            manaHalf = manaChangeBlink ? MANASICK_HALF_BLINKING : MANASICK_HALF;
        } else {
            manaFull = manaChangeBlink ? MANA_FULL_BLINKING : MANA_FULL;
            manaHalf = manaChangeBlink ? MANA_HALF_BLINKING : MANA_HALF;
        }

        for (int index = 0; index < manaIcons; index++) {
            int row = index / ROW_CAPACITY;
            int column = index % ROW_CAPACITY;
            int xo = xRight - column * ICON_SPACING - 9;
            int yo = yLine - row * 10;
            int pointsForThisIcon = (index + 1) * POINTS_PER_ICON;

            graphics.blit(RenderPipelines.GUI_TEXTURED, manaEmpty, xo, yo,
                    0.0F, 0.0F, ICON_SIZE, ICON_SIZE, ICON_SIZE, ICON_SIZE, ARGB.opaque(-1));

            if (pointsForThisIcon <= mana) {
                graphics.blit(RenderPipelines.GUI_TEXTURED, manaFull, xo, yo,
                        0.0F, 0.0F, ICON_SIZE, ICON_SIZE, ICON_SIZE, ICON_SIZE, ARGB.opaque(-1));
            } else if (pointsForThisIcon - 1 <= mana) {
                graphics.blit(RenderPipelines.GUI_TEXTURED, manaHalf, xo, yo,
                        0.0F, 0.0F, ICON_SIZE, ICON_SIZE, ICON_SIZE, ICON_SIZE, ARGB.opaque(-1));
            }
        }

        if (hypermana > 0) {
            Identifier hyperEmpty = hyperChangeBlink ? MANA_EMPTY_BLINKING : MANA_EMPTY;
            Identifier hyperFull = hyperChangeBlink ? HYPERMANA_FULL_BLINKING : HYPERMANA_FULL;
            Identifier hyperHalf = hyperChangeBlink ? HYPERMANA_HALF_BLINKING : HYPERMANA_HALF;

            for (int i = 0; i < hyperIcons; i++) {
                int index = manaIcons + i;
                int row = index / ROW_CAPACITY;
                int column = index % ROW_CAPACITY;
                int xo = xRight - column * ICON_SPACING - 9;
                int yo = yLine - row * 10;
                int pointsForThisIcon = (i + 1) * POINTS_PER_ICON;

                graphics.blit(RenderPipelines.GUI_TEXTURED, hyperEmpty, xo, yo,
                        0.0F, 0.0F, ICON_SIZE, ICON_SIZE, ICON_SIZE, ICON_SIZE, ARGB.opaque(-1));

                if (pointsForThisIcon <= hypermana) {
                    graphics.blit(RenderPipelines.GUI_TEXTURED, hyperFull, xo, yo,
                            0.0F, 0.0F, ICON_SIZE, ICON_SIZE, ICON_SIZE, ICON_SIZE, ARGB.opaque(-1));
                } else if (pointsForThisIcon - 1 <= hypermana) {
                    graphics.blit(RenderPipelines.GUI_TEXTURED, hyperHalf, xo, yo,
                            0.0F, 0.0F, ICON_SIZE, ICON_SIZE, ICON_SIZE, ICON_SIZE, ARGB.opaque(-1));
                }
            }
        }
    }
}