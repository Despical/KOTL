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

import me.despical.kotl.KOTL;
import me.despical.kotl.api.StatisticType;
import me.despical.kotl.user.User;
import org.jetbrains.annotations.NotNull;

/**
 * @author Despical
 * <p>
 * Created at 20.06.2020
 */
public abstract sealed class UserDatabase permits FileStats, MySQLStatistics {

    @NotNull
    protected final KOTL plugin;

    public UserDatabase(@NotNull KOTL plugin) {
        this.plugin = plugin;
    }

    public abstract void saveStatistic(@NotNull User user, StatisticType statisticType);

    public abstract void saveStatistics(@NotNull User user);

    public abstract void loadStatistics(@NotNull User user);

    public void shutdown() {
    }
}
