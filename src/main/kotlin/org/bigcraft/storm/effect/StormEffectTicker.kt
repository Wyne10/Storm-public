package org.bigcraft.storm.effect

import com.google.inject.Inject
import com.google.inject.Singleton
import me.wyne.wutils.common.scheduler.Schedulers
import me.wyne.wutils.common.scheduler.Task
import me.wyne.wutils.common.terminable.Terminable
import org.bigcraft.storm.Storm
import org.bigcraft.storm.api.TickableStormEffect
import org.bigcraft.storm.player.PlayerEffectManager
import org.bukkit.Bukkit

@Singleton
class StormEffectTicker @Inject constructor(plugin: Storm, private val playerManager: PlayerEffectManager) : Terminable {

    private val effectTasks = mutableMapOf<String, Task>()
    private val effects = mutableMapOf<String, MutableSet<Pair<StormEffectInstance, TickableStormEffect>>>()

    init {
        plugin.bind(this)
    }

    fun registerEffect(effectKey: String, periodTicks: Long) {
        if (effectTasks.containsKey(effectKey)) return
        effectTasks[effectKey] =
            Schedulers.sync().runRepeating(Runnable { tick(effectKey) }, 0, periodTicks)
    }

    fun registerEffectInstance(effectInstance: StormEffectInstance, effect: TickableStormEffect) {
        val effectSet = effects.getOrPut(effectInstance.effectKey) { mutableSetOf() }
        effectSet.add(Pair(effectInstance, effect))
    }

    private fun tick(effectKey: String) {
        if (!effectTasks.containsKey(effectKey)) return
        for (player in Bukkit.getOnlinePlayers()) {
            val affected = effects[effectKey]!!
                .filter { playerManager.isAffected(player, it.first.key) }
                .map { it.second }
            affected.forEach { it.tick(player) }
        }
    }

    fun clear() {
        effects.clear()
    }

    override fun close() {
        effectTasks.values.forEach(Task::closeAndReportException)
    }

}