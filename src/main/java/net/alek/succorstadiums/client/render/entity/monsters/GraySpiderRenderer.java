package net.alek.succorstadiums.client.render.entity.monsters;

import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.layers.SpiderEyesLayer;
import net.minecraft.client.model.monster.spider.SpiderModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.world.entity.monster.spider.Spider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.resources.Identifier;

import org.jspecify.annotations.NonNull;

import net.alek.succorstadiums.SuccorStadiums;

// GraySpiderRenderer class
public class GraySpiderRenderer<T extends Spider> extends MobRenderer<T, LivingEntityRenderState, SpiderModel> {
    public GraySpiderRenderer(final EntityRendererProvider.Context context) {
        this(context, ModelLayers.SPIDER);
    }

    public GraySpiderRenderer(final EntityRendererProvider.Context context, final ModelLayerLocation model) {
        super(context, new SpiderModel(context.bakeLayer(model)), 0.8F);
        this.addLayer(new SpiderEyesLayer<>(this));

    }

    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(SuccorStadiums.MOD_ID, "textures/entity/gray_spider/gray_spider.png");

    @Override
    public @NonNull Identifier getTextureLocation(@NonNull LivingEntityRenderState state) {
        return TEXTURE;
    }

    @Override
    public @NonNull LivingEntityRenderState createRenderState() {
        return new LivingEntityRenderState();
    }
}
