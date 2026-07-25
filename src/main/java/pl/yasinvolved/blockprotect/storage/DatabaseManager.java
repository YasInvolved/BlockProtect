package pl.yasinvolved.blockprotect.storage;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import com.mojang.logging.LogUtils;
import org.slf4j.Logger;
import pl.yasinvolved.blockprotect.storage.dbentities.ClaimEntity;
import pl.yasinvolved.blockprotect.storage.dbentities.LogEntity;

import java.io.File;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DatabaseManager implements AutoCloseable {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final ConnectionSource connectionSource;
    private final Dao<ClaimEntity, String> claimDao;
    private final Dao<LogEntity, String> logDao;

    public DatabaseManager(Path dbPath) {
        try {
            File dbFile = dbPath.toFile();
            if (dbFile.getParentFile() != null) {
                dbFile.getParentFile().mkdirs();
            }

            String cleanPath = dbFile.getAbsolutePath().replace("\\", "/");
            String jdbcUrl = "jdbc:sqlite:" + cleanPath;

            this.connectionSource = new JdbcConnectionSource(jdbcUrl);

            this.claimDao = DaoManager.createDao(connectionSource, ClaimEntity.class);
            this.logDao = DaoManager.createDao(connectionSource, LogEntity.class);

            TableUtils.createTableIfNotExists(connectionSource, ClaimEntity.class);
            TableUtils.createTableIfNotExists(connectionSource, LogEntity.class);

            autoMigrateTable(claimDao);
            autoMigrateTable(logDao);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize database!", e);
        }
    }

    private <T, ID> void autoMigrateTable(Dao<T, ID> dao) throws SQLException {
        LOGGER.info(String.format("Checking if %s needs an update...", dao.getTableName()));
        Set<String> existingColumns = new HashSet<>();
        String tableName = dao.getTableName();
        List<String[]> rows = dao.queryRaw("PRAGMA table_info('" + tableName + "');").getResults();
        for (String[] row : rows) {
            if (row.length > 1 && row[1] != null) {
                existingColumns.add(row[1].toLowerCase());
            }
        }

        FieldType[] fieldTypes = dao.getTableInfo().getFieldTypes();
        for (FieldType fieldType : fieldTypes) {
            String columnName = fieldType.getColumnName().toLowerCase();

            if (!existingColumns.contains(columnName)) {
                String sqlType = getSqlTypeForField(fieldType);
                String alterSql = String.format("ALTER TABLE '%s' ADD COLUMN '%s' %s;", tableName, fieldType.getColumnName(), sqlType);
                dao.executeRaw(alterSql);
                LOGGER.info(String.format("%s altered.", dao.getTableName()));
            }
        }
        LOGGER.info("Done.");
    }

    private String getSqlTypeForField(FieldType fieldType) {
        Class<?> type = fieldType.getType();

        if (type == byte[].class) {
            return "BLOB";
        }

        if (type == int.class || type == Integer.class ||
            type == long.class || type == Long.class ||
            type == boolean.class || type == Boolean.class ||
            type == short.class || type == Short.class ||
            type == byte.class || type == Byte.class) {
            return "INTEGER";
        }

        if (type == float.class || type == Float.class ||
            type == double.class || type == Double.class) {
            return "REAL";
        }

        return "TEXT";
    }

    public void saveClaim(ClaimEntity claim) {
        try {
            claimDao.createOrUpdate(claim);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteClaim(ClaimEntity claim) {
        try {
            claimDao.deleteById(claim.getId());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<ClaimEntity> loadAllClaims() {
        try {
            return claimDao.queryForAll();
        } catch (SQLException e) {
            e.printStackTrace();
            return List.of();
        }
    }

    public void insertLogBatch(List<LogEntity> logs) {
        try {
            logDao.callBatchTasks(() -> {
                for (LogEntity log : logs) {
                    logDao.create(log);
                }
                return null;
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<LogEntity> queryLogsAt(String dimension, int x, int y, int z, int limit) {
        try {
            return logDao.queryBuilder()
                    .orderBy("timestamp", false)
                    .where()
                    .eq("x", x).and()
                    .eq("y", y).and()
                    .eq("z", z)
                    .query();
        } catch (SQLException e) {
            e.printStackTrace();
            return List.of();
        }
    }

    @Override
    public void close() {
        if (connectionSource != null) {
            try {
                connectionSource.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
