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

package me.despical.kotl.commands;

import me.despical.commandframework.CommandArguments;
import me.despical.commandframework.Message;
import me.despical.commandframework.annotations.Command;
import me.despical.commons.string.StringMatcher;
import me.despical.kotl.Main;
import me.despical.kotl.api.StatsStorage;
import me.despical.kotl.user.User;
import me.despical.kotl.user.data.MysqlManager;
import org.apache.commons.lang.StringUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Despical
 * <p>
 * Created at 24.07.2022
 */
public class PlayerCommands extends AbstractCommand {

	public PlayerCommands(Main plugin) {
		super(plugin);

		Stream.of(Message.SHORT_ARG_SIZE, Message.LONG_ARG_SIZE).forEach(message -> message.setMessage((command, arguments) -> {
			arguments.sendMessage(chatManager.prefixedMessage("commands.correct_usage").replace("%usage%", command.usage()));
			return true;
		}));
	}

	@Command(
		name = "kotl",
		desc = "Main command of the plugin.",
		usage = "/kotl help"
	)
	public void mainCommand(CommandArguments arguments) {
		if (arguments.isArgumentsEmpty()) {
			arguments.sendMessage("&3This server is running &bKing of the Ladder v" + plugin.getDescription().getVersion() + " &3by &bDespical&3!");

			if (arguments.hasPermission("kotl.admin")) {
				arguments.sendMessage("&3Commands: &b/" + arguments.getLabel() + " help");
			}

			return;
		}

		var commandFramework = plugin.getCommandFramework();
		String label = arguments.getLabel(), arg = arguments.getArgument(0);
		List<String> commands = commandFramework.getCommands().stream().map(cmd -> cmd.name().replace(label + ".", "")).collect(Collectors.toList());
		List<StringMatcher.Match> matches = StringMatcher.match(arg, commands);

		if (!matches.isEmpty()) {
			Optional<Command> optionalMatch = commandFramework.getCommands().stream().filter(cmd -> cmd.name().equals(label + "." + matches.get(0).getMatch())).findFirst();

			if (optionalMatch.isPresent()) {
				String matchedName = getMatchingParts(optionalMatch.get().name(), label + "." + String.join(".", arguments.getArguments()));
				Optional<Command> matchedCommand = commandFramework.getSubCommands().stream().filter(cmd -> cmd.name().equals(matchedName)).findFirst();

				if (matchedCommand.isPresent()) {
					arguments.sendMessage(chatManager.prefixedMessage("commands.correct_usage").replace("%usage%", matchedCommand.get().usage()));
					return;
				}

				arguments.sendMessage(chatManager.prefixedMessage("commands.did_you_mean").replace("%command%", optionalMatch.get().usage()));
				return;
			}

			arguments.sendMessage(chatManager.prefixedMessage("commands.did_you_mean").replace("%command%", '/' + label));
		}
	}

	@Command(
		name = "kotl.stats",
		usage = "/kotl stats <player name>",
		max = 1,
		senderType = Command.SenderType.PLAYER
	)
	public void statsCommand(CommandArguments arguments) {
		final Player sender = arguments.getSender();
		final var user = plugin.getUserManager().getUser(sender);

		if (arguments.isArgumentsEmpty()) {
			chatManager.getStringList("commands.stats-command.messages").stream().map(message -> formatStats(message, true, user)).forEach(arguments::sendMessage);
			return;
		}

		arguments.getPlayer(0).ifPresentOrElse(player -> {
			final var targetUser = plugin.getUserManager().getUser(player);
			final var self = sender.equals(player);

			chatManager.getStringList("commands.stats-command.messages").stream().map(message -> formatStats(message, self, targetUser)).forEach(arguments::sendMessage);
		}, () -> arguments.sendMessage(chatManager.prefixedMessage("commands.player_not_found")));
	}

	private String formatStats(String message, boolean self, User user) {
		message = message.replace("%header%", chatManager.message("commands.stats-command.header" + (self ? "" : "-other")));
		message = message.replace("%player%", user.getName());
		message = message.replace("%tours_played%", StatsStorage.StatisticType.TOURS_PLAYED.from(user));
		message = message.replace("%score%", StatsStorage.StatisticType.SCORE.from(user));
		message = message.replace("%kills%", StatsStorage.StatisticType.KILLS.from(user));
		message = message.replace("%deaths%", StatsStorage.StatisticType.DEATHS.from(user));
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

				if (plugin.getUserManager().getDatabase() instanceof MysqlManager mysqlManager) {
					try (Connection connection = plugin.getMysqlDatabase().getConnection()) {
						Statement statement = connection.createStatement();
						ResultSet set = statement.executeQuery("SELECT name FROM %s WHERE UUID='%s'".formatted(mysqlManager.getTable(), current.toString()));

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

	public String getMatchingParts(String matched, String current) {
		String[] matchedArray = matched.split("\\."), currentArray = current.split("\\.");
		int max = Math.min(matchedArray.length, currentArray.length);
		List<String> matchingParts = new ArrayList<>();

		for (int i = 0; i < max; i++) {
			if (matchedArray[i].equals(currentArray[i])) {
				matchingParts.add(matchedArray[i]);
			}
		}

		return String.join(".", matchingParts);
	}
}