/*
 * KOTL - Don't let others to climb top of the ladders!
 * Copyright (C) 2021 Despical and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package me.despical.kotl.handlers.setup;

import me.despical.commons.configuration.ConfigUtils;
import me.despical.commons.serializer.LocationSerializer;
import me.despical.kotl.Main;
import org.bukkit.configuration.file.FileConfiguration;

/**
 * @author Despical
 * <p>
 * Created at 22.06.2020
 */
public class SetupUtilities {

	private final FileConfiguration config;
	
	SetupUtilities(Main plugin) {
		this.config = ConfigUtils.getConfig(plugin, "arenas");
	}

	public String isOptionDoneBool(String path) {
		return config.isSet(path) ? LocationSerializer.isDefaultLocation(config.getString(path)) ? "&c&l✘ Not Completed" : "&a&l✔ Completed" : "&c&l✘ Not Completed";
	}
}