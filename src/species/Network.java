package species;

import main.MemoryUtils;
import species.node.*;

import java.util.*;
import java.util.function.*;
import java.util.stream.Collectors;

import static main.Constants.*;
import static main.MemoryUtils.*;

public class Network implements Comparable<Network> {
    public final List<Node> nodes = new ArrayList<>();
    public final List<Connection> connections = new ArrayList<>();
    private int fitness = 0;

    private static final List<Character> charset = new ArrayList<>();
    private String id;
    private int species; // only for printing

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

    private String generateId() {
        StringBuilder id = new StringBuilder();
        for (int i = 0; i < 5; i++) {
            id.append(charset.get(random.nextInt(charset.size())));
        }
        return id.toString();
    }

    Network() {
        this.id = generateId();

        for (int i = 0; i < 16; i++) {
            for (int j = 0; j < 14; j++) {
                nodes.add(new BlockNode(i, j));
            }
        }

        for (int button : MemoryUtils.buttons) {
            nodes.add(new OutputNode(button));
        }

        do {
            mutate();
        } while(connections.isEmpty());
    }

    Network(Network copyFrom, boolean copyId) {
        nodes.addAll(copyFrom.nodes);
        connections.addAll(copyFrom.connections);
        this.id = copyId ? copyFrom.id : generateId();
    }

    // TODO make sure all nodes and connections copy over properly (all connections should refer to one set of nodes)
    Network(Network parent1, Network parent2) {
        this.id = generateId();

        parent1 = new Network(parent1, true);
        parent2 = new Network(parent2, true);

        // make sure parent1 always has the higher fitness
        if (parent1.fitness < parent2.fitness) {
            Network temp = parent1;
            parent1 = parent2;
            parent2 = temp;
        }

        Consumer<Connection> addToList = x -> {
            connections.add(x);

            if (!nodes.contains(x.input)) {
                nodes.add(x.input);
            }

            if (!nodes.contains(x.output)) {
                nodes.add(x.output);
            }
        };

        outer:
        for (Connection connection1 : parent1.connections) {
            for (Connection connection2 : parent2.connections) {
                if (connection1.getInnovation() == connection2.getInnovation()) {
                    Connection connection = connection1.copy();
                    if (random.nextBoolean()) {
                        connection.setWeight(connection2.getWeight());
                    }
                    addToList.accept(connection);

                    continue outer;
                }
            }

            addToList.accept(connection1);
        }
    }

    void mutate() {
        // for example, chance = 1.4 means one guaranteed mutation and a 0.4 chance of a second mutation
        BiConsumer<Double, Runnable> mutator = (chance, mutation) -> {
            while (chance > 0) {
                if (Math.random() < chance) {
                    mutation.run();
                }

                chance--;
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
                        (connection.input == b && connection.output == a)) {
                    return true;
                }
            }

            return false;
        };

        BiPredicate<Node, Node> findNew = hasConnection.or((a, b) -> a instanceof OutputNode || b instanceof InputNode)
                .or((a, b) -> a.getLevel() >= b.getLevel());

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
        Node node = new HiddenNode((input.getLevel() + output.getLevel()) / 2);
        nodes.add(node);
        connections.add(new Connection(input, node, 1));
        connections.add(new Connection(node, output, connection.getWeight()));
    }

    private void mutateWeights() {
        for (Connection connection : connections) {
            if (Math.random() < mutateWeightChance) {
                connection.mutateWeight();
            } else {
                connection.randomWeight();
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

    boolean isCompatible(Network other) {
        return deltaDisjoint * countDisjoint(other) + deltaWeights * getAverageWeightDistance(other) < compatibilityThreshold;
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

    private int previousFitnessNoTime = 0;
    private int stationaryFrames = 0;

    public boolean runFrame() {
        if (isDead()) {
            return true;
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

        return false;
    }

    int getFitness() {
        return fitness;
    }

    void resetFitness() {
        fitness = 0;
    }

    void setSpecies(int species) {
        this.species = species;
    }

    @Override
    public int compareTo(Network other) {
        return this.fitness - other.fitness;
    }

    @Override
    public String toString() {
        return id + ", " + fitness + "; species " + species;
    }
}
