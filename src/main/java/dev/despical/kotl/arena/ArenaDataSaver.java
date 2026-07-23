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

package dev.despical.kotl.arena;

import dev.despical.commons.configuration.ConfigUtils;
import dev.despical.kotl.KOTL;
import dev.despical.kotl.arena.options.ArenaKeys;
import dev.despical.kotl.arena.options.ArenaOption;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.Set;

/**
 * @author Despical
 * <p>
 * Created at 05.06.2026
 */
public class ArenaDataSaver {

    private final KOTL plugin;

    public ArenaDataSaver(KOTL plugin) {
        this.plugin = plugin;
    }

    public void saveAllArenas() {
        ArenaRegistry arenaRegistry = plugin.getArenaRegistry();
        Set<Arena> arenas = arenaRegistry.getArenas();

        FileConfiguration config = arenaRegistry.getConfig();
        for (Arena arena : arenas) {
            saveArenaData(arena, config);
        }

        ConfigUtils.saveConfig(plugin, config, "arenas");
    }

    private void saveArenaData(Arena arena, FileConfiguration config) {
        String rootPath = arena.getId() + ".";

        for (ArenaOption<?> option : ArenaKeys.getPersistentKeys()) {
            saveSingleOption(arena, config, rootPath, option);
        }
    }

    private <T> void saveSingleOption(Arena arena, FileConfiguration config, String rootPath, ArenaOption<T> option) {
        T value = arena.getOption(option);
        Object serializedValue = option.serialize(value);

        config.set(rootPath + option.getKey(), serializedValue);
    }
}
