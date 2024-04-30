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

package me.despical.kotl.user;

import me.despical.kotl.ConfigPreferences;
import me.despical.kotl.Main;
import me.despical.kotl.api.StatsStorage;
import me.despical.kotl.api.events.player.KOTLPlayerStatisticChangeEvent;
import me.despical.kotl.arena.Arena;
import me.despical.kotl.handlers.rewards.Reward;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Scoreboard;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author Despical
 * <p>
 * Created at 20.06.2020
 */
public class User {

	private static final Main plugin = JavaPlugin.getPlugin(Main.class);
	private static long cooldownCounter = 0;

	private final UUID uuid;
	private final Map<String, Double> cooldowns;
	private final Map<String, Boolean> variables;
	private final Map<StatsStorage.StatisticType, Integer> stats;

	private Scoreboard cachedScoreboard;

	public User(UUID uuid) {
		this.uuid = uuid;
		this.cooldowns = new HashMap<>();
		this.variables = new HashMap<>();
		this.stats = new EnumMap<>(StatsStorage.StatisticType.class);
	}

	public Arena getArena() {
		return plugin.getArenaRegistry().getArena(getPlayer());
	}

	public Player getPlayer() {
		return plugin.getServer().getPlayer(uuid);
	}

	public UUID getUniqueId() {
		return uuid;
	}

	public int getStat(StatsStorage.StatisticType statisticType) {
		return stats.computeIfAbsent(statisticType, stat -> 0);
	}

	public void setStat(StatsStorage.StatisticType stat, int value) {
		stats.put(stat, value);

		plugin.getServer().getScheduler().runTask(plugin, () -> plugin.getServer().getPluginManager().callEvent(new KOTLPlayerStatisticChangeEvent(getArena(), getPlayer(), stat, value)));
	}

	public void addStat(StatsStorage.StatisticType stat, int value) {
		setStat(stat, getStat(stat) + value);
	}

	public void performReward(Reward.RewardType rewardType, Arena arena) {
		plugin.getServer().getScheduler().runTask(plugin, () -> plugin.getRewardsFactory().performReward(this, rewardType, arena));
	}

	public void giveKit() {
		plugin.getKitManager().giveKit(getPlayer());
	}

	public void cacheScoreboard() {
		this.cachedScoreboard = getPlayer().getScoreboard();
	}

	public void removeScoreboard() {
		if (this.cachedScoreboard == null) return;

		this.getPlayer().setScoreboard(this.cachedScoreboard);
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

	public boolean get(String string) {
		return variables.computeIfAbsent(string, val -> false);
	}

	public void set(String string, boolean value) {
		if ("king".equals(string) && plugin.getOption(ConfigPreferences.Option.SEPARATE_COOLDOWNS)) {
			string = getArena().getId() + "king";
		}

		variables.put(string, value);
	}
}