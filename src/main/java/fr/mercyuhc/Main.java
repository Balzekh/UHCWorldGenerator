package fr.mercyuhc;

import fr.mercyuhc.world.WorldGenerator;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {
    private WorldGenerator worldGenerator;

    @Override
    public void onEnable() {
        worldGenerator = new WorldGenerator(this);
    }
}