package net.alek.succorstadiums.client.render.entity.monsters;

import net.minecraft.client.renderer.entity.state.ZombieRenderState;
import net.minecraft.client.renderer.entity.AbstractZombieRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.model.monster.zombie.BabyZombieModel;
import net.minecraft.client.model.monster.zombie.ZombieModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.ArmorModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.resources.Identifier;

import org.jspecify.annotations.NonNull;

import net.alek.succorstadiums.entity.monsters.Farmbie;
import net.alek.succorstadiums.SuccorStadiums;

// FarmbieRenderer class
public class FarmbieRenderer extends AbstractZombieRenderer<Farmbie, ZombieRenderState, ZombieModel<ZombieRenderState>> {
    public FarmbieRenderer(final EntityRendererProvider.Context context) {
        this(context, ModelLayers.ZOMBIE, ModelLayers.ZOMBIE_BABY, ModelLayers.ZOMBIE_ARMOR, ModelLayers.ZOMBIE_BABY_ARMOR);
    }

    private FarmbieRenderer(final EntityRendererProvider.Context context,
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

    // Initialize our farmbie texture
    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(SuccorStadiums.MOD_ID, "textures/entity/monsters/farmbie/farmbie.png");

    // Override getTextureLocation method with our custom texture
    @Override
    public @NonNull Identifier getTextureLocation(final @NonNull ZombieRenderState state) {
        return TEXTURE;
    }

    @Override
    public @NonNull ZombieRenderState createRenderState() {
        return new ZombieRenderState();
    }

    /*
    @Override
    public Identifier getTextureLocation(final S state) {
        return state.isBaby ? BABY_ZOMBIE_LOCATION : ZOMBIE_LOCATION;
    }
     */
}