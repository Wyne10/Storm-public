package org.bigcraft.storm.api;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface StormEffectRegistry {
    void register(@NotNull Class<? extends StormEffect> effect, @NotNull String effectKey);
    boolean isRegistered(@NotNull String effectKey);
    boolean isRegistered(@NotNull Class<? extends StormEffect> effect);
    @Nullable StormEffectInstance getEffectInstance(@NotNull String effectInstanceKey);
}
