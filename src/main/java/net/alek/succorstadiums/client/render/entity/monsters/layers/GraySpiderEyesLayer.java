package net.alek.succorstadiums.client.render.entity.monsters.layers;

import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.model.monster.spider.SpiderModel;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;

import org.jspecify.annotations.NonNull;

import net.alek.succorstadiums.SuccorStadiums;

// GraySpiderEyesLayer class
public class GraySpiderEyesLayer<M extends SpiderModel> extends EyesLayer<LivingEntityRenderState, M> {
    public GraySpiderEyesLayer(final RenderLayerParent<LivingEntityRenderState, M> renderer) {
        super(renderer);
    }

    private static final RenderType GRAY_SPIDER_EYES =
            RenderTypes.eyes(Identifier.fromNamespaceAndPath(SuccorStadiums.MOD_ID,"textures/entity/gray_spider/gray_spider_eyes.png"));

    @Override
    public @NonNull RenderType renderType() {
        return GRAY_SPIDER_EYES;
    }
}
