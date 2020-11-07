/*
 *  KOTL - Don't let others to climb top of the ladders!
 *  Copyright (C) 2020 Despical and contributors
 *  
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *  
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License
 *  along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package me.despical.kotl.user;

import java.util.EnumMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import me.despical.kotl.Main;
import me.despical.kotl.api.StatsStorage;
import me.despical.kotl.api.events.player.KOTLPlayerStatisticChangeEvent;
import me.despical.kotl.arena.Arena;
import me.despical.kotl.arena.ArenaRegistry;
import org.bukkit.scoreboard.ScoreboardManager;

/**
 * @author Despical
 * <p>
 * Created at 20.06.2020
 */
public class User {

	private final Main plugin = JavaPlugin.getPlugin(Main.class);
	private final ScoreboardManager scoreboardManager = Bukkit.getScoreboardManager();
	private final Player player;
	private final Map<StatsStorage.StatisticType, Integer> stats = new EnumMap<>(StatsStorage.StatisticType.class);

	public User(Player player) {
		this.player = player;
	}

	public Arena getArena() {
		return ArenaRegistry.getArena(player);
	}

	public Player getPlayer() {
		return player;
	}

	public int getStat(StatsStorage.StatisticType stat) {
		if (!stats.containsKey(stat)) {
			stats.put(stat, 0);

			return 0;
		} else if (stats.get(stat) == null) {
			return 0;
		}

		return stats.get(stat);
	}

	public void setStat(StatsStorage.StatisticType stat, int i) {
		stats.put(stat, i);

		Bukkit.getScheduler().runTask(plugin, () -> {
			KOTLPlayerStatisticChangeEvent playerStatisticChangeEvent = new KOTLPlayerStatisticChangeEvent(getArena(), player, stat, i);
			Bukkit.getPluginManager().callEvent(playerStatisticChangeEvent);
		});
	}

	public void addStat(StatsStorage.StatisticType stat, int i) {
		stats.put(stat, getStat(stat) + i);

		Bukkit.getScheduler().runTask(plugin, () -> {
			KOTLPlayerStatisticChangeEvent playerStatisticChangeEvent = new KOTLPlayerStatisticChangeEvent(getArena(), player, stat, getStat(stat));
			Bukkit.getPluginManager().callEvent(playerStatisticChangeEvent);
		});
	}

	public void removeScoreboard() {
		player.setScoreboard(scoreboardManager.getNewScoreboard());
	}
}