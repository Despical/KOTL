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

package dev.despical.kotl.user;

import dev.despical.kotl.KOTL;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * @author Despical
 * <p>
 * Created at 20.06.2020
 */
public class UserManager {

    private final KOTL plugin;
    private final Map<UUID, User> users;

    public UserManager(KOTL plugin) {
        this.plugin = plugin;
        this.users = new HashMap<>();
    }

    public User getUser(Player player) {
        User user = users.get(player.getUniqueId());

        if (user != null) {
            return user;
        }

        return createNewUser(player);
    }

    public void removeUser(UUID uuid) {
        users.remove(uuid);
    }

    public Set<User> getUsers() {
        return Set.copyOf(users.values());
    }

    public User createNewUser(Player player) {
        User user = new User(player);
        plugin.getDatabase().loadData(user);

        users.put(player.getUniqueId(), user);
        return user;
    }
}
