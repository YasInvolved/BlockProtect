package pl.yasinvolved.blockprotect.selection;

import net.minecraft.core.BlockPos;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class SelectionManager {
    private static final Map<UUID, BlockPos> POS1_MAP = new ConcurrentHashMap<>();
    private static final Map<UUID, BlockPos> POS2_MAP = new ConcurrentHashMap<>();

    public static void setPos1(UUID playerId, BlockPos pos) {
        POS1_MAP.put(playerId, pos);
    }

    public static void setPos2(UUID playerId, BlockPos pos) {
        POS2_MAP.put(playerId, pos);
    }

    public static PlayerSelection getSelection(UUID playerId) {
        return new PlayerSelection(POS1_MAP.get(playerId), POS2_MAP.get(playerId));
    }

    public static void clearSelection(UUID playerId) {
        POS1_MAP.remove(playerId);
        POS2_MAP.remove(playerId);
    }
}
