package pl.yasinvolved.blockprotect.async;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;
import pl.yasinvolved.blockprotect.storage.DatabaseManager;
import pl.yasinvolved.blockprotect.storage.dbentities.LogEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class LogQueueManager {
    private final ConcurrentLinkedQueue<LogEntity> queue = new ConcurrentLinkedQueue<>();
    private final ScheduledExecutorService executor;
    private final DatabaseManager databaseManager;
    private final Logger LOGGER = LogUtils.getLogger();

    public LogQueueManager(DatabaseManager databaseManager, int interval) {
        this.databaseManager = databaseManager;
        this.executor = Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread thread = new Thread(runnable, "BlockProtect-DB-Flush-Thread");
            thread.setDaemon(true);
            return thread;
        });

        this.executor.scheduleAtFixedRate(this::flush, interval, interval, TimeUnit.SECONDS);
    }

    public void offer(LogEntity entity) {
        queue.offer(entity);
    }

    public void flush() {
        if (queue.isEmpty()) return;

        List<LogEntity> batch = new ArrayList<>();
        LogEntity log;
        while ((log = queue.poll()) != null) {
            batch.add(log);
        }

        if (!batch.isEmpty()) {
            databaseManager.insertLogBatch(batch);
        }
    }

    public void shutdown() {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(3, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }

        flush();
    }
}
