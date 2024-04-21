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

package me.despical.kotl.api;

import me.despical.commons.configuration.ConfigUtils;
import me.despical.commons.sorter.SortUtils;
import me.despical.kotl.Main;
import me.despical.kotl.user.data.MysqlManager;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author Despical
 * @since 1.0.0
 * <p>
 * Created at 20.06.2020
 */
public class StatsStorage {

	private static final Main plugin = JavaPlugin.getPlugin(Main.class);

	@NotNull
	public static Map<UUID, Integer> getStats(StatisticType stat) {
		if (plugin.getUserManager().getDatabase() instanceof MysqlManager mysqlManager) {
			try (Connection connection = plugin.getMysqlDatabase().getConnection()) {
				final Statement statement = connection.createStatement();
				final ResultSet set = statement.executeQuery("SELECT UUID, %s FROM %s ORDER BY %s".formatted(stat.name, mysqlManager.getTable(), stat.name));

				final var column = new LinkedHashMap<UUID, Integer>();

				while (set.next()) {
					column.put(UUID.fromString(set.getString("UUID")), set.getInt(stat.getName()));
				}

				return column;
			} catch (SQLException e) {
				plugin.getLogger().warning("SQLException occurred during getting statistics from database!");
				return new LinkedHashMap<>();
			}
		}

		final var config = ConfigUtils.getConfig(plugin, "stats");
		final var stats = new LinkedHashMap<UUID, Integer>();

		for (final var string : config.getKeys(false)) {
			stats.put(UUID.fromString(string), config.getInt(string + "." + stat.getName()));
		}

		return SortUtils.sortByValue(stats);
	}

	public static int getUserStats(Player player, StatisticType statisticType) {
		return plugin.getUserManager().getUser(player).getStat(statisticType);
	}

	public enum StatisticType {

		TOURS_PLAYED("toursplayed"),
		SCORE("score"),
		LOCAL_COOLDOWN("local_cooldown", false),
		LOCAL_RESET_COOLDOWN("local_reset_cooldown", false);

		final String name;
		final boolean persistent;

		StatisticType(String name) {
			this (name, true);
		}

		StatisticType(String name, boolean persistent) {
			this.name = name;
			this.persistent = persistent;
		}

		public String getName() {
			return name;
		}

		public boolean isPersistent() {
			return persistent;
		}
	}
}