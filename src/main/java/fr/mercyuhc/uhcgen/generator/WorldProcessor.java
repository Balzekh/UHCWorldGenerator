package fr.mercyuhc.uhcgen.generator;

import fr.mercyuhc.uhcgen.config.GenerationConfig;
import fr.mercyuhc.uhcgen.util.UHCLogger;
import net.minecraft.server.v1_8_R3.BiomeBase;
import net.minecraft.server.v1_8_R3.WorldServer;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.TreeType;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.EnumSet;
import java.util.Random;

public class WorldProcessor extends BukkitRunnable {

    private static final int RADIUS_ROOFED_SQ = GenerationConfig.RADIUS_ROOFED_FOREST * GenerationConfig.RADIUS_ROOFED_FOREST;
    private static final int RADIUS_TAIGA_SQ  = GenerationConfig.RADIUS_TAIGA * GenerationConfig.RADIUS_TAIGA;
    private static final int RADIUS_CLEAN_SQ  = GenerationConfig.RADIUS_PLAINS * GenerationConfig.RADIUS_PLAINS;
    private static final int RADIUS_WATER_SQ  = GenerationConfig.RADIUS_WATER_FIX * GenerationConfig.RADIUS_WATER_FIX;

    private static final int PREP_CHUNK_RADIUS = (GenerationConfig.RADIUS_PLAINS >> 4) + 2;
    private static final int TREE_CHUNK_RADIUS = (GenerationConfig.RADIUS_TAIGA >> 4) + 2;

    private static final int PREP_SKIP_DIST_SQ = (GenerationConfig.RADIUS_PLAINS + 32)
            * (GenerationConfig.RADIUS_PLAINS + 32);
    private static final int TREE_SKIP_DIST_SQ = (GenerationConfig.RADIUS_TAIGA + 32)
            * (GenerationConfig.RADIUS_TAIGA + 32);

    private static final EnumSet<Material> TREE_BLOCKS = EnumSet.of(
            Material.LOG, Material.LOG_2, Material.LEAVES, Material.LEAVES_2);
    private static final EnumSet<Material> REPLACEABLE_BLOCKS = EnumSet.of(
            Material.WATER, Material.STATIONARY_WATER,
            Material.LAVA, Material.STATIONARY_LAVA,
            Material.SAND, Material.GRAVEL);

    private enum Phase { PREPARE, TREES }

    private final World world;
    private final WorldServer nmsWorld;
    private final WorldManager worldManager;
    private final Random random = new Random();
    private final byte[] biomeBuffer = new byte[256];
    private final Location treeLocation;

    private Phase phase = Phase.PREPARE;
    private int cx, cz;
    private int phaseChunkRadius;
    private int processedChunks;
    private int totalChunks;
    private int nextPercent = 5;
    private final long startTime;

    public WorldProcessor(World world, WorldManager worldManager) {
        this.world = world;
        this.nmsWorld = ((CraftWorld) world).getHandle();
        this.worldManager = worldManager;
        this.treeLocation = new Location(world, 0, 0, 0);
        this.startTime = System.currentTimeMillis();
        initPhase(PREP_CHUNK_RADIUS);
    }

    private void initPhase(int chunkRadius) {
        this.phaseChunkRadius = chunkRadius;
        this.cx = -chunkRadius;
        this.cz = -chunkRadius;
        this.processedChunks = 0;
        int diameter = chunkRadius * 2 + 1;
        this.totalChunks = diameter * diameter;
        this.nextPercent = 5;
    }

    @Override
    public void run() {
        for (int i = 0; i < GenerationConfig.CHUNKS_PER_TICK; i++) {
            if (cz > phaseChunkRadius) {
                if (phase == Phase.PREPARE) {
                    UHCLogger.info("§ePreparation terminee. Generation des arbres...");
                    phase = Phase.TREES;
                    initPhase(TREE_CHUNK_RADIUS);
                    return;
                } else {
                    long elapsed = (System.currentTimeMillis() - startTime) / 1000;
                    UHCLogger.info("§aGeneration terminee en " + elapsed + "s !");
                    worldManager.onGenerationFinished();
                    cancel();
                    return;
                }
            }

            int originX = cx << 4;
            int originZ = cz << 4;
            int originDistSq = originX * originX + originZ * originZ;

            if (phase == Phase.PREPARE) {
                if (originDistSq <= PREP_SKIP_DIST_SQ) {
                    prepareChunk(cx, cz, originX, originZ);
                }
            } else {
                if (originDistSq <= TREE_SKIP_DIST_SQ) {
                    generateTreesInChunk(originX, originZ);
                }
            }

            processedChunks++;
            cx++;
            if (cx > phaseChunkRadius) {
                cx = -phaseChunkRadius;
                cz++;
            }
        }

        int percent = (processedChunks * 100) / totalChunks;
        if (percent >= nextPercent) {
            String label = phase == Phase.PREPARE ? "Preparation" : "Arbres";
            UHCLogger.info("§e" + label + ": " + percent + "%");
            nextPercent = percent - (percent % 5) + 5;
        }
    }

