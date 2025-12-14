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

import dev.despical.kotl.KOTL;
import dev.despical.kotl.user.User;

/**
 * @author Despical
 * <p>
 * Created at 20.06.2020
 */
public sealed abstract class Database permits FlatFileStorage, MySQLStorage {

    protected static final KOTL plugin = KOTL.getInstance();

    public abstract void loadData(User user);

    public abstract void saveData(User user);

    public abstract void shutdown();
}
