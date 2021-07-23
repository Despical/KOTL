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

import me.despical.commons.util.Collections;
import me.despical.kotl.ConfigPreferences;
import me.despical.kotl.Main;
import me.despical.kotl.arena.ArenaRegistry;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.*;

/**
 * @author Despical
 * <p>
 * Created at 22.06.2020
 */
public class Events implements Listener {

	private final Main plugin;

	public Events(Main plugin) {
		this.plugin = plugin;

		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}

	@EventHandler
	public void onDrop(PlayerDropItemEvent event) {
		if (ArenaRegistry.isInArena(event.getPlayer())) {
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onCommandExecute(PlayerCommandPreprocessEvent event) {
		Player player = event.getPlayer();

		if (!ArenaRegistry.isInArena(player)) {
			return;
		}

		if (!plugin.getConfig().getBoolean("Block-Commands-In-Game")) {
			return;
		}

		String message = event.getMessage();

		if (Collections.contains(message, plugin.getConfig().getStringList("Whitelisted-Commands"))) {
			return;
		}

		if (player.isOp() || player.hasPermission("kotl.admin")) {
			return;
		}

		if (message.startsWith("/kotl") || message.startsWith("/kingoftheladder") || message.contains("top") || message.contains("stats")) {
			return;
		}

		event.setCancelled(true);
		player.sendMessage(plugin.getChatManager().prefixedMessage("In-Game.Only-Command-Ingame-Is-Leave"));
	}
	
	@EventHandler
	public void onFallDamage(EntityDamageEvent e) {
		if (!(e.getEntity() instanceof Player)) {
			return;
		}

		Player victim = (Player) e.getEntity();

		if (!ArenaRegistry.isInArena(victim)) {
			return;
		}

		if (e.getCause().equals(EntityDamageEvent.DamageCause.FALL)) {
			if (!plugin.getConfigPreferences().getOption(ConfigPreferences.Option.DISABLE_FALL_DAMAGE)) {
				return;
			}

			e.setCancelled(true);
		}
	}

	@EventHandler
	public void onInGameBedEnter(PlayerBedEnterEvent event) {
		if (ArenaRegistry.isInArena(event.getPlayer())) {
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onFoodLevelChange(FoodLevelChangeEvent event) {
		if (event.getEntity() instanceof Player && ArenaRegistry.isInArena((Player) event.getEntity())) {
			event.setFoodLevel(20);
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onBlockBreakEvent(BlockBreakEvent event) {
		if (ArenaRegistry.isInArena(event.getPlayer())) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onBuild(BlockPlaceEvent event) {
		if (ArenaRegistry.isInArena(event.getPlayer())) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onPickUpItem(PlayerPickupItemEvent event) {
		if(ArenaRegistry.isInArena(event.getPlayer())) {
			event.setCancelled(true);
			event.getItem().remove();
		}
	}
}