/*
 * KOTL - Don't let others to climb top of the ladders!
 * Copyright (C) 2023 Despical
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

package me.despical.kotl;

import me.despical.commons.serializer.InventorySerializer;
import me.despical.commons.string.StringUtils;
import me.despical.commons.util.function.DoubleSupplier;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Despical
 * <p>
 * Created at 20.06.2020
 */
public class ConfigPreferences {

	private final static Main plugin = JavaPlugin.getPlugin(Main.class);

	private final Map<Option, Boolean> options;

	public ConfigPreferences() {
		this.options = new HashMap<>();

		plugin.saveDefaultConfig();

		this.loadOptions();
	}

	public boolean getOption(Option option) {
		return options.get(option);
	}

	public void loadOptions() {
		this.options.clear();

		final var config = plugin.getConfig();

		for (Option option : Option.values()) {
			options.put(option, config.getBoolean(option.path, option.def));
		}
	}

	public enum Option {

		BECOME_KING_IN_A_ROW(false), BLOCK_COMMANDS(false), BOSS_BAR_ENABLED, CHAT_FORMAT_ENABLED,
		CLEAR_EFFECTS, CLEAR_INVENTORY(false), DATABASE_ENABLED(false), DEATH_BLOCKS_ENABLED(false),
		DISABLE_FALL_DAMAGE, DISABLE_SEPARATE_CHAT(false), FIREWORKS_ON_NEW_KING, INVENTORY_MANAGER_ENABLED("Inventory-Manager.Enabled"),
		JOIN_NOTIFY, LEAVE_NOTIFY, SCOREBOARD_ENABLED, UPDATE_NOTIFIER_ENABLED(false),
		HEAL_PLAYER((config) -> {
			final var list = config.getStringList("Inventory-Manager.Do-Not-Restore");
			list.forEach(InventorySerializer::addNonSerializableElements);

			return !list.contains("health");
		});

		final String path;
		final boolean def;

		Option() {
			this(true);
		}

		Option(boolean def) {
			this.def = def;
			this.path = StringUtils.capitalize(name().replace('_', '-').toLowerCase(), '-', '.');
		}

		Option(String path) {
			this.def = true;
			this.path = path;
		}

		Option(DoubleSupplier<FileConfiguration, Boolean> supplier) {
			this.path = "";
			this.def = supplier.accept(plugin.getConfig());
		}
	}
}