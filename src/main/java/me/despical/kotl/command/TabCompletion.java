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

package me.despical.kotl.command;

import me.despical.commandframework.CommandArguments;
import me.despical.commandframework.CommandFramework;
import me.despical.commandframework.Completer;
import me.despical.commons.util.Collections;
import me.despical.kotl.arena.Arena;
import me.despical.kotl.arena.ArenaRegistry;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Despical
 * <p>
 * Created at 22.06.2020
 */
public class TabCompletion {

	public CommandFramework commandFramework;

	public TabCompletion(CommandFramework commandFramework) {
		this.commandFramework = commandFramework;
		this.commandFramework.registerCommands(this);
	}

	@Completer(
		name = "kotl"
	)
	public List<String> onTabComplete(CommandArguments arguments) {
		List<String> completions = new ArrayList<>(), commands = commandFramework.getCommands().stream().map(cmd -> cmd.name().replace(arguments.getLabel() + '.', "")).collect(Collectors.toList());
		String[] args = arguments.getArguments();

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

			if (args[0].equalsIgnoreCase("stats")) {
				return Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList());
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