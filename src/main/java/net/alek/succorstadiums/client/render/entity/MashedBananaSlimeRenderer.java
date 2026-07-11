package net.alek.succorstadiums.client.render.entity;

import net.alek.succorstadiums.SuccorStadiums;
import net.minecraft.client.renderer.entity.SlimeRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.SlimeRenderState;
import net.minecraft.resources.Identifier;

public class MashedBananaSlimeRenderer extends SlimeRenderer {

    public static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(
                    SuccorStadiums.MOD_ID,
                    "textures/entity/mashed_banana_slime/mashed_banana_slime.png"
            );

    public MashedBananaSlimeRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.layers.clear();
        this.addLayer(new MashedBananaSlimeOuterLayer(this, context.getModelSet()));
    }
    @Override
    public Identifier getTextureLocation(SlimeRenderState state) {
        return TEXTURE;
    }

}
