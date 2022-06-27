package me.despical.kotl.command.player;

import me.despical.kotl.api.StatsStorage;
import me.despical.kotl.command.SubCommand;
import me.despical.kotl.user.User;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * @author Despical
 * <p>
 * Created at 27.06.2022
 */
public class StatsCommand extends SubCommand {

	public StatsCommand() {
		super ("stats");
	}

	@Override
	public String getPossibleArguments() {
		return null;
	}

	@Override
	public int getMinimumArguments() {
		return 0;
	}

	@Override
	public void execute(CommandSender sender, String label, String[] args) {
		Player player = args.length == 0 ? (Player) sender : plugin.getServer().getPlayer(args[0]);

		if (player == null) {
			sender.sendMessage(chatManager.prefixedMessage("commands.player_not_found"));
			return;
		}

		User user = plugin.getUserManager().getUser(player);
		String path = "commands.stats_command.";

		if (player.equals(sender)) {
			sender.sendMessage(chatManager.message(path + "header", player));
		} else {
			sender.sendMessage(chatManager.message(path + "header_other", player));
		}

		sender.sendMessage(chatManager.message(path + "tours_played", player) + user.getStat(StatsStorage.StatisticType.TOURS_PLAYED));
		sender.sendMessage(chatManager.message(path + "score", player) + user.getStat(StatsStorage.StatisticType.SCORE));
		sender.sendMessage(chatManager.message(path + "footer", player));
	}

	@Override
	public String getTutorial() {
		return null;
	}

	@Override
	public CommandType getType() {
		return CommandType.HIDDEN;
	}

	@Override
	public SubCommand.SenderType getSenderType() {
		return SubCommand.SenderType.PLAYER;
	}
}