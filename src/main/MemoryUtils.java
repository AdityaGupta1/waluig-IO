package main;

import nintaco.api.API;
import static nintaco.api.GamepadButtons.*;

public class MemoryUtils {
    private static final API api = Main.api;

    public static boolean isDead() {
        return api.readCPU(0x00E) == 0x0B;
    }

    public static void setDead() {
        api.writeCPU(0x00E, 0x0B);
    }

    public static double[][] getBlocks() {
        // TODO
        return null;
    }

    public static int getX() {
        return api.readCPU(0x071C) + 256 * api.readCPU(0x071A);
    }

    private static final int[] buttons = {A, B, Up, Down, Left, Right};

    public static void setJoypad(boolean[] buttonValues) {
        for (int i = 0; i < 6; i++) {
            api.writeGamepad(0, buttons[i], buttonValues[i]);
        }
    }
}
