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

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;

import me.despical.commonsbox.serializer.LocationSerializer;

/**
 * @author Despical
 * <p>
 * Created at 22.06.2020
 */
public class SetupUtilities {

	private final FileConfiguration config;
	
	SetupUtilities(FileConfiguration config) {
		this.config = config;
	}

	public String isOptionDoneBool(String path) {
		if (config.isSet(path)) {
			if (Bukkit.getServer().getWorlds().get(0).getSpawnLocation().equals(LocationSerializer.locationFromString(config.getString(path)))) {
				return "&c&l✘ Not Completed";
			}

			return "&a&l✔ Completed";
		}

		return "&c&l✘ Not Completed";
	}
}