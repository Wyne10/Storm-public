package me.wyne.storm.api;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Implemented by a {@link StormEffect} that needs to run logic periodically while it affects a
 * player, instead of (or in addition to) reacting to Bukkit/Storm events.
 * <p>
 * All configured instances of a given effect key share one repeating task scheduled on the
 * server's main thread; {@link #getPeriodTicks()} of the first instance loaded for that key
 * determines the task's period.
 * <p>
 * Implementing this interface does not affect Bukkit listener registration: like any
 * {@link StormEffect}, a tickable effect is still registered automatically and still receives its
 * {@code @EventHandler} callbacks, so periodic logic and event handling can be combined in one
 * effect. Note the two are filtered differently &mdash; {@link #tick} is only called for players
 * already affected by this instance, whereas event handlers fire for every matching event and must
 * call {@link StormEffect#isAffected(Player)} themselves.
 */
public interface TickableStormEffect {
    /**
     * Returns the number of ticks between calls to {@link #tick}.
     */
    long getPeriodTicks();

    /**
     * Called every {@link #getPeriodTicks()} ticks, on the server's main thread, once for each
     * online player currently affected by this effect instance. Storm applies that filter itself,
     * so an implementation need not call {@link StormEffect#isAffected(Player)} on {@code player}.
     */
    void tick(@NotNull Player player);
}
