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

import me.despical.commons.miscellaneous.MiscUtils;
import me.despical.kotl.ConfigPreferences;
import me.despical.kotl.Main;
import me.despical.kotl.api.StatsStorage;
import me.despical.kotl.api.events.arena.KOTLNewKingEvent;
import me.despical.kotl.arena.Arena;
import me.despical.kotl.handlers.ChatManager.ActionType;
import me.despical.kotl.handlers.rewards.Reward;
import me.despical.kotl.user.User;
import me.despical.kotl.util.Utils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;

/**
 * @author Despical
 * <p>
 * Created at 22.06.2020
 */
public class ArenaEvents extends ListenerAdapter {

	public ArenaEvents(Main plugin) {
		super(plugin);
	}

	@EventHandler
	public void onInteractWithPlate(PlayerInteractEvent event) {
		var player = event.getPlayer();
		var arena = plugin.getArenaRegistry().getArena(player);

		if (arena == null || event.getAction() != Action.PHYSICAL) return;

		if (event.getClickedBlock().getType() == arena.getArenaPlate().parseMaterial()) {
			int size = arena.getPlayers().size();
			boolean isSameKing = arena.getKing() != null && arena.getKing().equals(player.getName());

			if (isSameKing && (size == 1 || !plugin.getOption(ConfigPreferences.Option.BECOME_KING_IN_A_ROW)))
				return;

			int cooldown = plugin.getConfig().getInt("King-Settings.Cooldown");
			String cooldownName = (plugin.getOption(ConfigPreferences.Option.SEPARATE_COOLDOWNS) ? arena.getId() : "") + "king";
			User user = plugin.getUserManager().getUser(player);

			if (plugin.getCooldownManager().getCooldown(user, cooldownName) > 0 || user.get((plugin.getOption(ConfigPreferences.Option.SEPARATE_COOLDOWNS) ? arena.getId() : "") + "local_cooldown")) {
				return;
			}

			cooldown_perm_check:
			if (size > 1 || (size == 1 && plugin.getOption(ConfigPreferences.Option.COOLDOWN_WHEN_ALONE))) {
				String permission = plugin.getConfig().getString("King-Settings.Cooldown-Override-Perm", "");

				if (!permission.isEmpty() && player.hasPermission(permission)) break cooldown_perm_check;

				if (plugin.getOption(ConfigPreferences.Option.APPLY_KING_DELAY_BAR)) {
					Utils.applyActionBarCooldown(user, cooldown);
				}

				plugin.getCooldownManager().setCooldown(user, cooldownName, cooldown);
			}

			var kingEvent = new KOTLNewKingEvent(arena, player, isSameKing);
			plugin.getServer().getPluginManager().callEvent(kingEvent);

			arena.setKing(player.getName());

			if (plugin.getOption(ConfigPreferences.Option.RESET_COOLDOWNS_ON_NEW_KING)) {
				var players = new HashSet<>(arena.getPlayers());
				players.remove(player);

				players.stream().map(plugin.getUserManager()::getUser).forEach(pUser -> pUser.setStat(StatsStorage.StatisticType.LOCAL_RESET_COOLDOWN, 1));
			}

			chatManager.broadcastAction(arena, player, ActionType.NEW_KING);

			user.addStat(StatsStorage.StatisticType.SCORE, 1);
			user.addStat(StatsStorage.StatisticType.TOURS_PLAYED, 1);
			user.performReward(Reward.RewardType.WIN, arena);

			var players = arena.getPlayers();
			players.remove(player);

			spawnFireworks(arena, player);

			for (var p : players) {
				final var u = plugin.getUserManager().getUser(p);
				u.addStat(StatsStorage.StatisticType.TOURS_PLAYED, 1);
				u.performReward(Reward.RewardType.LOSE, arena);

				spawnFireworks(arena, p);
			}
		}
	}

	@EventHandler
	public void onInteractWithDeathBlocks(PlayerInteractEvent event) {
		var player = event.getPlayer();

		if (!plugin.getOption(ConfigPreferences.Option.DEATH_BLOCKS_ENABLED)) {
			return;
		}

		var user = plugin.getUserManager().getUser(player);
		var arena = user.getArena();

		if (arena == null) return;

		if (event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.LEFT_CLICK_BLOCK) {
			for (final var material : plugin.getConfig().getStringList("Death-Blocks.Blacklisted-Blocks")) {
				if (event.getClickedBlock().getType() == Material.valueOf(material.toUpperCase())) {
					arena.doBarAction(player, 0);
					arena.broadcastMessage(chatManager.prefixedMessage("in_game.clicked_death_block").replace("%player%", player.getName()));
					arena.removePlayer(player);
					arena.teleportToEndLocation(player);

					user.performReward(Reward.RewardType.LOSE, arena);
					return;
				}
			}
		}
	}

	@EventHandler
	public void onDamage(EntityDamageByEntityEvent event) {
		if (!(event.getEntity() instanceof Player entity && event.getDamager() instanceof Player damager)) {
			return;
		}

		if (plugin.getArenaRegistry().isInArena(entity) && plugin.getArenaRegistry().isInArena(damager)) {
			if (!plugin.getOption(ConfigPreferences.Option.DAMAGE_ENABLED)) {
				event.setCancelled(false);
				event.setDamage(0d);
			}
		}
	}

	@EventHandler
	public void onDeath(PlayerDeathEvent event) {
		final var deadPlayer = event.getEntity();
		final var arena = plugin.getArenaRegistry().getArena(deadPlayer);

		if (arena == null) return;

		event.getDrops().clear();
		event.setDroppedExp(0);
		event.setKeepLevel(true);
		event.setDeathMessage("");

		plugin.getServer().getScheduler().runTaskLater(plugin, () -> deadPlayer.spigot().respawn(), 5);
		plugin.getUserManager().getUser(deadPlayer).setCooldown("death", 2);

		final var killerFound = deadPlayer.getKiller() != null;

		arena.broadcastMessage(chatManager.prefixedMessage("in_game." + (killerFound ? "killed_player" : "kill_command")).replace("%player%", killerFound ? deadPlayer.getKiller().getName() : "").replace("%victim%", deadPlayer.getName()));
	}

	@EventHandler
	public void onRespawn(PlayerRespawnEvent event) {
		final var player = event.getPlayer();
		final var arena = plugin.getArenaRegistry().getArena(player);

		if (arena == null) return;

		arena.removePlayer(player);
		event.setRespawnLocation(arena.getEndLocation());
	}

	private void spawnFireworks(Arena arena, Player player) {
		if (!plugin.getOption(ConfigPreferences.Option.FIREWORKS_ON_NEW_KING)) return;

		new BukkitRunnable() {

			private int i = 0;

			public void run() {
				if (i == 2 || !arena.getPlayers().contains(player)) {
					cancel();
				}

				MiscUtils.spawnRandomFirework(player.getLocation());
				i++;
			}
		}.runTaskTimer(plugin, 10, 20);
	}
}