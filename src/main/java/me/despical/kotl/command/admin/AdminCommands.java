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

package me.despical.kotl.command.admin;

import me.despical.commandframework.Command;
import me.despical.commandframework.CommandArguments;
import me.despical.commons.configuration.ConfigUtils;
import me.despical.commons.miscellaneous.AttributeUtils;
import me.despical.commons.miscellaneous.MiscUtils;
import me.despical.commons.serializer.InventorySerializer;
import me.despical.commons.serializer.LocationSerializer;
import me.despical.kotl.ConfigPreferences;
import me.despical.kotl.Main;
import me.despical.kotl.arena.Arena;
import me.despical.kotl.arena.ArenaRegistry;
import me.despical.kotl.handler.ChatManager;
import me.despical.kotl.handler.setup.SetupInventory;
import me.despical.kotl.util.Debugger;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static me.despical.kotl.handler.setup.SetupInventory.TUTORIAL_VIDEO;

/**
 * @author Despical
 * <p>
 * Created at 27.06.2021
 */
public class AdminCommands {

	private final Main plugin;
	private final ChatManager chatManager;
	private final FileConfiguration config;
	private final Set<CommandSender> confirmations;

	public AdminCommands(Main plugin) {
		this.plugin = plugin;
		this.chatManager = plugin.getChatManager();
		this.config = ConfigUtils.getConfig(plugin, "arenas");
		this.confirmations = new HashSet<>();

		this.plugin.getCommandFramework().registerCommands(this);
	}

	@Command(
		name = "kotl.create",
		permission = "kotl.admin.create",
		usage = "/kotl create <id>",
		desc = "Creates a new arena with default configuration",
		min = 1,
		senderType = Command.SenderType.PLAYER
	)
	public void createCommand(CommandArguments arguments) {
		Player player = arguments.getSender();
		String arg = arguments.getArgument(0);

		if (ArenaRegistry.isArena(arg) || config.contains("instances." + arg)) {
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

	private void setupDefaultConfiguration(String id) {
		String path = "instances." + id + ".", loc = LocationSerializer.SERIALIZED_LOCATION;

		config.set(path + "endlocation", loc);
		config.set(path + "areaMin", loc);
		config.set(path + "areaMax", loc);
		config.set(path + "isdone", false);
		config.set(path + "hologramLocation", loc);
		config.set(path + "plateLocation", loc);

		ConfigUtils.saveConfig(plugin, config, "arenas");

		Arena arena = new Arena(id);
		arena.setEndLocation(LocationSerializer.DEFAULT_LOCATION);
		arena.setPlateLocation(LocationSerializer.DEFAULT_LOCATION);
		arena.setHologramLocation(LocationSerializer.DEFAULT_LOCATION);
		arena.setReady(false);

		ArenaRegistry.registerArena(arena);
	}

	@Command(
		name = "kotl.delete",
		permission = "kotl.admin.delete",
		usage = "/kotl delete <arena>",
		desc = "Deletes arena with the current configuration",
		min = 1,
		senderType = Command.SenderType.PLAYER
	)
	public void deleteCommand(CommandArguments arguments) {
		CommandSender sender = arguments.getSender();
		Arena arena = ArenaRegistry.getArena(arguments.getArgument(0));

		if (arena == null) {
			sender.sendMessage(chatManager.prefixedMessage("Commands.No-Arena-Like-That"));
			return;
		}

		if (!confirmations.contains(sender)) {
			confirmations.add(sender);
			plugin.getServer().getScheduler().runTaskLater(plugin, () -> confirmations.remove(sender), 200);
			sender.sendMessage(chatManager.prefixedMessage("Commands.Are-You-Sure"));
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

		config.set("instances." + arguments.getArgument(0), null);
		ConfigUtils.saveConfig(plugin, config, "arenas");

		sender.sendMessage(chatManager.prefixedMessage("Commands.Removed-Game-Instance"));
	}

	@Command(
		name = "kotl.edit",
		permission = "kotl.admin.edit",
		usage = "/kotl edit <arena>",
		desc = "Opens the arena editor",
		min = 1,
		senderType = Command.SenderType.PLAYER
	)
	public void editCommand(CommandArguments arguments) {
		Player player = arguments.getSender();
		Arena arena = ArenaRegistry.getArena(arguments.getArgument(0));

		if (arena == null) {
			player.sendMessage(chatManager.prefixedMessage("Commands.No-Arena-Like-That"));
			return;
		}

		new SetupInventory(arena, player).openInventory();
	}

	@Command(
		name = "kotl.reload",
		permission = "kotl.admin",
		usage = "/kotl reload",
		desc = "Opens the arena editor",
		cooldown = 5
	)
	public void reloadCommand(CommandArguments arguments) {
		CommandSender sender = arguments.getSender();
		Debugger.debug("Initialized plugin reload by {0}.", sender.getName());

		long start = System.currentTimeMillis();

		plugin.reloadConfig();
		plugin.getChatManager().reloadConfig();

		for (Arena arena : ArenaRegistry.getArenas()) {
			Debugger.debug("[Reloader] Stopping arena called {0}.", arena.getId());

			arena.deleteHologram();

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
				arena.getScoreboardManager().removeScoreboard(player);
				AttributeUtils.resetAttackCooldown(player);
			}

			arena.teleportAllToEndLocation();
			arena.getPlayers().clear();
		}

		ArenaRegistry.registerArenas();
		sender.sendMessage(chatManager.prefixedMessage("Commands.Success-Reload"));

		Debugger.debug("[Reloader] Finished reloading took {0} ms", System.currentTimeMillis() - start);
	}

	@Command(
		name = "kotl.help",
		permission = "kotl.admin",
		usage = "/kotl help",
		desc = "Sends all of the command and their usages"
	)
	public void helpCommand(CommandArguments arguments) {
		arguments.sendMessage(chatManager.coloredRawMessage("&3&l---- King of the Ladder Admin Commands ----"));
		arguments.sendMessage("");

		CommandSender sender = arguments.getSender();
		boolean isPlayer = arguments.isSenderPlayer();

		for (Command command : plugin.getCommandFramework().getCommands()) {
			String usage = command.usage(), desc = command.desc();

			if (usage.isEmpty()) continue;

			if (isPlayer) {
				((Player) sender).spigot().sendMessage(new ComponentBuilder(usage)
					.color(ChatColor.AQUA)
					.event(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, usage))
					.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(desc)))
					.create());
			} else {
				sender.sendMessage(chatManager.coloredRawMessage("&b" + usage + " &3- &b" + desc));
			}
		}

