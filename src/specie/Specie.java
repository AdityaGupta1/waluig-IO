package specie;

import main.MemoryUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.BiFunction;
import java.util.stream.DoubleStream;

public class Specie implements Comparable<Specie> {
    private final BlockEntry[][] layer1;
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
        String id = "";
        for (int i = 0; i < 5; i++) {
            id += charset.get(random.nextInt(charset.size()));
        }
        this.id = id;
    }

    Specie() {
        layer1 = new BlockEntry[16][14];

        for (int i = 0; i < layer1.length; i++) {
            for (int j = 0; j < layer1[0].length; j++) {
                layer1[i][j] = new BlockEntry();
            }
        }
    }

    Specie(Specie parent1, Specie parent2) {
        layer1 = new BlockEntry[16][14];

        for (int i = 0; i < layer1.length; i++) {
            for (int j = 0; j < layer1[0].length; j++) {
                layer1[i][j] = new BlockEntry(parent1.layer1[i][j], parent2.layer1[i][j]);
            }
        }
    }

    private boolean[] calculate(MemoryUtils.Block[][] blocks) {
        double[] layer2 = new double[6];

        for (int i = 0; i < layer1.length; i++) {
            for (int j = 0; j < layer1[0].length; j++) {
                double[] out = layer1[i][j].get(blocks[i][j]);
                for (int k = 0; k < 6; k ++) {
                    layer2[k] += out[k];
                }
            }
        }

        Object[] out = DoubleStream.of(layer2).mapToObj(x -> x / (layer1.length * layer1[0].length) > 0.5).toArray();
        boolean[] result = new boolean[6];
        for (int i = 0; i < 6; i++) {
           result[i] = (boolean) out[i];
        }

        return result;
    }

    private int previousFitness = 0;
    private int stationaryFrames = 0;

    public boolean runFrame() {
        this.fitness = 0;

        if (MemoryUtils.isDead()) {
            return false;
        }

        MemoryUtils.setJoypad(calculate(MemoryUtils.getBlocks()));
        fitness = MemoryUtils.getX();

        if (fitness == previousFitness) {
            stationaryFrames++;
        } else {
            stationaryFrames = 0;
        }

        if (stationaryFrames > 90) {
            MemoryUtils.setDead();
        }

        previousFitness = fitness;

        return true;
    }

    int getFitness() {
        return fitness;
    }

    @Override
    public int compareTo(Specie other) {
        return this.fitness - other.fitness;
    }

    @Override
    public String toString() {
        return id + ", " + fitness;
    }
}
