/*
 *  KOTL - Don't let others to climb top of the ladders!
 *  Copyright (C) 2020 Despical and contributors
 *  
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *  
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License
 *  along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

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