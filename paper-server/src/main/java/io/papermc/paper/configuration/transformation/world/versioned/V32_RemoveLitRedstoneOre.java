package io.papermc.paper.configuration.transformation.world.versioned;

import java.util.List;
import org.spongepowered.configurate.transformation.ConfigurationTransformation;

import static org.spongepowered.configurate.NodePath.path;

/**
 * Lit redstone ore is no longer a separate block id. Remove the legacy id from
 * upgraded anti-xray configs before registry-backed block lists deserialize.
 */
public final class V32_RemoveLitRedstoneOre {

    private static final int VERSION = 32;

    private V32_RemoveLitRedstoneOre() {
    }

    public static void apply(final ConfigurationTransformation.VersionedBuilder builder) {
        builder.addVersion(VERSION, ConfigurationTransformation.builder()
            .addAction(path("anticheat", "anti-xray", "hidden-blocks"), (path, value) -> {
                final List<String> hiddenBlocks = value.getList(String.class);
                if (hiddenBlocks != null) {
                    hiddenBlocks.remove("lit_redstone_ore");
                    hiddenBlocks.remove("minecraft:lit_redstone_ore");
                    value.raw(hiddenBlocks);
                }
                return null;
            })
            .build()
        );
    }
}
