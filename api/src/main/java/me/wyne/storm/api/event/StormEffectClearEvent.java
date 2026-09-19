package me.wyne.storm.api.event;

import me.wyne.storm.api.StormEffectInstance;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Fired after an effect instance has been cleared from a player via
 * {@link me.wyne.storm.api.StormEffectManager#clearEffect}. Unlike
 * {@link StormEffectApplyEvent}, this is not cancellable: by the time it fires the player's
 * effect state has already been removed.
 */
public class StormEffectClearEvent extends Event {

    private static final HandlerList HANDLER_LIST = new HandlerList();

    private final @NotNull OfflinePlayer player;
    private final @NotNull StormEffectInstance effectInstance;

    public StormEffectClearEvent(@NotNull OfflinePlayer who, @NotNull StormEffectInstance effectInstance) {
        this.player = who;
        this.effectInstance = effectInstance;
    }

    /**
     * Returns the player the effect was cleared from.
     */
    public @NotNull OfflinePlayer getPlayer() {
        return player;
    }

    /**
     * Returns the effect instance that was cleared.
     */
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
