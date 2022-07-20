package me.despical.kotl.command.admin;

import me.despical.kotl.arena.Arena;
import me.despical.kotl.arena.ArenaRegistry;
import me.despical.kotl.command.SubCommand;
import org.bukkit.command.CommandSender;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Despical
 * <p>
 * Created at 27.06.2022
 */
public class ListCommand extends SubCommand {

	public ListCommand() {
		super ("list");

		setPermission("kotl.admin.list");
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
		final Set<Arena> arenas = ArenaRegistry.getArenas();

		if (arenas.isEmpty()) {
			sender.sendMessage(chatManager.prefixedMessage("commands.list_command.no_arenas_created"));
			return;
		}

		final String arenaNames = arenas.stream().map(Arena::getId).collect(Collectors.joining(", "));
		sender.sendMessage(chatManager.prefixedMessage("commands.list_command.format").replace("%list%", arenaNames));
	}

	@Override
	public String getTutorial() {
		return "Shows all of the existing arenas";
	}

	@Override
	public int getType() {
		return GENERIC;
	}

	@Override
	public int getSenderType() {
		return BOTH;
	}
}