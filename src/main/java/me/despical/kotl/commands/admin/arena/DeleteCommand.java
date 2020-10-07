/*
 * KOTL - Don't let others to climb top of the ladders!
 * Copyright (C) 2020 Despical
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package me.despical.kotl.commands.admin.arena;

import me.despical.commonsbox.configuration.ConfigUtils;
import me.despical.commonsbox.serializer.InventorySerializer;
import me.despical.kotl.ConfigPreferences;
import me.despical.kotl.arena.Arena;
import me.despical.kotl.arena.ArenaRegistry;
import me.despical.kotl.commands.SubCommand;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
	public void execute(CommandSender sender, String label, String[] args) {
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

		arena.getHologram().delete();

		if (arena.getPlayers().size() > 0) {
			arena.getScoreboardManager().stopAllScoreboards();

			for (Player player : arena.getPlayers()) {
				if (getPlugin().getConfigPreferences().getOption(ConfigPreferences.Option.INVENTORY_MANAGER_ENABLED)) {
					InventorySerializer.loadInventory(getPlugin(), player);
				}

				player.getInventory().clear();
				player.getInventory().setArmorContents(null);
				player.setWalkSpeed(0.2f);
				player.getActivePotionEffects().forEach(effect -> player.removePotionEffect(effect.getType()));
				if (!getPlugin().isBefore1_9_R1()) player.getAttribute(Attribute.GENERIC_ATTACK_SPEED).setBaseValue(4);

				arena.doBarAction(Arena.BarAction.REMOVE, player);
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