package com.etherealtowerdefense.game.entities.enemyType.elite;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.etherealtowerdefense.game.entities.Enemy;
import java.util.List;

/** Elite enemy with threshold-based slow, a timed shield, and a death-window flag. */
public class Girtablullu extends Enemy {
    public static final float BASE_HP = 1800f;
    public static final float BASE_SPEED = 70f;
    public static final float SHIELD_HP_RATIO = 0.30f;
    public static final float SHIELD_DURATION = 30f;
    public static final float EXPLOSION_RADIUS = 330f;
    public static final float EXPLOSION_DAMAGE = 200f;
    public static final int BASE_DAMAGE = 100;
    public static final int DENNY_REWARD = 2000;

    private float shieldHp;
    private float shieldTimer;
    private boolean shieldActivated;
    private boolean killedDuringShield;
    private boolean disappeared;
    private float speedStage;

    public Girtablullu(Texture texture, List<Vector2> path) {
        super(texture, path, BASE_SPEED, BASE_HP, BASE_DAMAGE, DENNY_REWARD);
    }
    @Override
    public void update(float delta) {
        if (shieldActivated && shieldTimer > 0f) {
            shieldTimer = Math.max(0f, shieldTimer - delta);
            if (shieldTimer == 0f) {
                // Timer ran out: Girtablullu vanishes (no reward, no base damage, no explosion).
                shieldHp = 0f;
                disappeared = true;
                active = false;
                return;
            }
        }
        super.update(delta);
    }

    @Override
    public void damage(float amount) {
        if (!active || amount <= 0f) return;
        hitFlashTimer = 0.12f;
        shakeTimer = 0.18f;

        if (!shieldActivated && health <= maxHealth * 0.20f) activateShield();

        float remainingDamage = amount;
        if (shieldActivated && shieldTimer > 0f && shieldHp > 0f) {
            float absorbed = Math.min(shieldHp, remainingDamage);
            shieldHp -= absorbed;
            remainingDamage -= absorbed;
        }
        if (remainingDamage > 0f) health -= remainingDamage;

        updateSpeedStage();
        if (health <= 0f) {
            killedDuringShield = shieldActivated && shieldTimer > 0f;
            active = false;
        }
        if (!shieldActivated && health <= maxHealth * 0.20f && health > 0f) activateShield();
    }

    private void activateShield() {
        if (shieldActivated) return;
        shieldActivated = true;
        shieldHp = maxHealth * SHIELD_HP_RATIO;
        shieldTimer = SHIELD_DURATION;
    }

    private void updateSpeedStage() {
        float lostRatio = 1f - Math.max(0f, health) / maxHealth;
        int stages = Math.min(4, (int)(lostRatio / 0.20f));
        speedStage = stages;
        // Each 20% HP lost reduces speed by 25% of the original speed.
        speed = health <= maxHealth * 0.20f
            ? 0f
            : BASE_SPEED * (float)Math.pow(0.75f, stages);
    }

    @Override public boolean isBoss() { return true; }
    public boolean isShieldActive() { return shieldActivated && shieldTimer > 0f && shieldHp > 0f; }
    public float getShieldHp() { return Math.max(0f, shieldHp); }
    public float getShieldTimer() { return Math.max(0f, shieldTimer); }
    public boolean hasDisappeared() { return disappeared; }
    public boolean wasKilledDuringShield() { return killedDuringShield; }
    public float getSpeedStage() { return speedStage; }

    @Override
    public void drawEffect(ShapeRenderer shapes) {
        if (!active || !isShieldActive()) return;
        shapes.setColor(new Color(0.25f, 0.85f, 1f, 0.9f));
        shapes.circle(position.x, position.y, 54f, 48);
    }
}
