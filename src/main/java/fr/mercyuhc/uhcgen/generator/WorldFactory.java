package fr.mercyuhc.uhcgen.generator;

import fr.mercyuhc.uhcgen.config.GenerationConfig;
import org.bukkit.World;
import org.bukkit.WorldCreator;

public class WorldFactory {

    public static World createUHCWorld(String worldName) {
        WorldCreator creator = new WorldCreator(worldName);
        creator.generatorSettings(generateWorldSettings());
        creator.environment(World.Environment.NORMAL);
        creator.generateStructures(false);

        World world = creator.createWorld();

        if (GenerationConfig.CAVES_ENABLED) {
            world.getPopulators().add(new CavePopulator());
        }

        return world;
    }

    private static String generateWorldSettings() {
        return "{"
                + "\"diamondSize\":" + GenerationConfig.DIAMOND_SIZE + ","
                + "\"diamondCount\":" + (int) (1 * GenerationConfig.DIAMOND_MULTIPLIER) + ","
                + "\"diamondMinHeight\":" + GenerationConfig.DIAMOND_MIN_HEIGHT + ","
                + "\"diamondMaxHeight\":" + GenerationConfig.DIAMOND_MAX_HEIGHT + ","
                + "\"goldSize\":" + GenerationConfig.GOLD_SIZE + ","
                + "\"goldCount\":" + (int) (2 * GenerationConfig.GOLD_MULTIPLIER) + ","
                + "\"goldMinHeight\":" + GenerationConfig.GOLD_MIN_HEIGHT + ","
                + "\"goldMaxHeight\":" + GenerationConfig.GOLD_MAX_HEIGHT + ","
                + "\"ironSize\":" + GenerationConfig.IRON_SIZE + ","
                + "\"ironCount\":" + (int) (20 * GenerationConfig.IRON_MULTIPLIER) + ","
                + "\"ironMinHeight\":" + GenerationConfig.IRON_MIN_HEIGHT + ","
                + "\"ironMaxHeight\":" + GenerationConfig.IRON_MAX_HEIGHT + ","
                + "\"lapisSize\":" + GenerationConfig.LAPIS_SIZE + ","
                + "\"lapisCount\":" + (int) (1 * GenerationConfig.LAPIS_MULTIPLIER) + ","
                + "\"lapisCenterHeight\":" + GenerationConfig.LAPIS_CENTER_HEIGHT + ","
                + "\"lapisSpread\":" + GenerationConfig.LAPIS_SPREAD
                + "}";
    }
}
