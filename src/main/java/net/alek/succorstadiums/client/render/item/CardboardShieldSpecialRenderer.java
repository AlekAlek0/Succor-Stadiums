package net.alek.succorstadiums.client.render.item;

import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import net.minecraft.client.renderer.blockentity.BannerRenderer;
import net.minecraft.client.resources.model.sprite.SpriteGetter;
import net.minecraft.client.model.object.equipment.ShieldModel;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.resources.model.sprite.SpriteId;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.client.renderer.Sheets;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import com.mojang.serialization.MapCodec;
import net.minecraft.world.item.DyeColor;
import com.mojang.math.Transformation;
import net.minecraft.util.Unit;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import java.util.function.Consumer;
import org.joml.Vector3fc;
import org.joml.Vector3f;
import java.util.Objects;

// CardboardShieldSpecialRenderer class
public class CardboardShieldSpecialRenderer implements SpecialModelRenderer<DataComponentMap> {

    // The offset applied to the model by default
    public static final Transformation DEFAULT_TRANSFORMATION = new Transformation(null, null, new Vector3f(1.0F, -1.0F, -1.0F), null);

    // Maps Identifiers to their Sprites
    private final SpriteGetter sprites;

    // What model should be used
    private final ShieldModel model;

    // The base white texture
    private final SpriteId baseSprite;

    // The texture used when no dye or banner patterns are present
    private final SpriteId baseSpriteNoPattern;

    // Public constructor
    public CardboardShieldSpecialRenderer(final SpriteGetter sprites, final ShieldModel model, final SpriteId baseSprite, final SpriteId baseSpriteNoPattern) {
        this.sprites = sprites;
        this.model = model;
        this.baseSprite = baseSprite;
        this.baseSpriteNoPattern = baseSpriteNoPattern;
    }

    // Called before drawing pull out the data the renderer needs
    public @Nullable DataComponentMap extractArgument(final ItemStack stack) {
        return stack.immutableComponents();
    }

    // Get corner points for model to size and position shield in GUIs
    public void getExtents(final @NonNull Consumer<Vector3fc> output) {
        PoseStack poseStack = new PoseStack();
        this.model.root().getExtentsForGui(poseStack, output);
    }

    //
    public void submit(final @Nullable DataComponentMap components, final @NonNull PoseStack poseStack, final SubmitNodeCollector submitNodeCollector, final int lightCoords, final int overlayCoords, final boolean hasFoil, final int outlineColor) {

        // Get the banner patterns from the components and get the shield's dye color or null if it has none
        BannerPatternLayers patterns = components != null ? components.getOrDefault(DataComponents.BANNER_PATTERNS, BannerPatternLayers.EMPTY) : BannerPatternLayers.EMPTY;
        DyeColor baseColor = components != null ? components.get(DataComponents.BASE_COLOR) : null;

        // Initialize a boolean if the shield has any banner patterns or dye color
        boolean hasPatterns = !patterns.layers().isEmpty() || baseColor != null;

        // Choose the white base texture if it will be tinted or patterned, otherwise use the plain no pattern one
        SpriteId sprite = hasPatterns ? this.baseSprite : this.baseSpriteNoPattern;

        // Queue the shiel model to be drawn
        submitNodeCollector.submitModel(this.model, Unit.INSTANCE, poseStack, lightCoords, overlayCoords, -1, sprite, this.sprites, outlineColor, null);

        // If shield has patterns or dye draw the banner pattern layers on top
        if (hasPatterns) {
            BannerRenderer.submitPatterns(this.sprites, poseStack, submitNodeCollector, lightCoords, overlayCoords, this.model, Unit.INSTANCE, false, Objects.requireNonNullElse(baseColor, DyeColor.WHITE), patterns, null);
        }

        // If the item is enchanted draw the model a second time with the glint render type
        if (hasFoil) {
            submitNodeCollector.submitModel(this.model, Unit.INSTANCE, poseStack, RenderTypes.entityGlint(), lightCoords, overlayCoords, -1, this.sprites.get(sprite), 0, null);
        }
    }

    //
    public record Unbaked(Identifier base, Identifier noPattern) implements SpecialModelRenderer.Unbaked<DataComponentMap> {

        // Create Identifiers for shield texture as a map codec
        public static final MapCodec<Unbaked> MAP_CODEC = RecordCodecBuilder.mapCodec((i) -> i.group(
                Identifier.CODEC.fieldOf("texture").forGetter(Unbaked::base),
                Identifier.CODEC.fieldOf("no_pattern_texture").forGetter(Unbaked::noPattern)
        ).apply(i, Unbaked::new));

        // Return the codec so the game knows how to handle this renderer type
        public @NonNull MapCodec<Unbaked> type() {
            return MAP_CODEC;
        }

        // Convert the unbaked data into the working renderer
        public CardboardShieldSpecialRenderer bake(final SpecialModelRenderer.BakingContext context) {
            return new CardboardShieldSpecialRenderer(

                    // Texture lookup
                    context.sprites(),

                    // Fetch the geometry registered from CardboardShieldLayers
                    new ShieldModel(context.entityModelSet().bakeLayer(CardboardShieldLayers.CARDBOARD_SHIELD)),

                    // Convert each plain identifier into a spriteId pointing to the shield textures
                    Sheets.SHIELD_MAPPER.apply(this.base),
                    Sheets.SHIELD_MAPPER.apply(this.noPattern));
        }
    }
}