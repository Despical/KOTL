/*
 * KOTL - Don't let others to climb top of the ladders!
 * Copyright (C) 2022 Despical
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

package me.despical.kotl.command.player;

import me.despical.commandframework.Command;
import me.despical.commandframework.CommandArguments;
import me.despical.commons.string.StringMatcher;
import me.despical.kotl.ConfigPreferences;
import me.despical.kotl.Main;
import me.despical.kotl.api.StatsStorage;
import me.despical.kotl.handler.ChatManager;
import me.despical.kotl.user.User;
import me.despical.kotl.user.data.MysqlManager;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
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
 * Created at 27.06.2021
 */
public class PlayerCommands {

	private final Main plugin;
	private final ChatManager chatManager;

	public PlayerCommands(Main plugin) {
		this.plugin = plugin;
		this.chatManager = plugin.getChatManager();

		this.plugin.getCommandFramework().registerCommands(this);
		this.plugin.getCommandFramework().setAnyMatch(arguments -> {
			if (arguments.isArgumentsEmpty()) return;

			String label = arguments.getLabel();
			List<StringMatcher.Match> matches = StringMatcher.match(arguments.getArgument(0), plugin.getCommandFramework().getCommands().stream().map(cmd -> cmd.name().replace(label + '.', "")).collect(Collectors.toList()));

			if (!matches.isEmpty()) {
				arguments.sendMessage(chatManager.message("commands.did_you_mean").replace("%command%", label + " " + matches.get(0).getMatch()));
			}
		});
	}

	@Command(
		name = "kotl.stats",
		usage = "/kotl stats [<player>]",
		senderType = Command.SenderType.PLAYER
	)
	public void statsCommand(CommandArguments arguments) {
		Player sender = arguments.getSender(), player = !arguments.isArgumentsEmpty() ? Bukkit.getPlayer(arguments.getArgument(0)) : sender;

		if (player == null) {
			arguments.sendMessage(chatManager.prefixedMessage("commands.player_not_found"));
			return;
		}

		User user = plugin.getUserManager().getUser(player);
		String path = "commands.stats_command.";

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
		name = "kotl.top",
		usage = "/kotl top <statisticType>",
		senderType = Command.SenderType.PLAYER
	)
	public void leaderboardCommand(CommandArguments arguments) {
		if (arguments.isArgumentsEmpty()) {
			arguments.sendMessage(chatManager.prefixedMessage("commands.statistics.type_name"));
			return;
		}

		try {
			printLeaderboard(arguments.getSender(), StatsStorage.StatisticType.valueOf(arguments.getArgument(0).toUpperCase(java.util.Locale.ENGLISH)));
		} catch (IllegalArgumentException exception) {
			arguments.sendMessage(chatManager.prefixedMessage("commands.statistics.invalid_name"));
		}
	}

	private void printLeaderboard(CommandSender sender, StatsStorage.StatisticType statisticType) {
		Map<UUID, Integer> stats = StatsStorage.getStats(statisticType);
		sender.sendMessage(plugin.getChatManager().message("commands.statistics.header"));

		String statistic = StringUtils.capitalize(statisticType.name().toLowerCase(java.util.Locale.ENGLISH).replace("_", " "));

		Object[] array = stats.keySet().toArray();
		UUID current = (UUID) array[array.length == 0 ? 0 : array.length - 1];

		for (int i = 0; i < 10; i++) {
			try {
				sender.sendMessage(formatMessage(statistic, plugin.getServer().getOfflinePlayer(current).getName(), i + 1, stats.remove(current)));
			} catch (IndexOutOfBoundsException ex) {
				sender.sendMessage(formatMessage(statistic, "Empty", i + 1, 0));
			} catch (NullPointerException ex) {
				if (plugin.getConfigPreferences().getOption(ConfigPreferences.Option.DATABASE_ENABLED)) {
					try (Connection connection = plugin.getMysqlDatabase().getConnection()) {
						Statement statement = connection.createStatement();
						ResultSet set = statement.executeQuery("SELECT name FROM " + ((MysqlManager) plugin.getUserManager().getDatabase()).getTableName() + " WHERE UUID='" + current.toString() + "'");

						if (set.next()) {
							sender.sendMessage(formatMessage(statistic, set.getString(1), i + 1, stats.get(current)));
							continue;
						}
					} catch (SQLException ignored) {}
				}

				sender.sendMessage(formatMessage(statistic, "Unknown Player", i + 1, 0));
			}
		}
	}

	private String formatMessage(String statisticName, String playerName, int position, int value) {
		String message = chatManager.message("commands.statistics.format");

		message = StringUtils.replace(message, "%position%", Integer.toString(position));
		message = StringUtils.replace(message, "%name%", playerName);
		message = StringUtils.replace(message, "%value%", Integer.toString(value));
		message = StringUtils.replace(message, "%statistic%", statisticName);
		return message;
	}
}