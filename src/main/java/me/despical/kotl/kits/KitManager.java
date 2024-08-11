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

package me.despical.kotl.kits;

import me.despical.commons.configuration.ConfigUtils;
import me.despical.kotl.Main;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;

public class KitManager {

	private boolean isEnabled;

	private final Main plugin;
	private final Set<Kit> kits;

	public KitManager(final Main plugin) {
		this.plugin = plugin;
		this.kits = new HashSet<>();
		this.loadKits();
	}

	public void loadKits() {
		this.kits.clear();

		final var config = ConfigUtils.getConfig(plugin, "kits");

		this.isEnabled = config.getBoolean("kits-enabled");

		final var section = config.getConfigurationSection("kits");

		if (section == null) {
			plugin.getLogger().warning("Section ''kits'' not found in kits.yml!");
			return;
		}

		for (final var path : section.getKeys(false)) {
			this.kits.add(new Kit(config, "kits." + path + "."));
		}
	}

	public void giveKit(final Player player) {
		if (!isEnabled) return;

		final var kit = kits.stream().filter(k -> k.hasPermission(player)).findFirst().orElse(null);

		if (kit == null) return;

		kit.giveKit(player);
	}
}