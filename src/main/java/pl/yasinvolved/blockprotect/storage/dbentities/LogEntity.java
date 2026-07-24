package pl.yasinvolved.blockprotect.storage.dbentities;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.UUID;

@DatabaseTable(tableName = "block_logs")
public class LogEntity {
    @DatabaseField(id = true)
    private String id;

    @DatabaseField(foreign = true, foreignAutoRefresh = true, columnName = "claimId", canBeNull = true)
    private ClaimEntity claim;

    @DatabaseField(columnName = "playerId", canBeNull = false)
    private String playerId;

    @DatabaseField(columnName = "playerName", canBeNull = false)
    private String playerName;

    @DatabaseField(columnName = "action", canBeNull = false)
    private String action;

    @DatabaseField(columnName = "dimension", canBeNull = false)
    private String dimension;

    @DatabaseField(index = true) private int x;
    @DatabaseField(index = true) private int y;
    @DatabaseField(index = true) private int z;

    @DatabaseField(columnName = "blockState")
    private String blockState;

    @DatabaseField(columnName = "timestamp")
    private long timestamp;

    public LogEntity() {}

    public LogEntity(ClaimEntity claim, String playerId, String playerName, String action,
                     String dimension, int x, int y, int z, String blockState, long timestamp) {
        this.id = UUID.randomUUID().toString();
        this.playerId = playerId;
        this.playerName = playerName;
        this.action = action;
        this.dimension = dimension;
        this.x = x;
        this.y = y;
        this.z = z;
        this.blockState = blockState;
        this.timestamp = timestamp;
    }

    public String getId() {
        return id;
    }

    public String getPlayerId() {
        return playerId;
    }

    public String getPlayerName() {
        return playerName;
    }

    public String getAction() {
        return action;
    }

    public String getDimension() {
        return dimension;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }

    public String getBlockState() {
        return blockState;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public ClaimEntity getClaim() {
        return claim;
    }
}
