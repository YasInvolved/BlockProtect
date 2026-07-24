package pl.yasinvolved.blockprotect.client;

import java.util.Collections;
import java.util.List;

public class ClientClaimCache {
    private static List<ClientClaimData> activeClaims = Collections.emptyList();

    public static List<ClientClaimData> getActiveClaims() {
        return activeClaims;
    }

    public static void setActiveClaims(List<ClientClaimData> activeClaims) {
        ClientClaimCache.activeClaims = activeClaims;
    }
}
