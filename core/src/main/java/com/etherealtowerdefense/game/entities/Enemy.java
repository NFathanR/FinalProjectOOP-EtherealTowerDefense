package com.etherealtowerdefense.game.entities;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import java.util.List;

public class Enemy {
    private final Vector2 position = new Vector2();
    private List<Vector2> path;
    private int waypoint;
    private float speed, health, maxHealth, distanceTravelled;
    private boolean active;
    private Texture texture;
    public void init(Texture texture, List<Vector2> path, float speed, float health) {
        this.texture=texture; this.path=path; this.speed=speed; this.health=this.maxHealth=health;
        this.waypoint=0; this.distanceTravelled=0f; this.active=true; position.set(path.get(0));
    }
    public void initAt(Texture texture, List<Vector2> path, Vector2 start, float speed, float health) {
        init(texture,path,speed,health); position.set(start);
    }
    public void update(float delta) {
        if (!active || path == null) return;
        float remaining=speed*delta;
        while (remaining>0 && waypoint<path.size()) {
            Vector2 target=path.get(waypoint);
            float distance=position.dst(target);
            if (distance <= remaining) { position.set(target); distanceTravelled += distance; remaining-=distance; waypoint++; }
            else { position.mulAdd(new Vector2(target).sub(position).nor(),remaining); distanceTravelled += remaining; remaining=0; }
        }
        if (waypoint>=path.size()) active=false;
    }
    public void draw(SpriteBatch batch) { if(active) batch.draw(texture,position.x-22,position.y-22,44,44); }
    public void damage(float amount) { health-=amount; if(health<=0) active=false; }
    public boolean isActive(){return active;} public Vector2 getPosition(){return position;}
    public float getHealth(){return health;} public float getMaxHealth(){return maxHealth;}
    public float getDistanceTravelled(){return distanceTravelled;}
    public void deactivate(){active=false;} public void dispose(){ }
}
