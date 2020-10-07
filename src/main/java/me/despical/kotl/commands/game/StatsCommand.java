/*
 * KOTL - Don't let others to climb top of the ladders!
 * Copyright (C) 2020 Despical
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package me.despical.kotl.commands.game;

import me.despical.kotl.api.StatsStorage;
import me.despical.kotl.commands.SubCommand;
import me.despical.kotl.user.User;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * @author Despical
 * <p>
 * Created at 22.06.2020
 */
public class StatsCommand extends SubCommand {

	public StatsCommand() {
		super("stats");
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
		Player player = args.length  == 1 ? Bukkit.getPlayerExact(args[0]) : (Player) sender;

		if (player == null) {
			sender.sendMessage(getPlugin().getChatManager().getPrefix() + getPlugin().getChatManager().colorMessage("Commands.Player-Not-Found"));
			return;
		}

		User user = getPlugin().getUserManager().getUser(player);

		if (player.equals(sender)) {
			sender.sendMessage(getPlugin().getChatManager().colorMessage("Commands.Stats-Command.Header", player));
		} else {
			sender.sendMessage(getPlugin().getChatManager().colorMessage("Commands.Stats-Command.Header-Other", player).replace("%player%", player.getName()));
		}

		sender.sendMessage(getPlugin().getChatManager().colorMessage("Commands.Stats-Command.Tours-Played", player) + user.getStat(StatsStorage.StatisticType.TOURS_PLAYED));
		sender.sendMessage(getPlugin().getChatManager().colorMessage("Commands.Stats-Command.Score", player) + user.getStat(StatsStorage.StatisticType.SCORE));
		sender.sendMessage(getPlugin().getChatManager().colorMessage("Commands.Stats-Command.Footer", player));
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