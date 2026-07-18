package net.alek.succorstadiums.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.monster.slime.SlimeModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.SlimeRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;

@Environment(EnvType.CLIENT)
public class BananaSlimeOuterLayer extends RenderLayer<SlimeRenderState, SlimeModel> {
    private final SlimeModel model;

    public BananaSlimeOuterLayer(final RenderLayerParent<SlimeRenderState, SlimeModel> renderer, final EntityModelSet modelSet) {
        super(renderer);
        this.model = new SlimeModel(modelSet.bakeLayer(ModelLayers.SLIME_OUTER ));
    }

    @Override
    public void submit(final PoseStack poseStack, final SubmitNodeCollector submitNodeCollector, final int lightCoords, final SlimeRenderState state, final float yRot, final float xRot) {
        boolean appearsGlowingWithInvisibility = state.appearsGlowing() && state.isInvisible;
        if (!state.isInvisible || appearsGlowingWithInvisibility) {
            int overlayCoords = LivingEntityRenderer.getOverlayCoords(state, 0.0F);
            int outlineColor = state.outlineColor;

            if (appearsGlowingWithInvisibility) {
                submitNodeCollector.order(1).submitModel(this.model, state, poseStack, RenderTypes.outline(BananaSlimeRenderer.TEXTURE), lightCoords, overlayCoords, outlineColor, (ModelFeatureRenderer.CrumblingOverlay)null);
            } else {
                submitNodeCollector.order(1).submitModel(this.model, state, poseStack, RenderTypes.entityTranslucent(BananaSlimeRenderer.TEXTURE), lightCoords, overlayCoords, outlineColor, (ModelFeatureRenderer.CrumblingOverlay)null);
            }
        }
    }
}