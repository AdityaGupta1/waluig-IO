package species;

import main.MemoryUtils;
import species.node.*;

import java.util.*;
import java.util.function.*;
import java.util.stream.Collectors;

import static main.Constants.*;
import static main.MemoryUtils.*;

public class Network implements Comparable<Network> {
    private final List<Node> nodes = new ArrayList<>();
    private final List<Connection> connections = new ArrayList<>();
    private int fitness = 0;

    private static final List<Character> charset = new ArrayList<>();
    private String id;

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

    private void generateId() {
        StringBuilder id = new StringBuilder();
        for (int i = 0; i < 5; i++) {
            id.append(charset.get(random.nextInt(charset.size())));
        }
        this.id = id.toString();
    }

    Network() {
        generateId();

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

    Network(Network copyFrom) {
        for (Node node : copyFrom.nodes) {
            nodes.add(node.copy());
        }

        for (Connection connection : copyFrom.connections) {
            connections.add(connection.copy());
        }

        this.id = copyFrom.id;
    }

    Network(Network parent1, Network parent2) {
        generateId();
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
        BiConsumer<Double, Runnable> mutator = (chance, mutation) -> {
            if (Math.random() < chance) {
                mutation.run();
            }
        };

        mutator.accept(addConnectionChance, this::mutateAddConnection);
        mutator.accept(addNodeChance, this::mutateAddNode);
        mutator.accept(mutateWeightsChance, this::mutateWeights);
        mutator.accept(enableChance, () -> this.mutateEnable(true));
        mutator.accept(disableChance, () -> this.mutateEnable(false));
    }

    private void mutateAddConnection() {
        BiPredicate<Node, Node> hasConnection = (a, b) -> {
            for (Connection connection : connections) {
                if ((connection.input == a && connection.output == b) ||
                        (connection.output == a && connection.input == b)) {
                    return true;
                }
            }

            return false;
        };

        BiPredicate<Node, Node> findNew = hasConnection.or((a, b) -> a instanceof OutputNode || b instanceof InputNode);

        Node input;
        Node output;
        Supplier<Node> randomNode = () -> nodes.get(random.nextInt(nodes.size()));

        do {
            input = randomNode.get();
            output = randomNode.get();
        } while(findNew.test(input, output));

        connections.add(new Connection(input, output, Math.random() * 4 - 2));
    }

    private void mutateAddNode() {
        List<Connection> candidates = connections.stream().filter(Connection::isEnabled).collect(Collectors.toList());
        if (candidates.isEmpty()) {
            return;
        }

        Connection connection = candidates.get(random.nextInt(candidates.size()));
        Node input = connection.input;
        Node output = connection.output;

        connection.setEnabled(false);
        Node node = new HiddenNode();
        nodes.add(node);
        connections.add(new Connection(input, node, 1));
        connections.add(new Connection(node, output, connection.getWeight()));
    }

    private void mutateWeights() {
        for (Connection connection : connections) {
            if (Math.random() < mutateWeightChance) {
                connection.mutateWeight();
            }
        }
    }

    private void mutateEnable(boolean enable) {
        List<Connection> candidates = connections.stream().filter(x -> x.isEnabled() != enable).collect(Collectors.toList());
        if (candidates.isEmpty()) {
            return;
        }

        candidates.get(random.nextInt(candidates.size())).toggleEnabled();
    }

    // counts both disjoint and excess connections
    private int countDisjoint(Network other) {
        List<Connection> all = new ArrayList<>();
        all.addAll(this.connections);
        all.addAll(other.connections);

        Map<Integer, Boolean> disjoint = new HashMap<>();
        for (Connection connection : all) {
            disjoint.put(connection.getInnovation(), false);
        }

        Consumer<List<Connection>> updateDisjoint = list -> list.forEach(x -> {
            int i = x.getInnovation();
            disjoint.put(i, !disjoint.get(i));
        });
        updateDisjoint.accept(this.connections);
        updateDisjoint.accept(other.connections);

        return (int) disjoint.keySet().stream().filter(disjoint::get).count();
    }

    private double getAverageWeightDistance(Network other) {
        double distance = 0;
        int count = 0;

        for (Connection connection : this.connections) {
            for (Connection otherConnection : other.connections) {
                if (connection.getInnovation() == otherConnection.getInnovation()) {
                    distance += Math.abs(connection.getWeight() - otherConnection.getWeight());
                    count++;
                }
            }
        }

        if (count == 0) {
            return 0;
        } else {
            return distance / count;
        }
    }

    public boolean isCompatible(Network other) {
        return deltaDisjoint * countDisjoint(other) + deltaWeights * getAverageWeightDistance(other) < compatibilityThreshold;
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

        if (stationaryFrames > framesBeforeReset) {
            setDead();
        }

        previousFitnessNoTime = fitnessNoTime;

        return true;
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
