package net.alek.succorstadiums.client.render.entity.monsters;

import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.model.monster.spider.SpiderModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.world.entity.monster.spider.Spider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.resources.Identifier;

import org.jspecify.annotations.NonNull;

import net.alek.succorstadiums.client.render.entity.monsters.layers.GraySpiderEyesLayer;
import net.alek.succorstadiums.SuccorStadiums;

// GraySpiderRenderer class
public class GraySpiderRenderer<T extends Spider> extends MobRenderer<T, LivingEntityRenderState, SpiderModel> {
    public GraySpiderRenderer(final EntityRendererProvider.Context context) {
        this(context, ModelLayers.SPIDER);
    }

    public GraySpiderRenderer(final EntityRendererProvider.Context context, final ModelLayerLocation model) {
        super(context, new SpiderModel(context.bakeLayer(model)), 0.8F);
        this.addLayer(new GraySpiderEyesLayer<>(this));

    }

    // Initialize our gray spider texture
    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(SuccorStadiums.MOD_ID, "textures/entity/monsters/gray_spider/gray_spider.png");

    // Override getTextureLocation method with our custom texture
    @Override
    public @NonNull Identifier getTextureLocation(@NonNull LivingEntityRenderState state) {
        return TEXTURE;
    }

    @Override
    public @NonNull LivingEntityRenderState createRenderState() {
        return new LivingEntityRenderState();
    }
}
