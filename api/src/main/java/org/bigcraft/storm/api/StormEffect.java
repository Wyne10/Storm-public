package org.bigcraft.storm.api;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public abstract class StormEffect implements Listener {

    private final ConfigurationSection configuration;
    private final String effectInstanceKey;

    public StormEffect(ConfigurationSection configuration) {
        this.configuration = configuration;
        this.effectInstanceKey = configuration.getName();
    }

    public abstract @NotNull String getKey();

    protected JavaPlugin getPlugin() {
        return StormApi.getPlugin();
    }

    protected @Nullable ConfigurationSection getConfiguration() {
        return configuration;
    }

    protected Logger getLogger() {
        return StormApi.getLogger();
    }

    protected void register() {
        StormApi.getEffectRegistry().register(this);
    }

    protected boolean isAffected(Player player) {
        return StormApi.getEffectManager().isAffected(player, effectInstanceKey);
    }

}
