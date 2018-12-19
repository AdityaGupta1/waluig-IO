package specie;

import main.MemoryUtils;

import java.util.concurrent.Callable;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

public class Specie implements Comparable<Specie> {
    private final BlockEntry[][] layer1;
    private int fitness = 0;

    public Specie() {
        layer1 = new BlockEntry[16][14];

        for (int i = 0; i < layer1.length; i++) {
            for (int j = 0; j < layer1[0].length; j++) {
                layer1[i][j] = new BlockEntry();
            }
        }
    }

    public Specie(Specie copyFrom) {
        layer1 = copyFrom.layer1;
    }

    public Specie(Specie parent1, Specie parent2) {
        // TODO
        layer1 = null;
    }

    private boolean[] run(double[][] blocks) {
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

        fitness = MemoryUtils.getX();

        return result;
    }

    public Callable<Integer> getTester() {
        return () -> {
            this.fitness = 0;

            int previousFitness = 0;
            int stationaryFrames = 0;

            while (!MemoryUtils.isDead()) {
                MemoryUtils.setJoypad(run(MemoryUtils.getBlocks()));

                if (fitness == previousFitness) {
                    stationaryFrames++;
                } else {
                    stationaryFrames = 0;
                }

                if (stationaryFrames > 90) {
                    MemoryUtils.setDead();
                }

                previousFitness = this.fitness;
            }

            return this.fitness;
        };
    }

    @Override
    public int compareTo(Specie other) {
        return this.fitness - other.fitness;
    }

    public class BlockEntry {
        private final double[][] weights;

        public BlockEntry(double[][] weights) {
            this.weights = weights;
        }

        public BlockEntry() {
            weights = new double[6][2];
            for (int i = 0; i < 6; i++) {
                weights[i][0] = Math.random() - 0.5;
                weights[i][1] = (Math.random() - 0.5) * 0.1;
            }
        }

        public double[] get(double input) {
            return IntStream.iterate(0, x -> x++).limit(weights.length)
                    .mapToDouble(i -> weights[i][0] * input + weights[i][1]).toArray();
        }
    }
}
