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
import me.despical.commons.database.MySQLDatabase;
import me.despical.kotl.KOTL;
import me.despical.kotl.api.StatsStorage;
import me.despical.kotl.user.User;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * @author Despical
 * <p>
 * Created at 20.06.2020
 */
public non-sealed class MysqlManager extends UserDatabase {

    private final String table;
    private final MySQLDatabase database;

    public MysqlManager(KOTL plugin) {
        super(plugin);
        this.table = ConfigUtils.getConfig(plugin, "mysql").getString("table", "kotl_stats");
        this.database = new MySQLDatabase(plugin, "mysql");

        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try (Connection connection = database.getConnection()) {
                Statement statement = connection.createStatement();

                statement.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS `%s` (
                      `UUID` char(36) NOT NULL PRIMARY KEY,
                      `name` varchar(32) NOT NULL,
                      `toursplayed` int(11) NOT NULL DEFAULT 0,
                      `kill` int(11) NOT NULL DEFAULT 0,
                      `death` int(11) NOT NULL DEFAULT 0,
                      `score` int(11) NOT NULL DEFAULT 0
                    );""".formatted(table));
            } catch (SQLException exception) {
                exception.printStackTrace();
            }
        });
    }

    @Override
    public void saveStatistic(@NotNull User user, StatsStorage.StatisticType stat) {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> database.executeUpdate("UPDATE %s SET %s=%d WHERE UUID='%s';".formatted(table, stat.getName(), user.getStat(stat), user.getUniqueId().toString())));
    }

    @Override
    public void saveStatistics(@NotNull User user) {
        StringBuilder update = new StringBuilder(" SET ");

        for (StatsStorage.StatisticType stat : StatsStorage.StatisticType.PERSISTENT_STATS) {
            int value = user.getStat(stat);
            String name = stat.getName();

            if (update.toString().equalsIgnoreCase(" SET ")) {
                update.append(name).append("=").append(value);
            }

            update.append(", ").append(name).append("=").append(value);
        }

        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> database.executeUpdate("UPDATE %s%s WHERE UUID='%s';".formatted(table, update.toString(), user.getUniqueId().toString())));
    }

    @Override
    public void loadStatistics(@NotNull User user) {
        String uuid = user.getUniqueId().toString(), name = user.getName();

        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try (Connection connection = database.getConnection()) {
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery("SELECT * from %s WHERE UUID='%s';".formatted(table, uuid));

                if (resultSet.next()) {
                    for (StatsStorage.StatisticType stat : StatsStorage.StatisticType.PERSISTENT_STATS) {
                        user.setStat(stat, resultSet.getInt(stat.getName()));
                    }
                } else {
                    statement.executeUpdate("INSERT INTO %s (UUID,name) VALUES ('%s','%s');".formatted(table, uuid, name));

                    for (StatsStorage.StatisticType stat : StatsStorage.StatisticType.PERSISTENT_STATS) {
                        user.setStat(stat, 0);
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    @NotNull
    public MySQLDatabase getDatabase() {
        return database;
    }

    @NotNull
    public String getTable() {
        return table;
    }

    @Override
    public void shutdown() {
        this.database.shutdownConnPool();
    }
}
