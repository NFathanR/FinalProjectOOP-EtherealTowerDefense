package com.etherealtowerdefense.game.map;

public class GameMap {
    //ukran mapnya dalam jumal tile 32 x 18
    private final int width = 32;
    private final int height = 18;
    private final int[][] tiles = new int[height][width];


    //contruktor jalan pas objek Gamemap dibuat wok
    public GameMap() {
        createPath();
    }

    //bikin method utk buat jalur musuhnya
    private void createPath() {

        //jalur hori dari kolom 0 - 10
        for(int x = 0; x <= 10; x++ ) {
            tiles[8][x] = 1;
        }

        for (int y = 8; y <= 12; y++) {
            tiles[y][10] = 1; //jadikan sebagai jalur
        }

        for( int x= 10; x <= 22; x++) {
            tiles[12][x] = 1;
        }

        for (int y = 12; y >= 7; y--) {
            tiles[y][22] = 1;
        }

        for (int x = 22; x >= 16; x-- ) {
            tiles[7][x] = 1;
        }

        for (int y = 7; y >= 4; y--) {
            tiles[y][16] = 1;
        }

        for (int x = 16; x <= 26; x++ ) {
            tiles[4][x] = 1;
        }

        for (int y = 4; y <= 7; y++) {
            tiles[y][26] = 1;
        }

        for (int x = 26; x < width; x++) {
            tiles[7][x] = 1;
        }
    }

    //cek bias buat tower kaj???
    public boolean isBuildable(int x, int y ) {
        if ( x < 0 || x >= width || y < 0 || y >= height) {
            return false;
        }
        //kalau 0 bisa meletakan tower
        return tiles[y][x] == 0;
    }


    //ngas info tile apa
    public int getTile(int x, int y) {

        //kalau ini maka invalid (di luar map)
        if (x < 0 || x >= width || y < 0 || y >= height) {
            return -1;
        }
        //memberi info jenis tile nya
        return tiles[y][x];
    }

}
