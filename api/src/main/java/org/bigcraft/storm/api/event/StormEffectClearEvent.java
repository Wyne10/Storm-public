package org.bigcraft.storm.api.event;

import org.bigcraft.storm.api.StormEffectInstance;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class StormEffectClearEvent extends Event {

    private static final HandlerList HANDLER_LIST = new HandlerList();

    private final @NotNull OfflinePlayer player;
    private final @NotNull StormEffectInstance effectInstance;

    public StormEffectClearEvent(@NotNull OfflinePlayer who, @NotNull StormEffectInstance effectInstance) {
        this.player = who;
        this.effectInstance = effectInstance;
    }

    public @NotNull OfflinePlayer getPlayer() {
        return player;
    }

    public @NotNull StormEffectInstance getEffectInstance() {
        return effectInstance;
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLER_LIST;
    }

}
