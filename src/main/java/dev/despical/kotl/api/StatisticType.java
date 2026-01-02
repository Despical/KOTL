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

package dev.despical.kotl.api;

import dev.despical.kotl.user.User;

/**
 * @author Despical
 * <p>
 * Created at 5.05.2025
 */
public enum StatisticType {

    TOURS_PLAYED("toursplayed"),
    SCORE("score"),
    KILLS("kill"),
    DEATHS("death"),
    LOCAL_RESET_COOLDOWN("local_reset_cooldown", false);

    private static final StatisticType[] PERSISTENT_STATS = {TOURS_PLAYED, SCORE, KILLS, DEATHS};

    private final String name;
    private final boolean persistent;

    StatisticType(String name) {
        this(name, true);
    }

    StatisticType(String name, boolean persistent) {
        this.name = name;
        this.persistent = persistent;
    }

    public String getName() {
        return name;
    }

    public boolean isPersistent() {
        return persistent;
    }

    public String from(User user) {
        return Integer.toString(user.getStat(this));
    }

    public static StatisticType[] getPersistentStats() {
        return PERSISTENT_STATS;
    }
}
