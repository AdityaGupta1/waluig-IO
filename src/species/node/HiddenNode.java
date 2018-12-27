package species.node;

import java.util.function.Function;

public class HiddenNode extends NonInputNode<Double> {
    public HiddenNode(Function<Double, Double> function) {
        super(function);
    }

    public HiddenNode() {
        this(Node::sigmoid);
    }

    @Override
    public HiddenNode copy() {
        return new HiddenNode(function);
    }
}
