/*
 * KOTL - Don't let others to climb top of the ladders!
 * Copyright (C) 2021 Despical and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package me.despical.kotl.event;

import me.despical.commons.serializer.InventorySerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import me.despical.kotl.ConfigPreferences;
import me.despical.kotl.Main;
import me.despical.kotl.util.UpdateChecker;

/**
 * @author Despical
 * <p>
 * Created at 23.06.2020
 */
public class JoinEvent implements Listener {

	private final Main plugin;

	public JoinEvent(Main plugin) {
		this.plugin = plugin;

		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}
	
	@EventHandler
	public void onJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();

		plugin.getUserManager().loadStatistics(player);

		if (plugin.getConfigPreferences().getOption(ConfigPreferences.Option.INVENTORY_MANAGER_ENABLED)) {
			InventorySerializer.loadInventory(plugin, player);
		}
	}
	
	@EventHandler
	public void onJoinCheckVersion(PlayerJoinEvent event) {
		Player player = event.getPlayer();

		if (!plugin.getConfig().getBoolean("Update-Notifier.Enabled", true) || !player.hasPermission("kotl.updatenotify")) {
			return;
		}

		plugin.getServer().getScheduler().runTaskLater(plugin, () -> UpdateChecker.init(plugin, 80686).requestUpdateCheck().whenComplete((result, exception) -> {
			if (!result.requiresUpdate()) {
				return;
			}

			if (result.getNewestVersion().contains("b")) {
				player.sendMessage(plugin.getChatManager().coloredRawMessage("&3[KOTL] &bFound a beta update: v" + result.getNewestVersion() + " Download:"));
			} else {
				player.sendMessage(plugin.getChatManager().coloredRawMessage("&3[KOTL] &bFound an update: v" + result.getNewestVersion() + " Download:"));
			}

			player.sendMessage(plugin.getChatManager().coloredRawMessage("&3>> &bhttps://www.spigotmc.org/resources/king-of-the-ladder.80686"));
		}), 25);
	}
}