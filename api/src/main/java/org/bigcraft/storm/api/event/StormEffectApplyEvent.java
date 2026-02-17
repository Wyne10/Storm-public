package org.bigcraft.storm.api.event;

import org.bigcraft.storm.api.EffectSource;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class StormEffectApplyEvent extends Event implements Cancellable {

    private static final HandlerList HANDLER_LIST = new HandlerList();

    private final @NotNull OfflinePlayer player;
    private final @NotNull String effectKey;
    private final @NotNull ConfigurationSection config;
    private final @NotNull EffectSource source;

    private boolean isCancelled = false;

    public StormEffectApplyEvent(@NotNull OfflinePlayer who, @NotNull String effectKey, @NotNull ConfigurationSection config, @NotNull EffectSource source) {
        this.player = who;
        this.effectKey = effectKey;
        this.config = config;
        this.source = source;
    }

    public @NotNull OfflinePlayer getPlayer() {
        return player;
    }

    public @NotNull String getEffectKey() {
        return effectKey;
    }

    public @NotNull String getEffectInstanceKey() {
        return config.getName();
    }

    public @NotNull ConfigurationSection getConfig() {
        return config;
    }

    public @NotNull EffectSource getSource() {
        return source;
    }

    @Override
    public boolean isCancelled() {
        return this.isCancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.isCancelled = cancelled;
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLER_LIST;
    }

}
