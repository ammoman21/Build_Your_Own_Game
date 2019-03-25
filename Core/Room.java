package byog.Core;

public class Room {
    int[] uR = new int[2];
    int[] uL = new int[2];
    int[] dL = new int[2];
    int[] dR = new int[2];
    int w;
    int h;

    public Room(int x, int y, int w, int h) {
        dL[0] = x;
        dL[1] = y;
        uR[0] = x + w - 1;
        uR[1] = y + h - 1;
        uL[0] = x;
        uL[1] = y + h - 1;
        dR[0] = x + w - 1;
        dR[1] = y;
        this.w = w;
        this.h = h;
    }

    public boolean isIntersect(Room room) {
        if (this.uL[0] > (room.dR[0] + 2)
                || this.dR[0] < (room.uL[0] - 2)
                || this.uL[1] < (room.dR[1] - 2)
                || this.dR[1] > (room.uL[1] + 2)) {
            return false;
        }
        return true;

    }

}
