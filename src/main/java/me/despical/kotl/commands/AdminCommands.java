/*
 * KOTL - Don't let others climb to top of the ladders!
 * Copyright (C) 2024  Berke Akçen
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.despical.kotl.commands;

import me.despical.commandframework.*;
import me.despical.commandframework.annotations.Command;
import me.despical.commandframework.annotations.Completer;
import me.despical.commandframework.annotations.Cooldown;
import me.despical.commons.configuration.ConfigUtils;
import me.despical.commons.miscellaneous.AttributeUtils;
import me.despical.commons.miscellaneous.MiscUtils;
import me.despical.commons.serializer.InventorySerializer;
import me.despical.commons.serializer.LocationSerializer;
import me.despical.commons.util.Strings;
import me.despical.kotl.ConfigPreferences;
import me.despical.kotl.Main;
import me.despical.kotl.arena.Arena;
import me.despical.kotl.handlers.setup.SetupInventory;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Despical
 * <p>
 * Created at 24.07.2022
 */
public class AdminCommands extends AbstractCommand {

	public AdminCommands(Main plugin) {
		super(plugin);
	}

	@Command(
		name = "kotl.create",
		permission = "kotl.admin.create",
		usage = "/kotl create <id>",
		desc = "Creates a new arena with default configuration",
		max = 1,
		senderType = Command.SenderType.PLAYER
	)
	public void createCommand(CommandArguments arguments) {
		if (arguments.isArgumentsEmpty()) {
			arguments.sendMessage(chatManager.prefixedRawMessage("&cPlease enter an name to create an arena!"));
			return;
		}

		final var arg = arguments.getArgument(0);
		final Player player = arguments.getSender();

		if ("default".equals(arg)) {
			player.sendMessage(chatManager.prefixedRawMessage("&cYou can not create an arena named default!"));
			return;
		}

		if (plugin.getArenaRegistry().isArena(arg)) {
			player.sendMessage(chatManager.prefixedRawMessage("&cArena with that ID already contains!"));
			player.sendMessage(chatManager.prefixedRawMessage("&cTo check existing arenas use: /kotl list"));
			return;
		}

		setupDefaultConfiguration(arg);

		arguments.sendMessage("&l--------------------------------------------");
		MiscUtils.sendCenteredMessage(player, "&eInstance " + arg + " created!");
		player.sendMessage("");
		MiscUtils.sendCenteredMessage(player, "&aEdit this arena via &6/kotl edit " + arg + "&a!");
		player.sendMessage("");
		MiscUtils.sendCenteredMessage(player, "&6Don't know where to start? Check out tutorial video:");
		MiscUtils.sendCenteredMessage(player, "&7https://www.youtube.com/watch?v=O_vkf_J4OgY");
		arguments.sendMessage("&l--------------------------------------------");
	}

	private void setupDefaultConfiguration(String id) {
		final String path = "instances." + id + ".", def = LocationSerializer.SERIALIZED_LOCATION;
		final var config = ConfigUtils.getConfig(plugin, "arenas");

		config.set(path + "endLocation", def);
		config.set(path + "areaMin", def);
		config.set(path + "areaMax", def);
		config.set(path + "isdone", false);
		config.set(path + "showOutlines", true);
		config.set(path + "plateLocation", def);
		config.set(path + "arenaPlate", "OAK_PRESSURE_PLATE");

		ConfigUtils.saveConfig(plugin, config, "arenas");

		var arena = new Arena(id);
		arena.setEndLocation(LocationSerializer.DEFAULT_LOCATION);
		arena.setPlateLocation(LocationSerializer.DEFAULT_LOCATION);
		arena.setReady(false);

		plugin.getArenaRegistry().registerArena(arena);
	}

	@Command(
		name = "kotl.delete",
		permission = "kotl.admin.delete",
		usage = "/kotl delete <arena>",
		desc = "Deletes arena with the current configuration",
		min = 1
	)
	public void deleteCommand(CommandArguments arguments) {
		final var sender = arguments.getSender();
		final var arena = plugin.getArenaRegistry().getArena(arguments.getArgument(0));

		if (arena == null) {
			sender.sendMessage(chatManager.prefixedMessage("commands.no_arena_like_that"));
			return;
		}

		if (!arena.getPlayers().isEmpty()) {
			arena.getScoreboardManager().stopAllScoreboards();

			for (final var player : arena.getPlayers()) {
				player.setFlySpeed(.1F);
				player.setWalkSpeed(.2F);
				player.getInventory().clear();
				player.getInventory().setArmorContents(null);

				if (plugin.getOption(ConfigPreferences.Option.INVENTORY_MANAGER_ENABLED)) {
					InventorySerializer.loadInventory(plugin, player);
				} else {
					player.getActivePotionEffects().forEach(effect -> player.removePotionEffect(effect.getType()));
				}

				AttributeUtils.resetAttackCooldown(player);
				arena.doBarAction(player, 0);
			}

			arena.teleportAllToEndLocation();
			arena.getPlayers().clear();
		}

		arena.setShowOutlines(false);

		plugin.getArenaRegistry().unregisterArena(arena);

		final var config = ConfigUtils.getConfig(plugin, "arenas");

		config.set("instances." + arguments.getArgument(0), null);
		ConfigUtils.saveConfig(plugin, config, "arenas");

		sender.sendMessage(chatManager.prefixedMessage("commands.removed_game_instance"));
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
		final var arena = plugin.getArenaRegistry().getArena(arguments.getArgument(0));

		if (arena == null) {
			arguments.sendMessage(chatManager.prefixedMessage("commands.no_arena_like_that"));
			return;
		}

		new SetupInventory(plugin, arena, arguments.getSender()).openInventory();
	}

