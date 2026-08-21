package org.bigcraft.storm.api;

import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

/**
 * A single, configured instance of an effect.
 * <p>
 * An effect implementation (registered under an {@code effectKey} via
 * {@link StormEffectRegistry#register}) can have multiple named instances, each with its own
 * configuration section; this record is the metadata for one such instance, as loaded from
 * Storm's effect configuration files.
 *
 * @param key       the unique key identifying this instance, also used as the effect instance key
 *                  passed to {@link StormEffectManager} and {@link StormEffect}
 * @param effectKey the key of the registered {@link StormEffect} implementation this instance
 *                  is backed by
 * @param name      the display name of this instance
 * @param config    the configuration section this instance was loaded from
 */
public record StormEffectInstance(@NotNull String key, @NotNull String effectKey, @NotNull String name,
                                  @NotNull ConfigurationSection config) {
}
