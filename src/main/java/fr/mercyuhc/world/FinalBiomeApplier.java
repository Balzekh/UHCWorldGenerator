package fr.mercyuhc.world;

import net.minecraft.server.v1_8_R3.BiomeBase;
import net.minecraft.server.v1_8_R3.Chunk;
import net.minecraft.server.v1_8_R3.WorldServer;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Random;

public class FinalBiomeApplier extends BukkitRunnable {

    private static final int CHUNK_SIZE = 16;
    private static final int RADIUS_ROOFED = 400;
    private static final int RADIUS_TAIGA  = 500;
    private final Plugin plugin;
    private final World bukkitWorld;
    private final WorldServer nmsWorld;
    private final int maxChunkRadius;
    private int currentChunkX;
    private int currentChunkZ;
    private final int minChunkCoord;
    private final int maxChunkCoord;
    private final WorldGenerator worldGenerator;
    private int processedChunks = 0;
    private final int totalChunks;
    private int nextProgressPercent = 5;

    public FinalBiomeApplier(Plugin plugin, World world, int maxRadiusInBlocks, WorldGenerator worldGenerator) {
        this.plugin = plugin;
        this.bukkitWorld = world;
        this.nmsWorld = ((CraftWorld) world).getHandle();
        this.worldGenerator = worldGenerator;
        this.maxChunkRadius = (maxRadiusInBlocks >> 4) + 2;
        this.minChunkCoord = -maxChunkRadius;
        this.maxChunkCoord =  maxChunkRadius;
        this.currentChunkX = minChunkCoord;
        this.currentChunkZ = minChunkCoord;
        int diameter = (maxChunkRadius * 2) + 1;
        this.totalChunks = diameter * diameter;
    }

    @Override
    public void run() {
        if (currentChunkX > maxChunkCoord) {
            currentChunkZ++;
            currentChunkX = minChunkCoord;
        }
        if (currentChunkZ > maxChunkCoord) {
            worldGenerator.onFinalBiomeAppliedFinished();
            this.cancel();
            return;
        }
        bukkitWorld.loadChunk(currentChunkX, currentChunkZ, true);
        final int chunkX = currentChunkX;
        final int chunkZ = currentChunkZ;
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            applyBiomesAndTreesToChunk(chunkX, chunkZ);
        }, 1L);

        processedChunks++;
        int percent = (processedChunks * 100) / totalChunks;
        if (percent >= nextProgressPercent) {
            log("§eAvancement de la génération des biomes: " + percent + "%");
            nextProgressPercent += 5;
        }
        currentChunkX++;
    }

    private void applyBiomesAndTreesToChunk(int cx, int cz) {
        Chunk nmsChunk = nmsWorld.getChunkAt(cx, cz);
        if (nmsChunk == null) {
            return;
        }

        byte[] newBiomes = new byte[256];
        Random random = new Random();

        int startX = cx << 4;
        int startZ = cz << 4;

        for (int x = 0; x < CHUNK_SIZE; x++) {
            for (int z = 0; z < CHUNK_SIZE; z++) {
                int realX = startX + x;
                int realZ = startZ + z;

                double distSq = realX * realX + realZ * realZ;
                double dist = Math.sqrt(distSq);

                BiomeBase finalBiome;
                if (dist <= RADIUS_ROOFED) {
                    finalBiome = BiomeBase.ROOFED_FOREST;
                } else if (dist <= RADIUS_TAIGA) {
                    finalBiome = BiomeBase.TAIGA;
                } else {
                    finalBiome = BiomeBase.FOREST;
                }
                newBiomes[x + z * CHUNK_SIZE] = (byte) finalBiome.id;
                if (dist <= RADIUS_ROOFED) {
                    double chance = random.nextDouble();
                    if (chance < 0.5) {
                        generateTree(realX, realZ, TreeType.DARK_OAK);
                    }
                } else if (dist <= RADIUS_TAIGA) {
                    if (random.nextDouble() < 0.11) {
                        TreeType[] sapinVariants = {
                                TreeType.REDWOOD,
                                TreeType.TALL_REDWOOD
                        };
                        TreeType chosen = sapinVariants[random.nextInt(sapinVariants.length)];
                        generateTree(realX, realZ, chosen);
                    }
                }
            }
        }
        nmsChunk.a(newBiomes);
    }

    private void generateTree(int x, int z, TreeType type) {
        int highestY = bukkitWorld.getHighestBlockYAt(x, z);
        if (highestY <= 0) return;
        Block ground = bukkitWorld.getBlockAt(x, highestY - 1, z);
        Material groundType = ground.getType();
        if (groundType == Material.GRASS || groundType == Material.DIRT) {
            Location loc = new Location(bukkitWorld, x, highestY, z);
            bukkitWorld.generateTree(loc, type);
        }
    }

    private void log(String message) {
        Bukkit.getConsoleSender().sendMessage("§8[§bFinalBiomeApplier§8] §7" + message);
    }
}
