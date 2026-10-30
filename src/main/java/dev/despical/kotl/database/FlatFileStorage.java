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
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;

/**
 * @author Despical
 * <p>
 * Created at 20.06.2020
 */
public final class FlatFileStorage extends Database {

    private final FileConfiguration config = ConfigUtils.getConfig(plugin, "data/stats");
    private final File statsFile = new File(plugin.getDataFolder(), "data/stats.yml");
    private final AtomicBoolean saveQueued = new AtomicBoolean();
    private final AtomicLong saveSequence = new AtomicLong();
    private long writtenSequence;

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
        updateData(user);

        // Every save rewrites the whole stats file, so doing it on the main thread for each quit
        // freezes servers with many stored players. Queue one async write; quits meanwhile share it.
        if (saveQueued.compareAndSet(false, true)) {
            plugin.getServer().getScheduler().runTaskLater(plugin, this::saveConfigAsync, 20L);
        }
    }

    private void updateData(User user) {
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
        return loadOfflineData(player.getUniqueId(), name, path);
    }

    private OfflineStats loadOfflineData(UUID uuid, String name, String path) {
        OfflineStats offlineStats = new OfflineStats(uuid, name);

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

        for (String uuidString : config.getKeys(false)) {
            String path = uuidString + ".";
            String name = config.getString(path + "name");
            if (name == null) continue;

            UUID uuid;
            try {
                uuid = UUID.fromString(uuidString);
            } catch (IllegalArgumentException ignored) {
                continue;
            }

            offlineStats.add(loadOfflineData(uuid, name, path));
        }

        return offlineStats;
    }

    @Override
    public void saveAllData() {
        plugin.getUserManager().getUsers().forEach(this::updateData);

        saveConfig();
    }

    @Override
    public void shutdown() {
        saveAllData();
    }

    private void saveConfig() {
        saveQueued.set(false);
        writeSnapshot(saveSequence.incrementAndGet(), config.saveToString());
    }

    private void saveConfigAsync() {
        saveQueued.set(false);

        long sequence = saveSequence.incrementAndGet();
        String data = config.saveToString();

        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> writeSnapshot(sequence, data));
    }

    private synchronized void writeSnapshot(long sequence, String data) {
        // An older async snapshot must not overwrite a newer one, e.g. the sync save on shutdown
        if (sequence < writtenSequence) {
            return;
        }

        writtenSequence = sequence;

        try {
            Files.writeString(statsFile.toPath(), data, StandardCharsets.UTF_8);
        } catch (IOException exception) {
            plugin.getLogger().log(Level.WARNING, "Could not save data/stats.yml", exception);
        }
    }
}
