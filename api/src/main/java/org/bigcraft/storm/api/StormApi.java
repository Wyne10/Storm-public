package org.bigcraft.storm.api;

import org.bukkit.plugin.java.JavaPlugin;
import org.slf4j.Logger;

/**
 * Static entry point into the Storm API.
 * <p>
 * The Storm plugin populates all fields here during its own startup, before any
 * {@link StormEffect} is registered or ticked, so consumers loading after Storm
 * (e.g. via a {@code depend}/{@code softdepend} on it) can rely on the accessors
 * being non-{@code null}. The setters are for Storm's own bootstrap and are not
 * intended to be called by other plugins.
 */
public final class StormApi {
    private static JavaPlugin plugin;
    private static Logger logger;
    private static StormEffectRegistry effectRegistry;
    private static StormEffectManager effectManager;

    /**
     * Returns the Storm plugin instance.
     */
    public static JavaPlugin getPlugin() {
        return plugin;
    }

    public static void setPlugin(JavaPlugin plugin) {
        StormApi.plugin = plugin;
    }

    /**
     * Returns Storm's logger.
     */
    public static Logger getLogger() {
        return logger;
    }

    public static void setLogger(Logger logger) {
        StormApi.logger = logger;
    }

    /**
     * Returns the registry used to register {@link StormEffect} implementations under
     * effect keys and to look up configured {@link StormEffectInstance}s.
     */
    public static StormEffectRegistry getEffectRegistry() {
        return effectRegistry;
    }

    public static void setEffectRegistry(StormEffectRegistry effectRegistry) {
        StormApi.effectRegistry = effectRegistry;
    }

    /**
     * Returns the manager used to query and change which effects currently affect a player.
     */
    public static StormEffectManager getEffectManager() {
        return effectManager;
    }

    public static void setEffectManager(StormEffectManager effectManager) {
        StormApi.effectManager = effectManager;
    }
}
