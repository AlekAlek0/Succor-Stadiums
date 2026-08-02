package net.alek.succorstadiums.util;

import net.alek.succorstadiums.item.ModItems;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.npc.villager.VillagerType;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

public class CustomVillagerSpawner {

    public static void spawnYeBuy(ServerLevel level, Vec3 pos, float yaw) {
        Villager villager = new Villager(EntityTypes.VILLAGER, level);

        villager.setPos(pos.x, pos.y, pos.z);
        villager.setYRot(yaw);
        villager.setYHeadRot(yaw);
        villager.setYBodyRot(yaw);
        villager.setXRot(0.0F);
        villager.yRotO = yaw;

        // Profession nitwit
        Holder<VillagerProfession> farmerProfession =
                level.registryAccess()
                        .lookupOrThrow(Registries.VILLAGER_PROFESSION)
                        .getOrThrow(VillagerProfession.NITWIT);

        // Plains type
        Holder<VillagerType> plainsType =
                level.registryAccess()
                        .lookupOrThrow(Registries.VILLAGER_TYPE)
                        .getOrThrow(VillagerType.PLAINS);

        // Set villager data to given profession, level, and type
        villager.setVillagerData(
                villager.getVillagerData()
                        .withProfession(farmerProfession)
                        .withLevel(2)
                        .withType(plainsType)
        );

        // NBT-equivalent flags
        villager.setInvulnerable(true);
        villager.setPersistenceRequired();
        villager.setSilent(true);
        villager.setNoAi(true);
        villager.setCustomName(Component.literal("Ye Buy"));

        // Build trades
        MerchantOffers offers = villager.getOffers();
        offers.clear();

        offers.add(new MerchantOffer(
                new ItemCost(Items.ROTTEN_FLESH, 16),
                new ItemStack(Items.COPPER_NUGGET, 3),
                9999999, 0, 0.0F
        ));

        offers.add(new MerchantOffer(
                new ItemCost(Items.BONE, 8),
                new ItemStack(Items.COPPER_NUGGET, 2),
                9999999, 0, 0.0F
        ));

        offers.add(new MerchantOffer(
                new ItemCost(Items.ARROW, 32),
                new ItemStack(Items.COPPER_NUGGET, 6),
                9999999, 0, 0.0F
        ));

        offers.add(new MerchantOffer(
                new ItemCost(Items.ROTTEN_FLESH, 16),
                Optional.of(new ItemCost(Items.BONE, 8)),
                new ItemStack(Items.COPPER_NUGGET, 12),
                9999999, 0, 0.0F
        ));

        offers.add(new MerchantOffer(
                new ItemCost(ModItems.PLANT_POWDER, 8),
                new ItemStack(Items.COPPER_NUGGET, 3),
                9999999, 0, 0.0F
        ));

        offers.add(new MerchantOffer(
                new ItemCost(Items.POISONOUS_POTATO, 2),
                new ItemStack(Items.COPPER_NUGGET, 4),
                9999999, 0, 0.0F
        ));

        level.addFreshEntity(villager);
    }

    public static void spawnOlSell(ServerLevel level, Vec3 pos, float yaw) {
        Villager villager = new Villager(EntityTypes.VILLAGER, level);

        villager.setPos(pos.x, pos.y, pos.z);
        villager.setYRot(yaw);
        villager.setYHeadRot(yaw);
        villager.setYBodyRot(yaw);
        villager.setXRot(0.0F);
        villager.yRotO = yaw;

        // Profession weaponsmith
        Holder<VillagerProfession> farmerProfession =
                level.registryAccess()
                        .lookupOrThrow(Registries.VILLAGER_PROFESSION)
                        .getOrThrow(VillagerProfession.WEAPONSMITH);

        // Plains type
        Holder<VillagerType> plainsType =
                level.registryAccess()
                        .lookupOrThrow(Registries.VILLAGER_TYPE)
                        .getOrThrow(VillagerType.PLAINS);

        // Set villager data to given profession, level, and type
        villager.setVillagerData(
                villager.getVillagerData()
                        .withProfession(farmerProfession)
                        .withLevel(2)
                        .withType(plainsType)
        );

        // NBT-equivalent flags
        villager.setInvulnerable(true);
        villager.setPersistenceRequired();
        villager.setSilent(true);
        villager.setNoAi(true);
        villager.setCustomName(Component.literal("Ol' Sell"));

        // Build trades
        MerchantOffers offers = villager.getOffers();
        offers.clear();

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
                new ItemCost(Items.COPPER_NUGGET, 20),
                new ItemStack(ModItems.FUMBLEBRINGER_FORK, 1),
                9999999, 0, 0.0F
        ));

        offers.add(new MerchantOffer(
                new ItemCost(Items.COPPER_NUGGET, 24),
                new ItemStack(ModItems.SWORD_OF_THE_FOREST, 1),
                9999999, 0, 0.0F
        ));

        offers.add(new MerchantOffer(
                new ItemCost(Items.COPPER_NUGGET, 32),
                new ItemStack(ModItems.FLINT_CHARM, 1),
                9999999, 0, 0.0F
        ));

        level.addFreshEntity(villager);
    }

    public static void spawnMarvin(ServerLevel level, Vec3 pos, float yaw) {
        Villager villager = new Villager(EntityTypes.VILLAGER, level);

        villager.setPos(pos.x, pos.y, pos.z);
        villager.setYRot(yaw);
        villager.setYHeadRot(yaw);
        villager.setYBodyRot(yaw);
        villager.setXRot(0.0F);
        villager.yRotO = yaw;

        // Profession nitwit
        Holder<VillagerProfession> butcherProfession =
                level.registryAccess()
                        .lookupOrThrow(Registries.VILLAGER_PROFESSION)
                        .getOrThrow(VillagerProfession.BUTCHER);

        // Plains type
        Holder<VillagerType> plainsType =
                level.registryAccess()
                        .lookupOrThrow(Registries.VILLAGER_TYPE)
                        .getOrThrow(VillagerType.PLAINS);

        // Set villager data to given profession, level, and type
        villager.setVillagerData(
                villager.getVillagerData()
                        .withProfession(butcherProfession)
                        .withLevel(2)
                        .withType(plainsType)
        );

        // NBT-equivalent flags
        villager.setInvulnerable(true);
        villager.setPersistenceRequired();
        villager.setSilent(true);
        villager.setNoAi(true);
        villager.setCustomName(Component.literal("Marvin Malarkey"));

        // Build trades
        MerchantOffers offers = villager.getOffers();
        offers.clear();

        offers.add(new MerchantOffer(
                new ItemCost(Items.COPPER_NUGGET, 4),
                new ItemStack(ModItems.ROTTEN_STEW, 16),
                9999999, 0, 0.0F
        ));

        offers.add(new MerchantOffer(
                new ItemCost(ModItems.PLANT_POWDER, 6),
                Optional.of(new ItemCost(Items.COPPER_NUGGET, 4)),
                new ItemStack(ModItems.CREEPER_SALVE, 4),
                9999999, 0, 0.0F
        ));

        offers.add(new MerchantOffer(
                new ItemCost(Items.COPPER_NUGGET, 4),
                new ItemStack(ModItems.GRAMBLE_BAPPLE, 2),
                9999999, 0, 0.0F
        ));

        level.addFreshEntity(villager);
    }

}