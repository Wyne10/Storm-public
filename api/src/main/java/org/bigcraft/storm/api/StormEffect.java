package org.bigcraft.storm.api;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public abstract class StormEffect implements Listener {

    public abstract @NotNull String getKey();

    protected JavaPlugin getPlugin() {
        return StormApi.getPlugin();
    }

    protected @Nullable ConfigurationSection getConfiguration() {
        return StormApi.getPlugin().getConfig().getConfigurationSection("effect." + getKey());
    }

    protected Logger getLogger() {
        return StormApi.getLogger();
    }

}
