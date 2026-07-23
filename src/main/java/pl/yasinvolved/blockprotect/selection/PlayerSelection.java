package pl.yasinvolved.blockprotect.selection;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

public record PlayerSelection(BlockPos pos1, BlockPos pos2) {
    public boolean isComplete() {
        return pos1 != null && pos2 != null;
    }

    public BoundingBox getBoundingBox() {
        if (!isComplete()) return null;
        return BoundingBox.fromCorners(pos1, pos2);
    }
}
