package net.alek.succorstadiums.arena;

import java.util.ArrayList;
import java.util.List;

// Wave class
public class Wave {

    // Variables to track wave number and list of wave mobs
    private int waveNumber;
    private final List<WaveMob> mobs;

    public Wave(int waveNumber) {
        this.waveNumber = waveNumber;
        this.mobs = new ArrayList<>();
    }

    // Accessor method to get the wave number
    public int getWaveNumber() { return waveNumber; }

    // Mutator method to set the wave number
    public void setWaveNumber(int waveNumber) { this.waveNumber = waveNumber; }

    // Accessor method to get the mobs in a wave
    public List<WaveMob> getMobs() { return mobs; }

    // Method to add a mob to a wave
    public void addMob(String mobType, int count, String ridingMob, String mainHandItem,
                       String offHandItem, List<String> armorItems,
                       String potionEffects, String enchantments) {

        WaveMob existingMob = mobs.stream()
                .filter(mob -> mob.getMobType().equalsIgnoreCase(mobType) &&
                        (ridingMob == null || ridingMob.equals(mob.getRidingMob())) &&
                        (mainHandItem == null || mainHandItem.equals(mob.getMainHandItem())) &&
                        (offHandItem == null || offHandItem.equals(mob.getOffHandItem())) &&
                        (armorItems.isEmpty() || armorItems.equals(mob.getArmorItems())))
                .findFirst()
                .orElse(null);

        if (existingMob != null) {
            existingMob.setCount(existingMob.getCount() + count);
        } else {
            mobs.add(new WaveMob(mobType, count, ridingMob, mainHandItem, offHandItem,
                    armorItems, potionEffects, enchantments));
        }
    }

    // Method to remove a mob from a wave
    public int removeMob(String mobType, int count) {
        for (WaveMob mob : mobs) {
            if (mob.getMobType().equalsIgnoreCase(mobType)) {
                int available = mob.getCount();
                int toRemove = count == -1 ? available : Math.min(count, available);
                int newCount = available - toRemove;
                if (newCount <= 0) {
                    mobs.remove(mob);
                } else {
                    mob.setCount(newCount);
                }
                return toRemove;
            }
        }
        return 0;
    }

    // Accessor method to get total mob count in a wave
    public int getTotalMobCount() {
        return mobs.stream().mapToInt(WaveMob::getCount).sum();
    }
}