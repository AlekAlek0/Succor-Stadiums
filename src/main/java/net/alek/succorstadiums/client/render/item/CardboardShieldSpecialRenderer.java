package net.alek.succorstadiums.client.render.item;

import java.util.Objects;
import java.util.function.Consumer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Transformation;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.client.model.object.equipment.ShieldModel;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BannerRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.resources.model.sprite.SpriteGetter;
import net.minecraft.client.resources.model.sprite.SpriteId;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Unit;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BannerPatternLayers;

public class CardboardShieldSpecialRenderer implements SpecialModelRenderer<DataComponentMap> {
    // The offset applied to the model by default
    public static final Transformation DEFAULT_TRANSFORMATION = new Transformation(null, null, new Vector3f(1.0F, -1.0F, -1.0F), null);

    // Maps Identifiers to their Sprites
    private final SpriteGetter sprites;
    // What model should be used
    private final ShieldModel model;
    // The base white texture (provided in the client item)
    private final SpriteId baseSprite;
    // The texture used when no dye or banner patterns are present
    private final SpriteId baseSpriteNoPattern;

    public CardboardShieldSpecialRenderer(final SpriteGetter sprites, final ShieldModel model, final SpriteId baseSprite, final SpriteId baseSpriteNoPattern) {
        this.sprites = sprites;
        this.model = model;
        this.baseSprite = baseSprite;
        this.baseSpriteNoPattern = baseSpriteNoPattern;
    }

    public @Nullable DataComponentMap extractArgument(final ItemStack stack) {
        return stack.immutableComponents();
    }

    public void getExtents(final @NonNull Consumer<Vector3fc> output) {
        PoseStack poseStack = new PoseStack();
        this.model.root().getExtentsForGui(poseStack, output);
    }

    public void submit(final @Nullable DataComponentMap components, final @NonNull PoseStack poseStack, final SubmitNodeCollector submitNodeCollector, final int lightCoords, final int overlayCoords, final boolean hasFoil, final int outlineColor) {
        BannerPatternLayers patterns = components != null ? components.getOrDefault(DataComponents.BANNER_PATTERNS, BannerPatternLayers.EMPTY) : BannerPatternLayers.EMPTY;
        DyeColor baseColor = components != null ? components.get(DataComponents.BASE_COLOR) : null;
        boolean hasPatterns = !patterns.layers().isEmpty() || baseColor != null;
        SpriteId sprite = hasPatterns ? this.baseSprite : this.baseSpriteNoPattern;

        submitNodeCollector.submitModel(this.model, Unit.INSTANCE, poseStack, lightCoords, overlayCoords, -1, sprite, this.sprites, outlineColor, null);

        if (hasPatterns) {
            BannerRenderer.submitPatterns(this.sprites, poseStack, submitNodeCollector, lightCoords, overlayCoords, this.model, Unit.INSTANCE, false, Objects.requireNonNullElse(baseColor, DyeColor.WHITE), patterns, null);
        }

        if (hasFoil) {
            submitNodeCollector.submitModel(this.model, Unit.INSTANCE, poseStack, RenderTypes.entityGlint(), lightCoords, overlayCoords, -1, this.sprites.get(sprite), 0, null);
        }
    }

    public record Unbaked(Identifier base, Identifier noPattern) implements SpecialModelRenderer.Unbaked<DataComponentMap> {
        public static final MapCodec<Unbaked> MAP_CODEC = RecordCodecBuilder.mapCodec((i) -> i.group(
                Identifier.CODEC.fieldOf("texture").forGetter(Unbaked::base),
                Identifier.CODEC.fieldOf("no_pattern_texture").forGetter(Unbaked::noPattern)
        ).apply(i, Unbaked::new));

        public @NonNull MapCodec<Unbaked> type() {
            return MAP_CODEC;
        }

        public CardboardShieldSpecialRenderer bake(final SpecialModelRenderer.BakingContext context) {
            return new CardboardShieldSpecialRenderer(
                    context.sprites(),
                    new ShieldModel(context.entityModelSet().bakeLayer(CardboardShieldLayers.CARDBOARD_SHIELD)),
                    Sheets.SHIELD_MAPPER.apply(this.base),
                    Sheets.SHIELD_MAPPER.apply(this.noPattern));
        }
    }
}