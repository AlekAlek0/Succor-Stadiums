package net.alek.succorstadiums.arena;

import java.util.ArrayList;
import java.util.List;

public class MobArena {

    private String name;
    private double centerX;
    private double centerY;
    private double centerZ;
    private int radius;
    private int delayBetweenWaves;
    private String group; // null/blank = "Ungrouped"
    private List<RewardItem> rewards = new ArrayList<>();
    private List<RewardItem> participationRewards = new ArrayList<>(); // "Good Luck Next Time" reward for players who died but the arena finished
    private final List<Wave> waves = new ArrayList<>();

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
    public String getGroup() { return group; }
    public List<Wave> getWaves() { return waves; }
    public int getWaveCount() { return waves.size(); }

    // Null-guarded: old save data predating this field deserializes with rewards == null via Gson.
    public List<RewardItem> getRewards() {
        if (rewards == null) rewards = new ArrayList<>();
        return rewards;
    }
    public void setRewards(List<RewardItem> newRewards) {
        if (rewards == null) rewards = new ArrayList<>();
        rewards.clear();
        if (newRewards != null) rewards.addAll(newRewards);
    }

    public List<RewardItem> getParticipationRewards() {
        if (participationRewards == null) participationRewards = new ArrayList<>();
        return participationRewards;
    }
    public void setParticipationRewards(List<RewardItem> newRewards) {
        if (participationRewards == null) participationRewards = new ArrayList<>();
        participationRewards.clear();
        if (newRewards != null) participationRewards.addAll(newRewards);
    }

    public Wave getWave(int waveNumber) {
        if (waveNumber < 1 || waveNumber > waves.size()) return null;
        return waves.get(waveNumber - 1);
    }

    public void setName(String name) { this.name = name; }
    public void setCenter(double x, double y, double z) { this.centerX = x; this.centerY = y; this.centerZ = z; }
    public void setRadius(int radius) { this.radius = radius; }
    public void setDelayBetweenWaves(int delay) { this.delayBetweenWaves = delay; }
    public void setGroup(String group) { this.group = (group == null || group.isBlank()) ? null : group; }

    public void addWave() {
        waves.add(new Wave(waves.size() + 1));
    }

    public void addWave(String name, Integer delaySeconds) {
        Wave wave = new Wave(waves.size() + 1);
        wave.setName(name);
        wave.setDelaySeconds(delaySeconds);
        waves.add(wave);
    }

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

    public void removeWave(int waveNumber) {
        if (waveNumber < 1 || waveNumber > waves.size()) return;
        waves.remove(waveNumber - 1);
        for (int i = 0; i < waves.size(); i++) waves.get(i).setWaveNumber(i + 1);
    }

    public void moveWave(int waveNumber, int direction) {
        if (waveNumber < 1 || waveNumber > waves.size()) return;
        int idx = waveNumber - 1;
        int newIdx = idx + direction;
        if (newIdx < 0 || newIdx >= waves.size()) return;
        Wave temp = waves.get(idx);
        waves.set(idx, waves.get(newIdx));
        waves.set(newIdx, temp);
        for (int i = 0; i < waves.size(); i++) waves.get(i).setWaveNumber(i + 1);
    }

    public void renameWave(int waveNumber, String name) {
        Wave wave = getWave(waveNumber);
        if (wave != null) wave.setName(name);
    }

    public void setWaveDelay(int waveNumber, Integer delaySeconds) {
        Wave wave = getWave(waveNumber);
        if (wave != null) wave.setDelaySeconds(delaySeconds);
    }

    @Override
    public String toString() {
        return "MobArena{name='" + name + "', center=(" + centerX + ", " + centerY + ", " + centerZ + "), radius=" + radius + ", delayBetweenWaves=" + delayBetweenWaves + ", group=" + group + ", waves=" + waves.size() + "}";
    }
}