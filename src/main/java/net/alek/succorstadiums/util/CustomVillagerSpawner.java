package net.alek.succorstadiums.util;

import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.entity.npc.villager.VillagerType;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.Holder;

import net.alek.succorstadiums.item.ModItems;

import java.util.Optional;

// CustomVillagerSpawner class
public class CustomVillagerSpawner {

    private static Villager createVillager(
            ServerLevel level, Vec3 pos, float yaw,
            ResourceKey<VillagerProfession> villagerProfession, int villagerLevel,
            ResourceKey<VillagerType> villagerType, String villagerName) {

        // Create a new villager object
        Villager villager = new Villager(EntityTypes.VILLAGER, level);

        // Set villager position
        villager.setPos(pos.x, pos.y, pos.z);
        villager.setYRot(yaw);
        villager.setYHeadRot(yaw);
        villager.setYBodyRot(yaw);
        villager.setXRot(0.0F);
        villager.yRotO = yaw;

        // Resolve villagerProfession
        Holder<VillagerProfession> professionHolder =
                level.registryAccess()
                        .lookupOrThrow(Registries.VILLAGER_PROFESSION)
                        .getOrThrow(villagerProfession);

        // Resolve villagerType
        Holder<VillagerType> typeHolder =
                level.registryAccess()
                        .lookupOrThrow(Registries.VILLAGER_TYPE)
                        .getOrThrow(villagerType);

        // Set villager data to given villagerProfession, villagerLevel, and villagerType
        villager.setVillagerData(
                villager.getVillagerData()
                        .withProfession(professionHolder)
                        .withLevel(villagerLevel)
                        .withType(typeHolder)
        );

        // NBT-equivalent flags
        villager.setInvulnerable(true);
        villager.setPersistenceRequired();
        villager.setSilent(true);
        villager.setNoAi(true);
        villager.setCustomName(Component.literal(villagerName));

        return villager;
    }

    public static void spawnYeBuy(ServerLevel level, Vec3 pos, float yaw) {

        // Create a new villager object
        Villager villager = createVillager(level, pos, yaw, VillagerProfession.NITWIT, 2, VillagerType.PLAINS, "Ye Buy");

        // Get default vanilla offers and clear them
        MerchantOffers offers = villager.getOffers();
        offers.clear();

        // Add new offers to villager
        offers.add(new MerchantOffer(
                new ItemCost(Items.ROTTEN_FLESH, 10),
                new ItemStack(Items.COPPER_NUGGET, 3),
                9999999, 0, 0.0F
        ));

        offers.add(new MerchantOffer(
                new ItemCost(Items.BONE, 8),
                new ItemStack(Items.COPPER_NUGGET, 4),
                9999999, 0, 0.0F
        ));

        offers.add(new MerchantOffer(
                new ItemCost(Items.ARROW, 12),
                new ItemStack(Items.COPPER_NUGGET, 6),
                9999999, 0, 0.0F
        ));

        offers.add(new MerchantOffer(
                new ItemCost(Items.ROTTEN_FLESH, 16),
                Optional.of(new ItemCost(Items.BONE, 8)),
                new ItemStack(Items.COPPER_NUGGET, 10),
                9999999, 0, 0.0F
        ));

        offers.add(new MerchantOffer(
                new ItemCost(ModItems.PLANT_POWDER, 2),
                new ItemStack(Items.COPPER_NUGGET, 3),
                9999999, 0, 0.0F
        ));

        // Add villager to the level
        level.addFreshEntity(villager);
    }

    public static void spawnOlSell(ServerLevel level, Vec3 pos, float yaw) {

        // Create a new villager object
        Villager villager = createVillager(level, pos, yaw, VillagerProfession.WEAPONSMITH, 2, VillagerType.PLAINS, "Ol' Sell");

        // Get default vanilla offers and clear them
        MerchantOffers offers = villager.getOffers();
        offers.clear();

        // Add new offers to villager
        offers.add(new MerchantOffer(
                new ItemCost(Items.COPPER_NUGGET, 8),
                new ItemStack(ModItems.BONE_DAGGER, 1),
                9999999, 0, 0.0F
        ));

        offers.add(new MerchantOffer(
                new ItemCost(Items.COPPER_NUGGET, 12),
                new ItemStack(ModItems.BEAN_POLE, 1),
                9999999, 0, 0.0F
        ));

        offers.add(new MerchantOffer(
                new ItemCost(Items.COPPER_NUGGET, 24),
                new ItemStack(ModItems.SWORD_OF_THE_FOREST, 1),
                9999999, 0, 0.0F
        ));

        offers.add(new MerchantOffer(
                new ItemCost(Items.COPPER_NUGGET, 36),
                new ItemStack(ModItems.FLINT_CHARM, 1),
                9999999, 0, 0.0F
        ));

        offers.add(new MerchantOffer(
                new ItemCost(Items.COPPER_NUGGET, 32),
                new ItemStack(ModItems.SPROUT_SICKLE, 1),
                9999999, 0, 0.0F
        ));

        // Add villager to the level
        level.addFreshEntity(villager);
    }

