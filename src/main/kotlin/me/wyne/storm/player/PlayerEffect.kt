package me.wyne.storm.player

import com.j256.ormlite.field.DataType
import com.j256.ormlite.field.DatabaseField
import com.j256.ormlite.table.DatabaseTable
import java.util.Date
import java.util.UUID

@DatabaseTable(tableName = "storm_effects")
data class PlayerEffect(
    @DatabaseField(generatedId = true)
    val id: Long = 0,
    @DatabaseField(uniqueIndexName = UNIQUE_INDEX_NAME, canBeNull = false)
    val uuid: UUID = UUID.randomUUID(),
    @DatabaseField(columnName = EFFECT_INSTANCE_FIELD, uniqueIndexName = UNIQUE_INDEX_NAME, canBeNull = false)
    val effectInstanceKey: String = "",
    @DatabaseField(columnName = TOTAL_FIELD, canBeNull = false)
    val totalMillis: Long = 0,
    @DatabaseField(columnName = CONSUMED_FIELD, canBeNull = false)
    val consumedMillis: Long = 0,
    // Non-null while a server has the player online and is spending this effect's time
    @DatabaseField(columnName = SESSION_START_FIELD)
    val sessionStart: Long? = null,
    @DatabaseField(columnName = SESSION_OWNER_FIELD, width = 64)
    val sessionOwner: String? = null,
    @DatabaseField(dataType = DataType.DATE, canBeNull = false)
    val timestamp: Date = Date()
) {
    val remainingMillis: Long
        get() = (totalMillis - consumedMillis).coerceAtLeast(0)

    constructor(uuid: UUID, effectInstanceKey: String, totalMillis: Long, sessionStart: Long?, sessionOwner: String?) :
            this(uuid = uuid, effectInstanceKey = effectInstanceKey, totalMillis = totalMillis,
                sessionStart = sessionStart, sessionOwner = sessionOwner, timestamp = Date())

    companion object {
        private const val UNIQUE_INDEX_NAME = "player_effect_idx"
        const val EFFECT_INSTANCE_FIELD = "effect_instance_key"
        const val TOTAL_FIELD = "total_ms"
        const val CONSUMED_FIELD = "consumed_ms"
        const val SESSION_START_FIELD = "session_start"
        const val SESSION_OWNER_FIELD = "session_owner"
        const val UUID_FIELD = "uuid"
    }
}
