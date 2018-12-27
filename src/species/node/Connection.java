package species.node;

public class Connection {
    public final Node input;
    public final Node output;

    private double weight = 0;
    private boolean enabled = true;

    private static int globalInnovation = 0;
    private final int innovation;

    public Connection(Node input, Node output, int innovation) {
        this.input = input;
        this.output = output;
        this.innovation = innovation;
    }

    public Connection(Node input, Node output) {
        this(input, output, ++globalInnovation);
    }

    public void toggleEnabled() {
        enabled = !enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public int getInnovation() {
        return innovation;
    }

    public Connection copy() {
        Connection clone = new Connection(input.copy(), output.copy());
        clone.weight = this.weight;
        clone.enabled = this.enabled;
        return clone;
    }
}
