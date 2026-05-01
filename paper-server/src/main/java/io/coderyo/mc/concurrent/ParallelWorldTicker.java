package io.coderyo.mc.concurrent;

import io.papermc.paper.configuration.GlobalConfiguration;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BooleanSupplier;

public final class ParallelWorldTicker {
    private static final AtomicInteger WORKER_ID = new AtomicInteger();
    private static volatile ExecutorService executor;
    private static volatile int executorThreads;
    private static volatile boolean disabledAfterFailure;

    private ParallelWorldTicker() {
    }

    public static boolean tickLevels(final MinecraftServer server, final BooleanSupplier haveTime) {
        final GlobalConfiguration configuration = GlobalConfiguration.get();
        if (configuration == null || configuration.coderyo == null || configuration.coderyo.parallelWorldTicking == null || !configuration.coderyo.parallelWorldTicking.enabled) {
            return false;
        }
        if (disabledAfterFailure) {
            return false;
        }

        final List<ServerLevel> levels = snapshot(server.getAllLevels());
        final int threadCount = configuredThreads(configuration.coderyo.parallelWorldTicking.threads, levels.size());
        if (levels.size() <= 1 || threadCount <= 1) {
            return false;
        }

        final ExecutorService executor = executor(threadCount);
        final CompletableFuture<?>[] futures = new CompletableFuture<?>[levels.size()];
        for (int i = 0; i < levels.size(); ++i) {
            final ServerLevel level = levels.get(i);
            futures[i] = CompletableFuture.runAsync(() -> tickLevel(level, haveTime), executor);
        }

        try {
            CompletableFuture.allOf(futures).join();
        } catch (final CompletionException ex) {
            disabledAfterFailure = true;
            MinecraftServer.LOGGER.error("Disabling Coderyo experimental parallel world ticking after worker failure", ex.getCause() == null ? ex : ex.getCause());
            throw ex;
        }
        return true;
    }

    public static void shutdown() {
        final ExecutorService oldExecutor;
        synchronized (ParallelWorldTicker.class) {
            oldExecutor = executor;
            executor = null;
            executorThreads = 0;
            disabledAfterFailure = false;
        }
        if (oldExecutor != null) {
            oldExecutor.shutdownNow();
        }
    }

    private static List<ServerLevel> snapshot(final Iterable<ServerLevel> levels) {
        final List<ServerLevel> ret = new ArrayList<>();
        for (final ServerLevel level : levels) {
            ret.add(level);
        }
        return ret;
    }

    private static int configuredThreads(final int configuredThreads, final int levelCount) {
        if (configuredThreads > 0) {
            return Math.min(configuredThreads, levelCount);
        }

        final int availableProcessors = Math.max(1, Runtime.getRuntime().availableProcessors());
        return Math.min(levelCount, Math.max(1, availableProcessors - 1));
    }

    private static ExecutorService executor(final int threadCount) {
        ExecutorService ret = executor;
        if (ret != null && executorThreads == threadCount) {
            return ret;
        }

        synchronized (ParallelWorldTicker.class) {
            ret = executor;
            if (ret != null && executorThreads == threadCount) {
                return ret;
            }

            final ExecutorService oldExecutor = ret;
            ret = new ThreadPoolExecutor(
                threadCount,
                threadCount,
                30L,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(),
                runnable -> {
                    final ca.spottedleaf.moonrise.common.util.TickThread thread = new ca.spottedleaf.moonrise.common.util.TickThread(runnable, "Coderyo Parallel World Tick Thread #" + WORKER_ID.incrementAndGet());
                    thread.setDaemon(true);
                    thread.setPriority(Thread.NORM_PRIORITY + 1);
                    return thread;
                }
            );
            executor = ret;
            executorThreads = threadCount;
            if (oldExecutor != null) {
                oldExecutor.shutdown();
            }
            return ret;
        }
    }

    private static void tickLevel(final ServerLevel level, final BooleanSupplier haveTime) {
        level.hasPhysicsEvent = org.bukkit.event.block.BlockPhysicsEvent.getHandlerList().getRegisteredListeners().length > 0;
        level.hasEntityMoveEvent = io.papermc.paper.event.entity.EntityMoveEvent.getHandlerList().getRegisteredListeners().length > 0;
        level.updateLagCompensationTick();
        level.tick(haveTime);
        level.explosionDensityCache.clear();
    }
}
