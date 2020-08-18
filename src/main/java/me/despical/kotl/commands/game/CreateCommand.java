package me.despical.kotl.commands.game;

import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import me.despical.commonsbox.configuration.ConfigUtils;
import me.despical.commonsbox.serializer.LocationSerializer;
import me.despical.kotl.arena.Arena;
import me.despical.kotl.arena.ArenaRegistry;
import me.despical.kotl.commands.SubCommand;
import me.despical.kotl.commands.exception.CommandException;

/**
 * @author Despical
 * <p>
 * Created at 22.06.2020
 */
public class CreateCommand extends SubCommand {

	public CreateCommand() {
		super("create");
		setPermission("kotl.admin.create");
	}

	@Override
	public String getPossibleArguments() {
		return "<arenaName>";
	}

	@Override
	public int getMinimumArguments() {
		return 1;
	}

	@Override
	public void execute(CommandSender sender, String label, String[] args) throws CommandException {
		Player player = (Player) sender;
		FileConfiguration config = ConfigUtils.getConfig(this.getPlugin(), "arenas");
		Arena arena = ArenaRegistry.getArena(args[0]);
		
		if(arena != null || config.contains("instances." + args[0])) {
			player.sendMessage(getPlugin().getChatManager().getPrefix() + ChatColor.RED + "Arena with that ID already contains!");
			player.sendMessage(getPlugin().getChatManager().getPrefix() + ChatColor.RED + "To check existing arenas use: /kotl list");
			return;
		}
		
		setupDefaultConfiguration(args[0]);
		player.sendMessage(ChatColor.BOLD + "------------------------------------------");
        player.sendMessage(ChatColor.YELLOW + "      Instance " + args[0] + " created!");
        player.sendMessage("");
        player.sendMessage(ChatColor.GREEN + "Edit this arena via " + ChatColor.GOLD + "/kotl " + "edit " + args[0] + ChatColor.GREEN + "!");
        player.sendMessage(ChatColor.BOLD + "------------------------------------------- ");
		return;
	}
	
	private void setupDefaultConfiguration(String id) {
		String path = "instances." + id + ".";
		FileConfiguration config = ConfigUtils.getConfig(this.getPlugin(), "arenas");
		config.set(path + "endlocation", LocationSerializer.locationToString(Bukkit.getServer().getWorlds().get(0).getSpawnLocation()));
		config.set(path + "areaMin", LocationSerializer.locationToString(Bukkit.getServer().getWorlds().get(0).getSpawnLocation()));
		config.set(path + "areaMax", LocationSerializer.locationToString(Bukkit.getServer().getWorlds().get(0).getSpawnLocation()));
		config.set(path + "isdone", false);
		config.set(path + "hologramLocation", LocationSerializer.locationToString(Bukkit.getServer().getWorlds().get(0).getSpawnLocation()));
		config.set(path + "plateLocation", LocationSerializer.locationToString(Bukkit.getServer().getWorlds().get(0).getSpawnLocation()));
		ConfigUtils.saveConfig(this.getPlugin(), config, "arenas");
		Arena arena = new Arena(id);
		
		arena.setEndLocation(LocationSerializer.locationFromString(config.getString(path + "endLocation")));
		arena.setPlateLocation(LocationSerializer.locationFromString(config.getString(path + "plateLocation")));
		arena.setHologramLocation(LocationSerializer.locationFromString(config.getString(path + "hologramLocation")));
		arena.setReady(false);
		
		ArenaRegistry.registerArena(arena);
	}

	@Override
	public List<String> getTutorial() {
		return Arrays.asList("Creates a new arena with default configuration");
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