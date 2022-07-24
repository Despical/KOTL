package me.despical.kotl.command;

import me.despical.commandframework.Command;
import me.despical.commandframework.CommandArguments;
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
import java.util.Map;
import java.util.UUID;

/**
 * @author Despical
 * <p>
 * Created at 24.07.2022
 */
public class PlayerCommands {

	private final Main plugin;
	private final ChatManager chatManager;

	public PlayerCommands(Main plugin) {
		this.plugin = plugin;
		this.chatManager = plugin.getChatManager();
		this.plugin.getCommandFramework().registerCommands(this);
	}

	@Command(
		name = "kotl"
	)
	public void mainCommand(CommandArguments arguments) {
		if (arguments.isArgumentsEmpty()) {
			arguments.sendMessage(chatManager.coloredRawMessage("&3This server is running &bKing of the Ladder &3v" + plugin.getDescription().getVersion() + " by &bDespical"));

			if (arguments.hasPermission("kotl.admin")) {
				arguments.sendMessage(chatManager.coloredRawMessage("&3Commands: &b/" + arguments.getLabel() + " help"));
				arguments.sendMessage(plugin.getChatManager().coloredRawMessage("&3If you liked this version then consider buying the premium one with better performance and additional features."));
				arguments.sendMessage(plugin.getChatManager().coloredRawMessage("&3>> &bhttps://www.spigotmc.org/resources/king-of-the-ladder-premium-1-8-1-19.102644/"));
			}
		}
	}

	@Command(
		name = "kotl.stats",
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
		senderType = Command.SenderType.PLAYER
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
		sender.sendMessage(plugin.getChatManager().message("commands.statistics.header"));

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
						ResultSet set = statement.executeQuery("SELECT name FROM " + ((MysqlManager) plugin.getUserManager().getDatabase()).getTableName() + " WHERE UUID='" + current.toString() + "'");

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

		message = StringUtils.replace(message, "%position%", Integer.toString(position));
		message = StringUtils.replace(message, "%name%", playerName);
		message = StringUtils.replace(message, "%value%", Integer.toString(value));
		message = StringUtils.replace(message, "%statistic%", statisticName);
		return message;
	}
}