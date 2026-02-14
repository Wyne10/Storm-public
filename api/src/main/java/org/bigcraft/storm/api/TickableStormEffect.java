package org.bigcraft.storm.api;

import org.bukkit.entity.Player;

public interface TickableStormEffect {
    long getIntervalTicks();
    void tick(Player player);
}
