package org.bigcraft.storm.player

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
    @DatabaseField(columnName = "remaining_ms", canBeNull = false)
    var remainingMillis: Long = 0,
    @DatabaseField(dataType = DataType.DATE, canBeNull = false)
    val timestamp: Date = Date()
) {
    constructor(uuid: UUID, effectInstanceKey: String, remainingMillis: Long) :
            this(uuid = uuid, effectInstanceKey = effectInstanceKey, remainingMillis = remainingMillis, timestamp = Date())

    companion object {
        private const val UNIQUE_INDEX_NAME = "idx_player_effect"
        const val EFFECT_INSTANCE_FIELD = "effect_instance_key"
    }
}