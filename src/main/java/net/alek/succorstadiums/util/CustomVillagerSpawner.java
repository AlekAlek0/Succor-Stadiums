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
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.Holder;

import net.alek.succorstadiums.item.ModItems;

import java.util.Optional;

// CustomVillagerSpawner class
public class CustomVillagerSpawner {

    // Helper method to create a base villager
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

    // Helper method to add an offer to a villager
    private static void addOffer(MerchantOffers existingOffers, ItemLike buyItem, int buyItemCount,
                                 ItemLike sellItem, int sellItemCount,
                                 int maxUses, int xp, float priceMultiplier) {

        existingOffers.add(new MerchantOffer(
                new ItemCost(buyItem, buyItemCount),
                new ItemStack(sellItem, sellItemCount),
                maxUses, xp, priceMultiplier
        ));
    }

    // Helper method to add an offer to a villager with a 2nd buy item
    private static void addOffer(MerchantOffers existingOffers,
                                 ItemLike buyItem, int buyItemCount,
                                 ItemLike secondBuyItem, int secondBuyItemCount,
                                 ItemLike sellItem, int sellItemCount,
                                 int maxUses, int xp, float priceMultiplier) {

        existingOffers.add(new MerchantOffer(
                new ItemCost(buyItem, buyItemCount),
                Optional.of(new ItemCost(secondBuyItem, secondBuyItemCount)),
                new ItemStack(sellItem, sellItemCount),
                maxUses, xp, priceMultiplier
        ));
    }

    public static void spawnYeBuy(ServerLevel level, Vec3 pos, float yaw) {

        // Create a new villager object
        Villager villager = createVillager(level, pos, yaw, VillagerProfession.NITWIT, 2, VillagerType.PLAINS, "Ye Buy");

        // Get default vanilla offers and clear them
        MerchantOffers offers = villager.getOffers();
        offers.clear();

        // Add new offers to villager
        addOffer(offers, Items.ROTTEN_FLESH, 10,
                Items.COPPER_NUGGET, 3,
                9999999, 0, 0.0F);

        addOffer(offers, Items.BONE, 8,
                Items.COPPER_NUGGET, 4,
                9999999, 0, 0.0F);

        addOffer(offers, Items.ARROW, 12,
                Items.COPPER_NUGGET, 6,
                9999999, 0, 0.0F);

        addOffer(offers, Items.ROTTEN_FLESH, 16,
                Items.BONE, 8,
                Items.COPPER_NUGGET, 10,
                9999999, 0, 0.0F);

        addOffer(offers, ModItems.PLANT_POWDER, 2,
                Items.COPPER_NUGGET, 3,
                9999999, 0, 0.0F);

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
        addOffer(offers, Items.COPPER_NUGGET, 8,
                ModItems.BONE_DAGGER, 1,
                9999999, 0, 0.0F);

        addOffer(offers, Items.COPPER_NUGGET, 12,
                ModItems.BEAN_POLE, 1,
                9999999, 0, 0.0F);

        addOffer(offers, Items.COPPER_NUGGET, 24,
                ModItems.SWORD_OF_THE_FOREST, 1,
                9999999, 0, 0.0F);

        addOffer(offers, Items.COPPER_NUGGET, 36,
                ModItems.FLINT_CHARM, 1,
                9999999, 0, 0.0F);

        addOffer(offers, Items.COPPER_NUGGET, 32,
                ModItems.SPROUT_SICKLE, 1,
                9999999, 0, 0.0F);

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
        addOffer(offers, Items.COPPER_NUGGET, 4,
                ModItems.ROTTEN_STEW, 16,
                9999999, 0, 0.0F);

        addOffer(offers, Items.COPPER_NUGGET, 4,
                ModItems.PLANT_POWDER, 6,
                ModItems.CREEPER_SALVE, 4,
                9999999, 0, 0.0F);

        addOffer(offers, Items.COPPER_NUGGET, 4,
                ModItems.GHRAMBLE_BAPPLE, 2,
                9999999, 0, 0.0F);

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
        addOffer(offers, Items.COPPER_NUGGET, 12,
                ModItems.DOG_WHISTLE, 1,
                9999999, 0, 0.0F);

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
        addOffer(offers, Items.COPPER_NUGGET, 12,
                ModItems.BALE_HELMET, 1,
                9999999, 0, 0.0F);

        addOffer(offers, Items.COPPER_NUGGET, 16,
                ModItems.BALE_CHESTPLATE, 1,
                9999999, 0, 0.0F);

        addOffer(offers, Items.COPPER_NUGGET, 14,
                ModItems.BALE_LEGGINGS, 1,
                9999999, 0, 0.0F);

        addOffer(offers, Items.COPPER_NUGGET, 10,
                ModItems.BALE_BOOTS, 1,
                9999999, 0, 0.0F);

        addOffer(offers, Items.COPPER_NUGGET, 20,
                ModItems.FUMBLEBRINGER_FORK, 1,
                9999999, 0, 0.0F);

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
        addOffer(offers, Items.POISONOUS_POTATO, 2,
                Items.COPPER_NUGGET, 3,
                9999999, 0, 0.0F);

        addOffer(offers, Items.POISONOUS_POTATO, 2,
                ModItems.GHRAMBLE_BAPPLE, 2,
                9999999, 0, 0.0F);

        addOffer(offers, ModItems.PLANT_POWDER, 2,
                Items.POISONOUS_POTATO, 2,
                9999999, 0, 0.0F);

        // Add the villager to the level
        level.addFreshEntity(villager);
    }
}