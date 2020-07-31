package me.despical.kotl.handler.setup;

import me.despical.commonsbox.serializer.LocationSerializer;
import me.despical.kotl.Main;
import me.despical.kotl.arena.Arena;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * @author Despical
 * <p>
 * Created at 22.06.2020
 */
public class SetupUtilities {

	private Main plugin = JavaPlugin.getPlugin(Main.class);
	private FileConfiguration config;

	SetupUtilities(FileConfiguration config, Arena arena) {
		this.config = config;
	}

	public String isOptionDoneBool(String path) {
		if (config.isSet(path)) {
			if (Bukkit.getServer().getWorlds().get(0).getSpawnLocation().equals(
				LocationSerializer.locationFromString(config.getString(path)))) {
				return plugin.getChatManager().colorRawMessage("&c&l✘ Not Completed");
			}
			return plugin.getChatManager().colorRawMessage("&a&l✔ Completed");
		}
		return plugin.getChatManager().colorRawMessage("&c&l✘ Not Completed");
	}
}