	@Command(
		name = "kotl.reload",
		permission = "kotl.admin.reload",
		usage = "/kotl reload",
		desc = "Reloads all configuration and stops arenas"
	)
	@Cooldown(
		cooldown = 5,
		bypassPerm = "kotl.admin.cooldown"
	)
	public void reloadCommand(CommandArguments arguments) {
		plugin.reload();

		for (final var arena : plugin.getArenaRegistry().getArenas()) {
			for (final var player : arena.getPlayers()) {
				player.setWalkSpeed(.2F);

				if (plugin.getOption(ConfigPreferences.Option.INVENTORY_MANAGER_ENABLED)) {
					InventorySerializer.loadInventory(plugin, player);
				} else {
					player.getInventory().clear();
					player.getInventory().setArmorContents(null);
					player.getActivePotionEffects().forEach(effect -> player.removePotionEffect(effect.getType()));
				}

				arena.doBarAction(player, 0);
				arena.getScoreboardManager().removeScoreboard(player);
				AttributeUtils.resetAttackCooldown(player);
			}

			arena.teleportAllToEndLocation();
			arena.getPlayers().clear();
		}

		plugin.getArenaRegistry().registerArenas();
		arguments.sendMessage(chatManager.prefixedMessage("commands.success_reload"));
	}

	@Command(
		name = "kotl.help",
		usage = "/kotl help",
		desc = "Displays a list of available commands along with their descriptions.",
		permission = "kotl.admin.help"
	)
	public void helpCommand(CommandArguments arguments) {
		arguments.sendMessage("");
		MiscUtils.sendCenteredMessage(arguments.getSender(), "&3&lKing of the Ladder");
		MiscUtils.sendCenteredMessage(arguments.getSender(), "&3[&boptional argument&3] &b- &3<&brequired argument&3>");
		arguments.sendMessage("");

		final CommandSender sender = arguments.getSender();
		final boolean isPlayer = arguments.isSenderPlayer();

		for (final var command : plugin.getCommandFramework().getSubCommands()) {
			final String usage = formatCommandUsage(command.usage()), desc = command.desc();

			if (usage.isEmpty() || desc.isEmpty()) continue;

			if (isPlayer) {
				((Player) sender).spigot().sendMessage(
					new ComponentBuilder(ChatColor.DARK_GRAY + " • ")
						.append(usage)
						.color(ChatColor.AQUA)
						.event(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, command.usage()))
						.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(desc)))
						.create());
			} else {
				arguments.sendMessage(" &8• &b" + usage + " &3- &b" + desc);
			}
		}

		if (isPlayer) {
			final Player player = arguments.getSender();

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
		permission = "kotl.admin.list",
		usage = "/kotl list",
		desc = "Shows all of the existing arenas"
	)
	public void listCommand(CommandArguments arguments) {
		final var arenas = plugin.getArenaRegistry().getArenas();

		if (arenas.isEmpty()) {
			arguments.sendMessage(chatManager.prefixedMessage("commands.list_command.no_arenas_created"));
			return;
		}

		var list = arenas.stream().map(Arena::getId).collect(Collectors.joining(", "));
		arguments.sendMessage(chatManager.prefixedMessage("commands.list_command.format").replace("%list%", list));
	}

	@Command(
		name = "kotl.kick",
		permission = "kotl.admin.kick",
		usage = "/kotl kick <player>",
		desc = "Kicks specified player if they're playing",
		min = 1
	)
	public void kickCommand(CommandArguments arguments) {
		arguments.getPlayer(0).ifPresentOrElse(player -> {
			final var arena = plugin.getArenaRegistry().getArena(player);
			if (arena == null) {
				arguments.sendMessage(chatManager.prefixedMessage("commands.not_playing"));
				return;
			}

			arena.removePlayer(player);
			arena.teleportToEndLocation(player);

			arguments.sendMessage(chatManager.prefixedMessage("commands.kicked_player"));
		}, () -> arguments.sendMessage(chatManager.prefixedMessage("commands.player_not_found")));
	}

	@Completer(
		name = "kotl"
	)
	public List<String> onTabComplete(CommandArguments arguments) {
		final List<String> completions = new ArrayList<>(), commands = plugin.getCommandFramework().getSubCommands().stream().map(cmd -> cmd.name().replace(arguments.getLabel() + '.', "")).collect(Collectors.toList());
		final String[] args = arguments.getArguments();

		if (args.length > 0) {
			if (List.of("create", "list", "help", "reload").contains(args[0])) {
				return completions;
			}
		}

		if (args.length == 1) {
			return StringUtil.copyPartialMatches(args[0], arguments.hasPermission("kotl.admin") ? commands : List.of("top", "stats"), completions);
		}

		final String arg = args[0];

		if (args.length == 2) {
			if (!List.of("delete", "edit", "help", "kick", "stats", "top").contains(arg)) return completions;

			if (arg.equalsIgnoreCase("top")) {
				return StringUtil.copyPartialMatches(args[1], List.of("tours_played", "score", "kills", "deaths"), completions);
			}

			if (arg.equalsIgnoreCase("stats") || arg.equalsIgnoreCase("kick")) {
				return plugin.getServer().getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList());
			}

			final var arenas = plugin.getArenaRegistry().getArenas().stream().map(Arena::getId).collect(Collectors.toList());

			return StringUtil.copyPartialMatches(args[1], arenas, completions);
		}

		return completions;
	}

	private String formatCommandUsage(String usage) {
		usage = "&3" + usage;

		final var array = usage.toCharArray();
		final var buffer = new StringBuilder(usage);

		for (int i = 0; i < array.length; i++) {
			if (array[i] == '[' || array[i] == '<') {
				buffer.insert(i, "&b");
				return Strings.format(buffer.toString());
			}
		}

		return Strings.format(usage);
	}
}