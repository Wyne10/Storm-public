package org.bigcraft.storm.api;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public abstract class StormEffect implements Listener {

    private final ConfigurationSection config;
    private final String effectInstanceKey;

    public StormEffect(ConfigurationSection config) {
        this.config = config;
        this.effectInstanceKey = config.getName();
    }

    protected final JavaPlugin getPlugin() {
        return StormApi.getPlugin();
    }

    protected final @NotNull ConfigurationSection getConfig() {
        return config;
    }

    protected  final @NotNull String getEffectInstanceKey() {
        return effectInstanceKey;
    }

    protected final Logger getLogger() {
        return StormApi.getLogger();
    }

    protected final boolean isAffected(@Nullable Player player) {
        return StormApi.getEffectManager().isAffected(player, effectInstanceKey);
    }

    protected final long getRemainingMillis(@Nullable Player player) {
        return StormApi.getEffectManager().getRemainingMillis(player, effectInstanceKey);
    }

}
