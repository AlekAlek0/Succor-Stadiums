package net.alek.succorstadiums.client.render.entity.monsters;

import net.minecraft.client.renderer.entity.state.ZombieRenderState;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.Identifier;

import org.jspecify.annotations.NonNull;

import net.alek.succorstadiums.SuccorStadiums;

// FarmbieBlueRenderer class
public class FarmbieBlueRenderer extends FarmbieRenderer {
    public FarmbieBlueRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    // Initialize our farmbie blue texture
    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(SuccorStadiums.MOD_ID, "textures/entity/monsters/farmbie/farmbie_blue.png");

    // Override getTextureLocation method with our custom texture
    @Override
    public @NonNull Identifier getTextureLocation(final @NonNull ZombieRenderState state) {
        return TEXTURE;
    }

}
