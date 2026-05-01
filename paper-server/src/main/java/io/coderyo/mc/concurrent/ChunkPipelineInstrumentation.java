package io.coderyo.mc.concurrent;

import io.papermc.paper.configuration.GlobalConfiguration;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;

public final class ChunkPipelineInstrumentation {
    private ChunkPipelineInstrumentation() {
    }

    public static long startSyncLoadTimer() {
        final GlobalConfiguration configuration = GlobalConfiguration.get();
        if (configuration == null || configuration.coderyo == null || configuration.coderyo.parallelChunkPipeline == null || configuration.coderyo.parallelChunkPipeline.syncLoadWarnMillis < 0) {
            return 0L;
        }
        return System.nanoTime();
    }

    public static void finishSyncLoadTimer(final long startNanos, final ServerLevel level, final int chunkX, final int chunkZ, final String reason) {
        if (startNanos == 0L) {
            return;
        }

        final GlobalConfiguration configuration = GlobalConfiguration.get();
        if (configuration == null || configuration.coderyo == null || configuration.coderyo.parallelChunkPipeline == null) {
            return;
        }

        final int warnMillis = configuration.coderyo.parallelChunkPipeline.syncLoadWarnMillis;
        if (warnMillis < 0) {
            return;
        }

        final long elapsedMillis = (System.nanoTime() - startNanos) / 1_000_000L;
        if (elapsedMillis >= warnMillis) {
            MinecraftServer.LOGGER.warn(
                "Synchronous chunk load took {} ms in world '{}' at {},{}{}",
                elapsedMillis,
                level.getWorld().getName(),
                chunkX,
                chunkZ,
                reason == null || reason.isEmpty() ? "" : " (" + reason + ")"
            );
        }
    }
}
