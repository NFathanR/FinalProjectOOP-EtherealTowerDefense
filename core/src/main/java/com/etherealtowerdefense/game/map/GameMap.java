package com.etherealtowerdefense.game.map;

import com.badlogic.gdx.math.Vector2;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Tile-based visual map with a continuous, collision-tested enemy route. */
public class GameMap {
    public static final int TILE_SIZE = 40;
    public static final int WIDTH = 32, HEIGHT = 18;
    public static final float WORLD_WIDTH = WIDTH * TILE_SIZE, WORLD_HEIGHT = HEIGHT * TILE_SIZE;
    public static final float PATH_HALF_WIDTH = 20f;
    private final int[][] tiles = new int[HEIGHT][WIDTH];
    private final List<Vector2> waypoints = new ArrayList<>();

    public GameMap(MapData mapData) {
        //sen ngambil waypoint map 1 atau 2
        waypoints.addAll(mapData.getWaypoints());

        for (int i=0; i < waypoints.size()-1; i++) {
            markSegment(waypoints.get(i), waypoints.get(i + 1));
        }
    }

    private void markSegment(Vector2 a, Vector2 b) {
        int steps = Math.max(1, (int)(a.dst(b) / 5f));
        for (int i = 0; i <= steps; i++) {
            float t = i / (float) steps;
            int x = (int)((a.x + (b.x-a.x)*t) / TILE_SIZE);
            int y = HEIGHT - 1 - (int)((a.y + (b.y-a.y)*t) / TILE_SIZE);
            if (x >= 0 && x < WIDTH && y >= 0 && y < HEIGHT) tiles[y][x] = 1;
        }
    }
    public int getTile(int x, int y) { return x < 0 || y < 0 || x >= WIDTH || y >= HEIGHT ? -1 : tiles[y][x]; }
    public boolean isBuildable(int x, int y) { return getTile(x,y) == 0; }
    public List<Vector2> getWaypoints() { return Collections.unmodifiableList(waypoints); }

    /** Circle-vs-capsule test: towers must remain outside the entire road plus its safety margin. */
    public boolean intersectsPath(float x, float y, float radius) {
        float required = PATH_HALF_WIDTH + radius;
        for (int i = 0; i < waypoints.size()-1; i++)
            if (distanceToSegment(x, y, waypoints.get(i), waypoints.get(i+1)) < required) return true;
        return false;
    }
    private float distanceToSegment(float px, float py, Vector2 a, Vector2 b) {
        float dx=b.x-a.x, dy=b.y-a.y;
        float len2=dx*dx+dy*dy;
        float t=len2==0?0:((px-a.x)*dx+(py-a.y)*dy)/len2;
        t=Math.max(0,Math.min(1,t));
        float qx=a.x+t*dx, qy=a.y+t*dy;
        return Vector2.dst(px,py,qx,qy);
    }
}
