package me.despical.kotl.commands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import me.despical.kotl.api.StatsStorage;
import me.despical.kotl.arena.Arena;
import me.despical.kotl.arena.ArenaRegistry;

/**
 * @author Despical
 * <p>
 * Created at 22.06.2020
 */
public class TabCompletion implements TabCompleter {

	public List<String> commands = new ArrayList<>();;
	
	public TabCompletion(CommandHandler commandHandler) {
		for (SubCommand command : commandHandler.getSubCommands()) {
			this.commands.add(command.getName().toLowerCase());
		}
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		List<String> completions = new ArrayList<>();
		if (!(sender instanceof Player)) {
			return Collections.emptyList();
		}
		Player player = (Player) sender;
		if (!(player.hasPermission("kotl.admin"))) {
			return Collections.emptyList();
		}
		if (args.length == 1) {
			StringUtil.copyPartialMatches(args[0], commands, completions);
		} 
		if (args.length == 2) {
			if (args[0].equalsIgnoreCase("create") || args[0].equalsIgnoreCase("help") || args[0].equalsIgnoreCase("list") ||
				args[0].equalsIgnoreCase("reload")) {
				return Collections.emptyList();
			}
			if (args[0].equalsIgnoreCase("top")) {
				List<String> possibilities = new ArrayList<>();
				for (StatsStorage.StatisticType statistic : StatsStorage.StatisticType.values()) {
					possibilities.add(statistic.name().toLowerCase());
				}
				return possibilities;
			}
			List<String> arenas = new ArrayList<>();
			for (Arena arena : ArenaRegistry.getArenas()) {
				arenas.add(arena.getId());
			}
			StringUtil.copyPartialMatches(args[1], arenas, completions);
			Collections.sort(arenas);
			return arenas;
		}
		Collections.sort(completions);
		return completions;
	}
}