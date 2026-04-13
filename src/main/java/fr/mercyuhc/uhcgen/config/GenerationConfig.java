package fr.mercyuhc.uhcgen.config;

/**
 * Toutes les valeurs configurables de la generation.
 * Modifier ici pour ajuster la map sans toucher au reste du code.
 */
public final class GenerationConfig {

    private GenerationConfig() {}

    // NOM DU MONDE

    public static final String WORLD_NAME = "uhc_map";

    //  ZONES (rayon en blocs depuis le centre)

    /** Rayon du biome Roofed Forest (dark oaks denses) */
    public static final int RADIUS_ROOFED_FOREST = 400;

    /** Rayon du biome Taiga (sapins) */
    public static final int RADIUS_TAIGA = 600;

    /** Rayon total de la zone plains (terrain plat, nettoyage des arbres vanilla) */
    public static final int RADIUS_PLAINS = 700;

    // ARBRES

    /** Chance (0.0 - 1.0) de placer un Dark Oak par bloc en Roofed Forest */
    public static final double TREE_CHANCE_DARK_OAK = 0.5;

    /** Chance (0.0 - 1.0) de placer un sapin par bloc en Taiga */
    public static final double TREE_CHANCE_TAIGA = 0.15;

    // NETTOYAGE LIQUIDES

    /** Rayon de nettoyage des liquides, sable et gravier en surface */
    public static final int RADIUS_WATER_FIX = 500;

    /** Hauteur Y minimum du nettoyage (les blocs en dessous sont ignores) */
    public static final int WATER_FIX_MIN_Y = 50;

    // MINERAIS
    // Multiplicateurs appliques aux spawn rates vanilla
    // (base vanilla : diamond=1, gold=2, iron=20, lapis=1)

    public static final double DIAMOND_MULTIPLIER = 2.3;
    public static final int    DIAMOND_SIZE       = 7;
    public static final int    DIAMOND_MIN_HEIGHT = 1;
    public static final int    DIAMOND_MAX_HEIGHT = 16;

    public static final double GOLD_MULTIPLIER    = 2.55;
    public static final int    GOLD_SIZE          = 9;
    public static final int    GOLD_MIN_HEIGHT    = 1;
    public static final int    GOLD_MAX_HEIGHT    = 32;

    public static final double IRON_MULTIPLIER    = 2.0;
    public static final int    IRON_SIZE          = 9;
    public static final int    IRON_MIN_HEIGHT    = 1;
    public static final int    IRON_MAX_HEIGHT    = 64;

    public static final double LAPIS_MULTIPLIER     = 2.22;
    public static final int    LAPIS_SIZE           = 7;
    public static final int    LAPIS_CENTER_HEIGHT  = 16;
    public static final int    LAPIS_SPREAD         = 16;

    // GROTTES

    /** Active la generation de grottes supplementaires */
    public static final boolean CAVES_ENABLED          = true;

    /** Nombre de tentatives de generation par chunk (plus = plus de grottes potentielles) */
    public static final int     CAVES_EXTRA_PASSES     = 5;

    /** Chance (0-100) de generer une grotte a chaque tentative */
    public static final int     CAVES_BOOST_PERCENTAGE = 70;

    // PERFORMANCE

    /** Nombre de chunks traites par tick pendant la generation (plus = plus rapide, plus de lag) */
    public static final int CHUNKS_PER_TICK = 15;
}
