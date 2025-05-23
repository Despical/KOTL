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

package me.despical.kotl.handlers.rewards;

import me.despical.commons.configuration.ConfigUtils;
import me.despical.kotl.KOTL;
import me.despical.kotl.arena.Arena;
import me.despical.kotl.handlers.rewards.Reward.RewardType;
import me.despical.kotl.handlers.rewards.Reward.SubReward;
import me.despical.kotl.user.User;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author Despical
 * <p>
 * Created at 30.06.2020
 */
public class RewardsFactory {

    private final KOTL plugin;
    private final Set<Reward> rewards;
    private double rewardInterval;

    public RewardsFactory(final KOTL plugin) {
        this.plugin = plugin;
        this.rewards = new HashSet<>();

        registerRewards();
    }

    public void performReward(User user, RewardType type, Arena arena) {
        final var rewardList = rewards.stream().filter(rew -> rew.getType() == type).toList();

        if (rewardList.isEmpty()) return;
        if (user.getCooldown("rewards") > 0) return;

        user.setCooldown("rewards", rewardInterval);

        for (final var mainRewards : rewardList) {
            for (final var reward : mainRewards.getRewards()) {
                if (reward.getChance() != -1 && ThreadLocalRandom.current().nextInt(0, 100) > reward.getChance())
                    continue;

                final var player = user.getPlayer();
                final var command = formatCommandPlaceholders(reward, user, arena);

                switch (reward.getExecutor()) {
                    case 1 -> plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), command);
                    case 2 -> player.performCommand(command);
                }
            }
        }
    }

    private String formatCommandPlaceholders(final SubReward reward, User user, Arena arena) {
        var formatted = reward.getExecutableCode();

        formatted = formatted.replace("%arena%", arena.getId());
        formatted = formatted.replace("%player%", user.getName());
        formatted = formatted.replace("%players%", Integer.toString(arena.getPlayers().size()));
        return formatted;
    }

    private void registerRewards() {
        var config = ConfigUtils.getConfig(plugin, "rewards");

        this.rewardInterval = config.getDouble("Reward-Interval", 5);

        if (!config.getBoolean("Rewards-Enabled")) return;

        for (final var rewardType : Reward.RewardType.values()) {
            rewards.add(new Reward(plugin, rewardType, config.getStringList(rewardType.path)));
        }
    }
}
