/*
 * KOTL - Don't let others to climb top of the ladders!
 * Copyright (C) 2022 Despical
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

package me.despical.kotl.user;

import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import me.despical.kotl.Main;
import me.despical.kotl.api.StatsStorage;
import me.despical.kotl.api.events.player.KOTLPlayerStatisticChangeEvent;
import me.despical.kotl.arena.Arena;
import me.despical.kotl.arena.ArenaRegistry;

/**
 * @author Despical
 * <p>
 * Created at 20.06.2020
 */
public class User {

	private static final Main plugin = JavaPlugin.getPlugin(Main.class);

	private final UUID uuid;
	private final Player player;
	private final Map<StatsStorage.StatisticType, Integer> stats;

	public User(UUID uuid) {
		this.uuid = uuid;
		this.player = plugin.getServer().getPlayer(uuid);
		this.stats = new EnumMap<>(StatsStorage.StatisticType.class);
	}

	public Arena getArena() {
		return ArenaRegistry.getArena(player);
	}

	public Player getPlayer() {
		return player;
	}

	public UUID getUniqueId() {
		return uuid;
	}

	public int getStat(StatsStorage.StatisticType statisticType) {
		Integer statistic = stats.get(statisticType);

		if (statistic == null) {
			stats.put(statisticType, 0);
			return 0;
		}

		return statistic;
	}

	public void setStat(StatsStorage.StatisticType stat, int value) {
		stats.put(stat, value);

		plugin.getServer().getScheduler().runTask(plugin, () -> plugin.getServer().getPluginManager().callEvent(new KOTLPlayerStatisticChangeEvent(getArena(), player, stat, value)));
	}

	public void addStat(StatsStorage.StatisticType stat, int value) {
		setStat(stat, getStat(stat) + value);
	}
}