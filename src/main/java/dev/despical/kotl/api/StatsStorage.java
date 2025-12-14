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

package dev.despical.kotl.api;

import dev.despical.commons.configuration.ConfigUtils;
import dev.despical.commons.sorter.SortUtils;
import dev.despical.kotl.KOTL;
import dev.despical.kotl.database.MySQLStorage;
import org.bukkit.configuration.file.FileConfiguration;
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

    private static final KOTL plugin = JavaPlugin.getPlugin(KOTL.class);

    @NotNull
    public static Map<UUID, Integer> getStats(StatisticType stat) {
        if (plugin.getDatabase() instanceof MySQLStorage mySQLStorage) {
            try (Connection connection = mySQLStorage.getDatabase().getConnection();
                 Statement statement = connection.createStatement()
            ) {
                ResultSet set = statement.executeQuery("SELECT UUID, %s FROM %s ORDER BY %s".formatted(stat.getName(), mySQLStorage.getStatsTable(), stat.getName()));

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

        FileConfiguration config = ConfigUtils.getConfig(plugin, "stats");
        final var stats = new LinkedHashMap<UUID, Integer>();

        for (final var string : config.getKeys(false)) {
            stats.put(UUID.fromString(string), config.getInt(string + "." + stat.getName()));
        }

        return SortUtils.sortByValue(stats);
    }

    public static int getUserStats(Player player, StatisticType statisticType) {
        return plugin.getUserManager().getUser(player).getStat(statisticType);
    }
}
