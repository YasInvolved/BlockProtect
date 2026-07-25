package pl.yasinvolved.blockprotect.storage.dbentities;

import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import pl.yasinvolved.blockprotect.selection.PlayerSelection;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@DatabaseTable(tableName = "claims")
public class ClaimEntity {
    @DatabaseField(id = true, columnName = "id")
    private String id;

    @ForeignCollectionField(eager = false, foreignFieldName = "claim")
    private ForeignCollection<LogEntity> logs;

    @DatabaseField(columnName = "name", canBeNull = false)
    private String name;

    @DatabaseField(columnName = "ownerId", canBeNull = false)
    private String ownerId;

    @DatabaseField(dataType = DataType.BYTE_ARRAY)
    private byte[] coOwnersBlob;

    @DatabaseField(columnName = "dimension", canBeNull = false)
    private String dimension;

    private transient List<UUID> coOwners;

    @DatabaseField(columnName = "min_x") private int minX;
    @DatabaseField(columnName = "min_y") private int minY;
    @DatabaseField(columnName = "min_z") private int minZ;

    @DatabaseField(columnName = "max_x") private int maxX;
    @DatabaseField(columnName = "max_y") private int maxY;
    @DatabaseField(columnName = "max_z") private int maxZ;

    private BoundingBox box;

    public ClaimEntity() {}

    public ClaimEntity(String name, UUID ownerId, String dimension,
                       int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.ownerId = ownerId.toString();
        this.dimension = dimension;
        this.minX = minX;
        this.minY = minY;
        this.minZ = minZ;
        this.maxX = maxX;
        this.maxY = maxY;
        this.maxZ = maxZ;
    }

    public static ClaimEntity fromSelection(String name, UUID ownerId, String dimension, PlayerSelection selection) {
        return new ClaimEntity(
                name, ownerId, dimension,
                selection.pos1().getX(),
                selection.pos1().getY(),
                selection.pos1().getZ(),
                selection.pos2().getX(),
                selection.pos2().getY(),
                selection.pos2().getZ()
        );
    }

    public void syncBlobs() {
        if (coOwners != null) this.coOwnersBlob = packUUIDs(coOwners);
    }

    private static byte[] packUUIDs(List<UUID> list) {
        if (list == null || list.isEmpty()) return new byte[0];

        ByteBuffer buffer = ByteBuffer.allocate(list.size() * 16);
        for (int i = 0; i < list.size(); i++) {
            UUID uuid = list.get(i);
            buffer.putLong(uuid.getMostSignificantBits());
            buffer.putLong(uuid.getLeastSignificantBits());
        }
        return buffer.array();
    }

    private static List<UUID> unpackUUIDs(byte[] bytes) {
        if (bytes == null || bytes.length == 0) return new ArrayList<>(0);

        int count = bytes.length / 16;
        List<UUID> list = new ArrayList<>(count);
        ByteBuffer buffer = ByteBuffer.wrap(bytes);

        for (int i = 0; i < count; i++) {
            list.add(new UUID(buffer.getLong(), buffer.getLong()));
        }
        return list;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getOwnerId() { return ownerId; }
    public byte[] getCoOwnersBlob() { return coOwnersBlob; }
    public String getDimension() { return dimension; }
    public int getMinX() { return minX; }
    public int getMinY() { return minY; }
    public int getMinZ() { return minZ; }
    public int getMaxX() { return maxX; }
    public int getMaxY() { return maxY; }
    public int getMaxZ() { return maxZ; }

    public void addCoOwner(UUID coOwnerId) {
        coOwners.add(coOwnerId);
        syncBlobs();
    }

    public BoundingBox getBox() {
        if (this.box == null) {
            this.box = BoundingBox.fromCorners(
                    new Vec3i(minX, minY, minZ),
                    new Vec3i(maxX, maxY, maxZ)
            );
        }

        return this.box;
    }

    public boolean contains(String dimension, BlockPos pos) {
        return getBox().isInside(pos);
    }

    public boolean isOwnerOrTrusted(String uuid) {
        return false;
    }
}
