package me.despical.kotl.commands.admin.arena;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;

import me.despical.commonsbox.serializer.InventorySerializer;
import me.despical.kotl.ConfigPreferences;
import me.despical.kotl.arena.Arena;
import me.despical.kotl.arena.ArenaRegistry;
import me.despical.kotl.commands.SubCommand;
import me.despical.kotl.commands.exception.CommandException;
import me.despical.kotl.utils.Debugger;

/**
 * @author Despical
 * <p>
 * Created at 22.06.2020
 */
public class ReloadCommand extends SubCommand {

	private final Set<CommandSender> confirmations = new HashSet<>();
	
	public ReloadCommand() {
		super("reload");
		setPermission("kotl.admin");
	}

	@Override
	public String getPossibleArguments() {
		return "";
	}

	@Override
	public int getMinimumArguments() {
		return 0;
	}

	@Override
	public void execute(CommandSender sender, String label, String[] args) throws CommandException {
		if(!(confirmations.contains(sender))) {
			confirmations.add(sender);
			Bukkit.getScheduler().runTaskLater(this.getPlugin(), () -> confirmations.remove(sender), 20 * 10);
			sender.sendMessage(getPlugin().getChatManager().getPrefix() + getPlugin().getChatManager().colorMessage("Commands.Are-You-Sure"));
			return;
		}
		confirmations.remove(sender);
		Debugger.debug(Level.INFO, "Initialized plugin reload by {0}", sender.getName());
		long start = System.currentTimeMillis();
		
		getPlugin().reloadConfig();
		getPlugin().getChatManager().reloadConfig();
		for (Arena arena : ArenaRegistry.getArenas()) {
			Debugger.debug(Level.INFO, "[Reloader] Stopping arena called {0}", arena.getId());
			long stop = System.currentTimeMillis();
			if (arena.getHologram() != null) arena.getHologram().delete();
			for (Player player : arena.getPlayers()) {
				if (this.getPlugin().getConfigPreferences().getOption(ConfigPreferences.Option.INVENTORY_MANAGER_ENABLED)) {
					InventorySerializer.loadInventory(this.getPlugin(), player);
				}
				player.getInventory().clear();
				player.getInventory().setArmorContents(null);
				player.setWalkSpeed(0.2f);
				player.getAttribute(Attribute.GENERIC_ATTACK_SPEED).setBaseValue(4);
				arena.doBarAction(Arena.BarAction.REMOVE, player);
				arena.getScoreboardManager().removeScoreboard(getPlugin().getUserManager().getUser(player));
				for (PotionEffect effect : player.getActivePotionEffects()) {
					player.removePotionEffect(effect.getType());
				}
			}
			arena.teleportAllToEndLocation();
			arena.getPlayers().clear();
			Debugger.debug(Level.INFO, "[Reloader] Arena {0} stopped, took {1} ms", arena.getId(), System.currentTimeMillis() - stop);
		}
		ArenaRegistry.registerArenas();
		sender.sendMessage(getPlugin().getChatManager().getPrefix() + getPlugin().getChatManager().colorMessage("Commands.Success-Reload"));
		Debugger.debug(Level.INFO, "[Reloader] Finished reloading took {0} ms", System.currentTimeMillis() - start);
	}

	@Override
	public List<String> getTutorial() {
		return Arrays.asList("Reloads all game arenas and their configuration" , "All of the arenas will be stoped!");
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