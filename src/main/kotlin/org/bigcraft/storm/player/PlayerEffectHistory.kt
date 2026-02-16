package org.bigcraft.storm.player

import com.j256.ormlite.field.DataType
import com.j256.ormlite.field.DatabaseField
import com.j256.ormlite.table.DatabaseTable
import java.util.Date
import java.util.UUID

@DatabaseTable(tableName = "storm_effects_history")
data class PlayerEffectHistory(
    @DatabaseField(generatedId = true)
    val id: Long = 0,
    @DatabaseField(index = true, canBeNull = false)
    val uuid: UUID = UUID.randomUUID(),
    @DatabaseField(columnName = EFFECT_INSTANCE_FIELD, canBeNull = false)
    val effectInstanceKey: String = "",
    @DatabaseField(columnName = "duration_ms", canBeNull = false)
    var durationMillis: Long = 0,
    @DatabaseField(canBeNull = false)
    val source: String = "",
    @DatabaseField(dataType = DataType.DATE, canBeNull = false)
    val timestamp: Date = Date()
) {
    constructor(uuid: UUID, effectInstanceKey: String, durationMillis: Long, source: String) :
            this(uuid = uuid, effectInstanceKey = effectInstanceKey, durationMillis = durationMillis, source = source, timestamp = Date())

    companion object {
        private const val UNIQUE_INDEX_NAME = "idx_player_effect"
        const val EFFECT_INSTANCE_FIELD = "effect_instance_key"
    }
}