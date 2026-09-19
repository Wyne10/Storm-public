package me.wyne.storm.api.event;

import me.wyne.storm.api.EffectSource;
import me.wyne.storm.api.StormEffectInstance;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Fired before an effect instance is applied to (or extended for) a player, i.e. before
 * {@link me.wyne.storm.api.StormEffectManager#setEffect} takes effect. Cancelling this event
 * aborts the call: no duration, history, or state changes happen.
 */
public class StormEffectApplyEvent extends Event implements Cancellable {

    private static final HandlerList HANDLER_LIST = new HandlerList();

    private final @NotNull OfflinePlayer player;
    private final @NotNull StormEffectInstance effectInstance;
    private final long durationMillis;
    private final @NotNull EffectSource source;

    private boolean isCancelled = false;

    public StormEffectApplyEvent(@NotNull OfflinePlayer who, @NotNull StormEffectInstance effectInstance, long durationMillis, @NotNull EffectSource source) {
        this.player = who;
        this.effectInstance = effectInstance;
        this.durationMillis = durationMillis;
        this.source = source;
    }

    /**
     * Returns the player the effect is being applied to.
     */
    public @NotNull OfflinePlayer getPlayer() {
        return player;
    }

    /**
     * Returns the effect instance being applied.
     */
    public @NotNull StormEffectInstance getEffectInstance() {
        return effectInstance;
    }

    /**
     * Returns how long the effect is being applied for, in milliseconds. This replaces whatever
     * duration the player had left, rather than adding to it.
     */
    public long getDurationMillis() {
        return durationMillis;
    }

    /**
     * Returns where this application originated from.
     */
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
