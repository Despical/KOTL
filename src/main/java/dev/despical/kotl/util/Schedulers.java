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

package dev.despical.kotl.util;

import dev.despical.kotl.KOTL;
import lombok.NoArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitScheduler;

/**
 * @author Despical
 * <p>
 * Created at 05.06.2026
 */
@NoArgsConstructor
public final class Schedulers {

    private static final KOTL plugin = KOTL.getInstance();
    private static final BukkitScheduler scheduler = Bukkit.getScheduler();

    public static void runInTheNextTick(Runnable runnable) {
        scheduler.runTask(plugin, runnable);
    }

    public static void runTaskLater(Runnable runnable, long delay) {
        scheduler.runTaskLater(plugin, runnable, delay);
    }

    public static void runTaskAsynchronously(Runnable runnable) {
        scheduler.runTaskAsynchronously(plugin, runnable);
    }

    public static void runTaskTimerAsynchronously(Runnable runnable, long delay, long period) {
        scheduler.runTaskTimerAsynchronously(plugin, runnable, delay, period);
    }

    public static void runTaskTimer(Runnable runnable, long delay, long period) {
        scheduler.runTaskTimer(plugin, runnable, delay, period);
    }
}
