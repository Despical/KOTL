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

package dev.despical.kotl.database;

import dev.despical.commons.configuration.ConfigUtils;
import dev.despical.commons.database.MySQLDatabase;
import dev.despical.kotl.api.StatisticType;
import dev.despical.kotl.user.User;
import lombok.AccessLevel;
import lombok.Getter;
import org.bukkit.configuration.file.FileConfiguration;

import java.sql.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;

/**
 * @author Despical
 * <p>
 * Created at 20.06.2020
 */
@Getter
public final class MySQLStorage extends Database {

    private String statsTable;
    private MySQLDatabase database;

    @Getter(AccessLevel.NONE)
    private final ExecutorService executor;

    public MySQLStorage() {
        this.executor = Executors.newSingleThreadExecutor();
        this.initializeDatabase();
    }

    private void initializeDatabase() {
        FileConfiguration config = ConfigUtils.getConfig(plugin, "mysql");
        statsTable = config.getString("table");

        executor.submit(() -> {
            database = new MySQLDatabase(config);
            database.setLogger(plugin.getLogger());

            try (Connection connection = database.getConnection();
                 Statement statement = connection.createStatement()
            ) {
                String tableQuery = """
                    CREATE TABLE IF NOT EXISTS `%s`
                    (
                      `UUID`        CHAR(36)    PRIMARY KEY,
                      `name`        VARCHAR(32) NOT NULL,
                      `toursplayed` INT         NOT NULL DEFAULT 0,
                      `kill`        INT         NOT NULL DEFAULT 0,
                      `death`       INT         NOT NULL DEFAULT 0,
                      `score`       INT         NOT NULL DEFAULT 0
                    );
                    """.formatted(statsTable);

                statement.executeUpdate(tableQuery);
            } catch (SQLException exception) {
                plugin.getLogger().log(Level.WARNING, "Could not connect to database.", exception);
            }
        });
    }

    @Override
    public void loadData(User user) {
        executor.submit(() -> {
            try (Connection connection = database.getConnection()) {
                String selectSql = "SELECT * FROM `%s` WHERE `uuid` = ?;".formatted(statsTable);

                try (var selectStatement = connection.prepareStatement(selectSql)) {
                    selectStatement.setString(1, user.getUniqueId().toString());

                    try (ResultSet result = selectStatement.executeQuery()) {
                        if (!result.next()) {
                            String insertSql = "INSERT INTO `%s` (`uuid`, `name`) VALUES (?, ?);".formatted(statsTable);

                            try (var insertStatement = connection.prepareStatement(insertSql)) {
                                insertStatement.setString(1, user.getUniqueId().toString());
                                insertStatement.setString(2, user.getName());
                                insertStatement.executeUpdate();
                            }

                            return;
                        }

                        for (StatisticType type : StatisticType.getPersistentStats()) {
                            user.setStat(type, result.getInt(type.getName()));
                        }
                    }
                }
            } catch (SQLException exception) {
                plugin.getLogger().log(Level.WARNING, "An error occurred while loading stats.", exception);
            }
        });
    }

    @Override
    public void saveData(User user) {
        executor.submit(() -> {
            try (Connection connection = database.getConnection()) {
                StringBuilder builder = new StringBuilder();

                for (StatisticType type : StatisticType.getPersistentStats()) {
                    builder.append('`')
                        .append(type.getName())
                        .append("` = ")
                        .append(user.getStat(type))
                        .append(",");
                }

                String stats = builder.deleteCharAt(builder.length() - 1).toString();
                String sql = "UPDATE %s SET %s WHERE `uuid` = ?;".formatted(statsTable, stats);

                try (PreparedStatement statement = connection.prepareStatement(sql)) {
                    statement.setString(1, user.getUniqueId().toString());
                    statement.executeUpdate();
                }
            } catch (SQLException exception) {
                plugin.getLogger().log(Level.WARNING, "An error occurred while saving stats.", exception);
            }
        });
    }

    @Override
    public void shutdown() {
        executor.shutdown();
        database.shutdownConnPool();
    }
}
