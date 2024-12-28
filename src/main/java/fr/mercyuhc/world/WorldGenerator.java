package fr.mercyuhc.world;

import net.minecraft.server.v1_8_R3.BiomeBase;
import org.bukkit.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class WorldGenerator implements Listener {

    private final Plugin plugin;
    private boolean isGenerating = false;
    private boolean isMapReady = false;
    private static final int WORLD_RADIUS = 1500;

    public WorldGenerator(Plugin plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
        Bukkit.getScheduler().runTaskLater(plugin, this::startGeneration, 20L);
    }

    @EventHandler
    public void onPlayerLogin(PlayerLoginEvent event) {
        if (!isMapReady) {
            event.disallow(PlayerLoginEvent.Result.KICK_OTHER,
                    "§cLa map UHC est en cours de génération...");
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (isMapReady) {
            World uhcWorld = Bukkit.getWorld("uhc_map");
            if (uhcWorld != null) {
                Location spawn = uhcWorld.getSpawnLocation();
                event.getPlayer().teleport(new Location(
                        uhcWorld,
                        spawn.getX() + 0.5,
                        spawn.getY() + 100,
                        spawn.getZ() + 0.5
                ));
            }
        }
    }

    private void startGeneration() {
        if (isGenerating) return;

        log("§eDémarrage de la génération de la map UHC...");
        isGenerating = true;
        isMapReady = false;

        safeDeleteWorld("uhc_map");
        Bukkit.getScheduler().runTaskLater(plugin, this::createUHCWorld, 40L);
    }

    private void createUHCWorld() {
                log("§eInitialisation du monde UHC...");
                World world = UHCWorldSettings.createUHCWorld("uhc_map");
                world.setGameRuleValue("doDaylightCycle", "false");
                world.setTime(6000L);
                CustomBiomeProvider.injectBiomeProvider(world);
                resetSpawnChunks(world);
                WaterFixer waterFixer = new WaterFixer(plugin);
                waterFixer.fixLiquids(world);
                new FinalBiomeApplier(plugin, world, 500, this).runTaskTimer(plugin, 20L, 1L);
    }

    private void resetSpawnChunks(World world) {
        int spawnChunkX = world.getSpawnLocation().getBlockX() >> 4;
        int spawnChunkZ = world.getSpawnLocation().getBlockZ() >> 4;

        for (int cx = spawnChunkX - 1; cx <= spawnChunkX + 1; cx++) {
            for (int cz = spawnChunkZ - 1; cz <= spawnChunkZ + 1; cz++) {
                if (world.isChunkLoaded(cx, cz)) {
                    world.unloadChunk(cx, cz, false, false);
                }
            }
        }
        world.loadChunk(spawnChunkX, spawnChunkZ);
    }

    public void onFinalBiomeAppliedFinished() {
        isMapReady = true;
        isGenerating = false;
        logSuccess("§aMap UHC générée avec succès !");
    }

    private void safeDeleteWorld(String worldName) {
        World world = Bukkit.getWorld(worldName);
        if (world != null) {
            world.getPlayers().forEach(player ->
                    player.teleport(Bukkit.getWorlds().get(0).getSpawnLocation())
            );
            Bukkit.unloadWorld(world, false);
            File worldFolder = new File(Bukkit.getWorldContainer(), worldName);
            if (worldFolder.exists()) {
                deleteFolder(worldFolder);
            }
        }
    }

    private void deleteFolder(File folder) {
        if (folder.exists()) {
            if (folder.isDirectory()) {
                File[] files = folder.listFiles();
                if (files != null) {
                    for (File file : files) {
                        deleteFolder(file);
                    }
                }
            }
            folder.delete();
        }
    }

    private void log(String message) {
        Bukkit.getConsoleSender().sendMessage("§8[§bUHC§8] §7" + message);
    }

    private void logSuccess(String message) {
        Bukkit.getConsoleSender().sendMessage("§8[§aUHC§8] §a" + message);
    }
}