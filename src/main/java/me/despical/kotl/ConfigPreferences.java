package me.despical.kotl;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Despical
 * <p>
 * Created at 20.06.2020
 */
public class ConfigPreferences {

	private Main plugin;
	private Map<Option, Boolean> options = new HashMap<>();

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
		CHAT_FORMAT_ENABLED("ChatFormat-Enabled", true), DATABASE_ENABLED("DatabaseActivated", false),
		INVENTORY_MANAGER_ENABLED("InventoryManager", true), DISABLE_FALL_DAMAGE("Disable-Fall-Damage", true), 
		JOIN_NOTIFY("Join-Notify", true), LEAVE_NOTIFY("Leave-Notify", true), BOSSBAR_ENABLED("Bossbar-Enabled", true),
		SCOREBOARD_ENABLED("Scoreboard-Enabled", false), DEATHBLOCKS_ENABLED("Death-Blocks.Enabled", true),
		CLEAR_INVENTORY("Clear-Inventory", true);

		private String path;
		private boolean def;

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