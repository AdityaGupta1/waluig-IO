import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

public class Specie {
    private final BlockEntry[][] layer1 = new BlockEntry[16][14];

    public Specie() {
        for (int i = 0; i < layer1.length; i++) {
            for (int j = 0; j < layer1[0].length; j++) {
                layer1[i][j] = new BlockEntry();
            }
        }
    }

    public Specie(Specie parent1, Specie parent2) {
        // TODO
    }

    public boolean[] get(double[][] blocks) {
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
