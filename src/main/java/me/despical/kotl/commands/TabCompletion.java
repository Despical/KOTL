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

import me.despical.commons.util.Collections;
import me.despical.kotl.arena.Arena;
import me.despical.kotl.arena.ArenaRegistry;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Despical
 * <p>
 * Created at 22.06.2020
 */
public class TabCompletion implements TabCompleter {

	public final CommandHandler commandHandler;

	public TabCompletion(CommandHandler commandHandler) {
		this.commandHandler = commandHandler;
	}

	@Override
	public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
		List<String> completions = new ArrayList<>(), commands = commandHandler.getSubCommands().stream().map(command -> command.getName().toLowerCase()).collect(Collectors.toList());

		if (args.length == 1) {
			StringUtil.copyPartialMatches(args[0], commands, completions);
		}

		if (args.length == 2) {
			if (Collections.contains(args[0], "create", "help", "list", "reload", "randomjoin", "stop")) {
				return null;
			}

			if (args[0].equalsIgnoreCase("top")) {
				return Collections.listOf("tours_played", "score");
			}

			List<String> arenas = ArenaRegistry.getArenas().stream().map(Arena::getId).collect(Collectors.toList());

			StringUtil.copyPartialMatches(args[1], arenas, completions);
			arenas.sort(null);
			return arenas;
		}

		completions.sort(null);
		return completions;
	}
}