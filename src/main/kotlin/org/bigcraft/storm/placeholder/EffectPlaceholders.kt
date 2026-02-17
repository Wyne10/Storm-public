package org.bigcraft.storm.placeholder

import com.google.inject.Inject
import com.google.inject.Singleton
import me.clip.placeholderapi.expansion.PlaceholderExpansion
import me.wyne.wutils.common.Args
import me.wyne.wutils.common.Ticks
import me.wyne.wutils.i18n.kotlin.placeholderComponent
import me.wyne.wutils.i18n.kotlin.placeholderString
import me.wyne.wutils.i18n.kotlin.replace
import org.bigcraft.storm.Storm
import org.bigcraft.storm.effect.StormEffectManager
import org.bigcraft.storm.player.PlayerEffectManager
import org.bukkit.OfflinePlayer

@Singleton
class EffectPlaceholders @Inject constructor(
    private val plugin: Storm,
    private val effectManager: StormEffectManager,
    private val playerManager: PlayerEffectManager
) : PlaceholderExpansion() {

    init {
        register()
    }

    override fun getIdentifier() = "effect"

    override fun getAuthor() = "Wyne"

    override fun getVersion() = plugin.description.version

    override fun persist() = true

    override fun onRequest(player: OfflinePlayer?, params: String): String? {
        val args = Args(params, "_")

        if (args.size() < 2) {
            Storm.logger.error("Not enough arguments for effect placeholder. Required: 2")
            return null
        }

        val effectInstanceKey = args[0]
        val effect = effectManager.getEffectInstance(effectInstanceKey) ?: return null

        return if (args[1].startsWith("name"))
            player.placeholderComponent(effect.name).style("name", args[1])
        else if (args[1].equals("remaining", true) && player?.isOnline == true)
            Ticks.ofMillis(playerManager.getRemainingMillis(player.player, effectInstanceKey)).toString()
        else if (args[1].equals("remaining-format", true) && player?.isOnline == true)
            Ticks.ofMillis(playerManager.getRemainingMillis(player.player, effectInstanceKey)).let {
                return if (it == 0L) {
                    player.placeholderString("format-effect-inactive", "key" replace effectInstanceKey).get()
                } else player.placeholderString("format-effect-active", "key" replace effectInstanceKey).get()
            }
        else
            null
    }

}