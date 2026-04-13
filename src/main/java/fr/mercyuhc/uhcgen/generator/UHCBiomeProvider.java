package fr.mercyuhc.uhcgen.generator;

import fr.mercyuhc.uhcgen.config.GenerationConfig;
import fr.mercyuhc.uhcgen.util.UHCLogger;
import net.minecraft.server.v1_8_R3.BiomeBase;
import net.minecraft.server.v1_8_R3.BlockPosition;
import net.minecraft.server.v1_8_R3.WorldChunkManager;
import net.minecraft.server.v1_8_R3.WorldProvider;
import net.minecraft.server.v1_8_R3.WorldServer;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;

import java.lang.reflect.Field;

public class UHCBiomeProvider extends WorldChunkManager {

    private static final int RADIUS = GenerationConfig.RADIUS_PLAINS;
    private static final int RADIUS_SQ = RADIUS * RADIUS;

    public UHCBiomeProvider(World world) {
        super(((CraftWorld) world).getHandle());
    }

    @Override
    public BiomeBase getBiome(BlockPosition pos) {
        int x = pos.getX(), z = pos.getZ();
        return x * x + z * z <= RADIUS_SQ ? BiomeBase.PLAINS : BiomeBase.FOREST;
    }

    @Override
    public BiomeBase[] getBiomes(BiomeBase[] biomes, int x, int z, int width, int length) {
        if (biomes == null || biomes.length < width * length) {
            biomes = new BiomeBase[width * length];
        }
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < length; j++) {
                int rx = x + i, rz = z + j;
                biomes[i + j * width] = rx * rx + rz * rz <= RADIUS_SQ
                        ? BiomeBase.PLAINS : BiomeBase.FOREST;
            }
        }
        return biomes;
    }

    @Override
    public BiomeBase[] a(BiomeBase[] biomes, int x, int z, int width, int length, boolean flag) {
        return getBiomes(biomes, x, z, width, length);
    }

    public static void injectBiomeProvider(World world) {
        try {
            WorldServer nmsWorld = ((CraftWorld) world).getHandle();
            UHCBiomeProvider provider = new UHCBiomeProvider(world);

            Field wpField = net.minecraft.server.v1_8_R3.World.class.getDeclaredField("worldProvider");
            wpField.setAccessible(true);
            WorldProvider wp = (WorldProvider) wpField.get(nmsWorld);

            Field cmField = WorldProvider.class.getDeclaredField("c");
            cmField.setAccessible(true);
            cmField.set(wp, provider);

            for (org.bukkit.Chunk chunk : world.getLoadedChunks()) {
                world.unloadChunk(chunk.getX(), chunk.getZ(), false, false);
            }

            UHCLogger.info("Biome provider injecte avec succes.");
        } catch (Exception e) {
            UHCLogger.error("Erreur lors de l'injection du biome provider:");
            e.printStackTrace();
        }
    }
}
