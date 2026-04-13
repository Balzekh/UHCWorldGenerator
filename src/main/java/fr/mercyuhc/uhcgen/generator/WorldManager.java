package fr.mercyuhc.uhcgen.generator;

import fr.mercyuhc.uhcgen.config.GenerationConfig;
import fr.mercyuhc.uhcgen.util.UHCLogger;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.plugin.Plugin;

import java.io.File;

public class WorldManager implements Listener {

    private final Plugin plugin;
    private boolean isGenerating = false;
    private boolean isMapReady = false;

    public WorldManager(Plugin plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
        Bukkit.getScheduler().runTaskLater(plugin, this::startGeneration, 20L);
    }

    @EventHandler
    public void onPlayerLogin(PlayerLoginEvent event) {
        if (!isMapReady) {
            event.disallow(PlayerLoginEvent.Result.KICK_OTHER,
                    "§cLa map UHC est en cours de generation...");
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (!isMapReady) return;

        World uhcWorld = Bukkit.getWorld(GenerationConfig.WORLD_NAME);
        if (uhcWorld != null) {
            Location spawn = uhcWorld.getSpawnLocation();
            event.getPlayer().teleport(new Location(
                    uhcWorld, spawn.getX() + 0.5, spawn.getY() + 100, spawn.getZ() + 0.5));
        }
    }

    private void startGeneration() {
        if (isGenerating) return;

        UHCLogger.info("§eDemarrage de la generation de la map UHC...");
        isGenerating = true;
        isMapReady = false;

        safeDeleteWorld(GenerationConfig.WORLD_NAME);
        Bukkit.getScheduler().runTaskLater(plugin, this::createUHCWorld, 40L);
    }

    private void createUHCWorld() {
        UHCLogger.info("§eInitialisation du monde UHC...");
        World world = WorldFactory.createUHCWorld(GenerationConfig.WORLD_NAME);
        world.setGameRuleValue("doDaylightCycle", "false");
        world.setTime(6000L);

        UHCBiomeProvider.injectBiomeProvider(world);

        new WorldProcessor(world, this).runTaskTimer(plugin, 20L, 1L);
    }

    public void onGenerationFinished() {
        isMapReady = true;
        isGenerating = false;
        UHCLogger.info("§aMap UHC generee avec succes !");
    }

    private void safeDeleteWorld(String worldName) {
        World world = Bukkit.getWorld(worldName);
        if (world != null) {
            world.getPlayers().forEach(p ->
                    p.teleport(Bukkit.getWorlds().get(0).getSpawnLocation()));
            Bukkit.unloadWorld(world, false);
        }
        File worldFolder = new File(Bukkit.getWorldContainer(), worldName);
        if (worldFolder.exists()) {
            deleteFolder(worldFolder);
        }
    }

    private void deleteFolder(File folder) {
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
