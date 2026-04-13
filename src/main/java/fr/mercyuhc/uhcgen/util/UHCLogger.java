package fr.mercyuhc.uhcgen.util;

import org.bukkit.Bukkit;

public final class UHCLogger {

    private static final String PREFIX = "§7[§bUHC§7] §f";

    private UHCLogger() {}

    public static void info(String msg) {
        Bukkit.getConsoleSender().sendMessage(PREFIX + msg);
    }

    public static void success(String msg) {
        Bukkit.getConsoleSender().sendMessage("§7[§aUHC§7] §a" + msg);
    }

    public static void error(String msg) {
        Bukkit.getLogger().severe("[UHC] " + msg);
    }
}
