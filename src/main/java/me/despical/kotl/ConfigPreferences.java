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

import me.despical.commons.string.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Despical
 * <p>
 * Created at 20.06.2020
 */
public class ConfigPreferences {

	private final Map<Option, Boolean> options;

	public ConfigPreferences(Main plugin) {
		this.options = new HashMap<>();

		plugin.saveDefaultConfig();

		for (Option option : Option.values()) {
			options.put(option, plugin.getConfig().getBoolean(option.path, option.def));
		}
	}

	public boolean getOption(Option option) {
		return options.get(option);
	}

	public enum Option {

		BLOCK_COMMANDS(false), BOSS_BAR_ENABLED, CHAT_FORMAT_ENABLED, CLEAR_EFFECTS,
		CLEAR_INVENTORY(false), DEBUG_MESSAGES(false), DATABASE_ENABLED(false), DEATH_BLOCKS_ENABLED(false),
		DISABLE_FALL_DAMAGE, DISABLE_SEPARATE_CHAT(false), INVENTORY_MANAGER_ENABLED, JOIN_NOTIFY,
		LEAVE_NOTIFY, REWARDS_ENABLED(false), SCOREBOARD_ENABLED, UPDATE_NOTIFIER_ENABLED(false), SEND_SETUP_TIPS,
		FIREWORKS_ON_NEW_KING;

		final String path;
		final boolean def;

		Option() {
			this(true);
		}

		Option(boolean def) {
			this.def = def;
			this.path = StringUtils.capitalize(name().replace('_', '-').toLowerCase(), '-', '.');
		}
	}
}