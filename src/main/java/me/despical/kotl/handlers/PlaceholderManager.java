/*
 * KOTL - Don't let others to climb top of the ladders!
 * Copyright (C) 2023 Despical
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

package me.despical.kotl.handlers;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.despical.kotl.Main;
import me.despical.kotl.api.StatsStorage;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * @author Despical
 * <p>
 * Created at 22.06.2020
 */
public class PlaceholderManager extends PlaceholderExpansion {
	
	private final Main plugin;

	public PlaceholderManager(Main plugin) {
		this.plugin = plugin;

		register();
	}
	
	@Override
	public boolean persist() {
		return true;
	}

	@NotNull
	@Override
	public String getIdentifier() {
		return "kotl";
	}

	@NotNull
	@Override
	public String getAuthor() {
		return "Despical";
	}

	@NotNull
	@Override
	public String getVersion() {
		return plugin.getDescription().getVersion();
	}

	public String onPlaceholderRequest(Player player, @NotNull String id) {
		if (player == null) return null;

		final var user = plugin.getUserManager().getUser(player);

		return switch (id.toLowerCase()) {
			case "score" -> Integer.toString(user.getStat(StatsStorage.StatisticType.SCORE));
			case "tours_played" -> Integer.toString(user.getStat(StatsStorage.StatisticType.TOURS_PLAYED));
			default -> handleArenaPlaceholderRequest(id);
		};
	}

	private String handleArenaPlaceholderRequest(String id) {
		final var data = id.split(":");
		final var arena = plugin.getArenaRegistry().getArena(data[0]);

		if (arena == null) return null;

		return switch (data[1].toLowerCase()) {
			case "players" -> Integer.toString(arena.getPlayers().size());
			case "king" -> arena.getKingName();
			default -> null;
		};
	}
}