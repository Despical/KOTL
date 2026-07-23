/*
 * KOTL - Don't let others climb to top of the ladders!
 * Copyright (C) 2026  Berke Akçen
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

package dev.despical.kotl.util;

import dev.despical.commons.serializer.InventorySerializer;
import dev.despical.kotl.KOTL;
import dev.despical.kotl.arena.Arena;
import dev.despical.kotl.option.BooleanOption;
import dev.despical.kotl.stats.Statistics;
import dev.despical.kotl.user.User;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

/**
 * @author Despical
 * <p>
 * Created at 18.04.2024
 */
public final class Utils {

    private static final KOTL plugin = JavaPlugin.getPlugin(KOTL.class);

    private Utils() {
    }

    public static void applyActionBarCooldown(User user, int seconds) {
        if (seconds == 0) return;

        var options = plugin.getOptions();
        boolean showCooldownOnRejoin = BooleanOption.SHOW_COOLDOWN_ON_REJOIN.value();
        boolean outsideCooldownCount = BooleanOption.COUNT_COOLDOWN_OUTSIDE.value();
        boolean separateCooldowns = BooleanOption.SEPARATE_COOLDOWNS.value();

        String arenaId = user.getArena().getId();
        String cooldownName = (separateCooldowns ? arenaId : "") + "king";

        if (!outsideCooldownCount) {
            user.setStatistic(Statistics.LOCAL_RESET_COOLDOWN, 1);
        }

        new BukkitRunnable() {

            private int ticks = 0;

            @Override
            public void run() {
                Player player = user.getPlayer();

                if (user.getStatistic(Statistics.LOCAL_RESET_COOLDOWN) == 1) {
                    cancel();

                    plugin.getCooldownManager().setCooldown(user, cooldownName, 0);

                    user.setStatistic(Statistics.LOCAL_RESET_COOLDOWN, 0);
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

                if (arena == null || !arena.getGame().getPlayers().contains(player)) {
                    if (!showCooldownOnRejoin) {
                        cancel();

                        user.setStatistic(Statistics.LOCAL_RESET_COOLDOWN, 0);
                    }

                    if (outsideCooldownCount) {
                        ticks += 2;
                    }

                    return;
                }

                String progress = getProgressBar(ticks, seconds * 20);
                Var[] vars = {
                    Var.of("%progress%", progress),
                    Var.of("%time%", Double.toString(((seconds * 20) - ticks) / 20D))
                };

                plugin.getChatManager().sendActionBar(user, "In-Game.Cooldown-Format", vars);

                if (ticks >= seconds * 20) {
                    cancel();

                    user.setStatistic(Statistics.LOCAL_RESET_COOLDOWN, 0);
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

    public static String NONE = getMessage("none");

    public static String getRawString(String string) {
        return plugin.getConfig().getString(string, "&cThe value inside the path is null. (path: " + string + ")");
    }

    public static String getRawString(FileConfiguration config, String string) {
        return config.getString(string);
    }

    public static List<String> getStringList(String path) {
        return plugin.getConfig().getStringList(path);
    }

    public static String format(String string, Var... variables) {
        for (Var variable : variables) {
            string = string.replace(variable.name, variable.value.toString());
        }

        return string;
    }

    public static String getString(String path) {
        if (plugin.getConfig().isList(path)) {
            return getListAsString(path);
        }

        return getRawString(path);
    }

    public static String getString(FileConfiguration file, String path) {
        if (file.isList(path)) {
            return getListAsString(file, path);
        }

        return getRawString(file, path);
    }

    public static String getMessage(String path, Var... variables) {
        return plugin.getChatManager().getRawString(path, variables);
    }

    public static List<String> getStringList(FileConfiguration config, String string) {
        return config.getStringList(string);
    }

    public static String getMessage(FileConfiguration config, String path) {
        String message = getString(config, path);

        return message
            .replace("%prefix%", getString("prefix"))
            .replace("%prefix-2%", getString("prefix-2"));
    }

    public static String getListAsString(String path) {
        return listToString(getStringList(path));
    }

    public static String getListAsString(FileConfiguration file, String path) {
        return listToString(getStringList(file, path));
    }

    public static String listToString(List<String> list) {
        return String.join("\n", list);
    }

    public static void restoreSavedPlayerState(Player player) {
        player.getActivePotionEffects().forEach(effect -> player.removePotionEffect(effect.getType()));
        InventorySerializer.loadInventory(plugin, player);
    }

    public static String formatTime(long millis) {
        long minutes = (millis / 1000) / 60;
        long seconds = (millis / 1000) % 60;
        long ms = millis % 1000;

        return String.format("%02d:%02d.%03d", minutes, seconds, ms);
    }
}
