package net.alek.succorstadiums.WaveSystem;

public class WaveManager {

    private static int currentWave = 1;
    private static int enemiesRemaining = 0;

    public static int getCurrentWave() {
        return currentWave;
    }

    public static void startNextWave() {
        currentWave++;

        enemiesRemaining = currentWave * 5;

        System.out.println("Starting wave " + currentWave);
    }

    public static void enemyKilled() {
        enemiesRemaining--;

        if (enemiesRemaining <= 0) {
            startNextWave();
        }
    }

    public static int getEnemiesRemaining() {
        return enemiesRemaining;
    }
}