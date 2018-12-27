package species.node;

import java.util.function.Supplier;

public class InputNode extends Node {
    private final Supplier<Double> supplier;

    public InputNode(Supplier<Double> supplier) {
        this.supplier = supplier;
    }

    public double get() {
        return supplier.get();
    }

    @Override
    public InputNode copy() {
        return new InputNode(supplier);
    }
}
