/*
 * KOTL - Don't let others to climb top of the ladders!
 * Copyright (C) 2021 Despical and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package me.despical.kotl.user.data;

import me.despical.kotl.Main;
import me.despical.kotl.api.StatsStorage;
import me.despical.kotl.user.User;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * @author Despical
 * <p>
 * Created at 20.06.2020
 */
public interface UserDatabase {

	Main plugin = JavaPlugin.getPlugin(Main.class);

	/**
	 * Saves player statistic into yaml or MySQL storage based on user choice
	 *
	 * @param user user to retrieve statistic from
	 * @param stat stat to save to storage
	 */
	void saveStatistic(User user, StatsStorage.StatisticType stat);

	/**
	 * Saves player statistic into yaml or MySQL storage based on user choice
	 *
	 * @param user user to retrieve statistic from
	 */
	void saveAllStatistic(User user);

	/**
	 * Loads player statistic from yaml or MySQL storage based on user choice
	 *
	 * @param user user to load statistic for
	 */
	void loadStatistics(User user);
}