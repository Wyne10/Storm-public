package org.bigcraft.storm.api;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface StormEffectManager {
    boolean isAffected(@Nullable Player player, @NotNull String effectInstanceKey);
    void setEffect(@Nullable OfflinePlayer player, @NotNull String effectInstanceKey, long durationMillis, EffectSource source);
    void clearEffect(@Nullable OfflinePlayer player, @NotNull String effectInstanceKey);
}
