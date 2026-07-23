package pl.yasinvolved.blockprotect.storage;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import pl.yasinvolved.blockprotect.storage.entities.ClaimEntity;
import pl.yasinvolved.blockprotect.storage.entities.LogEntity;

import java.io.File;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.List;

public class DatabaseManager implements AutoCloseable {
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
        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize database!", e);
        }
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
                    .where()
                    .eq("dimension", dimension).and()
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
