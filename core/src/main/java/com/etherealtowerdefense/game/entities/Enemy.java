package com.etherealtowerdefense.game.entities;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;

public class Enemy {
    protected final Vector2 position = new Vector2();
    private final Vector2 lastMovementDirection = new Vector2();
    private final Vector2 knockbackVelocity = new Vector2();
    protected List<Vector2> path;
    protected int waypoint;
    protected float speed, health, maxHealth, distanceTravelled, rotation, turningSpeed;
    protected boolean active;
    protected Texture texture;

    private final List<EnemyStatusEffect> statusEffects = new ArrayList<>();
    private final List<Float> shieldLayers = new ArrayList<>();
    private float statusSlowMultiplier = 1f;
    private boolean statusFrozen;
    private boolean statusStunned;

    protected float hitFlashTimer;
    protected float shakeTimer;

    protected float shakeX;
    protected float shakeY;

    private static final float FLASH_DURATION = 0.12f;
    private static final float SHAKE_DURATION = 0.18f;
    private static final float SHAKE_INTENSITY = 3f;

    public Enemy(Texture texture, List<Vector2> path, float speed, float health) {
        init(texture, path, speed, health);
    }

    public void init(Texture texture, List<Vector2> path, float speed, float health) {
        this.texture = texture;
        this.path = path;
        this.speed = speed;
        this.health = this.maxHealth = health;
        this.waypoint = 1;
        this.distanceTravelled = 0f;
        this.active = true;
        this.rotation = 0f;
        this.turningSpeed = speed * 3f;
        position.set(path.get(0));
        lastMovementDirection.setZero();
        knockbackVelocity.setZero();
        if (path != null && path.size() > 1) {
            lastMovementDirection.set(path.get(1)).sub(path.get(0)).nor();
        }
        statusEffects.clear();
        shieldLayers.clear();
        statusSlowMultiplier = 1f;
        statusFrozen = false;
        statusStunned = false;
        this.hitFlashTimer = 0f;
        this.shakeTimer = 0f;
        this.shakeX = 0f;
        this.shakeY = 0f;

        this.waypoint = 0;
        this.rotation = 0f;

        if (this.path != null && !this.path.isEmpty()) {
            Vector2 direction = new Vector2(this.path.get(0))
                .sub(this.position).nor();

            this.rotation = (direction.angleDeg() + 90f) % 360f;
        }
    }

    public void initAt(Texture texture, List<Vector2> path, Vector2 start, float speed, float health) {
        init(texture, path, speed, health);
        position.set(start);
    }

    public void update(float delta) {
        if (!active || path == null) return;
        updateStatusEffects(delta);
        if (!active) return;
        float remaining = (statusFrozen || statusStunned ? 0f : speed * statusSlowMultiplier) * delta;
        this.hitFlashTimer = Math.max(0f, this.hitFlashTimer - delta);
        this.shakeTimer = Math.max(0f, this.shakeTimer - delta);

        if (this.shakeTimer > 0f) {
            float progress = this.shakeTimer / SHAKE_DURATION;
            float intensity = SHAKE_INTENSITY * progress;

            float elapsed = SHAKE_DURATION - this.shakeTimer;

            this.shakeX = MathUtils.sin(elapsed * 70f) * intensity;
            this.shakeY = MathUtils.cos(elapsed * 85f) * intensity;
        } else {
            this.shakeX = 0f;
            this.shakeY = 0f;
        }
        if (waypoint < path.size()) {
            Vector2 target = path.get(waypoint);
            Vector2 direction = new Vector2(target).sub(position).nor();
            if (!direction.isZero(0.0001f)) lastMovementDirection.set(direction);

            float targetRotation = (direction.angleDeg() + 90f) % 360f;

            float difference = (targetRotation - rotation + 540f) % 360f - 180f;

            float maxTurn = turningSpeed * delta;

            if (Math.abs(difference) <= maxTurn) {
                rotation = targetRotation;
            } else {
                rotation += Math.signum(difference) * maxTurn;
            }

            rotation = (rotation + 360f) % 360f;
        }
        while (remaining > 0 && waypoint < path.size()) {
            Vector2 target = path.get(waypoint);
            float distance = position.dst(target);
            if (distance <= remaining) {
                position.set(target);
                distanceTravelled += distance;
                remaining -= distance;
                waypoint++;
            } else {
                Vector2 direction = new Vector2(target).sub(position).nor();
                if (!direction.isZero(0.0001f)) lastMovementDirection.set(direction);

                position.mulAdd(direction, remaining);
                distanceTravelled += remaining;
                remaining = 0;
            }
        }
        // Apply external knockback after path movement so it is not cancelled
        // by the path-following step in the same frame.
        if (!knockbackVelocity.isZero(0.01f)) {
            position.mulAdd(knockbackVelocity, delta);
            knockbackVelocity.scl(Math.max(0f, 1f - 6f * delta));
        }

        if (waypoint >= path.size()) active = false;
    }

    public void draw(SpriteBatch batch) {
        if (!active) return;

        Color previousColor = batch.getColor().cpy();

        if (hitFlashTimer > 0f) {
            batch.setColor(1f, 0.2f, 0.2f, 1f);
        }

        batch.draw(
            texture,
            position.x + shakeX - 22,
            position.y + shakeY - 22,
            22,
            22,
            44,
            44,
            1f,
            1f,
            rotation,
            0,
            0,
            texture.getWidth(),
            texture.getHeight(),
            false,
            false
        );

        batch.setColor(previousColor);
    }

