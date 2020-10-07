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

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.despical.kotl.Main;
import me.despical.kotl.api.StatsStorage;
import me.despical.kotl.arena.Arena;
import me.despical.kotl.arena.ArenaRegistry;

/**
 * @author Despical
 * <p>
 * Created at 22.06.2020
 */
public class PlaceholderManager extends PlaceholderExpansion {
	
	private final Main plugin = JavaPlugin.getPlugin(Main.class);
	
	@Override
	public boolean persist() {
		return true;
	}

	public String getIdentifier() {
		return "kotl";
	}

	public String getAuthor() {
		return "Despical";
	}

	public String getVersion() {
		return "1.1.3";
	}

	public String onPlaceholderRequest(Player player, String id) {
		if (player == null) {
			return null;
		}

		switch (id.toLowerCase()) {
			case "score":
				return String.valueOf(StatsStorage.getUserStats(player, StatsStorage.StatisticType.SCORE));
			case "tours_played":
				return String.valueOf(StatsStorage.getUserStats(player, StatsStorage.StatisticType.TOURS_PLAYED));
			default:
				return handleArenaPlaceholderRequest(id);
		}
	}

	private String handleArenaPlaceholderRequest(String id) {
		if (!id.contains(":")) {
			return null;
		}

		String[] data = id.split(":");
		Arena arena = ArenaRegistry.getArena(data[0]);

		if (arena == null) {
			return null;
		}

		switch (data[1].toLowerCase()) {
			case "players":
				return String.valueOf(arena.getPlayers().size());
			case "king":
				return arena.getKing() == null ? plugin.getChatManager().colorMessage("In-Game.There-Is-No-King") : arena.getKing().getName();
			default:
				return null;
		}
	}
}