package org.spigotmc;

import io.papermc.paper.configuration.GlobalConfiguration;
import net.minecraft.server.MinecraftServer;

public class AsyncCatcher {

    public static void catchOp(String reason) {
        if (!ca.spottedleaf.moonrise.common.util.TickThread.isTickThread()) { // Paper - chunk system
            final GlobalConfiguration configuration = GlobalConfiguration.get();
            if (configuration == null || configuration.coderyo == null || configuration.coderyo.schedulerSafety == null || configuration.coderyo.schedulerSafety.logAsyncApiViolations) {
                MinecraftServer.LOGGER.error("Thread {} failed main thread check: {}", Thread.currentThread().getName(), reason, new Throwable()); // Paper
            }
            throw new IllegalStateException("Asynchronous " + reason + "!");
        }
    }
}
