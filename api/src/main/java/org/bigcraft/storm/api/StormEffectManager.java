package org.bigcraft.storm.api;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Manages which effect instances currently affect which players. This is the runtime, per-player
 * counterpart to {@link StormEffectRegistry}: instances are applied and cleared here, in between
 * being registered ({@link StormEffectRegistry#register}) and ticked ({@link TickableStormEffect}).
 * The registered instance is retrievable via {@link StormApi#getEffectManager()}.
 * <p>
 * Effect state is keyed by player UUID and persisted across sessions; a player who is offline can
 * still have effects applied to or cleared from them, but affectedness/remaining-time queries only
 * make sense for online players and return "not affected" for offline or {@code null} players.
 */
public interface StormEffectManager {
    /**
     * Returns whether {@code player} is currently affected by the effect instance identified by
     * {@code effectInstanceKey}. Returns {@code false} if {@code player} is {@code null}.
     */
    boolean isAffected(@Nullable Player player, @NotNull String effectInstanceKey);

    /**
     * Returns how long the effect instance identified by {@code effectInstanceKey} will keep
     * affecting {@code player}, in milliseconds. Returns {@code 0} if {@code player} is
     * {@code null} or is not currently affected by that instance; never negative.
     */
    long getRemainingMillis(@Nullable Player player, @NotNull String effectInstanceKey);

    /**
     * Applies (or extends) the effect instance identified by {@code effectInstanceKey} to
     * {@code player} for {@code durationMillis}, replacing any remaining duration already in
     * effect. Fires a cancellable {@link org.bigcraft.storm.api.event.StormEffectApplyEvent}
     * beforehand; if that event is cancelled, no state is changed. Does nothing if {@code player}
     * is {@code null}.
     *
     * @param durationMillis how long, in milliseconds, the effect should affect the player for
     * @param source         where this call originated from, recorded in the player's effect history
     * @throws IllegalArgumentException if no effect instance is registered under
     *                                  {@code effectInstanceKey}
     */
    void setEffect(@Nullable OfflinePlayer player, @NotNull String effectInstanceKey, long durationMillis, EffectSource source);

    /**
     * Clears the effect instance identified by {@code effectInstanceKey} from {@code player}, if
     * present, and fires {@link org.bigcraft.storm.api.event.StormEffectClearEvent}. Does nothing
     * if {@code player} is {@code null} or if no effect instance is registered under
     * {@code effectInstanceKey}.
     */
    void clearEffect(@Nullable OfflinePlayer player, @NotNull String effectInstanceKey);
}
