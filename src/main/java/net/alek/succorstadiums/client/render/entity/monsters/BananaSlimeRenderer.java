package net.alek.succorstadiums.client.render.entity.monsters;

import net.alek.succorstadiums.SuccorStadiums;
import net.minecraft.client.renderer.entity.SlimeRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.SlimeRenderState;
import net.minecraft.resources.Identifier;

public class BananaSlimeRenderer extends SlimeRenderer {
    public BananaSlimeRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.layers.clear();
        this.addLayer(new BananaSlimeOuterLayer(this, context.getModelSet()));
    }

    public static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(
                    SuccorStadiums.MOD_ID,
                    "textures/entity/banana_slime/banana_slime.png"
            );

    @Override
    public Identifier getTextureLocation(SlimeRenderState state) {
        return TEXTURE;
    }

}