    public void drawEffect(ShapeRenderer shapes) {
        if (!active) return;
        float y = position.y + 29f;
        float offset = 0f;
        for (EnemyStatusEffect effect : statusEffects) {
            if (!effect.isActive()) continue;
            switch (effect.getType()) {
                case FIRE: shapes.setColor(1f, 0.35f, 0.05f, 0.95f); break;
                case SLOW: shapes.setColor(0.25f, 0.75f, 1f, 0.95f); break;
                case FREEZE: shapes.setColor(0.55f, 0.9f, 1f, 0.95f); break;
                case STUN: shapes.setColor(1f, 0.9f, 0.15f, 0.95f); break;
            }
            shapes.circle(position.x + offset, y, 4f, 12);
            offset += 11f;
        }
        if (!shieldLayers.isEmpty()) {
            shapes.setColor(0.35f, 0.85f, 1f, 0.95f);
            shapes.circle(position.x, position.y, 27f, 28);
        }
    }

    public void applyTowerEffects(List<Tower> towers) { }

    public void damage(float amount) {
        if (!active || amount <= 0f) return;
        float finalDamage = statusStunned ? amount * 1.25f : amount;
        if (!shieldLayers.isEmpty()) {
            int last = shieldLayers.size() - 1;
            float remainingShield = shieldLayers.get(last) - finalDamage;
            if (remainingShield > 0f) shieldLayers.set(last, remainingShield);
            else shieldLayers.remove(last);
        } else {
            health -= finalDamage;
            if (health <= 0f) {
                health = 0f;
                active = false;
            }
        }
        hitFlashTimer = FLASH_DURATION;
        shakeTimer = SHAKE_DURATION;
    }

    public void applyFire(float damagePerSecond, float duration) {
        applyStatus(EnemyStatusEffect.Type.FIRE, duration, damagePerSecond);
    }

    public void applySlow(float speedMultiplier, float duration) {
        applyStatus(EnemyStatusEffect.Type.SLOW, duration, Math.max(0f, Math.min(1f, speedMultiplier)));
    }

    public void applyFreeze(float duration) {
        applyStatus(EnemyStatusEffect.Type.FREEZE, duration, 0f);
    }

    public void applyStun(float duration) {
        applyStatus(EnemyStatusEffect.Type.STUN, duration, 0f);
    }

    public void addShield(float amount) {
        if (amount > 0f) shieldLayers.add(amount);
    }

    public float getShieldHealth() {
        float total = 0f;
        for (Float layer : shieldLayers) total += layer;
        return total;
    }

    public int getShieldLayers() { return shieldLayers.size(); }
    public boolean hasStatus(EnemyStatusEffect.Type type) {
        for (EnemyStatusEffect effect : statusEffects)
            if (effect.getType() == type && effect.isActive()) return true;
        return false;
    }

    public float getStatusRemaining(EnemyStatusEffect.Type type) {
        float remaining = 0f;
        for (EnemyStatusEffect effect : statusEffects)
            if (effect.getType() == type) remaining = Math.max(remaining, effect.getRemaining());
        return remaining;
    }

    private void applyStatus(EnemyStatusEffect.Type type, float duration, float strength) {
        if (duration <= 0f) return;
        // Refresh an existing effect rather than stacking duplicate copies.
        for (int i = 0; i < statusEffects.size(); i++) {
            EnemyStatusEffect current = statusEffects.get(i);
            if (current.getType() == type) {
                float mergedStrength = type == EnemyStatusEffect.Type.SLOW
                    ? Math.min(strength, current.getStrength())
                    : Math.max(strength, current.getStrength());
                statusEffects.set(i, new EnemyStatusEffect(type,
                    Math.max(duration, current.getRemaining()), mergedStrength));
                return;
            }
        }
        statusEffects.add(new EnemyStatusEffect(type, duration, strength));
    }

    private void updateStatusEffects(float delta) {
        statusSlowMultiplier = 1f;
        statusFrozen = false;
        statusStunned = false;
        float fireDamage = 0f;
        Iterator<EnemyStatusEffect> iterator = statusEffects.iterator();
        while (iterator.hasNext()) {
            EnemyStatusEffect effect = iterator.next();
            fireDamage += effect.update(delta);
            if (!effect.isActive()) { iterator.remove(); continue; }
            switch (effect.getType()) {
                case SLOW: statusSlowMultiplier = Math.min(statusSlowMultiplier, effect.getStrength()); break;
                case FREEZE: statusFrozen = true; break;
                case STUN: statusStunned = true; break;
                default: break;
            }
        }
        if (fireDamage > 0f) damage(fireDamage);
    }

    public boolean isActive() { return active; }
    public boolean isBoss() { return false; }
    public Vector2 getVelocity() { return new Vector2(lastMovementDirection).scl(speed); }
    public Vector2 getLastMovementDirection() { return new Vector2(lastMovementDirection); }
    public void applyKnockback(Vector2 direction, float strength) {
        if (direction == null || direction.isZero(0.0001f)) return;
        knockbackVelocity.mulAdd(direction.cpy().nor(), strength);
    }
    public Vector2 getPosition() { return position; }
    public float getHealth() { return health; }
    public float getMaxHealth() { return maxHealth; }
    public float getSpeed() { return speed; }
    public float getDistanceTravelled() { return distanceTravelled; }
    public void deactivate() { active = false; }
    public void dispose() { }
}
