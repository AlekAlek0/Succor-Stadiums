package net.alek.succorstadiums.client.render.entity.monsters;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.SlimeRenderState;
import net.minecraft.client.renderer.entity.SlimeRenderer;
import net.minecraft.resources.Identifier;

import org.jspecify.annotations.NonNull;

import net.alek.succorstadiums.client.render.entity.monsters.layers.BananaSlimeOuterLayer;
import net.alek.succorstadiums.SuccorStadiums;

// BananaSlimeRenderer class
public class BananaSlimeRenderer extends SlimeRenderer {
    public BananaSlimeRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.layers.clear();
        this.addLayer(new BananaSlimeOuterLayer(this, context.getModelSet()));
    }

    // Initialize our banana slime texture
    public static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(SuccorStadiums.MOD_ID, "textures/entity/monsters/banana_slime/banana_slime.png");

    // Override getTextureLocation method with our custom texture
    @Override
    public @NonNull Identifier getTextureLocation(@NonNull SlimeRenderState state) {
        return TEXTURE;
    }

}
