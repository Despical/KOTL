/*
 * KOTL - Don't let others climb to top of the ladders!
 * Copyright (C) 2025  Berke Akçen
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

package dev.despical.kotl.commands;

import dev.despical.commandframework.CommandArguments;
import dev.despical.commandframework.annotations.Command;
import dev.despical.commons.configuration.ConfigUtils;
import dev.despical.commons.miscellaneous.AttributeUtils;
import dev.despical.commons.miscellaneous.MiscUtils;
import dev.despical.commons.serializer.InventorySerializer;
import dev.despical.commons.serializer.LocationSerializer;
import dev.despical.commons.util.Strings;
import dev.despical.kotl.arena.Arena;
import dev.despical.kotl.handlers.setup.SetupInventory;
import dev.despical.kotl.options.Option;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Despical
 * <p>
 * Created at 24.07.2022
 */
public final class AdminCommands extends CommandCategory {

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

        if (arenaRegistry.isArena(arg)) {
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
        String path = "instances." + id + ".", def = LocationSerializer.SERIALIZED_LOCATION;
        FileConfiguration config = ConfigUtils.getConfig(plugin, "arenas");

        config.set(path + "endLocation", def);
        config.set(path + "areaMin", def);
        config.set(path + "areaMax", def);
        config.set(path + "isdone", false);
        config.set(path + "showOutlines", true);
        config.set(path + "plateLocation", def);
        config.set(path + "arenaPlate", "OAK_PRESSURE_PLATE");

        ConfigUtils.saveConfig(plugin, config, "arenas");

        Arena arena = new Arena(id);
        arena.setMinCorner(LocationSerializer.DEFAULT_LOCATION);
        arena.setMaxCorner(LocationSerializer.DEFAULT_LOCATION);
        arena.setEndLocation(LocationSerializer.DEFAULT_LOCATION);
        arena.setPlateLocation(LocationSerializer.DEFAULT_LOCATION);
        arena.setReady(false);

        arenaRegistry.registerArena(arena);
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
        Arena arena = arenaRegistry.getArena(arguments.getArgument(0));

        if (arena == null) {
            sender.sendMessage(chatManager.prefixedMessage("commands.no_arena_like_that"));
            return;
        }

        if (!arena.getPlayers().isEmpty()) {
            arena.getScoreboardManager().stopAllScoreboards();

            for (Player player : arena.getPlayers()) {
                player.setFlySpeed(.1F);
                player.setWalkSpeed(.2F);
                player.getInventory().clear();
                player.getInventory().setArmorContents(null);

                if (plugin.getConfigOptions().isEnabled(Option.INVENTORY_MANAGER_ENABLED)) {
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

        arenaRegistry.unregisterArena(arena);

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
        final var arena = arenaRegistry.getArena(arguments.getArgument(0));

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
    public void reloadCommand(CommandArguments arguments) {
        plugin.reload();

        for (Arena arena : arenaRegistry.getArenas()) {
            for (Player player : arena.getPlayers()) {
                player.setWalkSpeed(.2F);

                if (plugin.getConfigOptions().isEnabled(Option.INVENTORY_MANAGER_ENABLED)) {
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

        arenaRegistry.registerArenas();
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

        CommandSender sender = arguments.getSender();
        boolean isPlayer = arguments.isSenderPlayer();

        for (Command command : plugin.getCommandFramework().getSubCommands()) {
            String usage = formatCommandUsage(command.usage()), desc = command.desc();

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
        permission = "kotl.admin.list",
        usage = "/kotl list",
        desc = "Shows all of the existing arenas"
    )
    public void listCommand(CommandArguments arguments) {
        Set<Arena> arenas = arenaRegistry.getArenas();

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
        arguments.getPlayer(0).ifPresentOrElse(player -> {
            Arena arena = arenaRegistry.getArena(player);

            if (arena == null) {
                arguments.sendMessage(chatManager.prefixedMessage("commands.not_playing"));
                return;
            }

            arena.removePlayer(player);
            arena.teleportToEndLocation(player);

            arguments.sendMessage(chatManager.prefixedMessage("commands.kicked_player"));
        }, () -> arguments.sendMessage(chatManager.prefixedMessage("commands.player_not_found")));
    }

    @Command(
        name = "kotl.version",
        usage = "/kotl version",
        desc = "Displays detailed information about the plugin and server environment.",
        permission = "kotl.admin.version",
        senderType = Command.SenderType.PLAYER
    )
    public void versionCommand(CommandArguments arguments) {
        Player player = arguments.getSender();

        arguments.sendMessage("");
        MiscUtils.sendCenteredMessage(player, "&b&l==== [ &3&lKOTL &b&l] ==== ");
        arguments.sendMessage("");
        arguments.sendMessage(" &8• &3Plugin Version: &b{0}", plugin.getDescription().getVersion());
        arguments.sendMessage(" &8• &3Server Version: &b{0}", plugin.getServer().getVersion());
        arguments.sendMessage(" &8• &3Bukkit Version: &b{0}", plugin.getServer().getBukkitVersion());
        arguments.sendMessage(" &8• &3Loaded Plugins: &b{0}", plugin.getServer().getPluginManager().getPlugins().length);
        arguments.sendMessage("");
        arguments.sendMessage(" &8• &3Java Version: &b{0}", System.getProperty("java.version"));
        arguments.sendMessage(" &8• &3Java Vendor: &b{0}", System.getProperty("java.vendor"));
        arguments.sendMessage(" &8• &3JVM Version: &b{0}", System.getProperty("java.vm.version"));
        arguments.sendMessage(" &8• &3JVM Name: &b{0}", System.getProperty("java.vm.name"));
        arguments.sendMessage("");
        arguments.sendMessage(" &8• &3OS Name: &b{0} ({1})", System.getProperty("os.name"), System.getProperty("os.arch"));
        arguments.sendMessage("");
    }

    private String formatCommandUsage(String usage) {
        usage = "&3" + usage;

        char[] array = usage.toCharArray();
        StringBuilder buffer = new StringBuilder(usage);

        for (int i = 0; i < array.length; i++) {
            if (array[i] == '[' || array[i] == '<') {
                buffer.insert(i, "&b");
                return Strings.format(buffer.toString());
            }
        }

        return Strings.format(usage);
    }
}
