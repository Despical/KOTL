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

package me.despical.kotl.user.data;

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

	private MysqlDatabase database;

	public MysqlManager(Main plugin) {
		super(plugin);

		plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
			this.database = plugin.getMysqlDatabase();

			this.checkInitializedAndSleep();

			try (final var connection = database.getConnection()) {
				final var statement = connection.createStatement();
				statement.executeUpdate("""
					CREATE TABLE IF NOT EXISTS `playerstats` (
					  `UUID` char(36) NOT NULL PRIMARY KEY,
					  `name` varchar(32) NOT NULL,
					  `score` int(11) NOT NULL DEFAULT '0',
					  `toursplayed` int(11) NOT NULL DEFAULT '0'
					);""");
			} catch (SQLException exception) {
				exception.printStackTrace();

				plugin.getLogger().severe("Cannot save contents to MySQL database!");
			}
		});
	}

	@Override
	public void saveStatistic(@NotNull User user, StatsStorage.StatisticType stat) {
		plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
			this.checkInitializedAndSleep();

			database.executeUpdate("UPDATE playerstats SET " + stat.getName() + "=" + user.getStat(stat)+ " WHERE UUID='" + user.getUniqueId().toString() + "';");
		});
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

		plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
			this.checkInitializedAndSleep();

			database.executeUpdate("UPDATE playerstats" + finalUpdate + " WHERE UUID='" + user.getUniqueId().toString() + "';");
		});
	}

	@Override
	public void loadStatistics(@NotNull User user) {
		final String uuid = user.getUniqueId().toString(), name = user.getPlayer().getName();
		plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
			this.checkInitializedAndSleep();

			try (final var connection = database.getConnection()) {
				final var statement = connection.createStatement();
				final var rs = statement.executeQuery("SELECT * from playerstats WHERE UUID='%s';".formatted(uuid));

				if (rs.next()) {
					for (StatsStorage.StatisticType stat : StatsStorage.StatisticType.values()) {
						if (!stat.isPersistent()) continue;

						user.setStat(stat, rs.getInt(stat.getName()));
					}
				} else {
					statement.executeUpdate("INSERT INTO playerstats (UUID,name) VALUES ('%s','%s');".formatted(uuid, name));

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

	private void checkInitializedAndSleep() {
		try {
			if (plugin.getMysqlDatabase() == null || this.database == null) {
				Thread.sleep(5000L);

				if (plugin.getMysqlDatabase() != null) this.database = plugin.getMysqlDatabase();
			}
		} catch (InterruptedException exception) {
			throw new RuntimeException(exception);
		}
	}
}