package net.alek.succorstadiums.client.render.entity.monsters;

import net.minecraft.client.renderer.entity.layers.SkeletonClothingLayer;
import net.minecraft.client.renderer.entity.state.SkeletonRenderState;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.StrayRenderer;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.resources.Identifier;

import org.jspecify.annotations.NonNull;

import net.alek.succorstadiums.SuccorStadiums;

// SkelcrowRenderer class
public class SkelcrowRenderer extends StrayRenderer {
    public SkelcrowRenderer(final EntityRendererProvider.Context context) {
        super(context);
        this.layers.removeIf(layer -> layer instanceof SkeletonClothingLayer);
        this.addLayer(new SkeletonClothingLayer<>(this, context.getModelSet(), ModelLayers.STRAY_OUTER_LAYER, OVERLAY));
    }

    // Initialize our skelcrow and skelcrow overlay textures
    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(SuccorStadiums.MOD_ID, "textures/entity/monsters/skelcrow/skelcrow.png");
    private static final Identifier OVERLAY =
            Identifier.fromNamespaceAndPath(SuccorStadiums.MOD_ID, "textures/entity/monsters/skelcrow/skelcrow_overlay.png");

    // Override getTextureLocation method with our custom texture
    @Override
    public @NonNull Identifier getTextureLocation(final @NonNull SkeletonRenderState state) {
        return TEXTURE;
    }
}
