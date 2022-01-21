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
	private final boolean papiEnabled;
	private final Map<Option, Boolean> options = new HashMap<>();

	public ConfigPreferences(Main plugin) {
		this.plugin = plugin;
		this.papiEnabled = plugin.getServer().getPluginManager().isPluginEnabled("PlaceholderAPI");

		loadOptions();
	}

	public boolean getOption(Option option) {
		return options.get(option);
	}

	private void loadOptions() {
		for (Option option : Option.values()) {
			options.put(option, plugin.getConfig().getBoolean(option.getPath(), option.getDefault()));
		}
	}

	public boolean isPapiEnabled() {
		return papiEnabled;
	}

	public enum Option {
		BOSS_BAR_ENABLED("Boss-Bar-Enabled"), CHAT_FORMAT_ENABLED("Chat-Format-Enabled"),
		CLEAR_EFFECTS("Clear-Effects"), CLEAR_INVENTORY("Clear-Inventory", false),
		DATABASE_ENABLED("Database-Activated", false), DEATH_BLOCKS_ENABLED("Death-Blocks.Enabled"),
		DISABLE_FALL_DAMAGE("Disable-Fall-Damage"), INVENTORY_MANAGER_ENABLED("Inventory-Manager"),
		JOIN_NOTIFY("Join-Notify"), LEAVE_NOTIFY("Leave-Notify"), SCOREBOARD_ENABLED("Scoreboard-Enabled", false),
		DISABLE_SEPARATE_CHAT("Disable-Separate-Chat", false), REWARDS_ENABLED("Rewards-Enabled", false);

		private final String path;
		private final boolean def;

		Option(String path) {
			this(path, true);
		}

		Option(String path, boolean def) {
			this.path = path;
			this.def = def;
		}

		public String getPath() {
			return path;
		}

		public boolean getDefault() {
			return def;
		}
	}
}