package me.despical.kotl.command;

import me.despical.commons.configuration.ConfigUtils;
import me.despical.kotl.Main;
import me.despical.kotl.handler.ChatManager;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public interface CommandImpl {

	Main plugin = JavaPlugin.getPlugin(Main.class);
	ChatManager chatManager = plugin.getChatManager();
	FileConfiguration config = ConfigUtils.getConfig(plugin, "arenas");

	default void register(Object object) {
		plugin.getCommandFramework().registerCommands(object);
	}

	static void registerCommands() {
		new PlayerCommands();
		new AdminCommands();
		new TabCompletion();
	}
}