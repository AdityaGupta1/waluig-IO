package species.node;

import java.util.function.Function;

public abstract class NonInputNode<T> extends Node {
    final Function<Double, T> function;

    NonInputNode(Function<Double, T> function) {
        this.function = function;
    }

    public T apply(double x) {
        return function.apply(x);
    }
}
