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

package me.despical.kotl.command;

import me.despical.commandframework.Command;
import me.despical.commandframework.CommandArguments;
import me.despical.commons.string.StringMatcher;
import me.despical.kotl.ConfigPreferences;
import me.despical.kotl.Main;
import me.despical.kotl.api.StatsStorage;
import me.despical.kotl.user.User;
import org.apache.commons.lang.StringUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @author Despical
 * <p>
 * Created at 24.07.2022
 */
public class PlayerCommands extends AbstractCommand {

	public PlayerCommands(Main plugin) {
		super(plugin);

		plugin.getCommandFramework().setAnyMatch(arguments -> {
			if (arguments.isArgumentsEmpty()) return;

			String label = arguments.getLabel(), arg = arguments.getArgument(0);

			List<StringMatcher.Match> matches = StringMatcher.match(arg, plugin.getCommandFramework().getCommands().stream().map(cmd -> cmd.name().replace(label + ".", "")).collect(Collectors.toList()));

			if (!matches.isEmpty()) {
				arguments.sendMessage(chatManager.prefixedMessage("commands.did_you_mean").replace("%command%", label + " " + matches.get(0).getMatch()));
			}
		});
	}

	@Command(
		name = "kotl",
		desc = "Main command of King of the Ladder plugin.",
		usage = "/kotl help"
	)
	public void mainCommand(CommandArguments arguments) {
		if (arguments.isArgumentsEmpty()) {
			arguments.sendMessage(chatManager.coloredRawMessage("&3This server is running &bKing of the Ladder &3v" + plugin.getDescription().getVersion() + " by &bDespical"));

			if (arguments.hasPermission("kotl.admin")) {
				arguments.sendMessage(chatManager.coloredRawMessage("&3Commands: &b/" + arguments.getLabel() + " help"));
			}
		}
	}

	@Command(
		name = "kotl.stats",
		senderType = Command.SenderType.PLAYER
	)
	public void statsCommand(CommandArguments arguments) {
		final Player sender = arguments.getSender(), player = !arguments.isArgumentsEmpty() ? plugin.getServer().getPlayer(arguments.getArgument(0)) : sender;

		if (player == null) {
			arguments.sendMessage(chatManager.prefixedMessage("commands.player_not_found"));
			return;
		}

		final User user = plugin.getUserManager().getUser(player);
		final String path = "commands.stats_command.";

		if (player.equals(sender)) {
			arguments.sendMessage(chatManager.message(path + "header", player));
		} else {
			arguments.sendMessage(chatManager.message(path + "header_other", player));
		}

		sender.sendMessage(chatManager.message(path + "tours_played", player) + user.getStat(StatsStorage.StatisticType.TOURS_PLAYED));
		sender.sendMessage(chatManager.message(path + "score", player) + user.getStat(StatsStorage.StatisticType.SCORE));
		sender.sendMessage(chatManager.message(path + "footer", player));
	}

	@Command(
		name = "kotl.top"
	)
	public void leaderboardCommand(CommandArguments arguments) {
		if (arguments.isArgumentsEmpty()) {
			arguments.sendMessage(chatManager.prefixedMessage("Commands.statistics.type_name"));
			return;
		}

		try {
			printLeaderboard(arguments.getSender(), StatsStorage.StatisticType.valueOf(arguments.getArgument(0).toUpperCase(java.util.Locale.ENGLISH)));
		} catch (IllegalArgumentException exception) {
			arguments.sendMessage(chatManager.prefixedMessage("Commands.statistics.invalid_name"));
		}
	}

	private void printLeaderboard(CommandSender sender, StatsStorage.StatisticType statisticType) {
		sender.sendMessage(chatManager.message("commands.statistics.header"));

		final Map<UUID, Integer> stats = StatsStorage.getStats(statisticType);
		final String statistic = StringUtils.capitalize(statisticType.name().toLowerCase(java.util.Locale.ENGLISH).replace("_", " "));

		for (int i = 0; i < 10; i++) {
			try {
				UUID current = (UUID) stats.keySet().toArray()[stats.keySet().toArray().length - 1];
				sender.sendMessage(formatMessage(statistic, plugin.getServer().getOfflinePlayer(current).getName(), i + 1, stats.get(current)));
				stats.remove(current);
			} catch (IndexOutOfBoundsException ex) {
				sender.sendMessage(formatMessage(statistic, "Empty", i + 1, 0));
			} catch (NullPointerException ex) {
				UUID current = (UUID) stats.keySet().toArray()[stats.keySet().toArray().length - 1];

				if (plugin.getConfigPreferences().getOption(ConfigPreferences.Option.DATABASE_ENABLED)) {
					try (Connection connection = plugin.getMysqlDatabase().getConnection()) {
						Statement statement = connection.createStatement();
						ResultSet set = statement.executeQuery("SELECT name FROM playerstats WHERE UUID='" + current.toString() + "'");

						if (set.next()) {
							sender.sendMessage(formatMessage(statistic, set.getString(1), i + 1, stats.get(current)));
							continue;
						}
					} catch (SQLException ignored) {}
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