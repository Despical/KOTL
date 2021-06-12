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

package me.despical.kotl.commands;

import java.util.*;
import java.util.stream.Collectors;

import me.despical.commons.string.StringMatcher;
import me.despical.kotl.handlers.ChatManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandException;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.despical.kotl.Main;
import me.despical.kotl.commands.SubCommand.SenderType;
import me.despical.kotl.commands.admin.HelpCommand;
import me.despical.kotl.commands.admin.ListCommand;
import me.despical.kotl.commands.admin.arena.DeleteCommand;
import me.despical.kotl.commands.admin.arena.EditCommand;
import me.despical.kotl.commands.admin.arena.ReloadCommand;
import me.despical.kotl.commands.admin.arena.CreateCommand;
import me.despical.kotl.commands.game.LeaderBoardCommand;
import me.despical.kotl.commands.game.StatsCommand;
import org.jetbrains.annotations.NotNull;

/**
 * @author Despical
 * <p>
 * Created at 22.06.2020
 */
public class CommandHandler implements CommandExecutor {

	private final Main plugin;
	private final Set<SubCommand> subCommands;

	public CommandHandler(Main plugin) {
		this.plugin = plugin;
		this.subCommands = new HashSet<>();

		registerSubCommand(new CreateCommand());
		registerSubCommand(new EditCommand());
		registerSubCommand(new ListCommand());
		registerSubCommand(new DeleteCommand());
		registerSubCommand(new ReloadCommand());
		registerSubCommand(new HelpCommand());
		registerSubCommand(new StatsCommand());
		registerSubCommand(new LeaderBoardCommand());

		Optional.ofNullable(plugin.getCommand("kotl")).ifPresent(kotl -> {
			kotl.setExecutor(this);
			kotl.setTabCompleter(new TabCompletion(this));
		});
	}
	
	public void registerSubCommand(SubCommand subCommand) {
		subCommands.add(subCommand);
	}
	
	public Set<SubCommand> getSubCommands() {
		return new HashSet<>(subCommands);
	}

	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
		ChatManager chatManager = plugin.getChatManager();

		if (args.length == 0) {
			sender.sendMessage(chatManager.coloredRawMessage("&3This server is running &bKing of the Ladder &3v" + plugin.getDescription().getVersion() + " by &bDespical"));

			if (sender.hasPermission("kotl.admin")) {
				sender.sendMessage(chatManager.coloredRawMessage("&3Commands: &b" + label + " help"));
			}

			return true;
		}
		
		for (SubCommand subCommand : subCommands) {
			if (subCommand.isValidTrigger(args[0])) {
				if (!subCommand.hasPermission(sender)) {
					sender.sendMessage(chatManager.prefixedMessage("Commands.No-Permission"));
					return true;
				}

				if (subCommand.getSenderType() == SenderType.PLAYER && !(sender instanceof Player)) {
					sender.sendMessage(chatManager.prefixedMessage("Commands.Only-By-Player"));
					return false;
				}

				if (args.length - 1 >= subCommand.getMinimumArguments()) {
					try {
						subCommand.execute(sender, Arrays.copyOfRange(args, 1, args.length));
					} catch (Exception e) {
						sender.sendMessage(chatManager.coloredRawMessage("&c" + e.getMessage()));
					}
				} else {
					if (subCommand.getType() == SubCommand.CommandType.GENERIC) {
						sender.sendMessage(chatManager.coloredRawMessage("&cUsage: /" + label + " " + subCommand.getName() + " " + (subCommand.getPossibleArguments().length() > 0 ? subCommand.getPossibleArguments() : "")));
					}
				}

				return true;
			}
		}

		List<StringMatcher.Match> matches = StringMatcher.match(args[0], subCommands.stream().map(SubCommand::getName).collect(Collectors.toList()));

		if (!matches.isEmpty()) {
          sender.sendMessage(chatManager.prefixedMessage("Commands.Did-You-Mean").replace("%command%", label + " " + matches.get(0).getMatch()));
          return true;
        }

        return true;
	}
}