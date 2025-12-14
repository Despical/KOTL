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

package dev.despical.kotl.options;

import dev.despical.kotl.KOTL;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Despical
 * <p>
 * Created at 20.06.2020
 */
public class ConfigOptions {

    private final KOTL plugin;
    private final Map<Option, Object> options;

    public ConfigOptions(KOTL plugin) {
        this.plugin = plugin;
        this.options = new HashMap<>();

        loadOptions();
    }

    public boolean isEnabled(Option option) {
        return (boolean) options.get(option);
    }

    public int getValue(Option option) {
        return (int) options.get(option);
    }

    public void loadOptions() {
        options.clear();

        FileConfiguration config = plugin.getConfig();

        for (Option option : Option.values()) {
            if (option.path.isEmpty()) {
                options.put(option, option.defaultValue);
                continue;
            }

            options.put(option, config.get(option.path, option.defaultValue));
        }
    }
}
