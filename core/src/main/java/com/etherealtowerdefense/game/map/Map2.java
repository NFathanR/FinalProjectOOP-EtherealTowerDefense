package com.etherealtowerdefense.game.map;
import com.badlogic.gdx.math.Vector2;
import java.util.ArrayList;
import java.util.List;

public class Map2 {
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

        //hubungin tuh semua waypoint diatas SEN
        for (int i = 0; i < waypoints.size() - 1; i++) {
            markSegment(waypoints.get(i), waypoints.get(i + 1));
        }
    }

    //nyimapan titik2 jalu musuh pada map 2 SEN
    private final List<Vector2> waypoints = new ArrayList<>(); //nyimpen rute musush
    private final int[][] tiles = new int[GameMap.HEIGHT][GameMap.WIDTH]; //nyimpan jenis tile, 0 rumput, 1 path SEN

    //nemtuin arah musuhnya SEN kasi titik koordinat mau kemana
    private void addWaypoint(int tileX, int tileY) {
        float x = tileX * GameMap.TILE_SIZE + GameMap.TILE_SIZE /2f;
        float y = GameMap.WORLD_HEIGHT - (tileY * GameMap.TILE_SIZE + GameMap.TILE_SIZE / 2f);
        waypoints.add(new Vector2(x, y));
    }

    //menghubungan titik yang ditandain addwaypoint menjadi jalur
    private void markSegment(Vector2 a, Vector2 b) {
        int steps = Math.max(1, (int) (a.dst(b) / 5f));

        for(int i= 0; i <= steps; i++) {
            //menghitung posisi diantara 2 waypoint SEN
            float t = i / (float) steps;
            float worldX = a.x + (b.x - a.x) * t;
            float worldY = a.y + (b.y - a.y) * t;

            //mengubah koordinat world menjadi indeks tile SEN
            int x = (int) (worldX / GameMap.TILE_SIZE);
            int y = GameMap.HEIGHT - 1 - (int) (worldY / GameMap.TILE_SIZE);

            //menandai tile yg dilewati sebagai jalur SENN
            if (x >= 0 && x < GameMap.WIDTH && y >= 0 && y < GameMap.HEIGHT) {
                tiles[y][x] = 1;
            }
        }
    }
}
