package org.bigcraft.storm.api;

import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

public record StormEffectInstance(@NotNull String key, @NotNull String effectKey, @NotNull String name,
                                  @NotNull ConfigurationSection config) {
}
