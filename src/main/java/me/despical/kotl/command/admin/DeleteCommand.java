package me.despical.kotl.command.admin;

import me.despical.commons.configuration.ConfigUtils;
import me.despical.commons.miscellaneous.AttributeUtils;
import me.despical.commons.serializer.InventorySerializer;
import me.despical.kotl.ConfigPreferences;
import me.despical.kotl.arena.Arena;
import me.despical.kotl.arena.ArenaRegistry;
import me.despical.kotl.command.SubCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Despical
 * <p>
 * Created at 27.06.2022
 */
public class DeleteCommand extends SubCommand {

	private final Set<CommandSender> confirmations;

	public DeleteCommand() {
		super ("delete");
		this.confirmations = new HashSet<>();

		setPermission("kotl.admin.delete");
	}

	@Override
	public String getPossibleArguments() {
		return "<arena>";
	}

	@Override
	public int getMinimumArguments() {
		return 0;
	}

	@Override
	public void execute(CommandSender sender, String label, String[] args) {
		if (args.length == 0) {
			sender.sendMessage(chatManager.prefixedMessage("commands.type_arena_name"));
			return;
		}

		String arenaName = args[0];
		Arena arena = ArenaRegistry.getArena(arenaName);

		if (arena == null) {
			sender.sendMessage(chatManager.prefixedMessage("commands.no_arena_like_that"));
			return;
		}

		if (!confirmations.contains(sender)) {
			confirmations.add(sender);
			plugin.getServer().getScheduler().runTaskLater(plugin, () -> confirmations.remove(sender), 200);
			sender.sendMessage(chatManager.prefixedMessage("commands.are_you_sure"));
			return;
		}

		confirmations.remove(sender);

		arena.deleteHologram();

		if (!arena.getPlayers().isEmpty()) {
			arena.getScoreboardManager().stopAllScoreboards();

			for (Player player : arena.getPlayers()) {
				player.setWalkSpeed(0.2f);

				if (plugin.getConfigPreferences().getOption(ConfigPreferences.Option.INVENTORY_MANAGER_ENABLED)) {
					InventorySerializer.loadInventory(plugin, player);
				} else {
					player.getInventory().clear();
					player.getInventory().setArmorContents(null);
					player.getActivePotionEffects().forEach(effect -> player.removePotionEffect(effect.getType()));
				}

				AttributeUtils.resetAttackCooldown(player);
				arena.doBarAction(Arena.BarAction.REMOVE, player);
			}

			arena.teleportAllToEndLocation();
			arena.getPlayers().clear();
		}

		ArenaRegistry.unregisterArena(arena);

		config.set("instances." + args[0], null);
		ConfigUtils.saveConfig(plugin, config, "arenas");

		sender.sendMessage(chatManager.prefixedMessage("commands.removed_game_instance"));
	}

	@Override
	public String getTutorial() {
		return "Deletes arena with the current configuration";
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