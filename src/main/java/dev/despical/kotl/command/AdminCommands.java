/*
 * KOTL - Don't let others climb to top of the ladders!
 * Copyright (C) 2026  Berke Akçen
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

package dev.despical.kotl.command;

import dev.despical.commandframework.CommandArguments;
import dev.despical.commandframework.annotations.Command;
import dev.despical.kotl.arena.Arena;
import dev.despical.kotl.api.events.player.PlayerLeaveArenaEvent;
import dev.despical.kotl.arena.options.ArenaKeys;
import dev.despical.kotl.game.Game;
import dev.despical.kotl.game.StopReason;
import dev.despical.kotl.option.BooleanOption;
import dev.despical.kotl.user.User;
import dev.despical.kotl.util.Var;
import org.bukkit.entity.Player;

/**
 * @author Despical
 * <p>
 * Created at 05.06.2026
 */
public final class AdminCommands extends CommandCategory {

    @Command(
        name = "kotl",
        fallbackPrefix = "thekotl",
        usage = "/kotl help",
        desc = "Main command of the King of the Ladder."
    )
    public void mainCommand(CommandArguments arguments) {
        if (arguments.isArgumentsEmpty()) {
            arguments.sendMessage("<#00aaaa>This server is running <#55ffff>King of the Ladder v{0} <#00aaaa>by <#55ffff>Despical<#00aaaa>.", plugin.getDescription().getVersion());

            if (arguments.hasPermission("kotl.admin.help")) {
                arguments.sendMessage("<#00aaaa>Commands: <#55ffff>/{0} help", arguments.getLabel());
            }

            return;
        }

        chatManager.sendMessage(arguments, "unrecognized-arguments", Var.of("%label%", arguments.getLabel()), Var.of("%arguments%", arguments.concatArguments()));
    }

    @Command(
        name = "kotl.reload",
        permission = "kotl.admin.reload",
        usage = "/%label% reload",
        desc = "Reloads configuration files."
    )
    public void reloadCommand(CommandArguments arguments) {
        gameManager.stopAllGames(StopReason.SERVER_RELOAD);

        chatManager.loadFile();
        plugin.getOptions().reloadOptions();
        plugin.registerItems();
        plugin.getEventManager().reload();
        plugin.getPlayingCommandPolicy().reload();
        gameManager.reload();
        plugin.getOutlineManager().refreshAll(arenaRegistry.getArenas());

        chatManager.sendMessage(arguments, "reloaded-configuration");
    }

    @Command(
        name = "kotl.stop",
        permission = "kotl.admin.stop",
        usage = "/%label% stop [arena]",
        desc = "Stops the current or specified arena game.",
        max = 1
    )
    public void stopCommand(CommandArguments arguments) {
        boolean isConsoleSender = arguments.isSenderConsole();

        if (arguments.isArgumentsEmpty()) {
            if (isConsoleSender) {
                chatManager.sendMessage(arguments, "stop-command.correct-usage", Var.of("%label%", arguments.getLabel()));
                return;
            }

            Player player = arguments.getSender();
            Arena arena = arenaRegistry.getArena(player);

            if (arena == null) {
                chatManager.sendMessage(player, "not-playing");
                return;
            }

            gameManager.stopGame(arena.getGame(), StopReason.STOP_COMMAND);
            return;
        }

        Arena arena = arenaRegistry.getArena(arguments.getFirst());
        if (arena == null) {
            chatManager.sendMessage(arguments, "no-arena-found-with-that-name");
            return;
        }

        Game game = arena.getGame();
        if (game == null) {
            chatManager.sendMessage(arguments, "stop-command.not-playing");
            return;
        }

        gameManager.stopGame(game, StopReason.STOP_COMMAND);

        if (!isConsoleSender && game.getPlayers().contains(arguments.<Player>getSender())) {
            return;
        }

        chatManager.sendMessage(arguments, "stop-command.stopped");
    }

    @Command(
        name = "kotl.help",
        permission = "kotl.command.help",
        usage = "/%label% help"
    )
    public void helpCommand(CommandArguments arguments) {
        Var var = Var.of("%label%", arguments.getLabel());
        chatManager.sendMessage(arguments, "help-message", var);

        if (arguments.hasPermission("kotl.admin.help")) {
            arguments.sendMessage("");
            chatManager.sendMessage(arguments, "admin-help-message", var);
            arguments.sendMessage("");

            if (BooleanOption.DEBUG.value()) {
                chatManager.sendMessage(arguments, "debug-help-message", var);
            }
        }
    }

    @Command(
        name = "kotl.kick",
        permission = "kotl.admin.kick",
        usage = "/%label% kick <player>",
        desc = "Removes a player from the game and teleports them to the arena's end location.",
        min = 1,
        max = 1,
        senderType = Command.SenderType.PLAYER
    )
    public void kickCommand(User user, CommandArguments arguments) {
        Player targetPlayer = arguments.getPlayer(0)
            .orElseGet(() -> {
                chatManager.sendMessage(arguments, "no-player-with-that-name");
                return null;
            });

        if (targetPlayer == null) {
            return;
        }

        User targetUser = plugin.getUserManager().getUser(targetPlayer);
        Arena playerArena = targetUser.getArena();

        if (playerArena == null) {
            chatManager.sendMessage(arguments, "kick-command.not-playing",
                Var.of("%player%", targetPlayer.getName()));
            return;
        }

        arenaManager.leaveAttempt(targetUser, PlayerLeaveArenaEvent.LeaveReason.KICK);
        targetPlayer.teleport(playerArena.getOption(ArenaKeys.END_LOCATION));

        chatManager.sendMessage(arguments, "kick-command.kicked",
            Var.of("%player%", targetPlayer.getName()),
            Var.of("%arena%", playerArena.getId()));
    }
}
