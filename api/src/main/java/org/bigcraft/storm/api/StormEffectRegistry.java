package org.bigcraft.storm.api;

import org.jetbrains.annotations.NotNull;

public interface StormEffectRegistry {
    void register(@NotNull Class<? extends StormEffect> effect, @NotNull String effectKey);
}
