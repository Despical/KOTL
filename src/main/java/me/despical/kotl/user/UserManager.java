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
import me.despical.kotl.user.data.IUserDatabase;
import me.despical.kotl.user.data.MysqlManager;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * @author Despical
 * <p>
 * Created at 20.06.2020
 */
public class UserManager {

	private final Map<UUID, User> users;
	private final IUserDatabase database;

	public UserManager(Main plugin) {
		this.users = new HashMap<>();
		this.database = plugin.getOption(ConfigPreferences.Option.DATABASE_ENABLED) ? new MysqlManager(plugin) : new FileStats(plugin);

		plugin.getServer().getOnlinePlayers().forEach(this::addUser);
	}

	public User addUser(Player player) {
		User user = new User(player);
		users.put(player.getUniqueId(), user);

		database.loadStatistics(user);
		return user;
	}

	@NotNull
	public User getUser(Player player) {
		User user = users.get(player.getUniqueId());

		if (user != null) {
			return user;
		}

		return this.addUser(player);
	}

	public void removeUser(Player player) {
		users.remove(player.getUniqueId());
	}

	@NotNull
	public IUserDatabase getDatabase() {
		return database;
	}
}