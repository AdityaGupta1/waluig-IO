package species.node;

public abstract class Node {
    double level;

    public abstract Node copy();

    // modified sigmoid from NEAT paper
    static double sigmoid(double x) {
        return 2 / (1 + Math.exp(-4.9 * x)) - 1;
    }

    public double getLevel() {
        return level;
    }
}