		if (isPlayer) {
			Player player = arguments.getSender();
			player.sendMessage("");
			player.spigot().sendMessage(new ComponentBuilder("TIP:").color(ChatColor.YELLOW).bold(true)
				.append(" Try to ", ComponentBuilder.FormatRetention.NONE).color(ChatColor.GRAY)
				.append("hover").color(ChatColor.WHITE).underlined(true)
				.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(ChatColor.LIGHT_PURPLE + "Hover on the commands to get info about them.")))
				.append(" or ", ComponentBuilder.FormatRetention.NONE).color(ChatColor.GRAY)
				.append("click").color(ChatColor.WHITE).underlined(true)
				.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(ChatColor.LIGHT_PURPLE + "Click on the commands to insert them in the chat.")))
				.append(" on the commands!", ComponentBuilder.FormatRetention.NONE).color(ChatColor.GRAY)
				.create());
		}
	}

	@Command(
		name = "kotl.list",
		permission = "kotl.admin",
		usage = "/kotl list",
		desc = "Shows all of the existing arenas"
	)
	public void listCommand(CommandArguments arguments) {
		if (ArenaRegistry.getArenas().isEmpty()) {
			arguments.sendMessage(chatManager.prefixedMessage("Commands.List-Command.No-Arenas-Created"));
			return;
		}

		Set<String> arenas = ArenaRegistry.getArenas().stream().map(Arena::getId).collect(Collectors.toSet());
		arguments.sendMessage(chatManager.prefixedMessage("Commands.List-Command.Format").replace("%list%", String.join(", ", arenas)));
	}
}