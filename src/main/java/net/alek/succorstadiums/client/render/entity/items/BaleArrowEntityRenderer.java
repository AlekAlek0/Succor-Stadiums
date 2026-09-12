package net.alek.succorstadiums.client.render.entity.items;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.ArrowRenderState;
import net.minecraft.client.renderer.entity.ArrowRenderer;

import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;

import net.alek.succorstadiums.entity.projectile.BaleArrowEntity;
import net.alek.succorstadiums.SuccorStadiums;

// BaleArrowEntityRenderer class
public class BaleArrowEntityRenderer extends ArrowRenderer<BaleArrowEntity, ArrowRenderState> {
    public BaleArrowEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    // Create an identifier for the bale arrow entity texture
    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(SuccorStadiums.MOD_ID, "textures/entity/projectiles/bale_arrow.png");

    // Override the render state of a regular arrow with our custom arrow entity render state
    @Override
    public @NonNull ArrowRenderState createRenderState() {
        return new ArrowRenderState();
    }

    // Override the texture location of a regular arrow with our custom arrow texture
    @Override
    protected @NonNull Identifier getTextureLocation(@NonNull ArrowRenderState renderState) {
        return TEXTURE;
    }
}