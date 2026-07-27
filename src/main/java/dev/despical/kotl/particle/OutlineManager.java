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

package dev.despical.kotl.particle;

import dev.despical.kotl.KOTL;
import dev.despical.kotl.arena.Arena;
import dev.despical.kotl.arena.options.ArenaKeys;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author Despical
 * <p>
 * Created at 06.06.2026
 */
public class OutlineManager {

    private final KOTL plugin;
    private final Map<String, BukkitTask> tasks;

    public OutlineManager(KOTL plugin) {
        this.plugin = plugin;
        this.tasks = new HashMap<>();
    }

    public void handleOutlines(Arena arena) {
        hideOutlines(arena);
        if (arena.getOption(ArenaKeys.SHOW_OUTLINES)) {
            showOutlines(arena);
        }
    }

    public void showOutlines(Arena arena) {
        if (tasks.containsKey(arena.getId()) || arena.getOption(ArenaKeys.MIN_CORNER) == null || arena.getOption(ArenaKeys.MAX_CORNER) == null) {
            return;
        }

        OutlineTask task = new OutlineTask(plugin, arena);
        BukkitTask bukkitTask = task.runTaskTimer(plugin, 20, 1);
        tasks.put(arena.getId(), bukkitTask);
    }

    public void hideOutlines(Arena arena) {
        BukkitTask task = tasks.remove(arena.getId());
        if (task != null) {
            task.cancel();
        }
    }

    public void refreshAll(Set<Arena> arenas) {
        cancelAll();
        arenas.forEach(this::handleOutlines);
    }

    public void cancelAll() {
        tasks.values().forEach(BukkitTask::cancel);
        tasks.clear();
    }
}
