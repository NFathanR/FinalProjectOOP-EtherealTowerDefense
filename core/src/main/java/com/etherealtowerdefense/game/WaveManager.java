package com.etherealtowerdefense.game;

import com.etherealtowerdefense.game.entities.Enemy;
import com.etherealtowerdefense.game.entities.enemyType.elite.FreddyFazbear;
import com.etherealtowerdefense.game.factories.EnemyFactory;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/** Controls the 50-wave campaign and endless waves after wave 50. */
public class WaveManager {
    public static final int CAMPAIGN_WAVES = 50;
    private static final float BASE_SPAWN_INTERVAL = 0.9f;
    private static final float MIN_SPAWN_INTERVAL = 0.25f;

    /** First wave Freddy appears; one more Freddy is added every FREDDY_COUNT_STEP waves after that. */
    private static final int FREDDY_FIRST_WAVE = 25;
    private static final int FREDDY_COUNT_STEP = 10;
    /**
     * Distance (in pixels along the path) between two Freddys spawned in the same wave.
     * Each Freddy stuns towers inside a SECONDARY_HITBOX_RADIUS circle, so two aura circles
     * are kept apart (2 * radius) plus a small margin.
     */
    private static final float FREDDY_SPACING = 2f * FreddyFazbear.SECONDARY_HITBOX_RADIUS + 40f;
    private static final float FREDDY_SPAWN_DELAY = FREDDY_SPACING / FreddyFazbear.BASE_SPEED;

    /** An elite scheduled relative to the start of the wave (independent of the common-enemy queue). */
    private static final class ScheduledSpawn {
        final EnemyFactory.Type type;
        final float spawnAt;
        ScheduledSpawn(EnemyFactory.Type type, float spawnAt) {
            this.type = type;
            this.spawnAt = spawnAt;
        }
    }

    private final Deque<EnemyFactory.Type> spawnQueue = new ArrayDeque<>();
    private final Deque<ScheduledSpawn> eliteSchedule = new ArrayDeque<>();
    private int wave = 0;
    private float spawnTimer;
    private float waveTime;
    private boolean started;
    private boolean waveActive;

    public void start() {
        if (started) return;
        started = true;
    }

    /** Starts the next wave only when the current wave is fully cleared. */
    public boolean startNextWave(List<Enemy> enemies) {
        if (!started || waveActive || !enemies.isEmpty()) return false;
        wave++;
        prepareWave();
        return true;
    }

    public void update(float delta, List<Enemy> enemies, EnemyFactory factory) {
        if (!started) return;
        if (waveActive) {
            waveTime += delta;

            // Elites first, so they always come out ahead of the common enemies of the same frame.
            while (!eliteSchedule.isEmpty() && eliteSchedule.peekFirst().spawnAt <= waveTime) {
                enemies.add(factory.create(eliteSchedule.removeFirst().type));
            }

            if (!spawnQueue.isEmpty()) {
                spawnTimer -= delta;
                if (spawnTimer <= 0f) {
                    EnemyFactory.Type type = spawnQueue.removeFirst();
                    enemies.add(factory.create(type));
                    spawnTimer = getSpawnInterval();
                }
            } else if (eliteSchedule.isEmpty() && enemies.isEmpty()) {
                waveActive = false;
            }
        }
    }

    private void prepareWave() {
        spawnQueue.clear();
        eliteSchedule.clear();
        int count = Math.min(100, 6 + wave * 2);
        for (int i = 0; i < count; i++) spawnQueue.addLast(commonTypeForWave(wave, i, count));

        // Elites spawn at the very start of the wave (t = 0). Freddys after the first are
        // delayed just enough to keep them spread apart along the path.
        int freddies = freddyCountForWave(wave);
        for (int i = 0; i < freddies; i++) {
            eliteSchedule.addLast(new ScheduledSpawn(EnemyFactory.Type.FREDDY_FAZBEAR, i * FREDDY_SPAWN_DELAY));
        }
        if (wave == 40 || wave == 50) {
            eliteSchedule.addFirst(new ScheduledSpawn(EnemyFactory.Type.GIRTABLULLU, 0f));
        }

        waveTime = 0f;
        spawnTimer = 0f;
        waveActive = true;
    }

    /** Wave 25-34 = 1 Freddy, 35-44 = 2, 45-54 = 3, ... Only on Freddy waves (every 5th wave). */
    private int freddyCountForWave(int currentWave) {
        if (!isFreddyWave(currentWave)) return 0;
        return 1 + (currentWave - FREDDY_FIRST_WAVE) / FREDDY_COUNT_STEP;
    }

    private EnemyFactory.Type commonTypeForWave(int currentWave, int index, int count) {
        // Later waves mix in stronger enemies while retaining a few early enemies.
        int tier = Math.min(3, (currentWave - 1) / 5);
        int pattern = (index + currentWave) % 10;
        if (tier >= 3 && pattern >= 6) return EnemyFactory.Type.HOPLITAI;
        if (tier >= 2 && pattern >= 5) return EnemyFactory.Type.FAUN;
        if (tier >= 1 && pattern >= 4) return EnemyFactory.Type.ALPECA;
        return EnemyFactory.Type.BLAST_CRAWLER;
    }

    private boolean isFreddyWave(int wave) {
        return wave == 25 || wave == 30 || wave == 35 || wave == 40 || wave == 45 || wave == 50;
    }

    private float getSpawnInterval() {
        return Math.max(MIN_SPAWN_INTERVAL, BASE_SPAWN_INTERVAL - wave * 0.012f);
    }

    public int getWave() { return wave; }
    public boolean isEndless() { return wave > CAMPAIGN_WAVES; }
    public boolean isWaveActive() { return waveActive; }
    public int getQueuedCount() { return spawnQueue.size() + eliteSchedule.size(); }
    public boolean canStartNextWave(List<Enemy> enemies) { return started && !waveActive && enemies.isEmpty(); }
    public String getDisplayText() {
        return isEndless() ? "Endless: " + wave : "Wave: " + wave + "/" + CAMPAIGN_WAVES;
    }
}
