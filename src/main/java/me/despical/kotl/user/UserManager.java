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

package me.despical.kotl.user;

import me.despical.kotl.ConfigPreferences;
import me.despical.kotl.Main;
import me.despical.kotl.user.data.FileStats;
import me.despical.kotl.user.data.MysqlManager;
import me.despical.kotl.user.data.IUserDatabase;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Despical
 * <p>
 * Created at 20.06.2020
 */
public class UserManager {

	private final Set<User> users;
	private final IUserDatabase database;

	public UserManager(Main plugin) {
		this.users = new HashSet<>();
		this.database = plugin.getOption(ConfigPreferences.Option.DATABASE_ENABLED) ? new MysqlManager(plugin) : new FileStats(plugin);

		plugin.getServer().getOnlinePlayers().forEach(this::loadStatistics);
	}

	@NotNull
	public User getUser(Player player) {
		final var uuid = player.getUniqueId();

		for (var user : users) {
			if (user.getUniqueId().equals(uuid)) {
				return user;
			}
		}

		final var user = new User(uuid);
		users.add(user);

		database.loadStatistics(user);
		return user;
	}

	public void loadStatistics(Player player) {
		database.loadStatistics(getUser(player));
	}

	public void removeUser(Player player) {
		users.remove(getUser(player));
	}

	@NotNull
	public IUserDatabase getDatabase() {
		return database;
	}
}