package com.etherealtowerdefense.game.entities;

public final class EnemyStatusEffect {
    public enum Type { FIRE, SLOW, FREEZE, STUN }

    private final Type type;
    private float remaining;
    private final float strength;
    private float tickTimer;
    private float accumulatedFireDamage;

    public EnemyStatusEffect(Type type, float duration, float strength) {
        if (type == null) throw new IllegalArgumentException("Status type cannot be null");
        this.type = type;
        this.remaining = Math.max(0f, duration);
        this.strength = Math.max(0f, strength);
        this.tickTimer = 0f;
        this.accumulatedFireDamage = 0f;
    }

    public Type getType() { return type; }
    public float getRemaining() { return remaining; }
    public float getStrength() { return strength; }
    public boolean isActive() { return remaining > 0f; }

    public float update(float delta) {
        float activeDelta = Math.min(delta, remaining);
        remaining = Math.max(0f, remaining - delta);
        if (type != Type.FIRE || activeDelta <= 0f) return 0f;
        tickTimer += activeDelta;
        accumulatedFireDamage += strength * activeDelta;
        float damage = 0f;
        while (tickTimer >= 0.5f) {
            tickTimer -= 0.5f;
            float tickDamage = strength * 0.5f;
            damage += tickDamage;
            accumulatedFireDamage = Math.max(0f, accumulatedFireDamage - tickDamage);
        }
        if (remaining <= 0f && accumulatedFireDamage > 0f) {
            damage += accumulatedFireDamage;
            accumulatedFireDamage = 0f;
            tickTimer = 0f;
        }
        return damage;
    }
}
