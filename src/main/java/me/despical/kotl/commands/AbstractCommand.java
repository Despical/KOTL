package me.despical.kotl.commands;

import me.despical.kotl.Main;
import me.despical.kotl.handlers.ChatManager;

/**
 * @author Despical
 * <p>
 * Created at 18.02.2023
 */
public abstract class AbstractCommand {

	protected final Main plugin;
	protected final ChatManager chatManager;

	public AbstractCommand(final Main plugin) {
		this.plugin = plugin;
		this.chatManager = plugin.getChatManager();
		this.plugin.getCommandFramework().registerCommands(this);
	}

	public static void registerCommands(final Main plugin) {
		new PlayerCommands(plugin);
		new AdminCommands(plugin);
	}
}