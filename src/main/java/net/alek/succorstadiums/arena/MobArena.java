package net.alek.succorstadiums.arena;

import java.util.ArrayList;
import java.util.List;

// Mob arena class
public class MobArena {

    // Initialize variables for name, center positions, radius, delay, group, and waves
    private String name;
    private double centerX;
    private double centerY;
    private double centerZ;
    private int radius;
    private int delayBetweenWaves; // in seconds; the DEFAULT delay used by any wave that doesn't override it
    private String group; // null/blank = "Ungrouped"
    private final List<Wave> waves = new ArrayList<>();
    private final List<RewardItem> rewards = new ArrayList<>();

    // Constructor method to create a MobArena with the given name, center position, radius, and wave delay
    public MobArena(String name, double centerX, double centerY, double centerZ, int radius, int delayBetweenWaves) {
        this.name = name;
        this.centerX = centerX;
        this.centerY = centerY;
        this.centerZ = centerZ;
        this.radius = radius;
        this.delayBetweenWaves = delayBetweenWaves;
    }

    // Accessor methods to get name, center coordinates, radius, delay, group, waves of a mob arena, wave count of an existing mob arena, and rewards
    public String getName() { return name; }
    public double getCenterX() { return centerX; }
    public double getCenterY() { return centerY; }
    public double getCenterZ() { return centerZ; }
    public int getRadius() { return radius; }
    public int getDelayBetweenWaves() { return delayBetweenWaves; }
    public String getGroup() { return group; }
    public List<Wave> getWaves() { return waves; }
    public int getWaveCount() { return waves.size(); }
    public List<RewardItem> getRewards() { return rewards; }

    // Accessor method to get the specific wave by its number (1-indexed)
    public Wave getWave(int waveNumber) {
        if (waveNumber < 1 || waveNumber > waves.size()) return null;
        return waves.get(waveNumber - 1);
    }

    // Mutator method to set name of an existing mob arena
    public void setName(String name) {
        this.name = name;
    }

    // Mutator method to set rewards of an existing mob arena
    public void setRewards(List<RewardItem> newRewards) {
        rewards.clear();
        if (newRewards != null) rewards.addAll(newRewards);
    }

    // Mutator method to set the center of an existing mob arena
    public void setCenter(double x, double y, double z) {
        this.centerX = x;
        this.centerY = y;
        this.centerZ = z;
    }

    // Mutator method to set the radius of an existing mob arena
    public void setRadius(int radius) {
        this.radius = radius;
    }

    // Mutator method to set the delay between waves of an existing mob arena
    public void setDelayBetweenWaves(int delay) {
        this.delayBetweenWaves = delay;
    }

    // Mutator method to set the group of an existing mob arena (blank/null clears it, treated as "Ungrouped")
    public void setGroup(String group) {
        this.group = (group == null || group.isBlank()) ? null : group;
    }

    // Mutator method to add a new wave with the next wave number automatically
    public void addWave() {
        Wave wave = new Wave(waves.size() + 1);
        waves.add(wave);
    }

    // Create a wave with a custom name and/or delay override in one atomic step
    public void addWave(String name, Integer delaySeconds) {
        Wave wave = new Wave(waves.size() + 1);
        wave.setName(name);
        wave.setDelaySeconds(delaySeconds);
        waves.add(wave);
    }

    // Adds a wave built from copied data (name, delay override, and a pre-built mob list)
    public void addWaveFromPaste(String name, Integer delaySeconds, List<WaveMob> mobs) {
        Wave wave = new Wave(waves.size() + 1);
        wave.setName(name);
        wave.setDelaySeconds(delaySeconds);
        for (WaveMob mob : mobs) {
            wave.addMob(
                    mob.getMobType(), mob.getCount(), mob.getSize(),
                    mob.getRidingMob(), mob.getMainHandItem(), mob.getOffHandItem(),
                    mob.getArmorItems(), mob.getPotionEffects(), mob.getEnchantments()
            );
        }
        waves.add(wave);
    }

    // Mutator method to remove a wave by number and re-numbers the remaining waves
    public void removeWave(int waveNumber) {
        if (waveNumber < 1 || waveNumber > waves.size()) return;
        waves.remove(waveNumber - 1);
        for (int i = 0; i < waves.size(); i++) {
            waves.get(i).setWaveNumber(i + 1);
        }
    }

    // Mutator method to move a wave up (-1) or down (+1) and renumber accordingly
    public void moveWave(int waveNumber, int direction) {
        if (waveNumber < 1 || waveNumber > waves.size()) return;

        int idx = waveNumber - 1;
        int newIdx = idx + direction;
        if (newIdx < 0 || newIdx >= waves.size()) return;

        Wave temp = waves.get(idx);
        waves.set(idx, waves.get(newIdx));
        waves.set(newIdx, temp);

        for (int i = 0; i < waves.size(); i++) {
            waves.get(i).setWaveNumber(i + 1);
        }
    }

    // Mutator method to rename a wave by number
    public void renameWave(int waveNumber, String name) {
        Wave wave = getWave(waveNumber);
        if (wave != null) wave.setName(name);
    }

    // Mutator method to set a wave's delay override (null clears it back to using the arena default)
    public void setWaveDelay(int waveNumber, Integer delaySeconds) {
        Wave wave = getWave(waveNumber);
        if (wave != null) wave.setDelaySeconds(delaySeconds);
    }

    @Override
    public String toString() {
        return "MobArena{name='" + name + "', center=(" + centerX + ", " + centerY + ", " + centerZ + "), radius=" + radius + ", delayBetweenWaves=" + delayBetweenWaves + ", group=" + group + ", waves=" + waves.size() + "}";
    }
}