package species.node;

import main.Constants;
import main.Main;

import java.util.function.Function;

public class OutputNode extends NonInputNode<Boolean> {
    public final int button;

    public OutputNode(Function<Double, Boolean> function, int button) {
        super(function);
        this.button = button;
        this.level = 100;
    }

    // default just checks if the sigmoid of the input value is greater than 0.5
    public OutputNode(int button) {
        this(x -> Node.sigmoid(x) > Constants.pressThreshold, button);
    }

    @Override
    public OutputNode copy() {
        return new OutputNode(function, button);
    }
}
