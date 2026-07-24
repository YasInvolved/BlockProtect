package pl.yasinvolved.blockprotect.storage.dbentities;

import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import pl.yasinvolved.blockprotect.selection.PlayerSelection;

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

    @DatabaseField(columnName = "dimension", canBeNull = false)
    private String dimension;

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

    public String getId() { return id; }
    public String getName() { return name; }
    public String getOwnerId() { return ownerId; }
    public String getDimension() { return dimension; }
    public int getMinX() { return minX; }
    public int getMinY() { return minY; }
    public int getMinZ() { return minZ; }
    public int getMaxX() { return maxX; }
    public int getMaxY() { return maxY; }
    public int getMaxZ() { return maxZ; }

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
