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

package me.despical.kotl.arena.managers;

import me.despical.kotl.KOTL;
import me.despical.kotl.arena.managers.schedulers.ArenaScheduler;
import me.despical.kotl.arena.managers.schedulers.SchedulerOptions;

public class ArenaManager {

    private final SchedulerOptions options;
    private final ArenaScheduler arenaScheduler;

    public ArenaManager(KOTL plugin) {
        final var config = plugin.getConfig();

        this.arenaScheduler = switch (config.getInt("Arena-Schedulers.Type")) {
            case 1 -> ArenaScheduler.GENERAL;
            case 2 -> ArenaScheduler.PER_ARENA;
            default -> ArenaScheduler.EVENT;
        };

        final int interval = config.getInt("Arena-Schedulers.Interval");
        final boolean async = config.getBoolean("Arena-Schedulers.Async");

        this.options = new SchedulerOptions(async, interval);

        arenaScheduler.register(options);
    }

    public ArenaScheduler getArenaScheduler() {
        return arenaScheduler;
    }

    public SchedulerOptions getOptions() {
        return options;
    }
}