    private void prepareChunk(int chunkX, int chunkZ, int originX, int originZ) {
        world.loadChunk(chunkX, chunkZ, true);

        net.minecraft.server.v1_8_R3.Chunk nmsChunk = nmsWorld.getChunkAt(chunkX, chunkZ);
        if (nmsChunk == null) return;

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int rx = originX + x;
                int rz = originZ + z;
                int distSq = rx * rx + rz * rz;

                if (distSq <= RADIUS_CLEAN_SQ || distSq <= RADIUS_WATER_SQ) {
                    int topY = world.getHighestBlockYAt(rx, rz);
                    if (distSq <= RADIUS_CLEAN_SQ) {
                        cleanTreeColumn(rx, rz, topY);
                    }
                    if (distSq <= RADIUS_WATER_SQ) {
                        fixLiquidColumn(rx, rz, topY);
                    }
                }

                BiomeBase biome;
                if (distSq <= RADIUS_ROOFED_SQ) {
                    biome = BiomeBase.ROOFED_FOREST;
                } else if (distSq <= RADIUS_TAIGA_SQ) {
                    biome = BiomeBase.TAIGA;
                } else {
                    biome = BiomeBase.FOREST;
                }
                biomeBuffer[x + z * 16] = (byte) biome.id;
            }
        }

        nmsChunk.a(biomeBuffer);
    }

    private void cleanTreeColumn(int x, int z, int topY) {
        if (topY <= 0) return;

        Block block = world.getBlockAt(x, topY, z);
        if (!TREE_BLOCKS.contains(block.getType())) return;

        int y = topY;
        while (y > 0) {
            block = world.getBlockAt(x, y, z);
            if (!TREE_BLOCKS.contains(block.getType())) break;
            block.setType(Material.AIR);
            y--;
        }
    }

    private void fixLiquidColumn(int x, int z, int topY) {
        boolean topPlaced = false;

        for (int y = topY; y > GenerationConfig.WATER_FIX_MIN_Y; y--) {
            Block block = world.getBlockAt(x, y, z);
            Material type = block.getType();

            if (REPLACEABLE_BLOCKS.contains(type)) {
                block.setType(topPlaced ? Material.DIRT : Material.GRASS);
                topPlaced = true;
            } else if (type.isSolid()) {
                break;
            }
        }
    }


    private void generateTreesInChunk(int originX, int originZ) {
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int rx = originX + x;
                int rz = originZ + z;
                int distSq = rx * rx + rz * rz;

                if (distSq <= RADIUS_ROOFED_SQ) {
                    if (random.nextDouble() < GenerationConfig.TREE_CHANCE_DARK_OAK) {
                        tryPlaceTree(rx, rz, TreeType.DARK_OAK);
                    }
                } else if (distSq <= RADIUS_TAIGA_SQ) {
                    if (random.nextDouble() < GenerationConfig.TREE_CHANCE_TAIGA) {
                        tryPlaceTree(rx, rz,
                                random.nextBoolean() ? TreeType.REDWOOD : TreeType.TALL_REDWOOD);
                    }
                }
            }
        }
    }

    private void tryPlaceTree(int x, int z, TreeType type) {
        int y = world.getHighestBlockYAt(x, z);
        if (y <= 0) return;

        Material ground = world.getBlockAt(x, y - 1, z).getType();
        if (ground == Material.GRASS || ground == Material.DIRT) {
            treeLocation.setX(x);
            treeLocation.setY(y);
            treeLocation.setZ(z);
            world.generateTree(treeLocation, type);
        }
    }
}
