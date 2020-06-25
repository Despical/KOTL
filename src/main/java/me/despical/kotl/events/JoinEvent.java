package me.despical.kotl.events;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import me.despical.kotl.Main;
import me.despical.kotl.utils.UpdateChecker;

/**
 * @author Despical
 * <p>
 * Created at 23.06.2020
 */
public class JoinEvent implements Listener {

	private Main plugin;

	public JoinEvent(Main plugin) {
		this.plugin = plugin;
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}

	@EventHandler
	public void onJoinCheckVersion(final PlayerJoinEvent event) {
		if (!plugin.getConfig().getBoolean("Update-Notifier.Enabled", true)
			|| !event.getPlayer().hasPermission("kotl.updatenotify")) {
			return;
		}
		Bukkit.getScheduler().runTaskLater(plugin, () -> UpdateChecker.init(plugin, 1).requestUpdateCheck().whenComplete((result, exception) -> {
		if (!result.requiresUpdate()) {
			return;
		}
		if (result.getNewestVersion().contains("b")) {
			event.getPlayer().sendMessage(ChatColor.DARK_AQUA + "[KOTL] " + ChatColor.AQUA + "Found an beta update: v" + result.getNewestVersion());
			event.getPlayer().sendMessage(ChatColor.AQUA + "Update is available at: NOT IMPLEMENTED YET");
		} else {
			event.getPlayer().sendMessage(ChatColor.DARK_AQUA + "[KOTL] " + ChatColor.AQUA + "Found an update: v" + result.getNewestVersion());
			event.getPlayer().sendMessage(ChatColor.AQUA + "Software is ready for update! Download it to keep with latest changes and fixes.");
			event.getPlayer().sendMessage(ChatColor.AQUA + "Update is available at: NOT IMPLEMENTED YET");
			}
		}), 25);
	}
}