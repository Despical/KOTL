package me.despical.kotl.command;

import me.despical.commons.configuration.ConfigUtils;
import me.despical.kotl.Main;
import me.despical.kotl.handler.ChatManager;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * @author Despical
 * <p>
 * Created at 27.06.2022
 */
public abstract class SubCommand {

	private String permission;
	private final String name;

	protected final Main plugin;
	protected final FileConfiguration config;
	protected final ChatManager chatManager;

	public SubCommand(String name) {
		this.name = name;
		this.plugin = JavaPlugin.getPlugin(Main.class);
		this.config = ConfigUtils.getConfig(plugin, "arenas");
		this.chatManager = plugin.getChatManager();
	}

	public String getName() {
		return name;
	}

	public void setPermission(String permission) {
		this.permission = permission;
	}

	public final boolean hasPermission(CommandSender sender) {
		return permission == null || sender.hasPermission(permission);
	}

	public abstract String getPossibleArguments();

	public abstract int getMinimumArguments();

	public abstract void execute(CommandSender sender, String label, String[] args);

	public abstract String getTutorial();

	public abstract CommandType getType();

	public abstract SenderType getSenderType();

	public enum CommandType {
		GENERIC, HIDDEN
	}

	public enum SenderType {
		PLAYER, BOTH
	}
}