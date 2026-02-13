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
    @DatabaseField(uniqueCombo = true, canBeNull = false)
    val uuid: UUID = UUID.randomUUID(),
    @DatabaseField(columnName = "effect_key", uniqueCombo = true, canBeNull = false)
    val effectKey: String,
    @DatabaseField(columnName = "expires_after_ticks", canBeNull = false)
    var expiresAfterTicks: Long = 0,
    @DatabaseField(columnName = "obtained_at_ticks", canBeNull = false)
    val obtainedAtTicks: Long = 0,
    @DatabaseField(dataType = DataType.DATE, canBeNull = false)
    val timestamp: Date = Date()
)