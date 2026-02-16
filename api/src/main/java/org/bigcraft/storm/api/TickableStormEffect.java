package org.bigcraft.storm.api;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public interface TickableStormEffect {
    long getPeriodTicks();
    void tick(@NotNull Player player);
}
