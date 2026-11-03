package net.alek.succorstadiums.client.render.entity.monsters;

import net.minecraft.client.renderer.entity.state.CreeperRenderState;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.CreeperRenderer;
import net.minecraft.resources.Identifier;

import org.jspecify.annotations.NonNull;

import net.alek.succorstadiums.SuccorStadiums;

// GrassCreeperRenderer class
public class GrassCreeperRenderer extends CreeperRenderer {
    public GrassCreeperRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    // Initialize our grass creeper texture
    public static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(SuccorStadiums.MOD_ID, "textures/entity/monsters/grass_creeper/grass_creeper.png");

    // Override getTextureLocation method with our custom texture
    @Override
    public @NonNull Identifier getTextureLocation(@NonNull CreeperRenderState state) {
        return TEXTURE;
    }
}
