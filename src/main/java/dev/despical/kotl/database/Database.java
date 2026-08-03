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

import dev.despical.kotl.KOTL;
import dev.despical.kotl.stats.offline.OfflineStats;
import dev.despical.kotl.user.User;
import org.bukkit.OfflinePlayer;

import java.util.Set;

/**
 * @author Despical
 * <p>
 * Created at 20.06.2020
 */
public sealed abstract class Database permits FlatFileStorage, MySQLStorage {

    protected static final KOTL plugin = KOTL.getInstance();

    public abstract void loadData(User user);

    public abstract OfflineStats loadOfflineData(OfflinePlayer player);

    public abstract Set<OfflineStats> getAllPlayers();

    public abstract void saveData(User user);

    public abstract void saveAllData();

    public abstract void shutdown();
}
