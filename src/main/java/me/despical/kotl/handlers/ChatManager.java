/*
 * KOTL - Don't let others climb to top of the ladders!
 * Copyright (C) 2024  Berke Akçen
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.despical.kotl.handlers;

import me.clip.placeholderapi.PlaceholderAPI;
import me.despical.commons.configuration.ConfigUtils;
import me.despical.commons.util.Strings;
import me.despical.kotl.Main;
import me.despical.kotl.arena.Arena;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * @author Despical
 * <p>
 * Created at 22.06.2020
 */
public class ChatManager {

	private String prefix;
	private FileConfiguration config;

	private final Main plugin;
	private final boolean papiEnabled;

	public ChatManager(Main plugin) {
		this.plugin = plugin;
		this.papiEnabled = plugin.getServer().getPluginManager().isPluginEnabled("PlaceholderAPI");
		this.config = ConfigUtils.getConfig(plugin, "messages");
		this.prefix = message("in_game.plugin_prefix");
	}

	public String coloredRawMessage(String message) {
		return Strings.format(message);
	}

	public String prefixedRawMessage(String message) {
		return prefix + coloredRawMessage(message);
	}

	public String message(String path) {
		path = me.despical.commons.string.StringUtils.capitalize(path.replace('_', '-'), '-', '.');
		return coloredRawMessage(config.getString(path));
	}

	public String prefixedMessage(String path) {
		return prefix + message(path);
	}

	public boolean isPapiEnabled() {
		return papiEnabled;
	}

	public String message(String path, Player player) {
		String returnString = message(path);
		returnString = returnString.replace("%player%", player.getName());
		returnString = formatMessage(returnString, player);

		return returnString;
	}

	public String formatMessage(String message, Player player) {
		String returnString = message;

		if (papiEnabled) {
			returnString = PlaceholderAPI.setPlaceholders(player, returnString);
		}

		return returnString;
	}

	private String formatMessage(Arena arena, String message, Player player) {
		String returnString = message;
		returnString = returnString.replace("%player%", player.getName());
		returnString = formatMessage(returnString, arena);
		returnString = formatMessage(returnString, player);

		return coloredRawMessage(returnString);
	}

	private String formatMessage(String message, Arena arena) {
		String returnString = message;

		returnString = returnString.replace("%arena%", arena.getId());
		returnString = returnString.replace("%players%", Integer.toString(arena.getPlayers().size()));
		returnString = returnString.replace("%king%", arena.getKingName());
		return returnString;
	}

	public List<String> getStringList(String path) {
		path = me.despical.commons.string.StringUtils.capitalize(path.replace('_', '-'), '-', '.');
		return config.getStringList(path);
	}

	public void broadcastAction(Arena arena, Player player, ActionType action) {
		String path;

		switch (action) {
			case JOIN -> path = "join";
			case LEAVE -> path = "leave";
			case NEW_KING -> path = "new_king";
			default -> {
				return;
			}
		}

		arena.broadcastMessage(prefix + formatMessage(arena, message("in_game." + path), player));
	}

	public String getPrefix() {
		return prefix;
	}

	public void reload() {
		config = ConfigUtils.getConfig(plugin, "messages");
		prefix = message("in_game.plugin_prefix");
	}

	public enum ActionType {
		JOIN, LEAVE, NEW_KING
	}
}