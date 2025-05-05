/*
 * KOTL - Don't let others climb to top of the ladders!
 * Copyright (C) 2025  Berke Akçen
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
import me.despical.kotl.api.StatisticType;
import me.despical.kotl.user.User;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;

/**
 * @author Despical
 * <p>
 * Created at 20.06.2020
 */
public final class MySQLStatistics extends UserDatabase {

    private final String table;
    private final MySQLDatabase database;
    private final ExecutorService executor;

    public MySQLStatistics(KOTL plugin) {
        super(plugin);

        FileConfiguration config = ConfigUtils.getConfig(plugin, "mysql");
        this.table = config.getString("table", "kotl_stats");
        this.database = new MySQLDatabase(plugin, config);
        this.executor = Executors.newSingleThreadExecutor();

        executor.submit(() -> {
            try (Connection connection = database.getConnection();
                 Statement statement = connection.createStatement()
            ) {
                statement.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS `%s`
                    (
                      `UUID`        CHAR(36)    PRIMARY KEY,
                      `name`        VARCHAR(32) NOT NULL,
                      `toursplayed` INT         NOT NULL DEFAULT 0,
                      `kill`        INT         NOT NULL DEFAULT 0,
                      `death`       INT         NOT NULL DEFAULT 0,
                      `score`       INT         NOT NULL DEFAULT 0
                    );""".formatted(table));
            } catch (SQLException exception) {
                plugin.getLogger().log(Level.SEVERE, "Could not create the statistics table!", exception);
            }
        });
    }

    @Override
    public void saveStatistic(@NotNull User user, StatisticType stat) {
        executor.submit(() -> {
            String query = "UPDATE `%s` SET `%s`='%d' WHERE `UUID`='%s';".formatted(table, stat.getName(), user.getStat(stat), user.getUniqueId());

            database.executeUpdate(query);
        });
    }

    @Override
    public void saveStatistics(@NotNull User user) {
        executor.submit(() -> {
            StringBuilder update = new StringBuilder(" SET ");

            for (StatisticType stat : StatisticType.getPersistentStats()) {
                update
                    .append('`')
                    .append(stat.getName())
                    .append('`')
                    .append('=')
                    .append(user.getStat(stat))
                    .append(',');
            }

            update.deleteCharAt(update.length() - 1);

            database.executeUpdate("UPDATE `%s` SET %s WHERE UUID='%s';".formatted(table, update.toString(), user.getUniqueId().toString()));
        });
    }

    @Override
    public void loadStatistics(@NotNull User user) {

        executor.submit(() -> {
            try (Connection connection = database.getConnection();
                 Statement statement = connection.createStatement()
            ) {
                String uuid = user.getUniqueId().toString();
                ResultSet resultSet = statement.executeQuery("SELECT * from %s WHERE UUID='%s';".formatted(table, uuid));

                if (resultSet.next()) {
                    for (StatisticType stat : StatisticType.getPersistentStats()) {
                        user.setStat(stat, resultSet.getInt(stat.getName()));
                    }

                    return;
                }

                String query = "INSERT INTO `%s` (`UUID`, `name`) VALUES ('%s', '%s')".formatted(table, uuid, user.getName());
                statement.executeUpdate(query);
            } catch (SQLException exception) {
                plugin.getLogger().log(Level.SEVERE, "An exception occurred while loading statistics", exception);
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
        executor.shutdown();
        database.shutdownConnPool();
    }
}
