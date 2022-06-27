package me.despical.kotl.command.admin;

import me.despical.commons.configuration.ConfigUtils;
import me.despical.commons.miscellaneous.MiscUtils;
import me.despical.commons.serializer.LocationSerializer;
import me.despical.kotl.arena.Arena;
import me.despical.kotl.arena.ArenaRegistry;
import me.despical.kotl.command.SubCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static me.despical.kotl.handler.setup.SetupInventory.TUTORIAL_VIDEO;

/**
 * @author Despical
 * <p>
 * Created at 27.06.2022
 */
public class CreateCommand extends SubCommand {

	public CreateCommand() {
		super ("create");

		setPermission("kotl.admin.create");
	}

	@Override
	public String getPossibleArguments() {
		return "<id>";
	}

	@Override
	public int getMinimumArguments() {
		return 1;
	}

	@Override
	public void execute(CommandSender sender, String label, String[] args) {
		Player player = (Player) sender;

		if (args.length == 0) {
			player.sendMessage(chatManager.prefixedRawMessage("&cPlease enter an name to create an arena!"));
			return;
		}

		String arg = args[0];

		if (ArenaRegistry.isArena(arg)) {
			player.sendMessage(chatManager.prefixedRawMessage("&cArena with that ID already contains!"));
			player.sendMessage(chatManager.prefixedRawMessage("&cTo check existing arenas use: /kotl list"));
			return;
		}

		setupDefaultConfiguration(arg);

		player.sendMessage(chatManager.coloredRawMessage("&l--------------------------------------------"));
		MiscUtils.sendCenteredMessage(player, "&eInstance " + arg + " created!");
		player.sendMessage("");
		MiscUtils.sendCenteredMessage(player, "&aEdit this arena via &6/kotl edit " + arg + "&a!");
		player.sendMessage("");
		MiscUtils.sendCenteredMessage(player, "&6Don't know where to start? Check out tutorial video:");
		MiscUtils.sendCenteredMessage(player, "&7" + TUTORIAL_VIDEO);
		player.sendMessage(chatManager.coloredRawMessage("&l--------------------------------------------"));
	}

	@Override
	public String getTutorial() {
		return "Creates a new arena with default configuration";
	}

	@Override
	public CommandType getType() {
		return CommandType.GENERIC;
	}

	@Override
	public SenderType getSenderType() {
		return SenderType.PLAYER;
	}

	private void setupDefaultConfiguration(String id) {
		String path = "instances." + id + ".", def = LocationSerializer.SERIALIZED_LOCATION;

		config.set(path + "endlocation", def);
		config.set(path + "areaMin", def);
		config.set(path + "areaMax", def);
		config.set(path + "isdone", false);
		config.set(path + "hologramLocation", def);
		config.set(path + "plateLocation", def);

		ConfigUtils.saveConfig(plugin, config, "arenas");

		Arena arena = new Arena(id);
		arena.setEndLocation(LocationSerializer.DEFAULT_LOCATION);
		arena.setPlateLocation(LocationSerializer.DEFAULT_LOCATION);
		arena.setHologramLocation(LocationSerializer.DEFAULT_LOCATION);
		arena.setReady(false);

		ArenaRegistry.registerArena(arena);
	}
}