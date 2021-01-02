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

package me.despical.kotl;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Despical
 * <p>
 * Created at 20.06.2020
 */
public class ConfigPreferences {

	private final Main plugin;
	private final Map<Option, Boolean> options = new HashMap<>();

	public ConfigPreferences(Main plugin) {
		this.plugin = plugin;

		loadOptions();
	}

	/**
	 * Returns whether option value is true or false
	 *
	 * @param option option to get value from
	 * @return true or false based on user configuration
	 */
	public boolean getOption(Option option) {
		return options.get(option);
	}

	private void loadOptions() {
		for (Option option : Option.values()) {
			options.put(option, plugin.getConfig().getBoolean(option.getPath(), option.getDefault()));
		}
	}

	public enum Option {
		BOSSBAR_ENABLED("Bossbar-Enabled", true), CHAT_FORMAT_ENABLED("ChatFormat-Enabled", true),
		CLEAR_EFFECTS("Clear-Effects", true), CLEAR_INVENTORY("Clear-Inventory", false),
		DATABASE_ENABLED("DatabaseActivated", false), DEATHBLOCKS_ENABLED("Death-Blocks.Enabled", true),
		DISABLE_FALL_DAMAGE("Disable-Fall-Damage", true), INVENTORY_MANAGER_ENABLED("InventoryManager", true),
		JOIN_NOTIFY("Join-Notify", true), LEAVE_NOTIFY("Leave-Notify", true), SCOREBOARD_ENABLED("Scoreboard-Enabled", false),
		DISABLE_SEPARATE_CHAT("Disable-Separate-Chat", false);

		private final String path;
		private final boolean def;

		Option(String path, boolean def) {
			this.path = path;
			this.def = def;
		}

		public String getPath() {
			return path;
		}

		/**
		 * @return default value of option if absent in config
		 */
		public boolean getDefault() {
			return def;
		}
	}
}