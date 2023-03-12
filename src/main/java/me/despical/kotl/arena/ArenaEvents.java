/*
 * KOTL - Don't let others to climb top of the ladders!
 * Copyright (C) 2023 Despical
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

package me.despical.kotl.arena;

import me.despical.commons.miscellaneous.MiscUtils;
import me.despical.kotl.ConfigPreferences;
import me.despical.kotl.Main;
import me.despical.kotl.api.StatsStorage;
import me.despical.kotl.event.ListenerAdapter;
import me.despical.kotl.handler.ChatManager.ActionType;
import me.despical.kotl.handler.rewards.Reward;
import me.despical.kotl.user.User;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Set;

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
	public void onEnterAndLeaveGameArea(PlayerMoveEvent event) {
		Player player = event.getPlayer();
		Arena arena = isInArea(player);

		if (!plugin.getArenaRegistry().isInArena(player) && arena != null) {
			arena.addPlayer(player);
		}

		if (plugin.getArenaRegistry().isInArena(player) && arena == null) {
			Arena tempArena = plugin.getArenaRegistry().getArena(player);

			tempArena.removePlayer(player);
		}
	}

	@EventHandler
	public void onInteractWithPlate(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		Arena arena = plugin.getArenaRegistry().getArena(player);

		if (arena == null) return;

		if (event.getAction() == Action.PHYSICAL) {
			if (event.getClickedBlock().getType() == arena.getArenaPlate().parseMaterial()) {
				if (arena.getKing() != null && arena.getKing().equals(player) && (arena.getPlayers().size() == 1 || !plugin.getConfigPreferences().getOption(ConfigPreferences.Option.BECOME_KING_IN_A_ROW))) return;
				arena.setKing(player);

				chatManager.broadcastAction(arena, player, ActionType.NEW_KING);
				plugin.getRewardsFactory().performReward(player, Reward.RewardType.WIN);

				User user = plugin.getUserManager().getUser(player);
				user.addStat(StatsStorage.StatisticType.SCORE, 1);
				user.addStat(StatsStorage.StatisticType.TOURS_PLAYED, 1);

				Set<Player> players = arena.getPlayers();
				players.remove(player);

				spawnFireworks(arena, player);

				for (Player p : players) {
					plugin.getUserManager().getUser(p).addStat(StatsStorage.StatisticType.TOURS_PLAYED, 1);

					plugin.getRewardsFactory().performReward(p, Reward.RewardType.LOSE);

					spawnFireworks(arena, p);
				}
			}
		}
	}

	@EventHandler
	public void onInteractWithDeathBlocks(PlayerInteractEvent event) {
		Player player = event.getPlayer();

		if (!plugin.getConfigPreferences().getOption(ConfigPreferences.Option.DEATH_BLOCKS_ENABLED)) {
			return;
		}

		Arena arena = plugin.getArenaRegistry().getArena(player);

		if (arena == null) {
			return;
		}

		if (event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.LEFT_CLICK_BLOCK) {
			for (String material : plugin.getConfig().getStringList("Death-Blocks.Blacklisted-Blocks")) {
				if (event.getClickedBlock().getType() == Material.valueOf(material.toUpperCase())) {
					arena.doBarAction(player, 0);
					arena.broadcastMessage(chatManager.prefixedMessage("in_game.clicked_death_block").replace("%player%", player.getName()));
					arena.removePlayer(player);
					arena.teleportToEndLocation(player);

					plugin.getRewardsFactory().performReward(player, Reward.RewardType.LOSE);
					return;
				}
			}
		}
	}

	@EventHandler
	public void onDamage(EntityDamageByEntityEvent event) {
		if (!(event.getEntity() instanceof Player && event.getDamager() instanceof Player)) {
			return;
		}

		Player entity = (Player) event.getEntity(), damager = (Player) event.getDamager();

		if (plugin.getArenaRegistry().isInArena(entity) && plugin.getArenaRegistry().isInArena(damager)) {
			event.setCancelled(false);
			event.setDamage(0d);
		}
	}

	private Arena isInArea(final Player player) {
		for (Arena arena : plugin.getArenaRegistry().getArenas()) {
			final Arena target = arena.isInArea(player);

			if (target != null) return target;
		}

		return null;
	}

	private void spawnFireworks(Arena arena, Player player) {
		if (!plugin.getConfigPreferences().getOption(ConfigPreferences.Option.FIREWORKS_ON_NEW_KING)) return;

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