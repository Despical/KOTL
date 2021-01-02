/*
 * KOTL - Don't let others to climb top of the ladders!
 * Copyright (C) 2021 Despical and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package me.despical.kotl.commands.admin.arena;

import me.despical.commonsbox.miscellaneous.AttributeUtils;
import me.despical.commonsbox.serializer.InventorySerializer;
import me.despical.kotl.ConfigPreferences;
import me.despical.kotl.arena.Arena;
import me.despical.kotl.arena.ArenaRegistry;
import me.despical.kotl.commands.SubCommand;
import me.despical.kotl.handlers.ChatManager;
import me.despical.kotl.utils.Debugger;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
	public void execute(CommandSender sender, ChatManager chatManager, String[] args) {
		if(!(confirmations.contains(sender))) {
			confirmations.add(sender);
			Bukkit.getScheduler().runTaskLater(plugin, () -> confirmations.remove(sender), 20 * 10);
			sender.sendMessage(chatManager.getPrefix() + chatManager.colorMessage("Commands.Are-You-Sure"));
			return;
		}

		confirmations.remove(sender);
		Debugger.debug("Initialized plugin reload by {0}", sender.getName());

		long start = System.currentTimeMillis();
		
		plugin.reloadConfig();
		chatManager.reloadConfig();

		for (Arena arena : ArenaRegistry.getArenas()) {
			Debugger.debug("[Reloader] Stopping arena called {0}", arena.getId());
			long stop = System.currentTimeMillis();

			if (arena.getHologram() != null) arena.getHologram().delete();

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
				arena.getScoreboardManager().removeScoreboard(plugin.getUserManager().getUser(player));
				plugin.getUserManager().getUser(player).removeScoreboard();
			}

			arena.teleportAllToEndLocation();
			arena.getPlayers().clear();

			Debugger.debug("[Reloader] Arena {0} stopped, took {1} ms", arena.getId(), System.currentTimeMillis() - stop);
		}

		ArenaRegistry.registerArenas();
		sender.sendMessage(chatManager.getPrefix() + chatManager.colorMessage("Commands.Success-Reload"));

		Debugger.debug("[Reloader] Finished reloading took {0} ms", System.currentTimeMillis() - start);
	}

	@Override
	public List<String> getTutorial() {
		return Arrays.asList("Reloads all game arenas and their configuration" , "All of the arenas will be stopped!");
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