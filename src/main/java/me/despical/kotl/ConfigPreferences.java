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

package me.despical.kotl;

import me.despical.commons.serializer.InventorySerializer;
import me.despical.commons.string.StringUtils;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;

/**
 * @author Despical
 * <p>
 * Created at 20.06.2020
 */
public class ConfigPreferences {

	private final Main plugin;
	private final Map<Option, Boolean> options;

	public ConfigPreferences(Main plugin) {
		this.plugin = plugin;
		this.options = new HashMap<>();
		this.loadOptions();
	}

	public boolean getOption(Option option) {
		return options.get(option);
	}

	void loadOptions() {
		this.options.clear();

		final var config = plugin.getConfig();

		for (final var option : Option.values()) {
			options.put(option, config.getBoolean(option.path, option.def));
		}
	}

	public enum Option {

		BECOME_KING_IN_A_ROW("King-Settings.Become-King-In-A-Row"),
		BLOCK_COMMANDS(false),
		BOSS_BAR_ENABLED,
		CHAT_FORMAT_ENABLED,
		CLEAR_EFFECTS,
		CLEAR_INVENTORY(false),
		DAMAGE_ENABLED,
		DATABASE_ENABLED(false),
		DEATH_BLOCKS_ENABLED(false),
		DISABLE_FALL_DAMAGE,
		DISABLE_SEPARATE_CHAT(false),
		FIREWORKS_ON_NEW_KING,
		HEAL_PLAYER((config) -> {
			final var list = config.getStringList("Inventory-Manager.Do-Not-Restore");
			list.forEach(InventorySerializer::addNonSerializableElements);

			return !list.contains("health");
		}),
		INVENTORY_MANAGER_ENABLED("Inventory-Manager.Enabled"),
		JOIN_NOTIFY,
		LEAVE_NOTIFY,
		SCOREBOARD_ENABLED,
		REMOVE_COOLDOWN_ON_JOIN("King-Settings.Remove-Cooldown-On.Join", false),
		REMOVE_COOLDOWN_ON_LEAVE("King-Settings.Remove-Cooldown-On.Leave", false),
		COOLDOWN_WHEN_ALONE("King-Settings.Cooldown-When-Alone", false),
		SHOW_COOLDOWN_ON_REJOIN("King-Settings.Show-Cooldown-If-Rejoin"),
		RESET_COOLDOWNS_ON_NEW_KING("King-Settings.Reset-Cooldowns-On-New-King"),
		APPLY_KING_DELAY_BAR("King-Settings.Cooldown-Bar"),
		COUNT_COOLDOWN_OUTSIDE("King-Settings.Count-Cooldown-Bar-Outside", false),
		SEPARATE_COOLDOWNS("King-Settings.Separate-Cooldowns"),
		UPDATE_GAME_MODE((config) -> !config.getStringList("Inventory-Manager.Do-Not-Restore").contains("game-mode")),
		UPDATE_HUNGER((config) -> !config.getStringList("Inventory-Manager.Do-Not-Restore").contains("hunger")),
		UPDATE_NOTIFIER_ENABLED(false),
		PICK_UP_ITEMS(false);

		private final String path;
		private final boolean def;

		Option() {
			this(true);
		}

		Option(boolean def) {
			this.def = def;
			this.path = StringUtils.capitalize(name().replace('_', '-').toLowerCase(Locale.ENGLISH), '-', '.');
		}

		Option(String path) {
			this(path, true);
		}

		Option(String path, boolean def) {
			this.path = path;
			this.def = def;
		}

		Option(Function<FileConfiguration, Boolean> supplier) {
			this.path = "";
			this.def = supplier.apply(JavaPlugin.getPlugin(Main.class).getConfig());
		}
	}
}