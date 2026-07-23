package pl.yasinvolved.blockprotect.async;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class InspectorManager {
    private static final Set<UUID> INSPECTOR_PLAYERS = ConcurrentHashMap.newKeySet();

    public static boolean toggleInspector(UUID playerUuid) {
        if (INSPECTOR_PLAYERS.contains(playerUuid)) {
            INSPECTOR_PLAYERS.remove(playerUuid);
            return false;
        } else {
            INSPECTOR_PLAYERS.add(playerUuid);
            return true;
        }
    }

    public static void removeInspector(UUID playerUuid) {
        INSPECTOR_PLAYERS.remove(playerUuid);
    }

    public static boolean isInspecting(UUID playerUuid) {
        return INSPECTOR_PLAYERS.contains(playerUuid);
    }
}
