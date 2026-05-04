package org.bigcraft.storm.api.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

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

    public @NotNull UUID getPlayer() {
        return player;
    }

    public @NotNull String getEffectInstanceKey() {
        return effectInstanceKey;
    }

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
