package species.node;

import main.MemoryUtils;

public class BlockNode extends InputNode {
    public final int x;
    public final int y;

    public BlockNode(int x, int y) {
        super(() -> MemoryUtils.getBlocks()[x][y].getValue());
        this.x = x;
        this.y = y;
    }
}
