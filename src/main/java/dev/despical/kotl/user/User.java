/*
 * KOTL - Don't let others climb to top of the ladders!
 * Copyright (C) 2025  Berke Akçen
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

package dev.despical.kotl.user;

import dev.despical.kotl.KOTL;
import dev.despical.kotl.api.StatisticType;
import dev.despical.kotl.api.events.player.KOTLPlayerStatisticChangeEvent;
import dev.despical.kotl.arena.Arena;
import dev.despical.kotl.handlers.rewards.Reward.RewardType;
import dev.despical.kotl.options.Option;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

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

    private static final KOTL plugin = JavaPlugin.getPlugin(KOTL.class);
    private static long cooldownCounter;

    static {
        plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, () -> cooldownCounter++, 20, 20);
    }

    private final UUID uuid;

    @Getter
    private final String name;
    private final Map<String, Double> cooldowns;
    private final Map<String, Boolean> variables;
    private final Map<StatisticType, Integer> stats;

    public User(Player player) {
        this.uuid = player.getUniqueId();
        this.name = player.getName();
        this.cooldowns = new HashMap<>();
        this.variables = new HashMap<>();
        this.stats = new EnumMap<>(StatisticType.class);
    }

    public Arena getArena() {
        return plugin.getArenaRegistry().getArena(getPlayer());
    }

    public Player getPlayer() {
        return Bukkit.getPlayer(uuid);
    }

    public UUID getUniqueId() {
        return uuid;
    }

    public void sendRawMessage(String message) {
        Player player = getPlayer();

        message = plugin.getChatManager().formatMessage(message, player);

        player.sendMessage(message);
    }

    public int getStat(StatisticType statisticType) {
        return stats.computeIfAbsent(statisticType, stat -> 0);
    }

    public void setStat(StatisticType stat, int value) {
        stats.put(stat, value);

        plugin.callEvent(() -> new KOTLPlayerStatisticChangeEvent(getArena(), getPlayer(), stat, value));
    }

    public void addStat(StatisticType stat, int value) {
        setStat(stat, getStat(stat) + value);
    }

    public void performReward(RewardType rewardType, Arena arena) {
        plugin.getServer().getScheduler().runTask(plugin, () -> plugin.getRewardsFactory().performReward(this, rewardType, arena));
    }

    public void giveKit() {
        plugin.getKitManager().giveKit(getPlayer());
    }

    public void setCooldown(String s, double seconds) {
        cooldowns.put(s, seconds + cooldownCounter);
    }

    public double getCooldown(String s) {
        final var cooldown = cooldowns.get(s);

        return (cooldown == null || cooldown <= cooldownCounter) ? 0 : cooldown - cooldownCounter;
    }

    public boolean get(String string) {
        return variables.computeIfAbsent(string, value -> false);
    }

    public void set(String string, boolean value) {
        if ("king".equals(string) && plugin.getConfigOptions().isEnabled(Option.SEPARATE_COOLDOWNS)) {
            string = getArena().getId() + "king";
        }

        variables.put(string, value);
    }
}
