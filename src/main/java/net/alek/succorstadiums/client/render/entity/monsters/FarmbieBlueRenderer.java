package net.alek.succorstadiums.client.render.entity.monsters;

import net.alek.succorstadiums.SuccorStadiums;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.ZombieRenderState;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;

public class FarmbieBlueRenderer extends FarmbieRenderer {
    public FarmbieBlueRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(SuccorStadiums.MOD_ID, "textures/entity/farmbie/farmbie_blue.png");


    @Override
    public @NonNull Identifier getTextureLocation(final @NonNull ZombieRenderState state) {
        return TEXTURE;
    }

}
