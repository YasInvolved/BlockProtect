package pl.yasinvolved.blockprotect.claim;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import pl.yasinvolved.blockprotect.client.ClientClaimData;
import pl.yasinvolved.blockprotect.networking.S2CSyncClaimsPacket;
import pl.yasinvolved.blockprotect.storage.entities.ClaimEntity;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ClaimManager {
    private static final Map<String, ClaimEntity> CLAIMS = new ConcurrentHashMap<>();

    public static List<ClaimEntity> getActiveClaims(String dimension) {
        List<ClaimEntity> active = new ArrayList<>();
        for (ClaimEntity claim : CLAIMS.values()) {
            if (claim.getDimension().equals(dimension)) {
                active.add(claim);
            }
        }

        return active;
    }

    public static void syncToClient(ServerPlayer player) {
        List<ClientClaimData> claimDataList = ClaimManager.getActiveClaims(player.level().dimension().location().toString())
                .stream().map(c -> new ClientClaimData(
                        c.getName(),
                        new BlockPos(c.getMinX(), c.getMinY(), c.getMinZ()),
                        new BlockPos(c.getMaxX(), c.getMaxY(), c.getMaxZ())
                )).toList();
        PacketDistributor.sendToPlayer(player, new S2CSyncClaimsPacket(claimDataList));
    }

    public static Optional<ClaimEntity> getClaimAt(String dimension, BlockPos pos) {
        for (ClaimEntity claim : CLAIMS.values()) {
            if (claim.contains(dimension, pos)) {
                return Optional.of(claim);
            }
        }
        return Optional.empty();
    }

    public static boolean intersects(String dimension, BlockPos pos1, BlockPos pos2) {
        for (ClaimEntity claim : CLAIMS.values()) {
            if (claim.contains(dimension, pos1) || claim.contains(dimension, pos2)) {
                return true;
            }
        }

        return false;
    }

    public static void addClaim(ClaimEntity claim) {
        CLAIMS.put(claim.getId(), claim);
    }

    public static void removeClaim(String claimId) {
        CLAIMS.remove(claimId);
    }

    public static List<ClaimEntity> getClaimsByOwner(String ownerId) {
        List<ClaimEntity> ownerClaims = new ArrayList<>();
        for (ClaimEntity claim : CLAIMS.values()) {
            if (claim.getOwnerId().equals(ownerId)) {
                ownerClaims.add(claim);
            }
        }

        return ownerClaims;
    }

    public static Optional<ClaimEntity> getClaimByName(UUID ownerId, String name) {
        for (ClaimEntity claim : CLAIMS.values()) {
            if (claim.getOwnerId().equals(ownerId.toString()) && claim.getName().equals(name)) {
                return Optional.of(claim);
            }
        }

        return Optional.empty();
    }

    public static void loadAll(List<ClaimEntity> loadedClaims) {
        CLAIMS.clear();
        for (ClaimEntity claim : loadedClaims) {
            CLAIMS.put(claim.getId(), claim);
        }

        System.out.println("[BlockProtect] Loaded " + CLAIMS.size() + " protected regions!");
    }
}
