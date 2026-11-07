package me.wyne.storm.player

import java.util.UUID
import java.util.concurrent.CompletableFuture

/**
 * Where players' effects live between sessions. A soft effect's time is only spent while a
 * session is open: [beginSession] opens one for every soft effect, [endSession] charges the
 * elapsed time and closes it. A hard effect counts down from [StoredEffect.appliedAt] and never
 * opens a session.
 */
interface PlayerEffectStorage {

    /** Replaces the player's effect with a fresh one of [totalMillis], opening a session at [sessionStart] if it's not null. */
    fun setEffect(player: UUID, effectInstanceKey: String, totalMillis: Long, sessionStart: Long?): CompletableFuture<Unit>

    /**
     * Opens a session at [sessionStart] on every effect of the player except [hardKeys], first charging
     * any session left open by a server that never closed it, and drops effects that ran out.
     *
     * @return every effect the player still has
     */
    fun beginSession(player: UUID, hardKeys: Set<String>, sessionStart: Long): CompletableFuture<List<StoredEffect>>

    /** Charges the session that started at [sessionStart] up to [endedAt] and closes it, if it is still this server's. */
    fun endSession(player: UUID, effectInstanceKey: String, sessionStart: Long, endedAt: Long): CompletableFuture<Unit>

    fun clearEffect(player: UUID, effectInstanceKey: String): CompletableFuture<Unit>

    fun record(player: UUID, effectInstanceKey: String, action: EffectHistoryAction, durationMillis: Long, source: String?): CompletableFuture<Unit>

}

data class StoredEffect(
    val uuid: UUID,
    val effectInstanceKey: String,
    val totalMillis: Long,
    val remainingMillis: Long,
    val appliedAt: Long
)

enum class EffectHistoryAction {
    APPLY,
    CLEAR
}
