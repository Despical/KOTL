package me.despical.kotl.handler;

import me.despical.kotl.HookManager;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import me.clip.placeholderapi.PlaceholderAPI;
import me.despical.commonsbox.configuration.ConfigUtils;
import me.despical.kotl.Main;
import me.despical.kotl.arena.Arena;

/**
 * @author Despical
 * <p>
 * Created at 22.06.2020
 */
public class ChatManager {

	private String prefix;
	
	private final Main plugin;
	private FileConfiguration config;
	
	public ChatManager(Main plugin) {
		this.plugin = plugin;
		this.config = ConfigUtils.getConfig(plugin, "messages");
		this.prefix = colorRawMessage(config.getString("In-Game.Plugin-Prefix"));
	}
	
	public String getPrefix() {
		return prefix;
	}
	
	public String colorRawMessage(String message) {
		return ChatColor.translateAlternateColorCodes('&', message);
	}

	public String colorMessage(String message) {
		return ChatColor.translateAlternateColorCodes('&', config.getString(message));
	}
	
	public String colorMessage(String message, Player player) {
		String returnString = config.getString(message);

		if (plugin.getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
			returnString = PlaceholderAPI.setPlaceholders(player, returnString);
		}

		return ChatColor.translateAlternateColorCodes('&', returnString);
	}

	private String formatMessage(Arena arena, String message, Player player) {
		String returnString = message;
		returnString = StringUtils.replace(returnString, "%player%", player.getName());
		returnString = colorRawMessage(formatPlaceholders(returnString, arena));

		if (plugin.getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
			returnString = PlaceholderAPI.setPlaceholders(player, returnString);
		}

		return returnString;
	}

	private String formatPlaceholders(String message, Arena arena) {
		String returnString = message;
		returnString = StringUtils.replace(returnString, "%arena%", arena.getId());
		returnString = StringUtils.replace(returnString, "%players%", Integer.toString(arena.getPlayers().size()));
		returnString = StringUtils.replace(returnString, "%king%", arena.getKing() == null ? colorMessage("In-Game.There-Is-No-King") : arena.getKing().getName());
		return returnString;
	}
	
	public void broadcastMessage(Arena a, String msg) {
		a.getPlayers().forEach(p -> p.sendMessage(prefix + msg));
	}

	public void broadcastAction(Arena a, Player p, ActionType action) {
		String message;

		switch (action) {
			case JOIN:
				message = formatMessage(a, colorMessage("In-Game.Join"), p);
				break;
			case LEAVE:
				message = formatMessage(a, colorMessage("In-Game.Leave"), p);
				break;
			case NEW_KING:
				message = formatMessage(a, colorMessage("In-Game.New-King"), p);
				if (plugin.getHookManager().isFeatureEnabled(HookManager.HookFeature.HOLOGRAPHIC_DISPLAYS)) {
					a.getHologram().getLine(0).removeLine();
					a.getHologram().insertTextLine(0, formatMessage(a, colorMessage("In-Game.Last-King-Hologram"), p));
				}
				break;
			default:
				return;
		}

		broadcastMessage(a, prefix + message);
	}
	
	public void reloadConfig() {
		config = ConfigUtils.getConfig(plugin, "messages");
		prefix = colorRawMessage(config.getString("In-Game.Plugin-Prefix"));
	}

	public enum ActionType {
		JOIN, LEAVE, NEW_KING
	}
}