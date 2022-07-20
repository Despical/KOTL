package me.despical.kotl.command.admin;

import me.despical.commons.miscellaneous.AttributeUtils;
import me.despical.commons.serializer.InventorySerializer;
import me.despical.commons.util.LogUtils;
import me.despical.kotl.ConfigPreferences;
import me.despical.kotl.arena.Arena;
import me.despical.kotl.arena.ArenaRegistry;
import me.despical.kotl.command.SubCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * @author Despical
 * <p>
 * Created at 27.06.2022
 */
public class ReloadCommand extends SubCommand {

	public ReloadCommand() {
		super ("reload");

		setPermission("kotl.admin.reload");
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
		LogUtils.log("Initialized plugin reload by {0}.", sender.getName());

		final long start = System.currentTimeMillis();

		plugin.reloadConfig();
		plugin.getChatManager().reloadConfig();

		for (Arena arena : ArenaRegistry.getArenas()) {
			LogUtils.log("Stopping arena called {0}.", arena.getId());

			arena.deleteHologram();
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

				arena.doBarAction(Arena.BarAction.REMOVE, player);
				AttributeUtils.resetAttackCooldown(player);
			}

			arena.teleportAllToEndLocation();
			arena.getPlayers().clear();
		}

		ArenaRegistry.registerArenas();
		sender.sendMessage(chatManager.prefixedMessage("commands.success_reload"));

		LogUtils.log("Finished reloading took {0} ms", System.currentTimeMillis() - start);
	}

	@Override
	public String getTutorial() {
		return "Reloads all of the system configuration and arenas";
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