    public static void spawnMarvin(ServerLevel level, Vec3 pos, float yaw) {

        // Create a new villager object
        Villager villager = createVillager(level, pos, yaw, VillagerProfession.BUTCHER, 2, VillagerType.PLAINS, "Marvin Malarkey");

        // Get default vanilla offers and clear them
        MerchantOffers offers = villager.getOffers();
        offers.clear();

        // Add new offers to villager
        offers.add(new MerchantOffer(
                new ItemCost(Items.COPPER_NUGGET, 4),
                new ItemStack(ModItems.ROTTEN_STEW, 16),
                9999999, 0, 0.0F
        ));

        offers.add(new MerchantOffer(
                new ItemCost(Items.COPPER_NUGGET, 4),
                Optional.of(new ItemCost(ModItems.PLANT_POWDER, 6)),
                new ItemStack(ModItems.CREEPER_SALVE, 4),
                9999999, 0, 0.0F
        ));

        offers.add(new MerchantOffer(
                new ItemCost(Items.COPPER_NUGGET, 4),
                new ItemStack(ModItems.GHRAMBLE_BAPPLE, 2),
                9999999, 0, 0.0F
        ));

        // Add villager to the level
        level.addFreshEntity(villager);
    }

    public static void spawnBimbleton(ServerLevel level, Vec3 pos, float yaw) {

        // Create a new villager object
        Villager villager = createVillager(level, pos, yaw, VillagerProfession.SHEPHERD, 2, VillagerType.PLAINS, "Ghimple Bimbleton");

        // Get default vanilla offers and clear them
        MerchantOffers offers = villager.getOffers();
        offers.clear();

        // Add new offers to villager
        offers.add(new MerchantOffer(
                new ItemCost(Items.COPPER_NUGGET, 12),
                new ItemStack(ModItems.DOG_WHISTLE, 1),
                9999999, 0, 0.0F
        ));

        // Add villager to the level
        level.addFreshEntity(villager);
    }

    public static void spawnBartholomew(ServerLevel level, Vec3 pos, float yaw) {

        // Create a new villager object
        Villager villager = createVillager(level, pos, yaw, VillagerProfession.FARMER, 2, VillagerType.PLAINS, "Bartholomew Bale");

        // Get default vanilla offers and clear them
        MerchantOffers offers = villager.getOffers();
        offers.clear();

        // Add new offers to villager
        offers.add(new MerchantOffer(
                new ItemCost(Items.COPPER_NUGGET, 12),
                new ItemStack(ModItems.BALE_HELMET, 1),
                9999999, 0, 0.0F
        ));

        offers.add(new MerchantOffer(
                new ItemCost(Items.COPPER_NUGGET, 16),
                new ItemStack(ModItems.BALE_CHESTPLATE, 1),
                9999999, 0, 0.0F
        ));

        offers.add(new MerchantOffer(
                new ItemCost(Items.COPPER_NUGGET, 14),
                new ItemStack(ModItems.BALE_LEGGINGS, 1),
                9999999, 0, 0.0F
        ));

        offers.add(new MerchantOffer(
                new ItemCost(Items.COPPER_NUGGET, 10),
                new ItemStack(ModItems.BALE_BOOTS, 1),
                9999999, 0, 0.0F
        ));

        offers.add(new MerchantOffer(
                new ItemCost(Items.COPPER_NUGGET, 20),
                new ItemStack(ModItems.FUMBLEBRINGER_FORK, 1),
                9999999, 0, 0.0F
        ));

        // Add the villager to the level
        level.addFreshEntity(villager);
    }

    public static void spawnPropung(ServerLevel level, Vec3 pos, float yaw) {

        // Create a new villager object
        Villager villager = createVillager(level, pos, yaw, VillagerProfession.CLERIC, 2, VillagerType.PLAINS, "Propung Giewish");

        // Get default vanilla offers and clear them
        MerchantOffers offers = villager.getOffers();
        offers.clear();

        // Add new offers to villager
        offers.add(new MerchantOffer(
                new ItemCost(Items.POISONOUS_POTATO, 2),
                new ItemStack(Items.COPPER_NUGGET, 3),
                9999999, 0, 0.0F
        ));

        offers.add(new MerchantOffer(
                new ItemCost(Items.POISONOUS_POTATO, 2),
                new ItemStack(ModItems.GHRAMBLE_BAPPLE, 2),
                9999999, 0, 0.0F
        ));

        offers.add(new MerchantOffer(
                new ItemCost(ModItems.PLANT_POWDER, 2),
                new ItemStack(Items.POISONOUS_POTATO, 2),
                9999999, 0, 0.0F
        ));

        // Add the villager to the level
        level.addFreshEntity(villager);
    }
}