/*
 * KOTL - Don't let others climb to top of the ladders!
 * Copyright (C) 2024  Berke Akçen
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.despical.kotl.events;

import me.despical.commons.serializer.InventorySerializer;
import me.despical.commons.util.UpdateChecker;
import me.despical.kotl.ConfigPreferences;
import me.despical.kotl.Main;
import me.despical.kotl.handlers.ChatManager;
import org.bukkit.ChatColor;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.*;

import java.util.regex.Pattern;

/**
 * @author Despical
 * <p>
 * Created at 22.06.2020
 */
public class Events extends ListenerAdapter {

	public Events(Main plugin) {
		super(plugin);
	}

	@EventHandler
	public void onJoin(PlayerJoinEvent event) {
		var player = event.getPlayer();

		plugin.getUserManager().loadStatistics(player);

		if (plugin.getOption(ConfigPreferences.Option.INVENTORY_MANAGER_ENABLED)) {
			InventorySerializer.loadInventory(plugin, player);
		}

		if (!plugin.getOption(ConfigPreferences.Option.UPDATE_NOTIFIER_ENABLED) || !player.hasPermission("kotl.updatenotify")) {
			return;
		}

		UpdateChecker.init(plugin, 80686).requestUpdateCheck().whenComplete((result, exception) -> {
			if (result.requiresUpdate()) {
				player.sendMessage(chatManager.coloredRawMessage("&3[KOTL] &bFound an update: v" + result.getNewestVersion()));
				player.sendMessage(chatManager.coloredRawMessage("&3>> &bhttps://spigotmc.org/resources/80686"));
			}
		});
	}

	@EventHandler
	public void onQuit(PlayerQuitEvent event) {
		var player = event.getPlayer();
		var arena = plugin.getArenaRegistry().getArena(player);

		if (arena != null) {
			chatManager.broadcastAction(arena, player, ChatManager.ActionType.LEAVE);

			arena.removePlayer(player);
		}

		plugin.getUserManager().removeUser(player);
	}

	@EventHandler
	public void onCommandExecute(PlayerCommandPreprocessEvent event) {
		var player = event.getPlayer();

		if (!plugin.getArenaRegistry().isInArena(player)) {
			return;
		}

		if (!plugin.getOption(ConfigPreferences.Option.BLOCK_COMMANDS)) {
			return;
		}

		String message = event.getMessage();

		if (plugin.getConfig().getStringList("Whitelisted-Commands").contains(message)) {
			return;
		}

		if (player.isOp() || player.hasPermission("kotl.command.override")) {
			return;
		}

		if (message.startsWith("/kotl") || message.startsWith("/kingoftheladder") || message.contains("top") || message.contains("stats")) {
			return;
		}

		event.setCancelled(true);
		player.sendMessage(chatManager.prefixedMessage("in_game.only_command_is_leave"));
	}
	
	@EventHandler
	public void onFallDamage(EntityDamageEvent e) {
		if (!(e.getEntity() instanceof Player victim)) return;
		if (!plugin.getArenaRegistry().isInArena(victim)) return;

		if (e.getCause() == EntityDamageEvent.DamageCause.BLOCK_EXPLOSION) {
			e.setCancelled(true);
			return;
		}

		if (e.getCause() == EntityDamageEvent.DamageCause.FALL) {
			if (!plugin.getOption(ConfigPreferences.Option.DISABLE_FALL_DAMAGE)) return;

			e.setCancelled(true);
		}
	}

	@EventHandler
	public void onFireworkDamage(EntityDamageByEntityEvent event) {
		if (!plugin.getOption(ConfigPreferences.Option.FIREWORKS_ON_NEW_KING)) return;
		if (!(event.getEntity() instanceof Player player)) return;
		if (!plugin.getArenaRegistry().isInArena(player)) return;

		if (event.getDamager() instanceof Firework) {
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onFoodLevelChange(FoodLevelChangeEvent event) {
		if (event.getEntity() instanceof Player && plugin.getArenaRegistry().isInArena((Player) event.getEntity())) {
			if (!plugin.getOption(ConfigPreferences.Option.UPDATE_HUNGER)) return;

			event.setFoodLevel(20);
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onBreak(BlockBreakEvent event) {
		final var player = event.getPlayer();

		if (plugin.getArenaRegistry().isInArena(player) && !player.isOp()) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onPlace(BlockPlaceEvent event) {
		final var player = event.getPlayer();

		if (plugin.getArenaRegistry().isInArena(player) && !player.isOp()) {
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onDrop(PlayerDropItemEvent event) {
		if (plugin.getArenaRegistry().isInArena(event.getPlayer())) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onPickUpItem(PlayerPickupItemEvent event) {
		if (!plugin.getArenaRegistry().isInArena(event.getPlayer())) {
			return;
		}

		if (!plugin.getOption(ConfigPreferences.Option.PICK_UP_ITEMS)) {
			event.setCancelled(true);
			event.getItem().remove();
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onChatInGame(AsyncPlayerChatEvent event) {
		final var player = event.getPlayer();
		final var arena = plugin.getArenaRegistry().getArena(player);

		if (arena == null) {
			if (!plugin.getOption(ConfigPreferences.Option.DISABLE_SEPARATE_CHAT)) {
				plugin.getArenaRegistry().getArenas().forEach(loopArena -> loopArena.getPlayers().forEach(p -> event.getRecipients().remove(p)));
			}

			return;
		}

		if (plugin.getOption(ConfigPreferences.Option.CHAT_FORMAT_ENABLED)) {
			String message = formatChatPlaceholders(chatManager.message("in_game.chat_format"), player, event.getMessage().replaceAll(Pattern.quote("[$\\]"), ""));

			if (!plugin.getOption(ConfigPreferences.Option.DISABLE_SEPARATE_CHAT)) {
				event.setCancelled(true);

				for (var p : arena.getPlayers()) {
					p.sendMessage(message);
				}

				plugin.getServer().getConsoleSender().sendMessage(message);
			} else {
				event.setMessage(message);
			}
		}
	}

	private String formatChatPlaceholders(String message, Player player, String saidMessage) {
		String formatted = message;

		formatted = formatted.replace("%player%", player.getName());
		formatted = formatted.replace("%message%", ChatColor.stripColor(saidMessage));
		formatted = chatManager.formatMessage(formatted, player);
		return chatManager.coloredRawMessage(formatted);
	}
}