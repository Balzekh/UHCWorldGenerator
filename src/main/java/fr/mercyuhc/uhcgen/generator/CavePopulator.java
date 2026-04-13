package fr.mercyuhc.uhcgen.generator;

import fr.mercyuhc.uhcgen.config.GenerationConfig;
import net.minecraft.server.v1_8_R3.ChunkSnapshot;
import net.minecraft.server.v1_8_R3.WorldGenCaves;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.generator.BlockPopulator;

import java.util.Random;

/**
 * BlockPopulator qui genere des grottes supplementaires via WorldGenCaves NMS.
 * Enregistre sur le monde au moment de sa creation dans WorldFactory.
 */
public final class CavePopulator extends BlockPopulator {

    @Override
    public void populate(World world, Random random, Chunk chunk) {
        if (chunk == null) return;

        CraftWorld craftWorld = (CraftWorld) world;

        for (int i = 0; i < GenerationConfig.CAVES_EXTRA_PASSES; i++) {
            if (random.nextInt(100) < GenerationConfig.CAVES_BOOST_PERCENTAGE) {
                new WorldGenCaves().a(
                        craftWorld.getHandle().chunkProviderServer,
                        craftWorld.getHandle(),
                        chunk.getX(),
                        chunk.getZ(),
                        new ChunkSnapshot()
                );
            }
        }
    }
}
