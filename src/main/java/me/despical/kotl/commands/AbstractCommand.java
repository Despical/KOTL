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
		final Class<?>[] commandClasses = new Class[] {AdminCommands.class, PlayerCommands.class};

		for (Class<?> clazz : commandClasses) {
			try {
				clazz.getConstructor(Main.class).newInstance(plugin);
			} catch (Exception exception) {
				exception.fillInStackTrace();
			}
		}
	}
}