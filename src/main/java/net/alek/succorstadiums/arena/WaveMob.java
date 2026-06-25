package net.alek.succorstadiums.arena;

import java.util.List;
import java.util.ArrayList;

public class WaveMob {

    private String mobType;
    private int count;
    private String ridingMob;
    private String mainHandItem;
    private String offHandItem;
    private List<String> armorItems;
    private String potionEffects; // New field
    private String enchantments;  // New field

    // Constructor to create a waveMob with given mobType and count.
    public WaveMob(String mobType, int count) {
        this(mobType, count, null, null, null, new ArrayList<>(), null, null);
    }

    public WaveMob(String mobType, int count, String ridingMob, String mainHandItem, String offHandItem, List<String> armorItems) {
        this(mobType, count, ridingMob, mainHandItem, offHandItem, armorItems, null, null);
    }

    public WaveMob(String mobType, int count, String ridingMob, String mainHandItem, String offHandItem, List<String> armorItems, String potionEffects, String enchantments) {
        this.mobType = mobType;
        this.count = count;
        this.ridingMob = ridingMob;
        this.mainHandItem = mainHandItem;
        this.offHandItem = offHandItem;
        this.armorItems = armorItems;
        this.potionEffects = potionEffects;
        this.enchantments = enchantments;
    }

    // Get the mobType of the Wavemob
    public String getMobType() {
        return mobType;
    }

    // Get mob count of the waveMob
    public int getCount() {
        return count;
    }

    // Get the riding mob type
    public String getRidingMob() {
        return ridingMob;
    }

    // Get the item in the main hand
    public String getMainHandItem() {
        return mainHandItem;
    }

    // Get the item in the off hand
    public String getOffHandItem() {
        return offHandItem;
    }

    // Get the list of armor items
    public List<String> getArmorItems() {
        return armorItems;
    }

    // Get the potion effects string
    public String getPotionEffects() {
        return potionEffects;
    }

    // Get the enchantments string
    public String getEnchantments() {
        return enchantments;
    }

    // Set the wave mobType
    public void setMobType(String mobType) {
        this.mobType = mobType;
    }

    // Set the mob count of the waveMob
    public void setCount(int count) { this.count = count; }

    // Set the riding mob type
    public void setRidingMob(String ridingMob) {
        this.ridingMob = ridingMob;
    }

    // Set the item in the main hand
    public void setMainHandItem(String mainHandItem) {
        this.mainHandItem = mainHandItem;
    }

    // Set the item in the offhand
    public void setOffHandItem(String offHandItem) {
        this.offHandItem = offHandItem;
    }

    // Set the list of armor items
    public void setArmorItems(List<String> armorItems) {
        this.armorItems = armorItems;
    }

    // Set the potion effects string
    public void setPotionEffects(String potionEffects) {
        this.potionEffects = potionEffects;
    }

    // Set the enchantments string
    public void setEnchantments(String enchantments) {
        this.enchantments = enchantments;
    }
}
