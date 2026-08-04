package net.alek.succorstadiums.arena;

import java.util.ArrayList;
import java.util.List;

// Wave class
public class Wave {

    // Variables to track wave number and list of wave mobs
    private int waveNumber;
    private final List<WaveMob> mobs;

    // Constructor to create a wave
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

    // Mutator method to add a mob to a wave
    public void addMob(String mobType, int count, Integer size, String ridingMob,
                       String mainHandItem, String offHandItem, List<String> armorItems,
                       String potionEffects, String enchantments) {

        // Check if a mob with the same type matching passed arguments already exist in the wave
        WaveMob existingMob = mobs.stream()
                .filter(mob -> mob.matches(
                        mobType,
                        size,
                        ridingMob,
                        mainHandItem,
                        offHandItem,
                        armorItems))
                .findFirst()
                .orElse(null);

        // Check if mob already exists if so add to existing if not create new mob in wave
        if (existingMob != null) {
            existingMob.setCount(existingMob.getCount() + count);
        } else {
            mobs.add(new WaveMob(mobType, count, size, ridingMob, mainHandItem, offHandItem,
                    armorItems, potionEffects, enchantments));
        }
    }

    // Mutator method to remove a mob from a wave
    public void removeMob(String mobType, int count, Integer size, String ridingMob,
                          String mainHandItem, String offHandItem,
                          List<String> armorItems) {

        for (WaveMob mob : mobs) {
            if (mob.matches(
                    mobType,
                    size,
                    ridingMob,
                    mainHandItem,
                    offHandItem,
                    armorItems)) {

                int available = mob.getCount();
                int toRemove = count == -1 ? available : Math.min(count, available);
                int newCount = available - toRemove;

                if (newCount <= 0) {
                    mobs.remove(mob);
                } else {
                    mob.setCount(newCount);
                }
                return;
            }
        }
    }

    // Accessor method to get total mob count in a wave
    public int getTotalMobCount() {
        return mobs.stream().mapToInt(WaveMob::getCount).sum();
    }
}