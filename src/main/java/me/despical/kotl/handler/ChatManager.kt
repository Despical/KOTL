package me.despical.kotl.handler

import me.clip.placeholderapi.PlaceholderAPI
import me.despical.commonsbox.configuration.ConfigUtils
import me.despical.kotl.Main
import me.despical.kotl.arena.Arena
import org.apache.commons.lang.StringUtils
import org.bukkit.ChatColor
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.entity.Player

/**
 * @author Despical
 * <p>
 * Created at 31.07.2020
 */
class ChatManager constructor(val plugin: Main) {

	private var config: FileConfiguration = ConfigUtils.getConfig(plugin, "messages")
	private val prefix: String = colorRawMessage(config.getString("In-Game.Plugin-Prefix"))

	fun getPrefix(): String = prefix

	fun colorRawMessage(message: String): String = ChatColor.translateAlternateColorCodes('&', message)

	fun colorMessage(message: String): String = ChatColor.translateAlternateColorCodes('&', config.getString(message))

	fun colorMessage(message: String, player: Player): String {
		var returnString: String = config.getString(message)
		if (plugin.server.pluginManager.isPluginEnabled("PlaceholderAPI")) {
			returnString = PlaceholderAPI.setPlaceholders(player, returnString)
		}
		return ChatColor.translateAlternateColorCodes('&', returnString)
	}

	private fun formatMessage(arena: Arena, message: String, player: Player): String {
		var returnString: String = message
		returnString = StringUtils.replace(returnString, "%player%", player.name)
		returnString = colorRawMessage(formatPlaceholders(returnString, arena))
		if (plugin.server.pluginManager.isPluginEnabled("PlaceholderAPI")) {
			returnString = PlaceholderAPI.setPlaceholders(player, returnString)
		}
		return returnString
	}

	private fun formatPlaceholders(message: String, arena: Arena): String {
		var returnString: String = message
		returnString = StringUtils.replace(returnString, "%arena%", arena.id)
		returnString = StringUtils.replace(returnString, "%players%", arena.players.size.toString())
		returnString = StringUtils.replace(returnString, "%king%", if (arena.king == null) "Nobody" else arena.king!!.name)
		return returnString
	}

	fun broadcastMessage(a: Arena, msg: String) {
		for (p in a.players) {
			p.sendMessage(prefix + msg)
		}
	}

	fun broadcastAction(a: Arena, p: Player, action: ActionType) {
		val message: String
		when (action) {
			ActionType.NEW_KING -> {
				message = formatMessage(a, colorMessage("In-Game.New-King"), p)
				a.hologram.getLine(0).removeLine()
				a.hologram.insertTextLine(0, formatMessage(a, colorMessage("In-Game.Last-King-Hologram"), p))
			}
			ActionType.JOIN -> message = formatMessage(a, colorMessage("In-Game.Join"), p)
			ActionType.LEAVE -> message = formatMessage(a, colorMessage("In-Game.Leave"), p)
		}
		for (player in a.players) {
			player.sendMessage(prefix + message)
		}
	}

	fun reloadConfig() {
		config = ConfigUtils.getConfig(plugin, "messages")
	}

	enum class ActionType {
		JOIN, LEAVE, NEW_KING
	}
}