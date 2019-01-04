package species.node;

import static main.Constants.*;

public class Connection {
    public final Node input;
    public final Node output;

    private double weight   ;
    private boolean enabled = true;

    private static int globalInnovation = 1;
    private final int innovation;

    public Connection(Node input, Node output, double weight, int innovation) {
        this.input = input;
        this.output = output;
        this.weight = weight;
        this.innovation = innovation;
    }

    public Connection(Node input, Node output, double weight) {
        this(input, output, weight, globalInnovation++);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void toggleEnabled() {
        enabled = !enabled;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public void mutateWeight() {
        weight += (Math.random() * 2 - 1) * mutateWeightStep;
    }

    public void randomWeight() {
        weight = (Math.random() * 2 - 1) * randomWeightMax;
    }

    public int getInnovation() {
        return innovation;
    }

    public Connection copy() {
        // Connection clone = new Connection(input.copy(), output.copy(), weight, innovation);
        Connection clone = new Connection(input, output, weight, innovation);
        clone.enabled = this.enabled;
        return clone;
    }
}
