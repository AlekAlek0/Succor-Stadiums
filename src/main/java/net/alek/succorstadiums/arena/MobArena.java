package net.alek.succorstadiums.arena;

import java.util.ArrayList;
import java.util.List;

// Mob arena class
public class MobArena {

    // Initialize variables for name, center positions, radius, delay, and waves
    private String name;  // remove final
    private double centerX;
    private double centerY;
    private double centerZ;
    private int radius;
    private int delayBetweenWaves; // in seconds, applies between every wave
    private final List<Wave> waves = new ArrayList<>();

    // Constructor method to create a MobArena with the given name, center position, radius, and wave delay
    public MobArena(String name, double centerX, double centerY, double centerZ, int radius, int delayBetweenWaves) {
        this.name = name;
        this.centerX = centerX;
        this.centerY = centerY;
        this.centerZ = centerZ;
        this.radius = radius;
        this.delayBetweenWaves = delayBetweenWaves;
    }

    // Accessor methods to get name, center coordinates, radius, delay, waves of a mob arena, and wave count of an existing mob arena
    public String getName() { return name; }
    public double getCenterX() { return centerX; }
    public double getCenterY() { return centerY; }
    public double getCenterZ() { return centerZ; }
    public int getRadius() { return radius; }
    public int getDelayBetweenWaves() { return delayBetweenWaves; }
    public List<Wave> getWaves() { return waves; }
    public int getWaveCount() { return waves.size(); }

    // Accessor method to get the specific wave by its number (1-indexed)
    public Wave getWave(int waveNumber) {
        if (waveNumber < 1 || waveNumber > waves.size()) return null;
        return waves.get(waveNumber - 1);
    }

    // Mutator method to set name of an existing mob arena
    public void setName(String name) {
        this.name = name;
    }

    // Set the center of the Mob arena
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

    // Mutator method to add a new wave with the next wave number automatically
    public void addWave() {
        Wave wave = new Wave(waves.size() + 1);
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

    @Override
    public String toString() {
        return "MobArena{name='" + name + "', center=(" + centerX + ", " + centerY + ", " + centerZ + "), radius=" + radius + ", delayBetweenWaves=" + delayBetweenWaves + ", waves=" + waves.size() + "}";
    }
}