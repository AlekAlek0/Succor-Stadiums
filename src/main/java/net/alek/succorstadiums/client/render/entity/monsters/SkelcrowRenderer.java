package net.alek.succorstadiums.client.render.entity.monsters;

import net.alek.succorstadiums.SuccorStadiums;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.StrayRenderer;
import net.minecraft.client.renderer.entity.layers.SkeletonClothingLayer;
import net.minecraft.client.renderer.entity.state.SkeletonRenderState;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;

public class SkelcrowRenderer extends StrayRenderer {

    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(SuccorStadiums.MOD_ID, "textures/entity/skelcrow/skelcrow.png");
    private static final Identifier OVERLAY =
            Identifier.fromNamespaceAndPath(SuccorStadiums.MOD_ID, "textures/entity/skelcrow/skelcrow_overlay.png");

    public SkelcrowRenderer(final EntityRendererProvider.Context context) {
        super(context);
        this.layers.removeIf(layer -> layer instanceof SkeletonClothingLayer);
        this.addLayer(new SkeletonClothingLayer<>(this, context.getModelSet(), ModelLayers.STRAY_OUTER_LAYER, OVERLAY));
    }

    @Override
    public @NonNull Identifier getTextureLocation(final @NonNull SkeletonRenderState state) {
        return TEXTURE;
    }
}
