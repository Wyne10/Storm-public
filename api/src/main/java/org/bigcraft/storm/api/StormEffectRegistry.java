package org.bigcraft.storm.api;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Registry mapping effect keys to {@link StormEffect} implementations, and the entry point used to
 * make a custom effect type known to Storm. The registered instance is retrievable via
 * {@link StormApi#getEffectRegistry()}.
 */
public interface StormEffectRegistry {
    /**
     * Registers an effect implementation under {@code effectKey}. Once registered, Storm
     * instantiates {@code effect} (via a constructor taking a {@link org.bukkit.configuration.ConfigurationSection})
     * for every configured effect instance whose {@code effect} setting matches {@code effectKey}.
     *
     * @param effect    the effect implementation class; must expose a public constructor accepting
     *                  a single {@link org.bukkit.configuration.ConfigurationSection}
     * @param effectKey the key under which the implementation is registered
     */
    void register(@NotNull Class<? extends StormEffect> effect, @NotNull String effectKey);

    /**
     * Returns whether an implementation has been registered under {@code effectKey}.
     */
    boolean isRegistered(@NotNull String effectKey);

    /**
     * Returns whether {@code effect} has been registered under any effect key.
     */
    boolean isRegistered(@NotNull Class<? extends StormEffect> effect);

    /**
     * Looks up the configured effect instance for {@code effectInstanceKey}.
     *
     * @return the matching instance, or {@code null} if no instance is configured under that key
     */
    @Nullable StormEffectInstance getEffectInstance(@NotNull String effectInstanceKey);
}
