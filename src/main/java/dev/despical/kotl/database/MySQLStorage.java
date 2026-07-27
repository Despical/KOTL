/*
 * KOTL - Don't let others climb to top of the ladders!
 * Copyright (C) 2026  Berke Akçen
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
import dev.despical.kotl.stats.StatisticType;
import dev.despical.kotl.stats.Statistics;
import dev.despical.kotl.stats.offline.OfflineStats;
import dev.despical.kotl.user.User;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;

import java.sql.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

/**
 * @author Despical
 * <p>
 * Created at 20.06.2020
 */
public final class MySQLStorage extends Database {

    private final String statsTable;
    private final MySQLDatabase database;
    private final ExecutorService executor;

    private final String selectQuery;
    private final String insertQuery;
    private final String upsertQuery;

    public MySQLStorage() {
        this.executor = Executors.newSingleThreadExecutor();

        FileConfiguration config = ConfigUtils.getConfig(plugin, "mysql");
        this.statsTable = config.getString("stats-table");
        this.database = new MySQLDatabase(config);
        this.database.setLogger(plugin.getLogger());

        this.selectQuery = "SELECT * FROM `%s` WHERE `uuid` = ?;".formatted(statsTable);
        this.insertQuery = "INSERT INTO `%s` (`uuid`, `name`) VALUES (?, ?);".formatted(statsTable);
        this.upsertQuery = buildUpsertQuery();

        this.initializeDatabase();
    }

    private String buildUpsertQuery() {
        StringBuilder columnBuilder = new StringBuilder("`uuid`, `name`");
        StringBuilder valuesBuilder = new StringBuilder("?, ?");
        StringBuilder updateBuilder = new StringBuilder("`name` = VALUES(`name`)");

        for (StatisticType<?> type : Statistics.getPersistentStats()) {
            columnBuilder.append(", `").append(type.getKey()).append("`");
            valuesBuilder.append(", ?");
            updateBuilder.append(", `").append(type.getKey()).append("` = VALUES(`").append(type.getKey()).append("`)");
        }

        return "INSERT INTO `%s` (%s) VALUES (%s) ON DUPLICATE KEY UPDATE %s;"
            .formatted(statsTable, columnBuilder, valuesBuilder, updateBuilder);
    }

    private void initializeDatabase() {
        executor.submit(() -> {
            try (Connection connection = database.getConnection();
                 Statement statement = connection.createStatement()
            ) {
                StringBuilder columns = new StringBuilder();
                for (StatisticType<?> type : Statistics.getPersistentStats()) {
                    columns.append(",\n    `").append(type.getKey()).append("` ").append(getColumnDefinition(type));
                }

                String tableQuery = """
                    CREATE TABLE IF NOT EXISTS `%s` (
                        `uuid` CHAR(36) PRIMARY KEY,
                        `name` VARCHAR(16) NOT NULL%s
                    );
                    """.formatted(statsTable, columns.toString());

                statement.executeUpdate(tableQuery);
                ensureMissingColumns(connection);
            } catch (SQLException exception) {
                plugin.getLogger().log(Level.WARNING, "Database initialization failed.", exception);
            }
        });
    }

    private void ensureMissingColumns(Connection connection) throws SQLException {
        Set<String> existingColumns = new HashSet<>();

        try (ResultSet columns = connection.getMetaData().getColumns(connection.getCatalog(), null, statsTable, null)) {
            while (columns.next()) {
                existingColumns.add(columns.getString("COLUMN_NAME").toLowerCase(Locale.ENGLISH));
            }
        }

        List<String> missingColumns = new ArrayList<>();
        for (StatisticType<?> type : Statistics.getPersistentStats()) {
            if (!existingColumns.contains(type.getKey().toLowerCase(Locale.ENGLISH))) {
                missingColumns.add("ADD COLUMN `" + type.getKey() + "` " + getColumnDefinition(type));
            }
        }

        if (missingColumns.isEmpty()) {
            return;
        }

        String alterTableQuery = "ALTER TABLE `" + statsTable + "` " + String.join(", ", missingColumns) + ";";
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(alterTableQuery);
        }

