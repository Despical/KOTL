/*
 * KOTL - Don't let others to climb top of the ladders!
 * Copyright (C) 2021 Despical and contributors
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
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package me.despical.kotl.handler;

import me.despical.commons.configuration.ConfigUtils;
import me.despical.commons.util.Strings;
import org.apache.commons.lang.StringUtils;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import me.clip.placeholderapi.PlaceholderAPI;
import me.despical.kotl.Main;
import me.despical.kotl.arena.Arena;

import java.util.List;

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
		this.prefix = message("In-Game.Plugin-Prefix");
	}
	
	public String getPrefix() {
		return prefix;
	}

	public String coloredRawMessage(String message) {
		return Strings.format(message);
	}

	public String prefixedRawMessage(String message) {
		return prefix + coloredRawMessage(message);
	}

	public String message(String path) {
		return coloredRawMessage(config.getString(path));
	}

	public String prefixedMessage(String path) {
		return prefix + message(path);
	}
	
	public String message(String path, Player player) {
		String returnString = config.getString(path);
		returnString = StringUtils.replace(returnString, "%player%", player.getName());

		if (plugin.getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
			returnString = PlaceholderAPI.setPlaceholders(player, returnString);
		}

		return coloredRawMessage(returnString);
	}

	private String formatMessage(Arena arena, String message, Player player) {
		String returnString = message;

		returnString = StringUtils.replace(returnString, "%player%", player.getName());
		returnString = formatPlaceholders(returnString, arena);

		if (plugin.getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
			returnString = PlaceholderAPI.setPlaceholders(player, returnString);
		}

		return coloredRawMessage(returnString);
	}

	private String formatPlaceholders(String message, Arena arena) {
		String returnString = message;

		returnString = StringUtils.replace(returnString, "%arena%", arena.getId());
		returnString = StringUtils.replace(returnString, "%players%", Integer.toString(arena.getPlayers().size()));
		returnString = StringUtils.replace(returnString, "%king%", arena.getKingName());
		return returnString;
	}

	public List<String> getStringList(String path) {
		return config.getStringList(path);
	}
	
	public void broadcastAction(Arena arena, Player player, ActionType action) {
		String path;

		switch (action) {
			case JOIN:
				path = "In-Game.Join";
				break;
			case LEAVE:
				path = "In-Game.Leave";
				break;
			case NEW_KING:
				path = "In-Game.New-King";

				arena.getHologram().appendLine(formatMessage(arena, message("In-Game.Last-King-Hologram"), player));
				break;
			default:
				return;
		}

		arena.broadcastMessage(prefix + formatMessage(arena, message(path), player));
	}
	
	public void reloadConfig() {
		config = ConfigUtils.getConfig(plugin, "messages");
		prefix = message("In-Game.Plugin-Prefix");
	}

	public enum ActionType {
		JOIN, LEAVE, NEW_KING
	}
}