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

import me.despical.kotl.Main;
import me.despical.kotl.api.StatsStorage;
import me.despical.kotl.user.User;
import org.jetbrains.annotations.NotNull;

/**
 * @author Despical
 * <p>
 * Created at 20.06.2020
 */
public abstract sealed class IUserDatabase permits FileStats, MysqlManager {

	@NotNull
	protected final Main plugin;

	public IUserDatabase(final @NotNull Main plugin) {
		this.plugin = plugin;
	}

	public abstract void saveStatistic(final @NotNull User user, final StatsStorage.StatisticType statisticType);

	public abstract void saveStatistics(final @NotNull User user);

	public abstract void loadStatistics(final @NotNull User user);
}