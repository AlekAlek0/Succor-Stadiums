package net.alek.succorstadiums.arena;

import java.util.ArrayList;
import java.util.List;

public class MobArena {

    private final String name;
    private double centerX;
    private double centerY;
    private double centerZ;
    private int radius;
    private int delayBetweenWaves; // in seconds, applies between every wave
    private final List<Wave> waves = new ArrayList<>();

    // Create a MobArena with the given name, center position, radius, and wave delay
    public MobArena(String name, double centerX, double centerY, double centerZ, int radius, int delayBetweenWaves) {
        this.name = name;
        this.centerX = centerX;
        this.centerY = centerY;
        this.centerZ = centerZ;
        this.radius = radius;
        this.delayBetweenWaves = delayBetweenWaves;
    }

    public String getName() { return name; }
    public double getCenterX() { return centerX; }
    public double getCenterY() { return centerY; }
    public double getCenterZ() { return centerZ; }
    public int getRadius() { return radius; }
    public int getDelayBetweenWaves() { return delayBetweenWaves; }
    public List<Wave> getWaves() { return waves; }

    // Set the center of the MobArena
    public void setCenter(double x, double y, double z) {
        this.centerX = x;
        this.centerY = y;
        this.centerZ = z;
    }

    // Set the Radius of the MobArena
    public void setRadius(int radius) {
        this.radius = radius;
    }

    // Set the delay between Waves
    public void setDelayBetweenWaves(int delay) {
        this.delayBetweenWaves = delay;
    }

    // Adds a new wave with the next wave number automatically
    public Wave addWave() {
        Wave wave = new Wave(waves.size() + 1);
        waves.add(wave);
        return wave;
    }

    // Get a specific wave by its number (1-indexed)
    public Wave getWave(int waveNumber) {
        if (waveNumber < 1 || waveNumber > waves.size()) return null;
        return waves.get(waveNumber - 1);
    }

    public int getWaveCount() { return waves.size(); }

    // Removes a wave by number and re-numbers the remaining waves
    public boolean removeWave(int waveNumber) {
        if (waveNumber < 1 || waveNumber > waves.size()) return false;
        waves.remove(waveNumber - 1);
        for (int i = 0; i < waves.size(); i++) {
            waves.get(i).setWaveNumber(i + 1);
        }
        return true;
    }

    @Override
    public String toString() {
        return "MobArena{name='" + name + "', center=(" + centerX + ", " + centerY + ", " + centerZ + "), radius=" + radius + ", delayBetweenWaves=" + delayBetweenWaves + ", waves=" + waves.size() + "}";
    }
}