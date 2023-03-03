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

package me.despical.kotl.handler.rewards;

import me.despical.commons.configuration.ConfigUtils;
import me.despical.commons.engine.ScriptEngine;
import me.despical.commons.util.LogUtils;
import me.despical.kotl.ConfigPreferences;
import me.despical.kotl.Main;
import me.despical.kotl.arena.Arena;
import me.despical.kotl.user.User;
import org.apache.commons.lang.StringUtils;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author Despical
 * <p>
 * Created at 30.06.2020
 */
public class RewardsFactory {

	private final Main plugin;
	private final double delay;
	private final FileConfiguration config;
	private final Set<Reward> rewards;

	public RewardsFactory(Main plugin) {
		this.plugin = plugin;
		this.config = ConfigUtils.getConfig(plugin, "rewards");
		this.delay = config.getDouble("time-between-rewards", 5) * 20;
		this.rewards = new HashSet<>();

		registerRewards();
	}

	public void performReward(Player player, Reward.RewardType type) {
		if (rewards.isEmpty()) return;

		final User user = plugin.getUserManager().getUser(player);

		if (user.getCooldown("rewards") > 0) return;

		user.setCooldown("rewards", delay);

		Arena arena = plugin.getArenaRegistry().getArena(player);

		for (Reward reward : rewards) {
			if (reward.getType() == type) {
				if (ThreadLocalRandom.current().nextInt(0, 100) > reward.getChance()) {
					continue;
				}

				String command = reward.getExecutableCode();
				command = formatCommandPlaceholders(command, arena, player);

				switch (reward.getExecutor()) {
					case CONSOLE:
						plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), command);
						break;
					case PLAYER:
						player.performCommand(command);
						break;
					case SCRIPT:
						ScriptEngine engine = new ScriptEngine();

						engine.setValue("arena", arena);
						engine.setValue("player", player);
						engine.setValue("server", plugin.getServer());
						engine.execute(command);
						break;
					default:
						break;
				}
			}
		}
	}

	private String formatCommandPlaceholders(String command, Arena arena, Player player) {
		String formatted = command;

		formatted = StringUtils.replace(formatted, "%arena%", arena.getId());
		formatted = StringUtils.replace(formatted, "%player%", player.getName());
		formatted = StringUtils.replace(formatted, "%players%", Integer.toString(arena.getPlayers().size()));
		return formatted;
	}

	private void registerRewards() {
		if (!plugin.getConfigPreferences().getOption(ConfigPreferences.Option.REWARDS_ENABLED)) return;

		LogUtils.log("[Rewards Factory] Starting rewards registration.");

		long start = System.currentTimeMillis();

		for (Reward.RewardType rewardType : Reward.RewardType.values()) {
			for (String reward : config.getStringList(rewardType.getPath())) {
				rewards.add(new Reward(rewardType, reward));
			}
		}

		LogUtils.log("[Rewards Factory] Registered all rewards took {0} ms.", System.currentTimeMillis() - start);
	}
}