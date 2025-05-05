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

package me.despical.kotl.util;

import me.despical.commons.messages.ActionBar;
import me.despical.kotl.KOTL;
import me.despical.kotl.api.StatisticType;
import me.despical.kotl.arena.Arena;
import me.despical.kotl.options.Option;
import me.despical.kotl.user.User;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * @author Despical
 * <p>
 * Created at 18.04.2024
 */
public class Utils {

    private static final KOTL plugin = JavaPlugin.getPlugin(KOTL.class);

    private Utils() {
    }

    public static void applyActionBarCooldown(User user, int seconds) {
        if (seconds == 0) return;

        var options = plugin.getConfigOptions();
        boolean showCooldownOnRejoin = options.isEnabled(Option.SHOW_COOLDOWN_ON_REJOIN);
        boolean outsideCooldownCount = options.isEnabled(Option.COUNT_COOLDOWN_OUTSIDE);
        boolean separateCooldowns = options.isEnabled(Option.SEPARATE_COOLDOWNS);

        String arenaId = user.getArena().getId();
        String cooldownName = (separateCooldowns ? arenaId : "") + "king";
        String localCooldownName = (separateCooldowns ? arenaId : "") + "local_cooldown";

        if (!outsideCooldownCount) {
            user.set(localCooldownName, true);
        }

        new BukkitRunnable() {

            private int ticks = 0;

            @Override
            public void run() {
                Player player = user.getPlayer();

                if (user.getStat(StatisticType.LOCAL_RESET_COOLDOWN) == 1) {
                    cancel();

                    plugin.getCooldownManager().setCooldown(user, cooldownName, 0);

                    user.set(localCooldownName, false);
                    user.setStat(StatisticType.LOCAL_RESET_COOLDOWN, 0);
                    return;
                }

                Arena arena = user.getArena();

                if (separateCooldowns && arena != null && !arenaId.equals(arena.getId())) {
                    return;
                }

                if (!outsideCooldownCount) {
                    plugin.getCooldownManager().setCooldown(user, cooldownName, seconds - Math.ceil(ticks / 20D));
                } else if (ticks >= 20 * seconds) {
                    cancel();
                }

                if (arena == null || !arena.getPlayers().contains(player)) {
                    if (!showCooldownOnRejoin) {
                        cancel();

                        user.set(localCooldownName, false);
                    }

                    if (outsideCooldownCount) {
                        ticks += 2;
                    }

                    return;
                }

                String progress = getProgressBar(ticks, seconds * 20);
                ActionBar.sendActionBar(player, plugin.getChatManager().message("In-Game.Cooldown-Format", player)
                    .replace("%progress%", progress)
                    .replace("%time%", Double.toString(((seconds * 20) - ticks) / 20D)));

                if (ticks >= seconds * 20) {
                    cancel();

                    user.set(localCooldownName, false);
                    return;
                }

                ticks += 2;
            }
        }.runTaskTimer(plugin, 0, 2);
    }

    private static String getProgressBar(int current, int max) {
        float percent = (float) current / max;
        int progressBars = (int) (10 * percent), leftOver = (10 - progressBars);

        return "§a" +
            "■".repeat(Math.max(0, progressBars)) +
            "§c" +
            "■".repeat(Math.max(0, leftOver));
    }
}
