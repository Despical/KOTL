package me.despical.kotl.commands.admin.arena;

import me.despical.kotl.arena.Arena;
import me.despical.kotl.arena.ArenaRegistry;
import me.despical.kotl.commands.SubCommand;
import me.despical.kotl.commands.exception.CommandException;
import me.despical.kotl.handler.setup.SetupInventory;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;

/**
 * @author Despical
 * <p>
 * Created at 22.06.2020
 */
public class EditCommand extends SubCommand {

	public EditCommand(String name) {
		super("edit");
		setPermission("kotl.admin.edit");
	}

	@Override
	public String getPossibleArguments() {
		return "<arena>";
	}

	@Override
	public int getMinimumArguments() {
		return 1;
	}

	@Override
	public void execute(CommandSender sender, String label, String[] args) throws CommandException {
		Player player = (Player) sender;
		Arena arena = ArenaRegistry.getArena(args[0]);

		if (arena == null) {
			player.sendMessage(getPlugin().getChatManager().getPrefix() + getPlugin().getChatManager().colorMessage("Commands.No-Arena-Like-That"));
			return;
		}
		new SetupInventory(arena, player).openInventory();
	}

	@Override
	public List<String> getTutorial() {
		return Arrays.asList("Opens the arena editor");
	}

	@Override
	public CommandType getType() {
		return CommandType.GENERIC;
	}

	@Override
	public SenderType getSenderType() {
		return SenderType.PLAYER;
	}
}