        plugin.getLogger().info("MySQL stats table updated with new statistic columns: " + missingColumns.size());
    }

    private String getColumnDefinition(StatisticType<?> type) {
        if (type.getType() == Integer.class) {
            return "INT NOT NULL DEFAULT " + type.getDefaultValue();
        }

        return "LONGTEXT";
    }

    @Override
    public void loadData(User user) {
        executor.submit(() -> {
            try (Connection connection = database.getConnection();
                 PreparedStatement selectStatement = connection.prepareStatement(selectQuery)
            ) {
                selectStatement.setString(1, user.getUUID().toString());

                try (ResultSet result = selectStatement.executeQuery()) {
                    if (!result.next()) {
                        try (PreparedStatement insertStatement = connection.prepareStatement(insertQuery)) {
                            insertStatement.setString(1, user.getUUID().toString());
                            insertStatement.setString(2, user.getName());
                            insertStatement.executeUpdate();
                        }
                        return;
                    }

                    for (StatisticType<?> type : Statistics.getPersistentStats()) {
                        applyStatToUser(user, type, result);
                    }
                }
            } catch (SQLException exception) {
                plugin.getLogger().log(Level.WARNING, "An error occurred while loading stats.", exception);
            }
        });
    }

    private <T> void applyStatToUser(User user, StatisticType<T> type, ResultSet result) throws SQLException {
        Object rawValue = result.getObject(type.getKey());

        if (rawValue != null) {
            user.loadStatistic(type, type.deserialize(rawValue));
        } else {
            user.loadStatistic(type, type.getDefaultValue());
        }
    }
    @Override
    public OfflineStats loadOfflineData(OfflinePlayer player) {
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(selectQuery)
        ) {
            statement.setString(1, player.getUniqueId().toString());

            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) {
                    return null;
                }

                return mapResultSetToOfflineStats(result);
            }
        } catch (SQLException exception) {
            plugin.getLogger().log(Level.WARNING, "Failed to load offline stats for " + player.getName(), exception);
            return null;
        }
    }

    @Override
    public Set<OfflineStats> getAllPlayers() {
        Set<OfflineStats> allStats = new HashSet<>();
        String query = "SELECT * FROM `%s`".formatted(statsTable);

        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(query);
             ResultSet result = statement.executeQuery()) {

            while (result.next()) {
                allStats.add(mapResultSetToOfflineStats(result));
            }
        } catch (SQLException exception) {
            plugin.getLogger().log(Level.WARNING, "Failed to load all offline stats.", exception);
        }

        return allStats;
    }

    private OfflineStats mapResultSetToOfflineStats(ResultSet result) throws SQLException {
        UUID uuid = UUID.fromString(result.getString("uuid"));
        String name = result.getString("name");
        OfflineStats offlineStats = new OfflineStats(uuid, name);

        for (StatisticType<?> type : Statistics.getPersistentStats()) {
            applyStatToOfflineStats(offlineStats, type, result);
        }

        return offlineStats;
    }

    private <T> void applyStatToOfflineStats(OfflineStats offlineStats, StatisticType<T> type, ResultSet result) throws SQLException {
        Object rawValue = result.getObject(type.getKey());

        if (rawValue != null) {
            offlineStats.setStat(type, type.deserialize(rawValue));
        } else {
            offlineStats.setStat(type, type.getDefaultValue());
        }
    }

    @Override
    public void saveData(User user) {
        executor.submit(() -> {
            try (Connection connection = database.getConnection();
                 PreparedStatement statement = connection.prepareStatement(upsertQuery)
            ) {
                applySaveParameters(statement, user);
                statement.executeUpdate();
            } catch (SQLException exception) {
                plugin.getLogger().log(Level.WARNING, "An error occurred while saving stats.", exception);
            }
        });
    }

    @Override
    public CompletableFuture<Void> saveAllData() {
        List<User> activeUsers = List.copyOf(plugin.getUserManager().getUsers());
        if (activeUsers.isEmpty()) {
            return CompletableFuture.completedFuture(null);
        }

        return CompletableFuture.runAsync(() -> {
            try (Connection connection = database.getConnection();
                 PreparedStatement statement = connection.prepareStatement(upsertQuery)
            ) {
                boolean originalAutoCommit = connection.getAutoCommit();
                connection.setAutoCommit(false);

                try {
                    for (User user : activeUsers) {
                        applySaveParameters(statement, user);
                        statement.addBatch();
                    }

                    statement.executeBatch();
                    connection.commit();
                } catch (SQLException exception) {
                    connection.rollback();
                    plugin.getLogger().log(Level.WARNING, "Failed to execute batch data update.", exception);
                } finally {
                    connection.setAutoCommit(originalAutoCommit);
                }
            } catch (SQLException exception) {
                plugin.getLogger().log(Level.WARNING, "Database connection error during 'save all' operation.", exception);
            }
        }, executor);
    }


    private void applySaveParameters(PreparedStatement statement, User user) throws SQLException {
        int index = 1;
        statement.setString(index++, user.getUUID().toString());
        statement.setString(index++, user.getName());

        for (StatisticType<?> type : Statistics.getPersistentStats()) {
            index = setSingleStatToStatement(user, type, statement, index);
        }
    }

    private <T> int setSingleStatToStatement(User user, StatisticType<T> type, PreparedStatement statement, int index) throws SQLException {
        T value = user.getStatistic(type);
        Object serializedValue = type.serialize(value);

        statement.setObject(index++, serializedValue);
        return index;
    }

    @Override
    public void shutdown() {
        saveAllData();
        executor.shutdown();

        try {
            if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
                plugin.getLogger().log(Level.WARNING, "Database operations timed out after 10 seconds. Forcing shutdown.");
                executor.shutdownNow();
            }
        } catch (InterruptedException exception) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        } finally {
            database.shutdownConnPool();
        }
    }
}
