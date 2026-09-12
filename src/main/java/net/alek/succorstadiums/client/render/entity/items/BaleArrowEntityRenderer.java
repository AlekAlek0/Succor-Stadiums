package net.alek.succorstadiums.client.render.entity.items;

import net.alek.succorstadiums.SuccorStadiums;
import net.alek.succorstadiums.entity.projectile.BaleArrowEntity;
import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.ArrowRenderState;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;

public class BaleArrowEntityRenderer extends ArrowRenderer<BaleArrowEntity, ArrowRenderState> {

    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(SuccorStadiums.MOD_ID, "textures/entity/projectiles/bale_arrow.png");

    public BaleArrowEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public @NonNull ArrowRenderState createRenderState() {
        return new ArrowRenderState();
    }

    @Override
    protected @NonNull Identifier getTextureLocation(@NonNull ArrowRenderState renderState) {
        return TEXTURE;
    }
}