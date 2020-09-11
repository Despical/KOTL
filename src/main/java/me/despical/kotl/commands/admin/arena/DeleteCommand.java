package me.despical.kotl.commands.admin.arena;

import java.util.*;

import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;

import me.despical.commonsbox.configuration.ConfigUtils;
import me.despical.commonsbox.serializer.InventorySerializer;
import me.despical.kotl.ConfigPreferences;
import me.despical.kotl.arena.Arena;
import me.despical.kotl.arena.ArenaRegistry;
import me.despical.kotl.commands.SubCommand;
import me.despical.kotl.commands.exception.CommandException;

/**
 * @author Despical
 * <p>
 * Created at 22.06.2020
 */
public class DeleteCommand extends SubCommand {

	private final Set<CommandSender> confirmations = new HashSet<>();
	
	public DeleteCommand() {
		super("delete");
		setPermission("kotl.admin.delete");
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
		Arena arena = ArenaRegistry.getArena(args[0]);
		if (arena == null) {
			sender.sendMessage(getPlugin().getChatManager().getPrefix() + getPlugin().getChatManager().colorMessage("Commands.No-Arena-Like-That"));
			return;
		}
		if (!confirmations.contains(sender)) {
			confirmations.add(sender);
			Bukkit.getScheduler().runTaskLater(getPlugin(), () -> confirmations.remove(sender), 20 * 10);
			sender.sendMessage(getPlugin().getChatManager().getPrefix() + getPlugin().getChatManager().colorMessage("Commands.Are-You-Sure"));
			return;
		}
		confirmations.remove(sender);
		FileConfiguration config = ConfigUtils.getConfig(getPlugin(), "arenas");
		if (arena.getHologram() != null) arena.getHologram().delete();
		if (arena.getPlayers().size() > 0) {
			arena.getScoreboardManager().stopAllScoreboards();
			for (Player player : arena.getPlayers()) {
				if (getPlugin().getConfigPreferences().getOption(ConfigPreferences.Option.INVENTORY_MANAGER_ENABLED)) {
					InventorySerializer.loadInventory(getPlugin(), player);
				}
				player.getInventory().clear();
				player.getInventory().setArmorContents(null);
				player.setWalkSpeed(0.2f);
				if (!getPlugin().isBefore1_9_R1()) player.getAttribute(Attribute.GENERIC_ATTACK_SPEED).setBaseValue(4);
				arena.doBarAction(Arena.BarAction.REMOVE, player);
				for (PotionEffect effect : player.getActivePotionEffects()) {
					player.removePotionEffect(effect.getType());
				}
			}
			arena.teleportAllToEndLocation();
			arena.getPlayers().clear();
		}
		ArenaRegistry.unregisterArena(arena);
		config.set("instances." + args[0], null);
		ConfigUtils.saveConfig(getPlugin(), config, "arenas");
		sender.sendMessage(getPlugin().getChatManager().getPrefix() + getPlugin().getChatManager().colorMessage("Commands.Removed-Game-Instance"));
	}

	@Override
	public List<String> getTutorial() {
		return Collections.singletonList("Deletes arena with the current configuration");
	}

	@Override
	public CommandType getType() {
		return CommandType.GENERIC;
	}

	@Override
	public SenderType getSenderType() {
		return SenderType.BOTH;
	}
}