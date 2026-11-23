package com.etherealtowerdefense.game.entities;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.etherealtowerdefense.game.strategies.AttackStrategy;
import java.util.List;
import java.util.ArrayList;
import java.util.function.Supplier;
import com.badlogic.gdx.graphics.Color;

public class Tower {
    public enum TargetPriority { FIRST, LAST, CLOSE, STRONG }
    private final Vector2 position = new Vector2();
    private final Texture texture;
    private final float originX, originY, drawSize, range;
    private float rotation;
    private float visualScale = 1f;
    private float turnSpeed = 360f; // in degrees per second yall
    private static final float AIM_TOLERANCE_DEGREES = 2f;
    private final AttackStrategy strategy;
    private float cooldown;
    private static final float RECOIL_KICK_DURATION = 4f / 60f;
    private static final float RECOIL_RECOVERY_DURATION = 15f / 60f;
    private static final float RECOIL_DISTANCE = 6f;
    private static final float RECOIL_SCALE = 0.08f;
    private boolean stunned;
    private boolean selected;
    private float selectionFlashTime;
    private float selectionBopTime = -1f;
    private float selectionScale = 1f;
    private TargetPriority targetPriority = TargetPriority.FIRST;
    private String displayName = "Tower";
    private String elementName = "Physical";
    private String uniqueText = "";
    public static class StatLine {
        public final String label;
        public final Supplier<String> value;
        public StatLine(String label, Supplier<String> value) { this.label = label; this.value = value; }
    }
    private final List<StatLine> sidebarStats = new ArrayList<>();

    private float recoilElapsed = -1f;

    public Tower(Texture texture, float originX, float originY, float drawSize,
                 float range, AttackStrategy strategy) {
        this.texture = texture;
        this.originX = originX;
        this.originY = originY;
        this.drawSize = drawSize;
        this.range = range;
        this.strategy = strategy;
        configureDefaultStats();
    }

    public void place(float x, float y) { position.set(x, y); }

    public void update(float delta, List<Enemy> enemies) {
        updateSelectionAnimation(delta);
        if (stunned) return;

        if (recoilElapsed >= 0f) {
            recoilElapsed += delta;

            if (recoilElapsed >= RECOIL_KICK_DURATION + RECOIL_RECOVERY_DURATION) {
                recoilElapsed = -1f;
            }
        }

        if (strategy.usesCustomUpdate()) {
            strategy.update(this, enemies, delta);
            return;
        }
        cooldown -= delta;

        Enemy target = findTarget(enemies);
        if (target == null) return;

        float desiredRotation = target.getPosition().cpy().sub(position).angleDeg() + 90f;
        float angleDifference = ((desiredRotation - rotation + 540f) % 360f) - 180f;
        float maxTurn = turnSpeed * delta;
        rotation = MathUtils.lerpAngleDeg(rotation, desiredRotation,
                Math.abs(angleDifference) <= maxTurn ? 1f : maxTurn / Math.abs(angleDifference));

        float remainingAngle = ((desiredRotation - rotation + 540f) % 360f) - 180f;
        boolean isAimedAtTarget = Math.abs(remainingAngle) <= AIM_TOLERANCE_DEGREES;

        if (cooldown <= 0f && isAimedAtTarget) {
            strategy.fire(this, target);
            cooldown = strategy.getFireInterval();
        }
    }

    private Enemy findTarget(List<Enemy> enemies) {
        Enemy best = null;
        float bestValue = 0f;
        for (Enemy enemy : enemies) {
            if (!enemy.isActive() || position.dst(enemy.getPosition()) > range
                    || !strategy.canTarget(this, enemy)) continue;
            float value;
            switch (targetPriority) {
                case LAST: value = -enemy.getDistanceTravelled(); break;
                case CLOSE: value = -position.dst2(enemy.getPosition()); break;
                case STRONG: value = enemy.getMaxHealth(); break;
                case FIRST:
                default: value = enemy.getDistanceTravelled(); break;
            }
            if (best == null || value > bestValue) {
                best = enemy;
                bestValue = value;
            }
        }
        return best;
    }

    private void updateSelectionAnimation(float delta) {
        if (!selected) {
            selectionScale = MathUtils.lerp(selectionScale, 1f, Math.min(1f, delta * 12f));
            selectionBopTime = -1f;
            return;
        }
        selectionFlashTime += delta;
        if (selectionBopTime >= 0f) {
            selectionBopTime += delta;
            float duration = 0.18f;
            float t = Math.min(1f, selectionBopTime / duration);
            float bump = (float)Math.sin(t * Math.PI) * 0.13f;
            selectionScale = 1f + bump;
            if (t >= 1f) selectionBopTime = -1f;
        } else {
            selectionScale = MathUtils.lerp(selectionScale, 1f, Math.min(1f, delta * 10f));
        }
    }

    public void setSelected(boolean selected) {
        if (selected && !this.selected) selectionBopTime = 0f;
        this.selected = selected;
        if (!selected) selectionScale = 1f;
    }

