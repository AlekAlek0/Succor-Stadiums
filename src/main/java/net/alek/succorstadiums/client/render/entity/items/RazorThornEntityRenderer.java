package net.alek.succorstadiums.client.render.entity.items;

import net.minecraft.client.renderer.entity.state.ThrownItemRenderState;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.world.item.ItemDisplayContext;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.util.Mth;
import com.mojang.math.Axis;

import net.alek.succorstadiums.entity.items.RazorThornEntity;

import org.jspecify.annotations.NonNull;

public class RazorThornEntityRenderer extends EntityRenderer<RazorThornEntity, RazorThornEntityRenderer.RazorThornRenderState> {

    private final ItemModelResolver itemModelResolver;

    public RazorThornEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.itemModelResolver = context.getItemModelResolver();
    }

    @Override
    public @NonNull RazorThornRenderState createRenderState() {
        return new RazorThornRenderState();
    }

    @Override
    public void extractRenderState(@NonNull RazorThornEntity entity, @NonNull RazorThornRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        this.itemModelResolver.updateForNonLiving(state.item, entity.getItem(), ItemDisplayContext.GROUND, entity);

        state.yRot = Mth.rotLerp(partialTicks, entity.yRotO, entity.getYRot());
        state.xRot = Mth.lerp(partialTicks, entity.xRotO, entity.getXRot());
        state.plantedOnGround = entity.isPlantedInGround();
    }

    @Override
    public void submit(RazorThornRenderState state, PoseStack poseStack, @NonNull SubmitNodeCollector collector, @NonNull CameraRenderState camera) {
        poseStack.pushPose();

        if (state.plantedOnGround) {
            poseStack.mulPose(Axis.YP.rotationDegrees(state.yRot - 90.0f));
            poseStack.mulPose(Axis.ZP.rotationDegrees(270.0f));
        } else {
            poseStack.mulPose(Axis.YP.rotationDegrees(state.yRot - 90.0f));
            poseStack.mulPose(Axis.ZP.rotationDegrees(state.xRot));
        }

        state.item.submit(poseStack, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor);

        poseStack.popPose();
        super.submit(state, poseStack, collector, camera);
    }

    public static class RazorThornRenderState extends ThrownItemRenderState {
        public float yRot;
        public float xRot;
        public boolean plantedOnGround;
    }
}