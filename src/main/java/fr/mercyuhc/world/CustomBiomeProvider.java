package fr.mercyuhc.world;

import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.World;
import org.bukkit.Material;
import org.bukkit.block.Block;

public class CustomBiomeProvider extends WorldChunkManager {
    private static final int RADIUS = 700;
    private static final int MAX_RETRIES = 10;
    private static final double RADIUS_SQUARED = RADIUS * RADIUS;

    public CustomBiomeProvider(World world) {
        super(((CraftWorld)world).getHandle());
    }

    @Override
    public BiomeBase getBiome(BlockPosition pos) {
        double distanceSquared = pos.getX() * pos.getX() + pos.getZ() * pos.getZ();
        return distanceSquared <= RADIUS_SQUARED ? BiomeBase.PLAINS : BiomeBase.FOREST;
    }

    @Override
    public BiomeBase[] getBiomes(BiomeBase[] biomesArray, int x, int z, int width, int length) {
        if (biomesArray == null || biomesArray.length < width * length) {
            biomesArray = new BiomeBase[width * length];
        }

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < length; j++) {
                int realX = x + i;
                int realZ = z + j;
                double distanceSquared = realX * realX + realZ * realZ;
                biomesArray[i + j * width] = distanceSquared <= RADIUS_SQUARED ? BiomeBase.PLAINS : BiomeBase.FOREST;
            }
        }

        return biomesArray;
    }

    @Override
    public BiomeBase[] a(BiomeBase[] biomesArray, int x, int z, int width, int length, boolean flag) {
        return getBiomes(biomesArray, x, z, width, length);
    }

    private static void cleanChunk(World world, int cx, int cz) {
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int realX = (cx << 4) + x;
                int realZ = (cz << 4) + z;
                int realY = world.getHighestBlockYAt(realX, realZ);

                double distanceSquared = realX * realX + realZ * realZ;
                if (distanceSquared <= RADIUS_SQUARED) {
                    Block block = world.getBlockAt(realX, realY, realZ);
                    if (block.getType() == Material.LOG ||
                            block.getType() == Material.LOG_2 ||
                            block.getType() == Material.LEAVES ||
                            block.getType() == Material.LEAVES_2) {
                        block.setType(Material.AIR);
                        while (realY > 0 &&
                                (world.getBlockAt(realX, realY - 1, realZ).getType() == Material.LOG ||
                                        world.getBlockAt(realX, realY - 1, realZ).getType() == Material.LOG_2 ||
                                        world.getBlockAt(realX, realY - 1, realZ).getType() == Material.LEAVES ||
                                        world.getBlockAt(realX, realY - 1, realZ).getType() == Material.LEAVES_2)) {
                            world.getBlockAt(realX, realY - 1, realZ).setType(Material.AIR);
                            realY--;
                        }
                    }
                }
            }
        }
    }

    private static boolean verifyChunk(World world, int cx, int cz) {
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int realX = (cx << 4) + x;
                int realZ = (cz << 4) + z;
                double distanceSquared = realX * realX + realZ * realZ;

                if (distanceSquared <= RADIUS_SQUARED) {
                    int realY = world.getHighestBlockYAt(realX, realZ);
                    Block block = world.getBlockAt(realX, realY, realZ);

                    if (block.getType() == Material.LOG ||
                            block.getType() == Material.LOG_2 ||
                            block.getType() == Material.LEAVES ||
                            block.getType() == Material.LEAVES_2) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public static void patchBiomes(World world) {
        try {
            BiomeBase[] biomes = BiomeBase.getBiomes();
            for (int i = 0; i < biomes.length; i++) {
                if (biomes[i] != null && biomes[i] != BiomeBase.PLAINS && biomes[i] != BiomeBase.FOREST) {
                    biomes[i] = BiomeBase.FOREST;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void log(String message) {
        Bukkit.getConsoleSender().sendMessage("§8[§bForestApplier§8] §7" + message);
    }

    public static void injectBiomeProvider(World world) {
        try {
            for (int attempt = 0; attempt < MAX_RETRIES; attempt++) {
                WorldServer nmsWorld = ((CraftWorld) world).getHandle();
                CustomBiomeProvider provider = new CustomBiomeProvider(world);

                patchBiomes(world);

                try {
                    java.lang.reflect.Field worldProviderField = net.minecraft.server.v1_8_R3.World.class.getDeclaredField("worldProvider");
                    worldProviderField.setAccessible(true);
                    WorldProvider worldProvider = (WorldProvider) worldProviderField.get(nmsWorld);

                    java.lang.reflect.Field chunkManagerField = WorldProvider.class.getDeclaredField("c");
                    chunkManagerField.setAccessible(true);
                    chunkManagerField.set(worldProvider, provider);

                    int chunkRadius = (RADIUS >> 4) + 2;
                    boolean needsRegeneration = false;

                    for (int cx = -chunkRadius; cx <= chunkRadius; cx++) {
                        for (int cz = -chunkRadius; cz <= chunkRadius; cz++) {
                            if (world.isChunkLoaded(cx, cz)) {
                                world.unloadChunk(cx, cz, false, false);
                            }
                        }
                    }

                    for (int cx = -chunkRadius; cx <= chunkRadius; cx++) {
                        for (int cz = -chunkRadius; cz <= chunkRadius; cz++) {
                            int x = cx << 4;
                            int z = cz << 4;
                            if (x * x + z * z <= (RADIUS + 32) * (RADIUS + 32)) {
                                world.loadChunk(cx, cz);
                                cleanChunk(world, cx, cz);

                                net.minecraft.server.v1_8_R3.Chunk chunk = nmsWorld.getChunkAt(cx, cz);
                                byte[] biomes = new byte[256];
                                for (int i = 0; i < 16; i++) {
                                    for (int j = 0; j < 16; j++) {
                                        int realX = x + i;
                                        int realZ = z + j;
                                        double distanceSquared = realX * realX + realZ * realZ;
                                        biomes[i + j * 16] = (byte) (distanceSquared <= RADIUS_SQUARED ?
                                                BiomeBase.PLAINS.id : BiomeBase.FOREST.id);
                                    }
                                }
                                chunk.a(biomes);

                                if (!verifyChunk(world, cx, cz)) {
                                    needsRegeneration = true;
                                }
                            }
                        }
                    }

                    if (!needsRegeneration) {
                        log("Biomes et terrain correctement nettoyés après " + (attempt + 1) + " tentatives.");
                        break;
                    } else if (attempt < MAX_RETRIES - 1) {
                        log("Des arbres persistent, nouvelle tentative " + (attempt + 2) + "/" + MAX_RETRIES);
                        Thread.sleep(200);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}