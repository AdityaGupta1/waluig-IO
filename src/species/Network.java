package species;

import main.Main;
import main.MemoryUtils;
import species.node.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.BiFunction;

import static main.MemoryUtils.*;

public class Network implements Comparable<Network> {
    private final List<Node> nodes = new ArrayList<>();
    private final List<Connection> connections = new ArrayList<>();
    private int fitness = 0;

    private static final List<Character> charset = new ArrayList<>();
    private final String id;

    private static final Random random = new Random();

    static {
        // both sides inclusive
        BiFunction<Character, Character, List<Character>> chars = (a, b) -> {
            List<Character> set = new ArrayList<>();
            for (int i = a; i <= b; i++) {
                set.add((char) i);
            }
            return set;
        };

        charset.addAll(chars.apply('0', '9'));
        charset.addAll(chars.apply('A', 'Z'));
        charset.addAll(chars.apply('a', 'z'));
    }

    {
        StringBuilder id = new StringBuilder();
        for (int i = 0; i < 5; i++) {
            id.append(charset.get(random.nextInt(charset.size())));
        }
        this.id = id.toString();
    }

    Network() {
        for (int i = 0; i < 16; i++) {
            for (int j = 0; j < 14; j++) {
                int x = i;
                int y = j;
                nodes.add(new InputNode(() -> MemoryUtils.getBlocks()[x][y].getValue()));
            }
        }

        for (int button : MemoryUtils.buttons) {
            nodes.add(new OutputNode(button));
        }

        mutate();
    }

    Network(Network parent1, Network parent2) {
        // TODO
    }

    private <T> T calculate(NonInputNode<T> node) {
        return node.apply(connections.stream().filter(Connection::isEnabled).filter(x -> x.output == node).mapToDouble(x -> {
            Node input = x.input;
            double inputValue;
            if (input instanceof InputNode) {
                inputValue = ((InputNode) input).get();
            } else {
                // input nodes will always be InputNode or HiddenNode, so this cast should always work
                inputValue = calculate((HiddenNode) input);
            }
            return inputValue * x.getWeight();
        }).sum());
    }

    private void mutate() {
        // TODO
    }

    private int previousFitnessNoTime = 0;
    private int stationaryFrames = 0;

    public boolean runFrame() {
        if (isDead()) {
            return false;
        }

        nodes.stream().filter(x -> x instanceof OutputNode).forEach(x -> {
            OutputNode output = (OutputNode) x;
            MemoryUtils.setButton(output.button, calculate(output));
        });
        int fitnessNoTime = getX();
        fitness = fitnessNoTime + 4 * getTime();

        if (fitnessNoTime == previousFitnessNoTime) {
            stationaryFrames++;
        } else {
            stationaryFrames = 0;
        }

        if (stationaryFrames > Main.framesBeforeReset) {
            setDead();
        }

        previousFitnessNoTime = fitnessNoTime;

        return true;
    }

    int getFitness() {
        return fitness;
    }

    void resetFitness() {
        fitness = 0;
    }

    @Override
    public int compareTo(Network other) {
        if (this.fitness != other.fitness) {
            return this.fitness - other.fitness;
        }

        return random.nextBoolean() ? 1 : -1;
    }

    @Override
    public String toString() {
        return id + ", " + fitness;
    }
}
