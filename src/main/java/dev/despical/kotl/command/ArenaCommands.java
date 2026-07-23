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
import dev.despical.commandframework.annotations.Flag;
import dev.despical.kotl.arena.Arena;
import dev.despical.kotl.arena.options.ArenaKeys;
import dev.despical.kotl.game.Game;
import dev.despical.kotl.game.StopReason;
import dev.despical.kotl.setup.SetupMenu;
import dev.despical.kotl.user.User;
import dev.despical.kotl.util.Var;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.Set;
import java.util.StringJoiner;

/**
 * @author Despical
 * <p>
 * Created at 05.06.2026
 */
public final class ArenaCommands extends CommandCategory {

    @Command(
        name = "kotl.create",
        permission = "kotl.arena.create",
        usage = "/%label% create <arena id>",
        min = 1,
        max = 1
    )
    public void createArenaCommand(CommandArguments arguments) {
        Player player = arguments.getSender();
        String arenaId = arguments.getFirst();
        Var var = Var.of("%id%", arenaId);

        if (arenaRegistry.isArenaExists(arenaId)) {
            chatManager.sendCenteredMessage(player, "arena-already-exists", var);

            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
            return;
        }

        arenaRegistry.registerNewArena(arenaId);
        chatManager.sendCenteredMessage(player, "created-arena", var);

        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 2.0f);
    }

    @Flag({"confirm", "cancel"})
    @Command(
        name = "kotl.delete",
        permission = "kotl.arena.delete",
        usage = "/%label% delete <arena id> [--confirm] [--cancel]",
        min = 1,
        senderType = Command.SenderType.PLAYER
    )
    public void deleteArenaCommand(Arena arena, CommandArguments arguments) {
        Player player = arguments.getSender();
        Var var = Var.of("%id%", arguments.getFirst());

        if (arena == null) {
            chatManager.sendCenteredMessage(player, "no-arena-found", var);
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
            return;
        }

        if (arguments.isFlagPresent("cancel")) {
            chatManager.sendCenteredMessage(player, "delete-cancelled", var);
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
            return;
        }

        if (arguments.isFlagPresent("confirm")) {
            Game game = arena.getGame();
            gameManager.stopGame(game, StopReason.ARENA_DELETED);

            arenaRegistry.unregisterArena(arena);
            chatManager.sendCenteredMessage(player, "deleted-arena", var);

            player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1f, 0.5f);
            return;
        }

        chatManager.sendCenteredMessage(player, "delete-confirmation", var);
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 1.5f);
    }

    @Command(
        name = "kotl.list",
        permission = "kotl.arena.list",
        usage = "/%label% list",
        senderType = Command.SenderType.PLAYER
    )
    public void listArenaCommand(User user, CommandArguments arguments) {
        Player player = arguments.getSender();
        Set<Arena> arenas = arenaRegistry.getArenas();

        if (arenas.isEmpty()) {
            chatManager.sendMessage(player, "no-arenas-registered");
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
            return;
        }

        StringJoiner arenasJoiner = new StringJoiner("<dark_gray>, ");

        for (Arena arena : arenas) {
            boolean isReady = arena.getOption(ArenaKeys.READY);

            if (isReady) {
                arenasJoiner.add(
                    "<#00E676><hover:show_text:'<#00E676><b>✔ Ready to play!</b><br><gray>Click to join.'><click:run_command:'/kotl join %s'>%1$s</click></hover>".formatted(arena.getId())
                );
            } else {
                arenasJoiner.add(
                    "<#FF5252><hover:show_text:'<#FF5252><b>✖ Setup Incomplete!</b><br><gray>Click to edit.'><click:run_command:'/kotl edit %s'>%1$s</click></hover>".formatted(arena.getId())
                );
            }
        }

        chatManager.sendMessage(player, "created-arenas", Var.of("%arenas%", arenasJoiner.toString()));
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
    }

    @Command(
        name = "kotl.edit",
        permission = "kotl.arena.edit",
        usage = "/%label% edit <arena id>",
        min = 1,
        senderType = Command.SenderType.PLAYER
    )
    public void editArenaCommand(Arena arena, User user, CommandArguments arguments) {
        if (arena == null) {
            chatManager.sendMessage(arguments, "no-arena-found-with-that-name");
            return;
        }

        new SetupMenu(user, arena).open();
    }
}
