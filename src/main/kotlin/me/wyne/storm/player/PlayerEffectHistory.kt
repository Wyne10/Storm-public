package me.wyne.storm.player

import com.j256.ormlite.field.DataType
import com.j256.ormlite.field.DatabaseField
import com.j256.ormlite.table.DatabaseTable
import java.util.Date
import java.util.UUID

enum class EffectHistoryAction {
    APPLY,
    CLEAR
}

@DatabaseTable(tableName = "storm_effects_history")
data class PlayerEffectHistory(
    @DatabaseField(generatedId = true)
    val id: Long = 0,
    @DatabaseField(index = true, canBeNull = false)
    val uuid: UUID = UUID.randomUUID(),
    @DatabaseField(columnName = PlayerEffect.EFFECT_INSTANCE_FIELD, canBeNull = false)
    val effectInstanceKey: String = "",
    @DatabaseField(canBeNull = false, width = 16)
    val action: String = EffectHistoryAction.APPLY.name,
    @DatabaseField(columnName = "duration_ms", canBeNull = false)
    val durationMillis: Long = 0,
    // Null for a clear, which has no originating EffectSource
    @DatabaseField(width = 64)
    val source: String? = null,
    @DatabaseField(width = 64)
    val server: String? = null,
    @DatabaseField(dataType = DataType.DATE, canBeNull = false)
    val timestamp: Date = Date()
) {
    constructor(uuid: UUID, effectInstanceKey: String, action: EffectHistoryAction, durationMillis: Long, source: String?, server: String?) :
            this(uuid = uuid, effectInstanceKey = effectInstanceKey, action = action.name, durationMillis = durationMillis,
                source = source, server = server, timestamp = Date())
}
