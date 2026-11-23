package com.etherealtowerdefense.game.map;
import com.badlogic.gdx.math.Vector2;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;

public class Map2 implements MapData {
    //nyimapan titik2 jalu musuh pada map 2 SEN
    private final List<Vector2> waypoints = new ArrayList<>(); //nyimpen rute musush

    // Membentuk rute untuk map2 SEN koordinat itu wok
    public Map2() {
        addWaypoint(0, 4);
        addWaypoint(8, 4);
        addWaypoint(8, 12);
        addWaypoint(18, 12);
        addWaypoint(18, 6);
        addWaypoint(26, 6);
        addWaypoint(26, 14);
        addWaypoint(31, 14);

    }

    //nemtuin arah musuhnya SEN kasi titik koordinat mau kemana
    private void addWaypoint(int tileX, int tileY) {
        float x = tileX * GameMap.TILE_SIZE + GameMap.TILE_SIZE /2f;
        float y = GameMap.WORLD_HEIGHT - (tileY * GameMap.TILE_SIZE + GameMap.TILE_SIZE / 2f);
        waypoints.add(new Vector2(x, y));
    }

    // Memberikan daftar waypoint Map 2 kepada GameMap (SEN)
    @Override
    public List<Vector2> getWaypoints() {
        return Collections.unmodifiableList(waypoints);
    }
}
