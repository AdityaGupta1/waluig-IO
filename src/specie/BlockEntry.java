package specie;

import main.MemoryUtils;

import java.util.function.Supplier;
import java.util.stream.IntStream;

class BlockEntry {
    private final double[][] weights = new double[6][2];

    BlockEntry() {
        for (int i = 0; i < 6; i++) {
            weights[i][0] = Math.random() - 0.5;
            weights[i][1] = (Math.random() - 0.5) * 0.1;
        }
    }

    private final Supplier<Double> mutationSupplier = () -> Math.random() < Generation.mutationRate ? (Math.random() * 0.4) - 0.2 : 0;

    BlockEntry(BlockEntry parent) {
        for (int i = 0; i < 6; i++) {
            weights[i][0] = parent.weights[i][0] + mutationSupplier.get();
            weights[i][1] = parent.weights[i][1] + (mutationSupplier.get() / 10);
        }
    }

    BlockEntry(BlockEntry parent1, BlockEntry parent2) {
        Supplier<BlockEntry> parentSupplier = () -> Math.random() < 0.5 ? parent1 : parent2;

        for (int i = 0; i < 6; i++) {
            BlockEntry parent = parentSupplier.get();
            weights[i][0] = parent.weights[i][0] + mutationSupplier.get();
            weights[i][1] = parent.weights[i][1] + (mutationSupplier.get() / 10);
        }
    }

    private double sigmoid(double x) {
        return 1 / (1 + Math.exp(-x));
    }

    double[] get(MemoryUtils.Block block) {
        return IntStream.iterate(0, x -> x++).limit(weights.length)
                .mapToDouble(i -> sigmoid(weights[i][0] * block.getValue()+ weights[i][1])).toArray();
    }
}