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

package dev.despical.kotl.commands;

import dev.despical.commandframework.CommandArguments;
import dev.despical.commandframework.annotations.Command;
import dev.despical.kotl.api.StatisticType;
import dev.despical.kotl.api.StatsStorage;
import dev.despical.kotl.user.User;
import dev.despical.kotl.database.MySQLStorage;
import org.apache.commons.lang.StringUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.UUID;

/**
 * @author Despical
 * <p>
 * Created at 24.07.2022
 */
public final class PlayerCommands extends CommandCategory {

    @Command(
        name = "kotl",
        fallbackPrefix = "kingoftheladder",
        usage = "/kotl help",
        desc = "Main command of the plugin."
    )
    public void mainCommand(CommandArguments arguments) {
        if (arguments.isArgumentsEmpty()) {
            arguments.sendMessage("&3This server is running &bKing of the Ladder v{0} &3by &bDespical&3.", plugin.getDescription().getVersion());

            if (arguments.hasPermission("kotl.admin")) {
                arguments.sendMessage("&3Commands: &b/{0} help", arguments.getLabel());
            }

            return;
        }

        arguments.sendMessage("&cUnrecognized arguments: /{0} {1}", arguments.getLabel(), arguments.concatArguments());
    }

    @Command(
        name = "kotl.stats",
        usage = "/kotl stats <player name>",
        max = 1,
        senderType = Command.SenderType.PLAYER
    )
    public void statsCommand(CommandArguments arguments) {
        Player sender = arguments.getSender();
        var user = plugin.getUserManager().getUser(sender);

        if (arguments.isArgumentsEmpty()) {
            chatManager.getStringList("commands.stats-command.messages").stream().map(message -> formatStats(message, true, user)).forEach(arguments::sendMessage);
            return;
        }

        arguments.getPlayer(0).ifPresentOrElse(player -> {
            var targetUser = plugin.getUserManager().getUser(player);
            var self = sender.equals(player);

            chatManager.getStringList("commands.stats-command.messages").stream().map(message -> formatStats(message, self, targetUser)).forEach(arguments::sendMessage);
        }, () -> arguments.sendMessage(chatManager.prefixedMessage("commands.player_not_found")));
    }

    private String formatStats(String message, boolean self, User user) {
        message = message.replace("%header%", chatManager.message("commands.stats-command.header" + (self ? "" : "-other")));
        message = message.replace("%player%", user.getName());
        message = message.replace("%tours_played%", StatisticType.TOURS_PLAYED.from(user));
        message = message.replace("%score%", StatisticType.SCORE.from(user));
        message = message.replace("%kills%", StatisticType.KILLS.from(user));
        message = message.replace("%deaths%", StatisticType.DEATHS.from(user));
        return chatManager.coloredRawMessage(message);
    }

    @Command(
        name = "kotl.top",
        usage = "/kotl top <statistic type>",
        max = 1
    )
    public void leaderboardCommand(CommandArguments arguments) {
        if (arguments.isArgumentsEmpty()) {
            arguments.sendMessage(chatManager.prefixedMessage("commands.statistics.type_name"));
            return;
        }

        try {
            printLeaderboard(arguments.getSender(), StatisticType.valueOf(arguments.getArgument(0).toUpperCase(java.util.Locale.ENGLISH)));
        } catch (IllegalArgumentException exception) {
            arguments.sendMessage(chatManager.prefixedMessage("Commands.statistics.invalid_name"));
        }
    }

    private void printLeaderboard(CommandSender sender, StatisticType statisticType) {
        sender.sendMessage(chatManager.message("commands.statistics.header"));

        Map<UUID, Integer> stats = StatsStorage.getStats(statisticType);
        String statistic = StringUtils.capitalize(statisticType.name().toLowerCase(java.util.Locale.ENGLISH).replace("_", " "));

        for (int i = 0; i < 10; i++) {
            try {
                UUID current = (UUID) stats.keySet().toArray()[stats.keySet().toArray().length - 1];
                sender.sendMessage(formatMessage(statistic, plugin.getServer().getOfflinePlayer(current).getName(), i + 1, stats.get(current)));
                stats.remove(current);
            } catch (IndexOutOfBoundsException ex) {
                sender.sendMessage(formatMessage(statistic, "Empty", i + 1, 0));
            } catch (NullPointerException ex) {
                UUID current = (UUID) stats.keySet().toArray()[stats.keySet().toArray().length - 1];

                if (plugin.getDatabase() instanceof MySQLStorage mySQLStorage) {
                    try (Connection connection = mySQLStorage.getDatabase().getConnection()) {
                        Statement statement = connection.createStatement();
                        ResultSet set = statement.executeQuery("SELECT name FROM %s WHERE UUID='%s'".formatted(mySQLStorage.getStatsTable(), current.toString()));

                        if (set.next()) {
                            sender.sendMessage(formatMessage(statistic, set.getString(1), i + 1, stats.get(current)));
                            continue;
                        }
                    } catch (SQLException ignored) {
                    }
                }

                sender.sendMessage(formatMessage(statistic, "Unknown Player", i + 1, stats.get(current)));
            }
        }
    }

    private String formatMessage(String statisticName, String playerName, int position, int value) {
        String message = chatManager.message("Commands.Statistics.Format");

        message = message.replace("%position%", Integer.toString(position));
        message = message.replace("%name%", playerName);
        message = message.replace("%value%", Integer.toString(value));
        message = message.replace("%statistic%", statisticName);
        return message;
    }
}
