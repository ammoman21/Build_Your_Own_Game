package byog.Core;

import byog.TileEngine.TETile;

import java.io.Serializable;

public class SaveWorld implements Serializable {
    TETile[][] world;
    Position player;
    TETile prevOne;

    public SaveWorld(TETile[][] world, Position player, TETile prevOne) {
        this.world = world;
        this.player = player;
        this.prevOne = prevOne;
    }
}
