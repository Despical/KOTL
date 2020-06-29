package me.despical.kotl.events;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import me.despical.commonsbox.serializer.InventorySerializer;
import me.despical.kotl.ConfigPreferences;
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
	public void onJoin(PlayerJoinEvent event) {
		plugin.getUserManager().loadStatistics(plugin.getUserManager().getUser(event.getPlayer()));
		if (plugin.getConfigPreferences().getOption(ConfigPreferences.Option.INVENTORY_MANAGER_ENABLED)) {
			InventorySerializer.loadInventory(plugin, event.getPlayer());
		}
	}
	
	@EventHandler
	public void onJoinCheckVersion(final PlayerJoinEvent event) {
		if (!plugin.getConfig().getBoolean("Update-Notifier.Enabled", true)
			|| !event.getPlayer().hasPermission("kotl.updatenotify")) {
			return;
		}
		Bukkit.getScheduler().runTaskLater(plugin, () -> UpdateChecker.init(plugin, 80686).requestUpdateCheck().whenComplete((result, exception) -> {
			if (!result.requiresUpdate()) {
				return;
			}
			if (result.getNewestVersion().contains("b")) {
				event.getPlayer().sendMessage(plugin.getChatManager().colorRawMessage("&3[KOTL] &bFound a beta update: v" + result.getNewestVersion() + " Download"));
				event.getPlayer().sendMessage(plugin.getChatManager().colorRawMessage("&3>> &bhttps://www.spigotmc.org/resources/king-of-the-ladder-1-8-3-1-16-1.80686"));
			} else {
				event.getPlayer().sendMessage(plugin.getChatManager().colorRawMessage("&3[KOTL] &bFound an update: v" + result.getNewestVersion() + " Download:"));
				event.getPlayer().sendMessage(plugin.getChatManager().colorRawMessage("&3>> &bhttps://www.spigotmc.org/resources/king-of-the-ladder-1-8-3-1-16-1.80686"));
			}
		}), 25);
	}
}