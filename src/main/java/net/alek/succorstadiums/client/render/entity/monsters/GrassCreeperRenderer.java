package net.alek.succorstadiums.client.render.entity.monsters;

import net.alek.succorstadiums.SuccorStadiums;
import net.minecraft.client.renderer.entity.CreeperRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.CreeperRenderState;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;

public class GrassCreeperRenderer extends CreeperRenderer {
    public GrassCreeperRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    public static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(
                    SuccorStadiums.MOD_ID,
                    "textures/entity/grass_creeper/grass_creeper.png"
            );

    @Override
    public @NonNull Identifier getTextureLocation(@NonNull CreeperRenderState state) {
        return TEXTURE;
    }
}
