package net.alek.succorstadiums.arena;

import java.util.ArrayList;
import java.util.List;

// Wave class
public class Wave {

    // Variables to track wave number, optional name, optional delay override, and list of wave mobs
    private int waveNumber;
    private String name;           // null/empty = unnamed, falls back to "Wave N" in UI
    private Integer delaySeconds;  // null = inherit the arena's default delay
    private final List<WaveMob> mobs;
    private List<RewardItem> rewards = new ArrayList<>();

    // Constructor to create a wave
    public Wave(int waveNumber) {
        this.waveNumber = waveNumber;
        this.mobs = new ArrayList<>();
    }

    // Accessor method to get the wave number
    public int getWaveNumber() { return waveNumber; }

    // Mutator method to set the wave number
    public void setWaveNumber(int waveNumber) { this.waveNumber = waveNumber; }

    // Accessor method to get the wave's custom name
    public String getName() { return name; }

    // Mutator method to set the wave's custom name
    public void setName(String name) { this.name = (name == null || name.isBlank()) ? null : name; }

    // Accessor method to get this wave's raw delay override (may be null)
    public Integer getDelaySeconds() { return delaySeconds; }

    // Mutator method to set this wave's delay override (null clears it, falling back to the arena default)
    public void setDelaySeconds(Integer delaySeconds) { this.delaySeconds = delaySeconds; }

    // Resolves this wave's effective delay: its own override if set, otherwise the arena's default
    public int getEffectiveDelay(int arenaDefaultDelay) {
        return delaySeconds != null ? delaySeconds : arenaDefaultDelay;
    }

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
                        armorItems,
                        potionEffects,
                        enchantments
                ))
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
                          String mainHandItem, String offHandItem, List<String> armorItems,
                          String potionEffects, String enchantments) {

        for (WaveMob mob : mobs) {
            if (mob.matches(
                    mobType,
                    size,
                    ridingMob,
                    mainHandItem,
                    offHandItem,
                    armorItems,
                    potionEffects,
                    enchantments
            )) {

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

    public List<RewardItem> getRewards() {
        if (rewards == null) rewards = new ArrayList<>();
        return rewards;
    }

    public void setRewards(List<RewardItem> newRewards) {
        if (rewards == null) rewards = new ArrayList<>();
        rewards.clear();
        if (newRewards != null) rewards.addAll(newRewards);
    }

}