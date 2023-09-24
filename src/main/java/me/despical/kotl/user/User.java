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

package me.despical.kotl.user;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import me.despical.kotl.handlers.rewards.Reward;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import me.despical.kotl.Main;
import me.despical.kotl.api.StatsStorage;
import me.despical.kotl.api.events.player.KOTLPlayerStatisticChangeEvent;
import me.despical.kotl.arena.Arena;
import org.bukkit.scoreboard.Scoreboard;

/**
 * @author Despical
 * <p>
 * Created at 20.06.2020
 */
public class User {

	private static final Main plugin = JavaPlugin.getPlugin(Main.class);
	private static long cooldownCounter = 0;

	private final UUID uuid;
	private final Player player;
	private final Map<String, Double> cooldowns;
	private final Map<StatsStorage.StatisticType, Integer> stats;

	private Scoreboard cachedScoreboard;

	public User(UUID uuid) {
		this.uuid = uuid;
		this.player = plugin.getServer().getPlayer(uuid);
		this.cooldowns = new HashMap<>();
		this.stats = new EnumMap<>(StatsStorage.StatisticType.class);
	}

	public Arena getArena() {
		return plugin.getArenaRegistry().getArena(player);
	}

	public Player getPlayer() {
		return player;
	}

	public UUID getUniqueId() {
		return uuid;
	}

	public int getStat(StatsStorage.StatisticType statisticType) {
		final var statistic = stats.get(statisticType);

		if (statistic == null) {
			stats.put(statisticType, 0);
			return 0;
		}

		return statistic;
	}

	public void setStat(StatsStorage.StatisticType stat, int value) {
		stats.put(stat, value);

		plugin.getServer().getScheduler().runTask(plugin, () -> plugin.getServer().getPluginManager().callEvent(new KOTLPlayerStatisticChangeEvent(getArena(), player, stat, value)));
	}

	public void addStat(StatsStorage.StatisticType stat, int value) {
		setStat(stat, getStat(stat) + value);
	}

	public void performReward(final Reward.RewardType rewardType) {
		plugin.getRewardsFactory().performReward(this, rewardType);
	}

	public void giveKit() {
		plugin.getKitManager().giveKit(player);
	}

	public void cacheScoreboard() {
		this.cachedScoreboard = this.player.getScoreboard();
	}

	public void removeScoreboard() {
		if (this.cachedScoreboard == null) return;

		this.player.setScoreboard(this.cachedScoreboard);
		this.cachedScoreboard = null;
	}

	public void setCooldown(String s, double seconds) {
		cooldowns.put(s, seconds + cooldownCounter);
	}

	public double getCooldown(String s) {
		final var cooldown = cooldowns.get(s);

		return (cooldown == null || cooldown <= cooldownCounter) ? 0 : cooldown - cooldownCounter;
	}

	public static void cooldownHandlerTask() {
		plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, () -> cooldownCounter++, 20, 20);
	}
}