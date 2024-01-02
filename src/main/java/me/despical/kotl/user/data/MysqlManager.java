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

package me.despical.kotl.user.data;

import me.despical.commons.configuration.ConfigUtils;
import me.despical.commons.database.MysqlDatabase;
import me.despical.kotl.Main;
import me.despical.kotl.api.StatsStorage;
import me.despical.kotl.user.User;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;

/**
 * @author Despical
 * <p>
 * Created at 20.06.2020
 */
public non-sealed class MysqlManager extends IUserDatabase {

	private final String table;

	private MysqlDatabase database;

	public MysqlManager(Main plugin) {
		super(plugin);
		this.table = ConfigUtils.getConfig(plugin, "mysql").getString("table", "kotl_stats");

		plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
			this.database = plugin.getMysqlDatabase();

			try (final var connection = database.getConnection()) {
				final var statement = connection.createStatement();

				statement.executeUpdate("""
						CREATE TABLE IF NOT EXISTS `%s` (
						  `UUID` char(36) NOT NULL PRIMARY KEY,
						  `name` varchar(32) NOT NULL,
						  `toursplayed` int(11) NOT NULL DEFAULT '0',
						  `score` int(11) NOT NULL DEFAULT '0'
						);""".formatted(table));
			} catch (SQLException exception) {
				exception.printStackTrace();

				plugin.getLogger().severe("Couldn't create statistics table on MySQL database!");
			}
		});
	}

	@Override
	public void saveStatistic(@NotNull User user, StatsStorage.StatisticType stat) {
		plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> database.executeUpdate("UPDATE %s SET %s=%d WHERE UUID='%s';".formatted(table, stat.getName(), user.getStat(stat), user.getUniqueId().toString())));
	}

	@Override
	public void saveStatistics(@NotNull User user) {
		final var update = new StringBuilder(" SET ");

		for (final var stat : StatsStorage.StatisticType.values()) {
			if (!stat.isPersistent()) continue;

			final var value = user.getStat(stat);
			final var name = stat.getName();

			if (update.toString().equalsIgnoreCase(" SET ")) {
				update.append(name).append("=").append(value);
			}

			update.append(", ").append(name).append("=").append(value);
		}

		final var finalUpdate = update.toString();

		plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> database.executeUpdate("UPDATE %s%s WHERE UUID='%s';".formatted(table, finalUpdate, user.getUniqueId().toString())));
	}

	@Override
	public void loadStatistics(@NotNull User user) {
		final String uuid = user.getUniqueId().toString(), name = user.getPlayer().getName();

		plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
			try (final var connection = database.getConnection()) {
				final var statement = connection.createStatement();
				final var rs = statement.executeQuery("SELECT * from %s WHERE UUID='%s';".formatted(table, uuid));

				if (rs.next()) {
					for (StatsStorage.StatisticType stat : StatsStorage.StatisticType.values()) {
						if (!stat.isPersistent()) continue;

						user.setStat(stat, rs.getInt(stat.getName()));
					}
				} else {
					statement.executeUpdate("INSERT INTO %s (UUID,name) VALUES ('%s','%s');".formatted(table, uuid, name));

					for (final var stat : StatsStorage.StatisticType.values()) {
						if (!stat.isPersistent()) continue;

						user.setStat(stat, 0);
					}
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		});
	}
	
	@NotNull
	public MysqlDatabase getDatabase() {
		return database;
	}

	public String getTable() {
		return table;
	}
}