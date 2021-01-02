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

package me.despical.kotl.user.data;

import me.despical.commonsbox.configuration.ConfigUtils;
import me.despical.commonsbox.database.MysqlDatabase;
import me.despical.kotl.Main;
import me.despical.kotl.api.StatsStorage;
import me.despical.kotl.user.User;
import me.despical.kotl.utils.Debugger;
import me.despical.kotl.utils.MessageUtils;
import org.bukkit.Bukkit;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * @author Despical
 * <p>
 * Created at 20.06.2020
 */
public class MysqlManager implements UserDatabase {

	private final Main plugin;
	private final MysqlDatabase database;

	public MysqlManager(Main plugin) {
		this.plugin = plugin;
		database = plugin.getMysqlDatabase();

		Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
			try (Connection connection = database.getConnection()) {
				Statement statement = connection.createStatement();
				statement.executeUpdate("CREATE TABLE IF NOT EXISTS `" + getTableName() + "` (\n"
					+ "  `UUID` char(36) NOT NULL PRIMARY KEY,\n"
					+ "  `name` varchar(32) NOT NULL,\n"
					+ "  `score` int(11) NOT NULL DEFAULT '0',\n"
					+ "  `toursplayed` int(11) NOT NULL DEFAULT '0'\n"
					+ ");");
			} catch (SQLException e) {
				e.printStackTrace();
				MessageUtils.errorOccurred();
				Debugger.sendConsoleMessage("Cannot save contents to MySQL database!");
				Debugger.sendConsoleMessage("Check configuration of mysql.yml file or disable mysql option in config.yml");
			}
		});
	}

	@Override
	public void saveStatistic(User user, StatsStorage.StatisticType stat) {
		Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
			database.executeUpdate("UPDATE " + getTableName() + " SET " + stat.getName() + "=" + user.getStat(stat)+ " WHERE UUID='" + user.getPlayer().getUniqueId().toString() + "';");
			Debugger.debug("Executed MySQL: " + "UPDATE " + getTableName() + " SET " + stat.getName() + "=" + user.getStat(stat) + " WHERE UUID='" + user.getPlayer().getUniqueId().toString() + "';");
		});
	}

	@Override
	public void saveAllStatistic(User user) {
		StringBuilder update = new StringBuilder(" SET ");

		for (StatsStorage.StatisticType stat : StatsStorage.StatisticType.values()) {
			if (!stat.isPersistent()) continue;
			if (update.toString().equalsIgnoreCase(" SET ")) {
				update.append(stat.getName()).append("=").append(user.getStat(stat));
			}

			update.append(", ").append(stat.getName()).append("=").append(user.getStat(stat));
		}

		String finalUpdate = update.toString();
		Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> database.executeUpdate("UPDATE " + getTableName() + finalUpdate + " WHERE UUID='" + user.getPlayer().getUniqueId().toString() + "';"));
	}

	@Override
	public void loadStatistics(User user) {
		Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
			String uuid = user.getPlayer().getUniqueId().toString();

			try (Connection connection = database.getConnection()) {
				Statement statement = connection.createStatement();
				ResultSet rs = statement.executeQuery("SELECT * from " + getTableName() + " WHERE UUID='" + uuid + "';");

				if (rs.next()) {
					Debugger.debug("MySQL Stats | Player {0} already exist. Getting Stats...", user.getPlayer().getName());

					for (StatsStorage.StatisticType stat : StatsStorage.StatisticType.values()) {
						if (!stat.isPersistent()) continue;
						int val = rs.getInt(stat.getName());
						user.setStat(stat, val);
					}
				} else {
					Debugger.debug("MySQL Stats | Player {0} does not exist. Creating new one...", user.getPlayer().getName());
					statement.executeUpdate("INSERT INTO " + getTableName() + " (UUID,name) VALUES ('" + uuid + "','" + user.getPlayer().getName() + "');");

					for (StatsStorage.StatisticType stat : StatsStorage.StatisticType.values()) {
						if (!stat.isPersistent()) continue;
						user.setStat(stat, 0);
					}
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		});
	}
	
	public String getTableName() {
		return ConfigUtils.getConfig(plugin, "mysql").getString("table", "playerstats");
	}

	public MysqlDatabase getDatabase() {
		return database;
	}
}