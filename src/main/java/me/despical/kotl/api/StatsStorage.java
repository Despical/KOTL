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

package me.despical.kotl.api;

import me.despical.commons.configuration.ConfigUtils;
import me.despical.commons.sorter.SortUtils;
import me.despical.kotl.ConfigPreferences;
import me.despical.kotl.Main;
import me.despical.kotl.user.data.MysqlManager;
import me.despical.kotl.utils.Debugger;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.logging.Level;

/**
 * @author Despical
 * @since 1.0.0
 * <p>
 */
public class StatsStorage {

	private static final Main plugin = JavaPlugin.getPlugin(Main.class);

	public static Map<UUID, Integer> getStats(StatisticType stat) {
		if (plugin.getConfigPreferences().getOption(ConfigPreferences.Option.DATABASE_ENABLED)) {
			try (Connection connection = plugin.getMysqlDatabase().getConnection()) {
				Statement statement = connection.createStatement();
				ResultSet set = statement.executeQuery("SELECT UUID, " + stat.getName() + " FROM " + ((MysqlManager) plugin.getUserManager().getDatabase()).getTableName() + " ORDER BY " + stat.getName());
				Map<UUID, Integer> column = new HashMap<>();

				while (set.next()) {
					column.put(UUID.fromString(set.getString("UUID")), set.getInt(stat.getName()));
				}

				return column;
			} catch (SQLException e) {
				Debugger.debug(Level.WARNING, "SQL Exception occurred! " + e.getSQLState() + " (" + e.getErrorCode() + ")");
				Debugger.sendConsoleMessage("&cCannot get contents from MySQL database!");
				Debugger.sendConsoleMessage("&cCheck configuration of mysql.yml file or disable mysql option in config.yml");
				return null;
			}
		}

		FileConfiguration config = ConfigUtils.getConfig(plugin, "stats");
		Map<UUID, Integer> stats = new HashMap<>();

		for (String string : config.getKeys(false)) {
			stats.put(UUID.fromString(string), config.getInt(string + "." + stat.getName()));
		}

		return SortUtils.sortByValue(stats);
	}

	public static int getUserStats(Player player, StatisticType statisticType) {
		return plugin.getUserManager().getUser(player).getStat(statisticType);
	}

	public enum StatisticType {
		TOURS_PLAYED("toursplayed"), SCORE("score");

		private final String name;
		private final boolean persistent;

		StatisticType(String name) {
			this(name, true);
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