    public boolean isSelected() { return selected; }
    public float getSelectionFlashAlpha() {
        return selected ? 0.10f + 0.10f * (0.5f + 0.5f * MathUtils.sin(selectionFlashTime * 3.2f)) : 0f;
    }
    public TargetPriority getTargetPriority() { return targetPriority; }
    public void cycleTargetPriority(int direction) {
        TargetPriority[] values = TargetPriority.values();
        targetPriority = values[(targetPriority.ordinal() + direction + values.length) % values.length];
    }
    public void setTargetPriority(TargetPriority priority) { targetPriority = priority; }
    public Float getAttackDamage() { return strategy.getDamage(); }
    public Float getAttackSpeed() { return strategy.getFireInterval(); }
    public Float getDps() {
        Float damage = strategy.getDamage();
        float interval = strategy.getFireInterval();
        return damage == null || interval <= 0f ? null : damage / interval;
    }

    public void draw(SpriteBatch batch) {
        float recoil = getRecoilFactor();

        float animatedSize = drawSize * visualScale * selectionScale * (1f + RECOIL_SCALE * recoil);

        float ox = originX * animatedSize / 500f;
        float oy = originY * animatedSize / 500f;

        float recoilX = -MathUtils.sinDeg(rotation) * RECOIL_DISTANCE * recoil;
        float recoilY = MathUtils.cosDeg(rotation) * RECOIL_DISTANCE * recoil;

        Color previousColor = batch.getColor().cpy();

        if (stunned) {
            batch.setColor(0.45f, 0.65f, 1f, 1f);
        }

        batch.draw(
            texture,
            position.x + recoilX - ox,
            position.y + recoilY - oy,
            ox,
            oy,
            animatedSize,
            animatedSize,
            1f,
            1f,
            rotation,
            0,
            0,
            500,
            500,
            false,
            false
        );
        if (selected) {
            batch.setColor(1f, 1f, 1f, getSelectionFlashAlpha());
            batch.draw(texture,
                position.x + recoilX - ox, position.y + recoilY - oy,
                ox, oy, animatedSize, animatedSize, 1f, 1f, rotation,
                0, 0, 500, 500, false, false);
        }
        batch.setColor(previousColor);
    }

    private float getRecoilFactor() {
        if (recoilElapsed < 0f) {
            return 0f;
        }

        if (recoilElapsed <= RECOIL_KICK_DURATION) {
            return recoilElapsed / RECOIL_KICK_DURATION;
        }

        float recoveryProgress =
            (recoilElapsed - RECOIL_KICK_DURATION)
                / RECOIL_RECOVERY_DURATION;

        recoveryProgress = MathUtils.clamp(recoveryProgress, 0f, 1f);

        // Smoothly ease back to the original position.
        return 1f - recoveryProgress * recoveryProgress
            * (3f - 2f * recoveryProgress);
    }

    public void triggerRecoil() {
        recoilElapsed = 0f;
    }

    public void setStunned(boolean stunned) {
        this.stunned = stunned;
    }

    public boolean isStunned() {
        return stunned;
    }

    private void configureDefaultStats() {
        sidebarStats.clear();
        sidebarStats.add(new StatLine("ATK", () -> formatStat(getAttackDamage())));
        sidebarStats.add(new StatLine("DPS", () -> { Float d = getDps(); return d == null ? "NULL" : String.valueOf(Math.round(d)); }));
        sidebarStats.add(new StatLine("ATK Speed", () -> getAttackSpeed() == null ? "NULL" : formatStat(getAttackSpeed()) + "s"));
        sidebarStats.add(new StatLine("Range", () -> formatStat(getRange())));
    }
    private String formatStat(Float value) {
        if (value == null) return "NULL";
        return Math.abs(value - Math.round(value)) < .001f ? String.valueOf(Math.round(value)) : String.format(java.util.Locale.US, "%.1f", value);
    }
    public void setSidebarStats(StatLine... lines) {
        sidebarStats.clear();
        if (lines != null) java.util.Collections.addAll(sidebarStats, lines);
    }
    public List<StatLine> getSidebarStats() { return java.util.Collections.unmodifiableList(sidebarStats); }

    public void setDisplayInfo(String name, String element, String unique) {
        displayName = name; elementName = element; uniqueText = unique;
    }
    public String getDisplayName() { return displayName; }
    public String getElementName() { return elementName; }
    public String getUniqueText() { return uniqueText; }
    public Texture getTexture() { return texture; }
    public Vector2 getPosition() { return position; }
    public float getRange() { return range; }
    public AttackStrategy getStrategy() { return strategy; }
    public float getCollisionRadius() { return 28f; }
    public void setRotation(float rotation) { this.rotation = rotation; }
    public float getRotation() { return rotation; }
    public void setVisualScale(float visualScale) { this.visualScale = visualScale; }
    public float getVisualScale() { return visualScale; }
    public void setTurnSpeed(float turnSpeed) { this.turnSpeed = turnSpeed; }
}
