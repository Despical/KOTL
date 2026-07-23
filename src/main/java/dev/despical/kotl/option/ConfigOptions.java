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

package dev.despical.kotl.option;

import dev.despical.kotl.KOTL;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

/**
 * @author Despical
 * <p>
 * Created at 20.06.2020
 */
public class ConfigOptions {

    private final KOTL plugin;
    private final Map<ConfigOption<?>, Object> options;

    public ConfigOptions(KOTL plugin) {
        this.plugin = plugin;
        this.options = new HashMap<>();
        this.loadOptions();
    }

    public <T> T get(ConfigOption<T> option) {
        return option.getType().cast(options.computeIfAbsent(option, opt -> option.getDefaultValue()));
    }

    public boolean isEnabled(BooleanOption option) {
        return this.<Boolean>get(option);
    }

    public void reloadOptions() {
        plugin.reloadConfig();

        loadOptions();
    }

    private void loadOptions() {
        FileConfiguration config = plugin.getConfig();

        Stream.of(BooleanOption.values(), IntOption.values())
            .flatMap(Arrays::stream)
            .forEach(option -> options.put(option, config.get(option.getPath())));
    }
}
