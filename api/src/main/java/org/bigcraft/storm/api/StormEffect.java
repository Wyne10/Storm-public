package org.bigcraft.storm.api;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

/**
 * Base class for Storm effect implementations.
 * <p>
 * A subclass represents one <em>type</em> of effect (e.g. "hunter", "potion") and is registered
 * under a key via {@link StormEffectRegistry#register}. Storm then instantiates it once per
 * configured {@link StormEffectInstance} that references that key, via reflection, so every
 * subclass must expose a public constructor with the same single-argument signature as this
 * class's constructor.
 * <p>
 * Every subclass is registered as a Bukkit listener automatically, so {@code @EventHandler} methods
 * (including the {@link org.bigcraft.storm.api.event} lifecycle events) work out of the box. This
 * applies to {@link TickableStormEffect} subclasses too, which may therefore combine periodic logic
 * with event handling.
 * <p>
 * <strong>Event handlers must check {@link #isAffected(Player)} themselves.</strong> A listener is
 * registered once per configured effect instance and receives every matching event on the server,
 * regardless of who is affected by that instance, so an {@code @EventHandler} that acts on a player
 * without first calling {@link #isAffected(Player)} will act on unaffected players as well. This is
 * unlike {@link TickableStormEffect#tick}, which Storm already filters to affected players before
 * calling it.
 */
public abstract class StormEffect implements Listener {

    private final ConfigurationSection config;
    private final String effectInstanceKey;

    /**
     * Creates the effect for one configured instance.
     *
     * @param config the instance's configuration section; its name is used as the effect
     *               instance key
     */
    public StormEffect(ConfigurationSection config) {
        this.config = config;
        this.effectInstanceKey = config.getName();
    }

    /**
     * Returns the Storm plugin instance.
     */
    protected final JavaPlugin getPlugin() {
        return StormApi.getPlugin();
    }

    /**
     * Returns this instance's configuration section, as passed to the constructor.
     */
    protected final @NotNull ConfigurationSection getConfig() {
        return config;
    }

    /**
     * Returns the key identifying this configured effect instance, as used with
     * {@link org.bigcraft.storm.api.StormEffectManager} and reported on lifecycle events.
     */
    protected  final @NotNull String getEffectInstanceKey() {
        return effectInstanceKey;
    }

    /**
     * Returns Storm's logger.
     */
    protected final Logger getLogger() {
        return StormApi.getLogger();
    }

    /**
     * Returns whether {@code player} is currently affected by this effect instance. Returns
     * {@code false} if {@code player} is {@code null}.
     * <p>
     * Event-driven effects must gate their {@code @EventHandler} logic on this, since events are
     * delivered irrespective of who this instance affects. Effects driven by
     * {@link TickableStormEffect#tick} do not need to call it, as Storm applies the same check
     * before each tick.
     */
    protected final boolean isAffected(@Nullable Player player) {
        return StormApi.getEffectManager().isAffected(player, effectInstanceKey);
    }

    /**
     * Returns how long this effect instance will keep affecting {@code player}, in milliseconds.
     * Returns {@code 0} if {@code player} is {@code null} or is not currently affected.
     */
    protected final long getRemainingMillis(@Nullable Player player) {
        return StormApi.getEffectManager().getRemainingMillis(player, effectInstanceKey);
    }

}
