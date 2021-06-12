/*
 * KOTL - Don't let others to climb top of the ladders!
 * Copyright (C) 2021 Despical and contributors
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

package me.despical.kotl.commands.game;

import me.despical.kotl.ConfigPreferences;
import me.despical.kotl.api.StatsStorage;
import me.despical.kotl.commands.SubCommand;
import me.despical.kotl.user.data.MysqlManager;
import org.apache.commons.lang.StringUtils;
import org.bukkit.command.CommandSender;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * @author Despical
 * <p>
 * Created at 22.06.2020
 */
public class LeaderBoardCommand extends SubCommand {

	public LeaderBoardCommand() {
		super("top");
	}

	@Override
	public String getPossibleArguments() {
		return "<statisticType>";
	}

	@Override
	public int getMinimumArguments() {
		return 0;
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		if (args.length == 0) {
			sender.sendMessage(plugin.getChatManager().prefixedMessage("Commands.Statistics.Type-Name"));
			return;
		}

		try {
			printLeaderboard(sender, StatsStorage.StatisticType.valueOf(args[0].toUpperCase(java.util.Locale.ENGLISH)));
		} catch (IllegalArgumentException exception) {
			sender.sendMessage(plugin.getChatManager().prefixedMessage("Commands.Statistics.Invalid-Name"));
		}
	}

	private void printLeaderboard(CommandSender sender, StatsStorage.StatisticType statisticType) {
		Map<UUID, Integer> stats = StatsStorage.getStats(statisticType);
		sender.sendMessage(plugin.getChatManager().message("Commands.Statistics.Header"));

		String statistic = StringUtils.capitalize(statisticType.toString().toLowerCase(java.util.Locale.ENGLISH).replace("_", " "));

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
		String message = plugin.getChatManager().message("Commands.Statistics.Format");

		message = StringUtils.replace(message, "%position%", Integer.toString(position));
		message = StringUtils.replace(message, "%name%", playerName);
		message = StringUtils.replace(message, "%value%", Integer.toString(value));
		message = StringUtils.replace(message, "%statistic%", statisticName);
		return message;
	}

	@Override
	public List<String> getTutorial() {
		return null;
	}

	@Override
	public CommandType getType() {
		return CommandType.HIDDEN;
	}

	@Override
	public SenderType getSenderType() {
		return SenderType.PLAYER;
	}
}