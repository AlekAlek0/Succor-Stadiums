package net.alek.succorstadiums.arena;

import java.util.ArrayList;
import java.util.List;

public class Wave {

    private int waveNumber;
    private final List<WaveMob> mobs;

    public Wave(int waveNumber) {
        this.waveNumber = waveNumber;
        this.mobs = new ArrayList<>();
    }
    // Method to return the wave number
    public int getWaveNumber() { return waveNumber; }
    public void setWaveNumber(int waveNumber) { this.waveNumber = waveNumber; }
    public List<WaveMob> getMobs() { return mobs; }

    // Method to add a mob to the wave
    public void addMob(String mobType, int count) {

        WaveMob existingMob = mobs.stream()
                .filter(mob -> mob.getMobType().equalsIgnoreCase(mobType))
                .findFirst()
                .orElse(null);

        if (existingMob != null) {
            existingMob.setCount(existingMob.getCount() + count);
        } else {
            mobs.add(new WaveMob(mobType, count));
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
                // Return how many were actually removed
                return toRemove;
            }
        }
        // Mob type not found
        return 0;
    }

    // Method to get total mob count of a wave
    public int getTotalMobCount() {

        return mobs.stream().mapToInt(WaveMob::getCount).sum();
    }
}