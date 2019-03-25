package byog.Core;

import edu.princeton.cs.introcs.StdDraw;

import java.awt.Color;
import java.awt.Font;

import byog.TileEngine.TERenderer;
import byog.TileEngine.TETile;
import byog.TileEngine.Tileset;

import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.File;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;
import java.io.FileNotFoundException;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Game {
    TERenderer ter = new TERenderer();
    /* Feel free to change the width and height. */
    public static final int WIDTH = 80;
    public static final int HEIGHT = 30;
    TETile[][] world = new TETile[WIDTH][HEIGHT];
    int numberOfRooms = 20;
    List<Room> rooms = new ArrayList<>();
    TETile prevOne = Tileset.FLOOR;
    TETile nextOne;

    /**
     * Method used for playing a fresh game. The game should start from the main menu.
     */
    public void playWithKeyboard() {

        ter.initialize(WIDTH, HEIGHT);

        drawMainMenu();

        String input = takeInputNum(1);
        char mMInput = input.charAt(0);
        Position player = new Position();
        Position door = new Position();
        Position keyGoals = new Position();
        if (mMInput == 'n') {
            drawFrame("Please enter seed :", 2, 2);
            String seedString = takeInputTill('s');
            long seed = Long.parseLong(seedString);
            Random rand = new Random(seed);
            StdDraw.clear();
            StdDraw.clear(Color.black);
            startNewGame(seed, player, rand, door, keyGoals);
            startPlay(player, door);
        } else if (mMInput == 'q') {
            System.exit(0);
        } else if (mMInput == 'l') {
            SaveWorld worldObj = loadWorldInteractive();
            if (worldObj == null) {
                drawFrame("Please enter seed :", 2, 2);
                String seedString = takeInputTill('s');
                long seed = Long.parseLong(seedString);
                Random rand = new Random(seed);
                StdDraw.clear();
                StdDraw.clear(Color.black);
                startNewGame(seed, player, rand, door, keyGoals);
                startPlay(player, door);
            } else {
                player = worldObj.player;
                world = worldObj.world;
                prevOne = worldObj.prevOne;
                ter.renderFrame(world);
                startPlay(player, door);
            }
        } else {
            drawFrame("Please enter a valid choice on the main menu", 0, 0);
            StdDraw.pause(750);
            throw new IllegalArgumentException("Please enter a valid choice on the main menu");
        }


    }

    /**
     * Method used for autograding and testing the game code. The input string will be a series
     * of characters (for example, "n123sswwdasdassadwas", "n123sss:q", "lwww". The game should
     * behave exactly as if the user typed these characters into the game after playing
     * playWithKeyboard. If the string ends in ":q", the same world should be returned as if the
     * string did not end with q. For example "n123sss" and "n123sss:q" should return the same
     * world. However, the behavior is slightly different. After playing with "n123sss:q", the game
     * should save, and thus if we then called playWithInputString with the string "l", we'd expect
     * to get the exact same world back again, since this corresponds to loading the saved game.
     *
     * @param input the input string to feed to your program
     * @return the 2D TETile[][] representing the state of the world
     */
    public TETile[][] playWithInputString(String input) {
        Position door = new Position();
        Position keyGoals = new Position();
        input = input.toLowerCase();
        long seed = -1;
        String seedString = "";
        String moves = "";
        int endIndex = 0;
        Position player = new Position();
        int saveChooser = 0;
        if (input.charAt(0) == 'n') {
            for (int i = 0; i < input.length(); i++) {
                if (i == 0) {
                    if (input.charAt(i) != 'n') {
                        throw new RuntimeException("Please start with a new game.");
                    }
                } else if (input.charAt(i) == 's') {
                    seed = -2;
                    endIndex = i;
                    break;
                } else {
                    seedString += input.charAt(i);
                }
            }
            if (seed == -1) {
                throw new RuntimeException("Please start the game with s.");
            }
            try {
                seed = Long.parseLong(seedString);
            } catch (NumberFormatException nfe) {
                throw new RuntimeException("Please enter a number for the seed.");
            }
            if (endIndex < input.length() - 1) {
                for (int j = endIndex + 1; j < input.length(); j += 1) {
                    if (input.charAt(j) == ':') {
                        saveChooser = j;
                        break;
                    }
                    moves += input.charAt(j);
                }
            }
        } else if (input.charAt(0) == 'l') {
            for (int k = 1; k < input.length(); k += 1) {
                if (input.charAt(k) == ':') {
                    saveChooser = k;
                    break;
                }
                moves += input.charAt(k);
            }
        }
        Random rand = new Random(seed);
        if (input.charAt(0) == 'l') {
            SaveWorld worldObj = loadWorld(rand, player);
            player = worldObj.player;
            world = worldObj.world;
            prevOne = worldObj.prevOne;
        } else {
            for (int x = 0; x < WIDTH; x += 1) {
                for (int y = 0; y < HEIGHT; y += 1) {
                    world[x][y] = Tileset.NOTHING;
                }
            }
            genRandomRooms(15 + rand.nextInt(10), rand);
            addWalls();
            genHallways();
            genRandHallVert();
            addWalls();
            addDoor(rand, door);
            addIce(rand);
            addKey(rand, keyGoals);
            addPlayer(rand, player);
        }
        playGameWithInputString(moves, player, door);
        TETile[][] finalWorldFrame = world;
        saverfunc(saveChooser, input, player);
        return finalWorldFrame;
    }

    private void saverfunc(int saveChooser, String input, Position player) {
        if (saveChooser > 0) {
            if (input.charAt(saveChooser + 1) == 'q') {
                SaveWorld worldState = new SaveWorld(world, player, prevOne);
                saveCache(worldState);

            } else {
                throw new RuntimeException("Illegal argument after ':'.");
            }
        }
    }

    private void saveCache(SaveWorld worldState) {
        File f = new File("WorldCache.txt");
        try {
            if (!f.exists()) {
                f.createNewFile();
            }
            FileOutputStream fs = new FileOutputStream(f);
            ObjectOutputStream os = new ObjectOutputStream(fs);
            os.writeObject(worldState);
            os.close();
        } catch (FileNotFoundException e) {
            System.out.println("file not found");
            System.exit(0);
        } catch (IOException e) {
            System.out.println(e);
            System.exit(0);
        }
    }

    private SaveWorld loadWorld(Random rand, Position player) {
        File f = new File("WorldCache.txt");
        if (f.exists()) {
            try {
                FileInputStream fs = new FileInputStream(f);
                ObjectInputStream os = new ObjectInputStream(fs);
                SaveWorld loadWorld = (SaveWorld) os.readObject();
                os.close();
                return loadWorld;
            } catch (FileNotFoundException e) {
                System.out.println("file not found");
                System.exit(0);
            } catch (IOException e) {
                System.out.println(e);
                System.exit(0);
            } catch (ClassNotFoundException e) {
                System.out.println("class not found");
                System.exit(0);
            }
        }
        addPlayer(rand, player);
        return new SaveWorld(world, player, prevOne);
    }

    private void genRandomRooms(int nOR, Random rand) {
        boolean isIntersect;
        int i = 0;
        while (i < nOR) {
            isIntersect = false;
            int x = 2 + rand.nextInt(WIDTH - 4);
            int y = 2 + rand.nextInt(HEIGHT - 4);
            int w = Math.min(distR(x, y), 2 + rand.nextInt(5));
            int h = Math.min(distU(x, y), 2 + rand.nextInt(5));
            Room temp = new Room(x, y, w, h);
            for (int j = 0; j < rooms.size(); j++) {
                if (temp.isIntersect(rooms.get(j))) {
                    isIntersect = true;
                    break;
                }
            }
            if (!isIntersect) {
                rooms.add(temp);
                genRoom(temp.dL[0], temp.dL[1], temp.w, temp.h);
                i += 1;
            }
        }
    }

    private void addDoor(Random rand, Position door) {
        int x = -1;
        for (int i = 0; i < WIDTH; i += 1) {
            for (int j = 0; j < HEIGHT; j += 1) {
                if (world[i][j].equals(Tileset.WALL)) {
                    x = i;
                    break;
                }
            }
        }
        if (x < 0) {
            throw new RuntimeException("Could not find a place to put door. "
                    + "Layout Error Suggestion: Layout might be empty.");
        }
        while (true) {
            int randHeight = rand.nextInt(HEIGHT);
            if (world[x][randHeight].equals(Tileset.WALL)) {
                world[x][randHeight] = Tileset.LOCKED_DOOR;
                door.x = x;
                door.y = randHeight;
                break;
            }
        }

    }


    private void addIce(Random rand) {
        for (int i = 0; i < 3; i += 1) {
            Room targetRoom = rooms.get(rand.nextInt(rooms.size() - 1));
            for (int j = targetRoom.dL[0]; j < targetRoom.dR[0]; j += 1) {
                for (int k = targetRoom.dL[1]; k < targetRoom.uL[1]; k += 1) {
                    world[j][k] = Tileset.ICE;
                }
            }
        }

    }

    private void addKey(Random rand, Position keyGoal) {
        Room targetRoom = rooms.get(rand.nextInt(rooms.size()));
        int x = targetRoom.dL[0];
        int y = targetRoom.dL[1];
        while (true) {
            x = x + rand.nextInt(targetRoom.w - 1);
            y = y + rand.nextInt(targetRoom.h - 1);
            if (!world[x][y].equals(Tileset.NOTHING)) {
                keyGoal.x = x;
                keyGoal.y = y;
                world[x][y] = Tileset.KEY;
                break;
            }
        }
    }

    private void genHallways() {
        for (int i = 1; i < rooms.size(); i += 1) {
            Room room = rooms.get(i);
            Room prevRoom = rooms.get(i - 1);
            int xRoom = room.dL[0];
            int xPrevRoom = prevRoom.dL[0];
            int yRoom = room.dL[1];
            int yPrevRoom = prevRoom.dL[1];
            genCustomHallHor(xRoom, yRoom, xPrevRoom, yRoom);

        }
    }

    private void genRandHallVert() {
        for (int i = 0; i < rooms.size() - 1; i += 1) {
            Room room = rooms.get(i);
            Room prevRoom = rooms.get(i + 1);
            int xRoom = room.dL[0];
            int xPrevRoom = prevRoom.dL[0];
            int yRoom = room.dL[1];
            int yPrevRoom = prevRoom.dL[1];
            genCustomHallVert(xRoom, yRoom, xRoom, yPrevRoom);

        }

    }

    private void genCustomHallVert(int x1, int y1, int x2, int y2) {
        for (int i = 0; i < Math.abs(y1 - y2); i += 1) {
            if (y1 < y2) {
                manUp(Tileset.FLOOR, x1, y1 + i);
            } else {
                manDown(Tileset.FLOOR, x1, y1 - i);
            }
        }
    }

    private void genCustomHallHor(int x1, int y1, int x2, int y2) {
        for (int i = 0; i < Math.abs(x1 - x2); i += 1) {
            if (x1 < x2) {
                manRight(Tileset.FLOOR, x1 + i, y1);
            } else {
                manLeft(Tileset.FLOOR, x1 - i, y1);
            }
        }
    }

    private void genRoom(int x, int y, int rw, int rh) {
        for (int i = 0; i < rw; i += 1) {
            for (int j = 0; j < rh; j += 1) {
                world[x + i][y + j] = Tileset.FLOOR;
            }
        }
    }

    private void addPlayer(Random rand, Position player) {
        int x = -1;
        for (int i = WIDTH - 1; i >= 0; i -= 1) {
            for (int j = 0; j < HEIGHT; j += 1) {
                if (world[i][j] == Tileset.FLOOR) {
                    x = i;
                    break;
                }
            }
        }
        if (x < 0) {
            throw new RuntimeException("Could not find a place to put player. "
                    + "Layout Error Suggestion: Layout might be empty.");
        }
        while (true) {
            int randHeight = rand.nextInt(HEIGHT);
            if (world[x][randHeight] == Tileset.FLOOR) {
                world[x][randHeight] = Tileset.PLAYER;
                player.x = x;
                player.y = randHeight;
                break;
            }
        }
    }

    private void playGameWithInputString(String moves, Position player, Position door) {
        for (int i = 0; i < moves.length(); i += 1) {
            char move = moves.charAt(i);
            switch (move) {
                case 'w':
                    moveUp(player, door);
                    break;
                case 'a':
                    moveLeft(player, door);
                    break;
                case 's':
                    moveDown(player, door);
                    break;
                case 'd':
                    moveRight(player, door);
                    break;
                default:
                    throw new RuntimeException("Please enter valid moves.");
            }
        }
    }

    private int moveEmUp(Position player, Position door) {
        if (!peekUp(player.x, player.y).equals(Tileset.WALL)) {
            nextOne = peekUp(player.x, player.y);
            if (peekUp(player.x, player.y).equals(Tileset.KEY)) {
                world[door.x][door.y] = Tileset.UNLOCKED_DOOR;
                nextOne = Tileset.FLOOR;
            }
            if (peekUp(player.x, player.y).equals(Tileset.UNLOCKED_DOOR)) {
                System.out.print("Congratulations, you win the game!");
                return 1;
            }
            world[player.x][player.y] = prevOne;
            prevOne = nextOne;
            manUp(Tileset.PLAYER, player.x, player.y);
            player.y += 1;
        }
        return 0;

    }

    private int moveEmDown(Position player, Position door) {
        if (!peekDown(player.x, player.y).equals(Tileset.WALL)) {
            nextOne = peekDown(player.x, player.y);
            if (peekDown(player.x, player.y).equals(Tileset.KEY)) {
                world[door.x][door.y] = Tileset.UNLOCKED_DOOR;
                nextOne = Tileset.FLOOR;
            }
            if (peekDown(player.x, player.y).equals(Tileset.UNLOCKED_DOOR)) {
                System.out.print("Congratulations, you win the game!");
                return 1;
            }
            world[player.x][player.y] = prevOne;
            prevOne = nextOne;
            manDown(Tileset.PLAYER, player.x, player.y);
            player.y -= 1;
        }
        return 0;
    }

    private int moveEmRight(Position player, Position door) {
        nextOne = peekRight(player.x, player.y);
        if (!peekRight(player.x, player.y).equals(Tileset.WALL)) {
            if (peekRight(player.x, player.y).equals(Tileset.KEY)) {
                world[door.x][door.y] = Tileset.UNLOCKED_DOOR;
                nextOne = Tileset.FLOOR;
            }
            if (peekRight(player.x, player.y).equals(Tileset.UNLOCKED_DOOR)) {
                System.out.print("Congratulations, you win the game!");
                return 1;
            }
            world[player.x][player.y] = prevOne;
            prevOne = nextOne;
            manRight(Tileset.PLAYER, player.x, player.y);
            player.x += 1;
        }
        return 0;
    }

    private int moveEmLeft(Position player, Position door) {
        nextOne = peekLeft(player.x, player.y);
        if (!peekLeft(player.x, player.y).equals(Tileset.WALL)) {
            if (peekLeft(player.x, player.y).equals(Tileset.KEY)) {
                world[door.x][door.y] = Tileset.UNLOCKED_DOOR;
                nextOne = Tileset.FLOOR;
            }
            if (peekLeft(player.x, player.y).equals(Tileset.UNLOCKED_DOOR)) {
                System.out.print("Congratulations, you win the game!");
                return 1;
            }
            world[player.x][player.y] = prevOne;
            prevOne = nextOne;
            manLeft(Tileset.PLAYER, player.x, player.y);
            player.x -= 1;
        }
        return 0;
    }

    private int moveUp(Position player, Position door) {
        int endGame = 0;
        if (peekUp(player.x, player.y).equals(Tileset.ICE)) {
            for (int i = 0; i < 2; i += 1) {
                endGame = moveEmUp(player, door);
            }
        } else {
            endGame = moveEmUp(player, door);
        }
        return endGame;
    }

    private int moveDown(Position player, Position door) {
        int endGame = 0;
        if (peekDown(player.x, player.y).equals(Tileset.ICE)) {
            for (int i = 0; i < 2; i += 1) {
                endGame = moveEmDown(player, door);
            }
        } else {
            endGame = moveEmDown(player, door);
        }
        return endGame;
    }

    private int moveLeft(Position player, Position door) {
        int endGame = 0;
        if (peekLeft(player.x, player.y).equals(Tileset.ICE)) {
            for (int i = 0; i < 2; i += 1) {
                endGame = moveEmLeft(player, door);
            }
        } else {
            endGame = moveEmLeft(player, door);
        }
        return endGame;
    }

    private int moveRight(Position player, Position door) {
        int endGame = 0;
        if (peekRight(player.x, player.y).equals(Tileset.ICE)) {
            for (int i = 0; i < 2; i += 1) {
                endGame = moveEmRight(player, door);
            }
        } else {
            endGame = moveEmRight(player, door);
        }
        return endGame;
    }

    private int distR(int x, int y) {
        return WIDTH - x - 1;
    }

    private int distU(int x, int y) {
        return HEIGHT - y - 1;
    }

    private int distD(int x, int y) {
        return y;
    }

    private TETile peekUp(int x, int y) {
        if (y == HEIGHT) {
            return null;
        }
        return world[x][y + 1];
    }

    private TETile peekDown(int x, int y) {
        if (y == 0) {
            return null;
        }
        return world[x][y - 1];
    }

    private TETile peekLeft(int x, int y) {
        if (x == 0) {
            return null;
        }
        return world[x - 1][y];
    }

    private TETile peekRight(int x, int y) {
        if (x == WIDTH) {
            return null;
        }
        return world[x + 1][y];
    }

    private TETile peekLU(int x, int y) {
        if ((x == 0) || (y == HEIGHT)) {
            return null;
        }
        return world[x - 1][y + 1];
    }

    private TETile peekRU(int x, int y) {
        if ((x == WIDTH) || (y == HEIGHT)) {
            return null;
        }
        return world[x + 1][y + 1];
    }

    private TETile peekRD(int x, int y) {
        if ((x == WIDTH) || (y == 0)) {
            return null;
        }
        return world[x + 1][y - 1];
    }

    private TETile peekLD(int x, int y) {
        if ((x == 0) || (y == 0)) {
            return null;
        }
        return world[x - 1][y - 1];
    }

    private TETile[] analyzeEnvironment(int x, int y) {
        TETile[] env = new TETile[8];
        env[0] = peekLU(x, y);
        env[1] = peekUp(x, y);
        env[2] = peekRU(x, y);
        env[3] = peekRight(x, y);
        env[4] = peekRD(x, y);
        env[5] = peekDown(x, y);
        env[6] = peekLD(x, y);
        env[7] = peekLeft(x, y);
        return env;
    }

    private void manUp(TETile input, int x, int y) {
        world[x][y + 1] = input;
    }

    private void manDown(TETile input, int x, int y) {
        world[x][y - 1] = input;
    }

    private void manLeft(TETile input, int x, int y) {
        world[x - 1][y] = input;
    }

    private void manRight(TETile input, int x, int y) {
        world[x + 1][y] = input;
    }

    private void manLU(TETile input, int x, int y) {
        world[x - 1][y + 1] = input;
    }

    private void manRU(TETile input, int x, int y) {
        world[x + 1][y + 1] = input;
    }

    private void manRD(TETile input, int x, int y) {
        world[x + 1][y - 1] = input;
    }

    private void manLD(TETile input, int x, int y) {
        world[x - 1][y - 1] = input;
    }

    private void addWalls() {
        for (int i = 0; i < WIDTH; i += 1) {
            for (int j = 0; j < HEIGHT; j += 1) {
                if (world[i][j] != Tileset.WALL && world[i][j] != Tileset.NOTHING) {
                    TETile[] env = analyzeEnvironment(i, j);
                    for (int k = 0; k < 8; k += 1) {
                        if (env[k] == Tileset.NOTHING) {
                            if (k == 0) {
                                manLU(Tileset.WALL, i, j);
                            } else if (k == 1) {
                                manUp(Tileset.WALL, i, j);
                            } else if (k == 2) {
                                manRU(Tileset.WALL, i, j);
                            } else if (k == 3) {
                                manRight(Tileset.WALL, i, j);
                            } else if (k == 4) {
                                manRD(Tileset.WALL, i, j);
                            } else if (k == 5) {
                                manDown(Tileset.WALL, i, j);
                            } else if (k == 6) {
                                manLD(Tileset.WALL, i, j);
                            } else {
                                manLeft(Tileset.WALL, i, j);
                            }

                        }
                    }
                }
            }
        }

    }

    private void drawMainMenu() {
        StdDraw.clear();
        StdDraw.clear(Color.black);
        StdDraw.setPenColor(Color.white);

        int midWidth = WIDTH / 2;
        int midHeight = HEIGHT / 2;
        Font bigFont = new Font("Monaco", Font.BOLD, 30);
        Font smallFont = new Font("Monaco", Font.BOLD, 20);
        StdDraw.setFont(bigFont);
        StdDraw.text(midWidth, midHeight + 10, "CS61B: BYoG");
        StdDraw.setFont(smallFont);
        StdDraw.text(midWidth, midHeight + 5, "N: New Game");
        StdDraw.text(midWidth, midHeight, "L: Load Game");
        StdDraw.text(midWidth, midHeight - 5, "Q: Quit");
        StdDraw.show();
    }

    public void drawFrame(String s, int xOff, int yOff) {
        int midWidth = WIDTH / 2;
        int midHeight = HEIGHT / 2;

        StdDraw.clear();
        StdDraw.clear(Color.black);

        // Draw the actual text
        Font bigFont = new Font("Monaco", Font.BOLD, 20);
        StdDraw.setFont(bigFont);
        StdDraw.setPenColor(Color.white);
        StdDraw.text(midWidth + xOff, midHeight + yOff, s);
        StdDraw.show();
    }

    public String takeInputNum(int n) {
        String input = "";
//        drawFrame(input, 0, -12);

        while (input.length() < n) {
            if (!StdDraw.hasNextKeyTyped()) {
                continue;
            }
            char key = StdDraw.nextKeyTyped();
            input += String.valueOf(key);
            drawFrame(input, 0, -12);
        }
        StdDraw.pause(500);
        return input;
    }

    public String takeInputTill(char lim) {
        String input = "";
//        drawFrame(input, 0, -12);

        while (true) {
            if (!StdDraw.hasNextKeyTyped()) {
                continue;
            }
            char key = StdDraw.nextKeyTyped();
            if (key == lim) {
                break;
            }
            input += String.valueOf(key);
            drawFrame(input, 0, -12);
        }
        StdDraw.pause(500);
        return input;

    }

    private void startNewGame(Long seed, Position player, Random rand,
                              Position door, Position keyGoals) {
        for (int x = 0; x < WIDTH; x += 1) {
            for (int y = 0; y < HEIGHT; y += 1) {
                world[x][y] = Tileset.NOTHING;
            }
        }
        genRandomRooms(15 + rand.nextInt(10), rand);
        addWalls();
        genHallways();
        genRandHallVert();
        addWalls();
        addDoor(rand, door);
        addIce(rand);
        addKey(rand, keyGoals);
        addPlayer(rand, player);
        ter.renderFrame(world);
    }

    private void startPlay(Position player, Position door) {
        char input;
        char quitter;
        while (true) {
            input = takeInputNumXDisplay(1).charAt(0);
            if (input == ':') {
                quitter = takeInputNumXDisplay(1).charAt(0);
                if (quitter == 'q') {
                    SaveWorld worldState = new SaveWorld(world, player, prevOne);
                    saveCache(worldState);
                    System.exit(0);
                } else {
                    throw new RuntimeException("Illegal argument after ':'.");
                }
            } else {
                playGameWithInputString(String.valueOf(input), player, door);
                ter.renderFrame(world);
                locatemouse();
            }
        }
    }

    private String takeInputNumXDisplay(int n) {
        String input = "";
//        drawFrame(input, 0, -12);

        while (input.length() < n) {
            if (!StdDraw.hasNextKeyTyped()) {
                continue;
            }
            char key = StdDraw.nextKeyTyped();
            input += String.valueOf(key);
        }
        return input;
    }

    private SaveWorld loadWorldInteractive() {
        File f = new File("WorldCache.txt");
        if (f.exists()) {
            try {
                FileInputStream fs = new FileInputStream(f);
                ObjectInputStream os = new ObjectInputStream(fs);
                SaveWorld loadWorld = (SaveWorld) os.readObject();
                os.close();
                return loadWorld;
            } catch (FileNotFoundException e) {
                System.out.println("file not found");
                System.exit(0);
            } catch (IOException e) {
                System.out.println(e);
                System.exit(0);
            } catch (ClassNotFoundException e) {
                System.out.println("class not found");
                System.exit(0);
            }
        }
        drawFrame("A presaved world is non-existent. Let's make a new one.", 0, 0);
        StdDraw.pause(1000);
        return null;
    }

    private void locatemouse() {
        while (!StdDraw.hasNextKeyTyped()) {
            StdDraw.clear(Color.black);
            ter.renderFrame(world);
            int x = (int) StdDraw.mouseX();
            int y = (int) StdDraw.mouseY();
            Font bigFont = new Font("Monaco", Font.BOLD, 9);
            StdDraw.setFont(bigFont);
            StdDraw.setPenColor(Color.white);
            StdDraw.text(WIDTH / 2, 1, world[x][y].description());
            StdDraw.show();
            StdDraw.pause(50);
            StdDraw.clear(Color.black);
            StdDraw.setFont(new Font("Monaco", Font.BOLD, 20));
            ter.renderFrame(world);

        }
    }
}
