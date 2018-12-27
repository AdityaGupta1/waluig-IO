package main;

import nintaco.api.API;
import nintaco.api.GamepadButtons;

import static nintaco.api.GamepadButtons.*;

public class MemoryUtils {
    private static final API api = Main.api;

    public static boolean isDead() {
        return read(0x00E) == 0x0B;
    }

    public static void setDead() {
        write(0x00E, 0x0B);
    }

    public enum Block {
        BLOCK(10), ENEMY(-10), NONE(0);

        private int value;

        Block(int value) {
            this.value = value;
        }

        public double getValue() {
            return value;
        }
    }

    public static Block[][] getBlocks() {
        Block[][] blocks = new Block[16][14];

        for (int i = 0; i < 16; i++) {
            for (int j = 0; j < 14; j++) {
                int tempX = getX() + i * 16;
                int x = (tempX % 256) / 16;
                int y = j * 16;

                int address = 0x0500 + (((tempX / 256) % 2 == 1) ? 208 : 0) + x + y;
                Block block = Block.NONE;
                if (read(address) != 0 && y < 14 * 16 && y >= 0) {
                    block = Block.BLOCK;
                }

                blocks[i][j] = block;
            }
        }

        for (int i = 0; i < 5; i++) {
            if (read(0x00F + i) != 1) {
                continue;
            }

            int x = read(0x0087 + i) + 256 * read(0x006E + i) - getX();
            if (x < 0  || x > 256) {
                continue;
            }

            x = (x % 256) / 16;
            int y = (read(0x00CF + i) - 8) / 16;

            blocks[x][y] = Block.ENEMY;
        }

        return blocks;
    }

    public static int getX() {
        return read(0x071C) + 256 * read(0x071A);
    }

    public static final int[] buttons = {A, B, Up, Down, Left, Right};

    public static void setJoypad(boolean[] buttonValues) {
        for (int i = 0; i < 6; i++) {
            api.writeGamepad(0, buttons[i], buttonValues[i]);
        }
    }

    public static void setButton(int button, boolean buttonValue) {
        api.writeGamepad(0, button, buttonValue);
    }

    public static int getTime() {
        return 100 * read(0x07F8) + 10 * read(0x07F9) + 1 * read(0x07FA);
    }

    private static int read(int address) {
        return api.readCPU(address);
    }

    private static void write(int address, int value) {
        api.writeCPU(address, value);
    }
}
