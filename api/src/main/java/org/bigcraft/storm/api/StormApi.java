package org.bigcraft.storm.api;

import org.bukkit.plugin.java.JavaPlugin;
import org.slf4j.Logger;

public final class StormApi {
    private static JavaPlugin plugin;
    private static Logger logger;
    private static StormEffectRegistry effectRegistry;
    private static StormEffectManager effectManager;

    public static JavaPlugin getPlugin() {
        return plugin;
    }

    public static void setPlugin(JavaPlugin plugin) {
        StormApi.plugin = plugin;
    }

    public static Logger getLogger() {
        return logger;
    }

    public static void setLogger(Logger logger) {
        StormApi.logger = logger;
    }

    public static StormEffectRegistry getEffectRegistry() {
        return effectRegistry;
    }

    public static void setEffectRegistry(StormEffectRegistry effectRegistry) {
        StormApi.effectRegistry = effectRegistry;
    }

    public static StormEffectManager getEffectManager() {
        return effectManager;
    }

    public static void setEffectManager(StormEffectManager effectManager) {
        StormApi.effectManager = effectManager;
    }
}
