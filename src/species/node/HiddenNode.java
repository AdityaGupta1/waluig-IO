package species.node;

import java.util.function.Function;

public class HiddenNode extends NonInputNode<Double> {
    public HiddenNode(Function<Double, Double> function, double level) {
        super(function);
        this.level = level;
    }

    public HiddenNode(double level) {
        this(Node::sigmoid, level);
    }

    @Override
    public HiddenNode copy() {
        return new HiddenNode(function, level);
    }
}
