package org.bigcraft.storm.api;

import org.jetbrains.annotations.NotNull;

public interface StormEffectRegistry {
    void register(@NotNull StormEffect effect);
}
