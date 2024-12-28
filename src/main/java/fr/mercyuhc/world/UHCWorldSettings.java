package fr.mercyuhc.world;

import org.bukkit.World;
import org.bukkit.WorldCreator;

public class UHCWorldSettings {
    private static final double DIAMOND_MULTIPLIER = 2.3;
    private static final double GOLD_MULTIPLIER = 2.55;
    private static final double IRON_MULTIPLIER = 2;
    private static final double LAPIS_MULTIPLIER = 2.22;

    public static String generateWorldSettings() {
        StringBuilder str = new StringBuilder("{");
        str.append("\"diamondSize\":7,");
        str.append("\"diamondCount\":" + (int)(1 * DIAMOND_MULTIPLIER) + ",");
        str.append("\"diamondMinHeight\":1,");
        str.append("\"diamondMaxHeight\":16,");

        str.append("\"goldSize\":9,");
        str.append("\"goldCount\":" + (int)(2 * GOLD_MULTIPLIER) + ",");
        str.append("\"goldMinHeight\":1,");
        str.append("\"goldMaxHeight\":32,");

        str.append("\"ironSize\":9,");
        str.append("\"ironCount\":" + (int)(20 * IRON_MULTIPLIER) + ",");
        str.append("\"ironMinHeight\":1,");
        str.append("\"ironMaxHeight\":64,");

        str.append("\"lapisSize\":7,");
        str.append("\"lapisCount\":" + (int)(1 * LAPIS_MULTIPLIER) + ",");
        str.append("\"lapisCenterHeight\":16,");
        str.append("\"lapisSpread\":16");

        str.append("}");
        return str.toString();
    }

    public static World createUHCWorld(String worldName) {
        WorldCreator creator = new WorldCreator(worldName);
        creator.generatorSettings(generateWorldSettings());
        creator.environment(World.Environment.NORMAL);
        creator.generateStructures(false);

        return creator.createWorld();
    }
}