package me.wyne.storm.player

import com.google.inject.Singleton
import java.util.UUID
import java.util.concurrent.CompletableFuture

/**
 * Keeps effects in memory when there is no database: they survive rejoining but not a restart,
 * and are not shared with other servers. History is not kept.
 */
@Singleton
class LocalPlayerEffectStorage : PlayerEffectStorage {

    private val effects = mutableMapOf<UUID, MutableMap<String, Entry>>()

    @Synchronized
    override fun setEffect(player: UUID, effectInstanceKey: String, totalMillis: Long, sessionStart: Long?): CompletableFuture<Unit> {
        effects.getOrPut(player) { mutableMapOf() }[effectInstanceKey] =
            Entry(totalMillis, 0, sessionStart, System.currentTimeMillis())
        return done()
    }

    @Synchronized
    override fun beginSession(player: UUID, hardKeys: Set<String>, sessionStart: Long): CompletableFuture<List<StoredEffect>> {
        val playerEffects = effects[player] ?: return CompletableFuture.completedFuture(emptyList())
        playerEffects.filterKeys { it !in hardKeys }.values.forEach {
            it.consumedMillis += it.sessionStart?.let { start -> sessionStart - start } ?: 0
            it.sessionStart = sessionStart
        }
        playerEffects.values.removeIf { it.consumedMillis >= it.totalMillis }
        return CompletableFuture.completedFuture(playerEffects.map { (key, entry) -> entry.toStoredEffect(player, key) })
    }

    @Synchronized
    override fun endSession(player: UUID, effectInstanceKey: String, sessionStart: Long, endedAt: Long): CompletableFuture<Unit> {
        val playerEffects = effects[player] ?: return done()
        val entry = playerEffects[effectInstanceKey]?.takeIf { it.sessionStart == sessionStart } ?: return done()
        entry.consumedMillis += endedAt - sessionStart
        entry.sessionStart = null
        if (entry.consumedMillis >= entry.totalMillis)
            playerEffects.remove(effectInstanceKey)
        return done()
    }

    @Synchronized
    override fun clearEffect(player: UUID, effectInstanceKey: String): CompletableFuture<Unit> {
        effects[player]?.remove(effectInstanceKey)
        return done()
    }

    override fun record(player: UUID, effectInstanceKey: String, action: EffectHistoryAction, durationMillis: Long, source: String?): CompletableFuture<Unit> =
        done()

    private fun done(): CompletableFuture<Unit> = CompletableFuture.completedFuture(Unit)

    private class Entry(val totalMillis: Long, var consumedMillis: Long, var sessionStart: Long?, val appliedAt: Long) {
        fun toStoredEffect(player: UUID, effectInstanceKey: String) =
            StoredEffect(player, effectInstanceKey, totalMillis, (totalMillis - consumedMillis).coerceAtLeast(0), appliedAt)
    }

}
