package org.bigcraft.storm.api.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Fired when a player joins and one of their previously persisted, still-active effect instances
 * is restored. Fired once per restored instance, after the player's stored effects have been
 * loaded, before {@link org.bigcraft.storm.api.StormEffectManager#isAffected} reflects the
 * restored state for a given instance.
 * <p>
 * Unlike {@link StormEffectApplyEvent} and {@link StormEffectClearEvent}, the player is identified
 * by {@link UUID} rather than {@link org.bukkit.OfflinePlayer}.
 */
public class StormEffectJoinEvent extends Event {

    private static final HandlerList HANDLER_LIST = new HandlerList();

    private final @NotNull UUID player;
    private final @NotNull String effectInstanceKey;
    private final long expireAt;

    public StormEffectJoinEvent(@NotNull UUID player, @NotNull String effectInstanceKey, long expireAt) {
        this.player = player;
        this.effectInstanceKey = effectInstanceKey;
        this.expireAt = expireAt;
    }

    /**
     * Returns the UUID of the player who joined.
     */
    public @NotNull UUID getPlayer() {
        return player;
    }

    /**
     * Returns the key of the effect instance being restored.
     */
    public @NotNull String getEffectInstanceKey() {
        return effectInstanceKey;
    }

    /**
     * Returns when the restored effect will expire, as epoch milliseconds.
     */
    public long getExpireAt() {
        return expireAt;
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLER_LIST;
    }

}
