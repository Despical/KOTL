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
import me.despical.kotl.KOTL;
import me.despical.kotl.api.StatsStorage;
import me.despical.kotl.user.User;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;

/**
 * @author Despical
 * <p>
 * Created at 20.06.2020
 */
public non-sealed class FileStats extends UserDatabase {

    private final FileConfiguration config;

    public FileStats(KOTL plugin) {
        super(plugin);
        this.config = ConfigUtils.getConfig(plugin, "stats");
    }

    @Override
    public void saveStatistic(@NotNull User user, StatsStorage.StatisticType statisticType) {
        config.set(user.getUniqueId().toString() + "." + statisticType.getName(), user.getStat(statisticType));

        ConfigUtils.saveConfig(plugin, config, "stats");
    }

    @Override
    public void saveStatistics(@NotNull User user) {
        String uuid = user.getUniqueId().toString();

        for (StatsStorage.StatisticType stat : StatsStorage.StatisticType.PERSISTENT_STATS) {
            config.set(uuid + "." + stat.getName(), user.getStat(stat));
        }

        ConfigUtils.saveConfig(plugin, config, "stats");
    }

    @Override
    public void loadStatistics(@NotNull User user) {
        String uuid = user.getUniqueId().toString();

        for (StatsStorage.StatisticType stat : StatsStorage.StatisticType.PERSISTENT_STATS) {
            user.setStat(stat, config.getInt(uuid + "." + stat.getName()));
        }
    }
}
