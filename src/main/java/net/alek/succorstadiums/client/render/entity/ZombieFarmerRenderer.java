package net.alek.succorstadiums.client.render.entity;

import net.alek.succorstadiums.SuccorStadiums;
import net.alek.succorstadiums.entity.ZombieFarmer;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.monster.zombie.BabyZombieModel;
import net.minecraft.client.model.monster.zombie.ZombieModel;
import net.minecraft.client.renderer.entity.AbstractZombieRenderer;
import net.minecraft.client.renderer.entity.ArmorModelSet;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.ZombieRenderState;
import net.minecraft.resources.Identifier;

public class ZombieFarmerRenderer extends AbstractZombieRenderer<ZombieFarmer, ZombieRenderState, ZombieModel<ZombieRenderState>> {

    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(SuccorStadiums.MOD_ID, "textures/entity/zombie_farmer/zombie_farmer.png");

    public ZombieFarmerRenderer(final EntityRendererProvider.Context context) {
        this(context, ModelLayers.ZOMBIE, ModelLayers.ZOMBIE_BABY, ModelLayers.ZOMBIE_ARMOR, ModelLayers.ZOMBIE_BABY_ARMOR);
    }

    private ZombieFarmerRenderer(final EntityRendererProvider.Context context,
                                 final ModelLayerLocation body,
                                 final ModelLayerLocation babyBody,
                                 final ArmorModelSet<ModelLayerLocation> armorSet,
                                 final ArmorModelSet<ModelLayerLocation> babyArmorSet) {
        super(context,
                new ZombieModel<>(context.bakeLayer(body)),
                new BabyZombieModel<>(context.bakeLayer(babyBody)),
                ArmorModelSet.bake(armorSet, context.getModelSet(), ZombieModel::new),
                ArmorModelSet.bake(babyArmorSet, context.getModelSet(), BabyZombieModel::new));
    }

    @Override
    public ZombieRenderState createRenderState() {
        return new ZombieRenderState();
    }

    @Override
    public Identifier getTextureLocation(final ZombieRenderState state) {
        return TEXTURE;
    }

    /*
    @Override
    public Identifier getTextureLocation(final S state) {
        return state.isBaby ? BABY_ZOMBIE_LOCATION : ZOMBIE_LOCATION;
    }
     */
}