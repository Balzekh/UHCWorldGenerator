package fr.mercyuhc.uhcgen;

import fr.mercyuhc.uhcgen.generator.WorldManager;
import org.bukkit.plugin.java.JavaPlugin;

public class UHCGenEngine extends JavaPlugin {

    private WorldManager worldManager;

    @Override
    public void onEnable() {
        worldManager = new WorldManager(this);
    }
}
