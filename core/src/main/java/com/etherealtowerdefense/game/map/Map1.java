package com.etherealtowerdefense.game.map;
import com.badlogic.gdx.math.Vector2;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;

public class Map1 implements MapData{
    private final List<Vector2> waypoints = new ArrayList<>();

    public Map1(){
        addWaypoint(0, 8);
        addWaypoint(10, 8);
        addWaypoint(10, 12);
        addWaypoint(22, 12);
        addWaypoint(22, 7);
        addWaypoint(16, 7);
        addWaypoint(16, 4);
        addWaypoint(26, 4);
        addWaypoint(26, 7);
        addWaypoint(31, 7);
    }

    // Mengubah posisi tile menjadi koordinat game (SEN)
    private void addWaypoint(int tileX, int tileY) {
        float x = tileX * GameMap.TILE_SIZE + GameMap.TILE_SIZE / 2f;
        float y = GameMap.WORLD_HEIGHT - (tileY * GameMap.TILE_SIZE + GameMap.TILE_SIZE / 2f);
        waypoints.add(new Vector2(x, y));
    }

    // Memberikan daftar waypoint kepada GameMap (SEN)
    @Override
    public List<Vector2> getWaypoints() {
        return Collections.unmodifiableList(waypoints); //biar gak terjadi penambahan atau penghapusan waypoint dari luar by SEN <3
    }
}
