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
import dev.despical.kotl.stats.StatisticType;
import dev.despical.kotl.stats.Statistics;
import dev.despical.kotl.stats.offline.OfflineStats;
import dev.despical.kotl.user.User;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Despical
 * <p>
 * Created at 20.06.2020
 */
public final class FlatFileStorage extends Database {

    private final FileConfiguration config = ConfigUtils.getConfig(plugin, "data/stats");

    @Override
    public void loadData(User user) {
        String path = user.getUUID() + ".";

        for (StatisticType<?> type : Statistics.getPersistentStats()) {
            loadSingleStat(user, path, type);
        }
    }

    private <T> void loadSingleStat(User user, String path, StatisticType<T> type) {
        String fullPath = path + "stats." + type.getKey();

        if (config.contains(fullPath)) {
            Object rawValue = config.get(fullPath);
            T value = type.deserialize(rawValue);

            user.loadStatistic(type, value);
            return;
        }

        user.loadStatistic(type, type.getDefaultValue());
    }

    @Override
    public void saveData(User user) {
        String path = user.getUUID() + ".";
        config.set(path + "name", user.getName());

        for (StatisticType<?> type : Statistics.getPersistentStats()) {
            saveSingleStat(user, path, type);
        }
    }

    private <T> void saveSingleStat(User user, String path, StatisticType<T> type) {
        T value = user.getStatistic(type);
        Object serializedValue = type.serialize(value);

        config.set(path + "stats." + type.getKey(), serializedValue);
    }

    @Override
    @Nullable
    public OfflineStats loadOfflineData(OfflinePlayer player) {
        String path = player.getUniqueId() + ".";
        if (!config.contains(path + "name")) return null;

        String name = config.getString(path + "name");
        OfflineStats offlineStats = new OfflineStats(player.getUniqueId(), name);

        for (StatisticType<?> type : Statistics.getPersistentStats()) {
            loadSingleOfflineStat(offlineStats, path, type);
        }

        return offlineStats;
    }

    private <T> void loadSingleOfflineStat(OfflineStats offlineStats, String path, StatisticType<T> type) {
        String fullPath = path + "stats." + type.getKey();

        if (config.contains(fullPath)) {
            Object rawValue = config.get(fullPath);
            T value = type.deserialize(rawValue);

            offlineStats.setStat(type, value);
            return;
        }

        offlineStats.setStat(type, type.getDefaultValue());
    }

    @Override
    public Set<OfflineStats> getAllPlayers() {
        Set<OfflineStats> offlineStats = new HashSet<>();

        for (String uuid : config.getKeys(false)) {
            String name = config.getString(uuid + ".name");
            if (name == null) continue;

            OfflinePlayer player = Bukkit.getOfflinePlayerIfCached(name);
            if (player == null) continue;

            OfflineStats stats = loadOfflineData(player);
            if (stats != null) {
                offlineStats.add(stats);
            }
        }

        return offlineStats;
    }

    @Override
    public void saveAllData() {
        plugin.getUserManager().getUsers().forEach(this::saveData);
    }

    @Override
    public void shutdown() {
        for (User user : plugin.getUserManager().getUsers()) {
            saveData(user);
        }

        ConfigUtils.saveConfig(plugin, config, "data/stats");
    }
}
