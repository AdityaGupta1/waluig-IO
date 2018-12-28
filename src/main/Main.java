package main;

import nintaco.api.API;
import nintaco.api.ApiSource;
import nintaco.api.Colors;
import species.Generation;
import species.Network;
import species.node.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static main.Constants.speed;
import static main.Constants.waitTime;
import static main.MemoryUtils.Block;

public class Main {
    public static final API api = ApiSource.getAPI();

    private static Generation generation;
    private static Network currentNetwork;

    private static long resumeTime = 0;

    public static void main(String[] args) {
        generation = new Generation();
        currentNetwork = generation.nextNetwork();

        api.addFrameListener(Main::frameRendered);
        api.run();
    }

    private static void frameRendered() {
        api.setColor(Colors.WHITE);
        String[] display = generation.getDisplay();
        for (int i = 0; i < display.length; i++) {
            api.drawString(generation.getDisplay()[i], 10, 35 + (10 * i), false);
        }
        api.setSpeed(speed);

        if (System.currentTimeMillis() < resumeTime) {
            return;
        }

        if (currentNetwork == null) {
            return;
        }

        if (currentNetwork.runFrame()) {
            currentNetwork = generation.nextNetwork();
            resumeTime = System.currentTimeMillis() + waitTime;
        }

        Map<Node, Box> boxes = boxes();
        boxes.values().forEach(Box::draw);
        currentNetwork.connections.forEach(x -> boxes.get(x.input).drawLine(boxes.get(x.output), x.getWeight()));
    }

    private static final int side = 4;
    private static final int leftBound = 100;
    private static final int rightBound = 220;

    private static Map<Node, Box> boxes() {
        Map<Node, Box> output = new HashMap<>();

        List<BlockNode> blockNodes = currentNetwork.nodes.stream().filter(x -> x instanceof BlockNode).map(x -> (BlockNode) x).collect(Collectors.toList());
        List<HiddenNode> hiddenNodes = currentNetwork.nodes.stream().filter(x -> x instanceof HiddenNode).map(x -> (HiddenNode) x).collect(Collectors.toList());
        List<OutputNode> outputNodes = currentNetwork.nodes.stream().filter(x -> x instanceof OutputNode).map(x -> (OutputNode) x).collect(Collectors.toList());

        Block[][] blocks = MemoryUtils.getBlocks();
        final int x = 10;
        final int y = 60;

        for (int i = 0; i < 16; i++) {
            outer:
            for (int j = 0; j < 14; j++) {
                Block block = blocks[i][j];
                // InputNode node = currentNetwork.inputNodes[i][j];
                final Box box;

                if (block == Block.NONE) {
                    box = new Box(x + (i * side), y + (j * side));
                } else {
                    box = new Box(x + (i * side), y + (j * side), block == Block.ENEMY ? Colors.RED : Colors.WHITE);
                }

                for (BlockNode node : blockNodes) {
                    if (node.x == i && node.y == j) {
                        output.put(node, box);
                        continue outer;
                    }
                }
            }
        }

        outer:
        for (int i = 0; i < 6; i++) {
            int button = MemoryUtils.buttons[i];
            for (OutputNode node : outputNodes) {
                if (node.button == button) {
                    output.put(node, new Box(240, y + (i * 2 * side), MemoryUtils.getButton(button) ? Colors.WHITE : Colors.LIGHT_GRAY));
                    continue outer;
                }
            }
        }

        for (HiddenNode node : hiddenNodes) {
            output.put(node, new Box((leftBound + rightBound) / 2.0, 80, Colors.WHITE));
        }

        for (Connection connection : currentNetwork.connections) {
            if (!connection.isEnabled()) {
                continue;
            }

            Box a = output.get(connection.input);
            Box b = output.get(connection.output);

            Consumer<Box> bounds = (z) -> {
                if (z.x < leftBound) {
                    z.x = leftBound;
                }

                if (z.x > rightBound) {
                    z.x = rightBound;
                }
            };

            if (connection.input instanceof HiddenNode) {
                a.x = 0.75 * a.x + 0.25 * b.x;
                if (a.x >= b.x) {
                    a.x -= 40;
                }

                bounds.accept(a);

                a.y = 0.75 * a.y + 0.25 * b.y;
            }

            if (connection.output instanceof HiddenNode) {
                b.x = 0.75 * b.x + 0.25 * a.x;
                if (a.x >= b.x) {
                    b.x += 40;
                }

                bounds.accept(b);

                b.y = 0.75 * b.y + 0.25 * a.y;
            }
        }

        return output;
    }

    private static class Box {
        double x;
        double y;
        private final int color;

        Box(double x, double y, int color) {
            this.x = x;
            this.y = y;
            this.color = color;
        }

        Box(double x, double y) {
            this(x, y, -1);
        }

        private int round(double value) {
           return (int) Math.round(value);
        }

        void draw() {
            if (color == -1) {
                return;
            }

            api.setColor(color);
            api.fillRect(round(x), round(y), side, side);
        }

        void drawLine(Box other, double weight) {
            int color;

            if (weight > 1) {
                color = Colors.GREEN;
            } else if (weight < -1) {
                color = Colors.RED;
            } else if (weight > 0) {
                color = Colors.LIGHT_GREEN;
            } else if (weight < 0) {
                color = Colors.LIGHT_RED;
            } else { // weight == 0
                color = Colors.LIGHT_GRAY;
            }

            api.setColor(color);

            api.drawLine(round(x + side / 2.0), round(y + side / 2.0), round(other.x + side / 2.0), round(other.y + side / 2.0));
        }
    }
}
