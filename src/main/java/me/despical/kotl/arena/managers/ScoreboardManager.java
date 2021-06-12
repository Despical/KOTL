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

package me.despical.kotl.arena.managers;

import me.clip.placeholderapi.PlaceholderAPI;
import me.despical.commons.scoreboard.ScoreboardLib;
import me.despical.commons.scoreboard.common.EntryBuilder;
import me.despical.commons.scoreboard.type.Entry;
import me.despical.commons.scoreboard.type.Scoreboard;
import me.despical.commons.scoreboard.type.ScoreboardHandler;
import me.despical.kotl.Main;
import me.despical.kotl.api.StatsStorage;
import me.despical.kotl.arena.Arena;
import org.apache.commons.lang.StringUtils;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ScoreboardManager {

	private final Arena arena;
	private final Main plugin;
	private final Set<Scoreboard> scoreboards;

	public ScoreboardManager(Arena arena, Main plugin) {
		this.arena = arena;
		this.plugin = plugin;
		this.scoreboards = new HashSet<>();
	}

	public void createScoreboard(Player player) {
		Scoreboard scoreboard = ScoreboardLib.createScoreboard(player).setHandler(new ScoreboardHandler() {

			@Override
			public String getTitle(Player player) {
				return plugin.getChatManager().message("Scoreboard.Title");
			}

			@Override
			public List<Entry> getEntries(Player player) {
				return formatScoreboard(player);
			}
		});

		scoreboard.activate();
		scoreboards.add(scoreboard);
	}

	public void removeScoreboard(Player player) {
		for (Scoreboard board : scoreboards) {
			if (board.getHolder().equals(player)) {
				scoreboards.remove(board);
				board.deactivate();
				return;
			}
		}
	}

	public void stopAllScoreboards() {
		scoreboards.forEach(Scoreboard::deactivate);
		scoreboards.clear();
	}

	private List<Entry> formatScoreboard(Player player) {
		EntryBuilder builder = new EntryBuilder();

		for (String line : plugin.getChatManager().getStringList("Scoreboard.Content.Playing")) {
			builder.next(formatScoreboardLine(line, player));
		}

		return builder.build();
	}

	private String formatScoreboardLine(String line, Player player) {
		String formattedLine = line;

		formattedLine = StringUtils.replace(formattedLine, "%arena%", arena.getId());
		formattedLine = StringUtils.replace(formattedLine, "%players%", Integer.toString(arena.getPlayers().size()));
		formattedLine = StringUtils.replace(formattedLine, "%king%", arena.getKingName());
		formattedLine = StringUtils.replace(formattedLine, "%score%", Integer.toString(StatsStorage.getUserStats(player, StatsStorage.StatisticType.SCORE)));
		formattedLine = StringUtils.replace(formattedLine, "%tours_played%", String.valueOf(StatsStorage.getUserStats(player, StatsStorage.StatisticType.TOURS_PLAYED)));

		if (plugin.getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
			formattedLine = PlaceholderAPI.setPlaceholders(player, formattedLine);
		}

		return plugin.getChatManager().coloredRawMessage(formattedLine);
	}
}