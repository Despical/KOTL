/*
 * KOTL - Don't let others to climb top of the ladders!
 * Copyright (C) 2020 Despical
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package me.despical.kotl.handlers;

import me.despical.commonsbox.compat.VersionResolver;
import me.despical.commonsbox.string.StringMatcher;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import me.clip.placeholderapi.PlaceholderAPI;
import me.despical.commonsbox.configuration.ConfigUtils;
import me.despical.kotl.Main;
import me.despical.kotl.arena.Arena;

/**
 * @author Despical
 * <p>
 * Created at 22.06.2020
 */
public class ChatManager {

	private String prefix;
	
	private final Main plugin;
	private FileConfiguration config;
	
	public ChatManager(Main plugin) {
		this.plugin = plugin;
		this.config = ConfigUtils.getConfig(plugin, "messages");
		this.prefix = colorRawMessage(config.getString("In-Game.Plugin-Prefix"));
	}
	
	public String getPrefix() {
		return prefix;
	}

	public String colorRawMessage(String message) {
		if (message == null) {
			return "";
		}

		if (message.contains("#") && VersionResolver.isCurrentEqualOrHigher(VersionResolver.ServerVersion.v1_16_R1)) {
			message = StringMatcher.matchColorRegex(message);
		}

		return ChatColor.translateAlternateColorCodes('&', message);
	}
	public String colorMessage(String message) {
		return colorRawMessage(config.getString(message));
	}
	
	public String colorMessage(String message, Player player) {
		String returnString = config.getString(message);

		if (plugin.getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
			returnString = PlaceholderAPI.setPlaceholders(player, returnString);
		}

		return colorRawMessage(returnString);
	}

	private String formatMessage(Arena arena, String message, Player player) {
		String returnString = message;
		returnString = StringUtils.replace(returnString, "%player%", player.getName());
		returnString = colorRawMessage(formatPlaceholders(returnString, arena));

		if (plugin.getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
			returnString = PlaceholderAPI.setPlaceholders(player, returnString);
		}

		return returnString;
	}

	private String formatPlaceholders(String message, Arena arena) {
		String returnString = message;

		returnString = StringUtils.replace(returnString, "%arena%", arena.getId());
		returnString = StringUtils.replace(returnString, "%players%", Integer.toString(arena.getPlayers().size()));
		returnString = StringUtils.replace(returnString, "%king%", arena.getKing() == null ? colorMessage("In-Game.There-Is-No-King") : arena.getKing().getName());
		return returnString;
	}
	
	public void broadcastMessage(Arena a, String msg) {
		a.getPlayers().forEach(p -> p.sendMessage(prefix + msg));
	}

	public void broadcastAction(Arena a, Player p, ActionType action) {
		String message;

		switch (action) {
			case JOIN:
				message = formatMessage(a, colorMessage("In-Game.Join"), p);
				break;
			case LEAVE:
				message = formatMessage(a, colorMessage("In-Game.Leave"), p);
				break;
			case NEW_KING:
				message = formatMessage(a, colorMessage("In-Game.New-King"), p);

				a.getHologram().appendLine(formatMessage(a, colorMessage("In-Game.Last-King-Hologram"), p));
				break;
			default:
				return;
		}

		broadcastMessage(a, message);
	}
	
	public void reloadConfig() {
		config = ConfigUtils.getConfig(plugin, "messages");
		prefix = colorRawMessage(config.getString("In-Game.Plugin-Prefix"));
	}

	public enum ActionType {
		JOIN, LEAVE, NEW_KING
	}
}