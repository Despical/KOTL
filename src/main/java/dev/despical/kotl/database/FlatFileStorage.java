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
import dev.despical.kotl.api.StatisticType;
import dev.despical.kotl.user.User;
import org.bukkit.configuration.file.FileConfiguration;

/**
 * @author Despical
 * <p>
 * Created at 20.06.2020
 */
public final class FlatFileStorage extends Database {

    private final FileConfiguration config = ConfigUtils.getConfig(plugin, "stats");

    @Override
    public void loadData(User user) {
        String uuid = user.getUniqueId() + ".";

        for (StatisticType type : StatisticType.getPersistentStats()) {
            int value = config.getInt(uuid + type.getName(), 0);

            user.setStat(type, value);
        }
    }

    @Override
    public void saveData(User user) {
        String uuid = user.getUniqueId() + ".";

        for (StatisticType type : StatisticType.getPersistentStats()) {
            config.set(uuid + type.getName(), user.getStat(type));
        }
    }

    @Override
    public void shutdown() {
        plugin.getUserManager().getUsers().forEach(this::saveData);

        ConfigUtils.saveConfig(plugin, config, "stats");
    }
}
