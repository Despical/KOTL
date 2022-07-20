package me.despical.kotl.command.admin;

import me.despical.kotl.arena.Arena;
import me.despical.kotl.arena.ArenaRegistry;
import me.despical.kotl.command.SubCommand;
import me.despical.kotl.handler.setup.SetupInventory;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * @author Despical
 * <p>
 * Created at 27.06.2022
 */
public class EditCommand extends SubCommand {

	public EditCommand() {
		super ("edit");

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
	public void execute(CommandSender sender, String label, String[] args) {
		final Arena arena = ArenaRegistry.getArena(args[0]);

		if (arena == null) {
			sender.sendMessage(chatManager.prefixedMessage("commands.no_arena_like_that"));
			return;
		}

		new SetupInventory(arena, (Player) sender).openInventory();
	}

	@Override
	public String getTutorial() {
		return "Opens the arena editor";
	}

	@Override
	public int getType() {
		return GENERIC;
	}

	@Override
	public int getSenderType() {
		return PLAYER;
	}
}