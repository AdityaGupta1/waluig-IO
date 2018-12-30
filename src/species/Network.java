package species;

import main.MemoryUtils;
import species.node.*;

import java.util.*;
import java.util.function.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
            mutate(true);
        } while(connections.isEmpty());
    }

    Network(Network copyFrom, boolean copyId) {
        nodes.addAll(copyFrom.nodes);
        connections.addAll(copyFrom.connections.stream().map(Connection::copy).collect(Collectors.toList()));
        this.id = copyId ? copyFrom.id : generateId();
    }

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

        this.nodes.addAll(parent1.nodes);

        Consumer<Connection> addToList = x -> {
            this.connections.add(x);

            if (!this.nodes.contains(x.input)) {
                this.nodes.add(x.input);
            }

            if (!this.nodes.contains(x.output)) {
                this.nodes.add(x.output);
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

        mutate();
    }

    void mutate() {
        mutate(false);
    }

    private void mutate(boolean onlyStructural) {
        // for example, chance = 1.4 means one guaranteed mutation and a 0.4 chance of a second mutation
        BiConsumer<Double, Runnable> mutator = (chance, mutation) -> {
            do {
                if (Math.random() < chance) {
                    mutation.run();
                }

                chance--;
            } while (chance > 0);
        };

        mutator.accept(addConnectionChance, this::mutateAddConnection);
        mutator.accept(addNodeChance, this::mutateAddNode);

        if (onlyStructural) {
            return;
        }

        mutator.accept(mutateWeightsChance, this::mutateWeights);
        mutator.accept(enableChance, () -> this.mutateEnable(true));
        mutator.accept(disableChance, () -> this.mutateEnable(false));
    }

    private void mutateAddConnection() {
        BiPredicate<Node, Node> findNew = (a, b) -> {
            if (a == b) {
                return true;
            }

            if (a.getLevel() >= b.getLevel()) {
                return true;
            }

            for (Connection connection : connections) {
                if (connection.input == a && connection.output == b) {
                    return true;
                }
            }

            return false;
        };

        Node input, output;
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

    private TreeMap<Integer, boolean[]> compareConnections(Network other) {
        Set<Integer> innovations = Stream.of(this.connections, other.connections).flatMap(List::stream).map(Connection::getInnovation).collect(Collectors.toSet());

        TreeMap<Integer, boolean[]> comparison = new TreeMap<>();
        for (int innovation : innovations) {
            boolean[] has = new boolean[2];
            Arrays.fill(has, false);

            for (Connection connection : this.connections) {
                if (connection.getInnovation() == innovation) {
                    has[0] = true;
                }
            }

            for (Connection connection : other.connections) {
                if (connection.getInnovation() == innovation) {
                    has[1] = true;
                }
            }

            comparison.put(innovation, has);
        }

        return comparison;
    }

    private int[] countNonMatching(Network other) {
        TreeMap<Integer, boolean[]> comparison = compareConnections(other);

        boolean[] last = comparison.get(new ArrayList<>(comparison.descendingKeySet()).get(0));

        int excess = 0;

        outer:
        if (!Arrays.equals(last, new boolean[]{true, true})) {
            for (int innovation : comparison.descendingKeySet()) {
                if (Arrays.equals(comparison.get(innovation), last)) {
                    excess++;
                } else {
                    break outer;
                }
            }
        }

        int disjoint = 0; // includes excess until return statement
        for (boolean[] has : comparison.values()) {
            if (!Arrays.equals(has, new boolean[]{true, true})) {
                disjoint++;
            }
        }

        return new int[]{disjoint - excess, excess};
    }

    private double getAverageWeightDistance(Network other) {
        double distance = 0;
        int count = 0;

        outer:
        for (Connection connection : this.connections) {
            for (Connection otherConnection : other.connections) {
                if (connection.getInnovation() == otherConnection.getInnovation()) {
                    distance += Math.abs(connection.getWeight() - otherConnection.getWeight());
                    count++;
                    continue outer;
                }
            }
        }

        if (count == 0) {
            return 0;
        } else {
            return distance / count;
        }
    }

    private double getCompatibility(Network other) {
        int[] nonMatching = countNonMatching(other);
        return deltaDisjoint * nonMatching[0]
                + deltaExcess * nonMatching[1]
                + deltaWeights * getAverageWeightDistance(other);
    }

    boolean isCompatible(Network other) {
        return getCompatibility(other) <= compatibilityThreshold;
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
        return Integer.compare(this.fitness, other.fitness);
    }

    @Override
    public String toString() {
        return id + ", " + String.format("%4s", fitness) + "; species " + species;
    }
}
