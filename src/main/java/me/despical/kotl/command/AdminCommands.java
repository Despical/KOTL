/*
 * KOTL - Don't let others to climb top of the ladders!
 * Copyright (C) 2023 Despical
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

package me.despical.kotl.command;

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
import me.despical.kotl.handler.setup.SetupInventory;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static me.despical.commandframework.Command.SenderType.PLAYER;
import static me.despical.kotl.handler.setup.SetupInventory.TUTORIAL_VIDEO;

/**
 * @author Despical
 * <p>
 * Created at 24.07.2022
 */
public class AdminCommands extends AbstractCommand {

	private final Set<CommandSender> confirmations;

	public AdminCommands(Main plugin) {
		super(plugin);
		this.confirmations = new HashSet<>();
	}

	@Command(
		name = "kotl.create",
		permission = "kotl.admin.create",
		usage = "/kotl create <id>",
		desc = "Creates a new arena with default configuration",
		senderType = PLAYER
	)
	public void createCommand(CommandArguments arguments) {
		if (arguments.isArgumentsEmpty()) {
			arguments.sendMessage(chatManager.prefixedRawMessage("&cPlease enter an name to create an arena!"));
			return;
		}

		String arg = arguments.getArgument(0);
		Player player = arguments.getSender();

		if (arg.equals("default")) {
			player.sendMessage(chatManager.prefixedRawMessage("&cYou can not create an arena named default!"));
			return;
		}

		if (plugin.getArenaRegistry().isArena(arg)) {
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
		String path = "instances." + id + ".", def = LocationSerializer.SERIALIZED_LOCATION;

		arenaConfig.set(path + "endLocation", def);
		arenaConfig.set(path + "areaMin", def);
		arenaConfig.set(path + "areaMax", def);
		arenaConfig.set(path + "isdone", false);
		arenaConfig.set(path + "plateLocation", def);
		arenaConfig.set(path + "arenaPlate", "OAK_PRESSURE_PLATE");

		ConfigUtils.saveConfig(plugin, arenaConfig, "arenas");

		Arena arena = new Arena(id);
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
		CommandSender sender = arguments.getSender();
		Arena arena = plugin.getArenaRegistry().getArena(arguments.getArgument(0));

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

		if (!arena.getPlayers().isEmpty()) {
			arena.getScoreboardManager().stopAllScoreboards();

			for (Player player : arena.getPlayers()) {
				player.setWalkSpeed(.2F);

				if (plugin.getConfigPreferences().getOption(ConfigPreferences.Option.INVENTORY_MANAGER_ENABLED)) {
					InventorySerializer.loadInventory(plugin, player);
				} else {
					player.getInventory().clear();
					player.getInventory().setArmorContents(null);
					player.getActivePotionEffects().forEach(effect -> player.removePotionEffect(effect.getType()));
				}

				AttributeUtils.resetAttackCooldown(player);
				arena.doBarAction(player, 0);
			}

			arena.teleportAllToEndLocation();
			arena.getPlayers().clear();
		}

		plugin.getArenaRegistry().unregisterArena(arena);

		arenaConfig.set("instances." + arguments.getArgument(0), null);
		ConfigUtils.saveConfig(plugin, arenaConfig, "arenas");

		sender.sendMessage(chatManager.prefixedMessage("commands.removed_game_instance"));
	}

	@Command(
		name = "kotl.edit",
		permission = "kotl.admin.edit",
		usage = "/kotl edit <arena>",
		desc = "Opens the arena editor",
		min = 1,
		senderType = PLAYER
	)
	public void editCommand(CommandArguments arguments) {
		Player player = arguments.getSender();
		Arena arena = plugin.getArenaRegistry().getArena(arguments.getArgument(0));

		if (arena == null) {
			player.sendMessage(chatManager.prefixedMessage("commands.no_arena_like_that"));
			return;
		}

		new SetupInventory(arena, player).openInventory();
	}

	@Command(
		name = "kotl.reload",
		permission = "kotl.admin.reload",
		usage = "/kotl reload",
		desc = "Reloads all configuration and stops arenas"
	)
	public void reloadCommand(CommandArguments arguments) {
		chatManager.reload();

		for (Arena arena : plugin.getArenaRegistry().getArenas()) {
			for (Player player : arena.getPlayers()) {
				player.setWalkSpeed(.2F);

				if (plugin.getConfigPreferences().getOption(ConfigPreferences.Option.INVENTORY_MANAGER_ENABLED)) {
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
		permission = "kotl.admin.help"
	)
	public void helpCommand(CommandArguments arguments) {
		arguments.sendMessage("");
		arguments.sendMessage(chatManager.coloredRawMessage("&3&l---- King of the Ladder Admin Commands ----"));
		arguments.sendMessage("");

		final CommandSender sender = arguments.getSender();
		final boolean isPlayer = arguments.isSenderPlayer();

		for (final var command : plugin.getCommandFramework().getCommands().stream().sorted(Collections
			.reverseOrder(Comparator.comparingInt(cmd -> cmd.usage().length()))).toList()) {
			final String usage = command.usage(), desc = command.desc();

			if (usage.isEmpty() || usage.contains("help")) continue;

			if (isPlayer) {
				((Player) sender).spigot().sendMessage(new ComponentBuilder()
					.color(ChatColor.DARK_GRAY)
					.append(" • ")
					.append(usage)
					.color(ChatColor.AQUA)
					.event(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, usage))
					.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(desc)))
					.create());
			} else {
				sender.sendMessage(chatManager.coloredRawMessage(" &8• &b" + usage + " &3- &b" + desc));
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
		final Set<Arena> arenas = plugin.getArenaRegistry().getArenas();

		if (arenas.isEmpty()) {
			arguments.sendMessage(chatManager.prefixedMessage("commands.list_command.no_arenas_created"));
			return;
		}

		String list = arenas.stream().map(Arena::getId).collect(Collectors.joining(", "));
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
		final String target = arguments.getArgument(0);
		final Player player = plugin.getServer().getPlayer(target);

		if (player == null) {
			arguments.sendMessage(chatManager.prefixedMessage("commands.player_not_found"));
			return;
		}

		final Arena arena = plugin.getArenaRegistry().getArena(player);

		if (arena == null) {
			arguments.sendMessage(chatManager.prefixedMessage("commands.not_playing"));
			return;
		}

		arena.removePlayer(player);
		arena.teleportToEndLocation(player);

		arguments.sendMessage(chatManager.prefixedMessage("commands.kicked_player"));
	}
}