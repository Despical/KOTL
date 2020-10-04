package me.despical.kotl.events;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import me.despical.kotl.Main;
import me.despical.kotl.arena.ArenaRegistry;
import me.despical.kotl.handlers.ChatManager.ActionType;
import me.despical.kotl.user.User;

/**
 * @author Despical
 * <p>
 * Created at 23.06.2020
 */
public class QuitEvent implements Listener {

	private final Main plugin;

	public QuitEvent(Main plugin) {
		this.plugin = plugin;

		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}

	@EventHandler
	public void onQuit(PlayerQuitEvent event) {
		Player player = event.getPlayer();

		if (ArenaRegistry.isInArena(player)) {
			plugin.getChatManager().broadcastAction(ArenaRegistry.getArena(player), player, ActionType.LEAVE);

			ArenaRegistry.getArena(player).getPlayers().remove(player);
		}

		User user = plugin.getUserManager().getUser(player);
		plugin.getUserManager().removeUser(user);
	}
}