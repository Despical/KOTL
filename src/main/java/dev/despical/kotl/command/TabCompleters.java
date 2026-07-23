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
import dev.despical.commandframework.CompleterHelper;
import dev.despical.commandframework.annotations.Completer;
import dev.despical.commandframework.debug.Debug;
import dev.despical.kotl.option.BooleanOption;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Despical
 * <p>
 * Created at 05.06.2026
 */
public final class TabCompleters extends CommandCategory {

    @Completer(
        name = "kotl",
        permission = "kotl.command.tabcompleter"
    )
    public List<String> onTabCompletion(CommandArguments arguments, CompleterHelper helper) {
        int length = arguments.getLength();
        List<String> availableCommands = collectAvailableCommands(arguments);

        return switch (length) {
            case 0 -> availableCommands;
            case 1 -> helper.copyMatches(0, availableCommands);
            case 2 -> {
                if (helper.equalsAny(0, "edit", "delete", "stop")) {
                    yield helper.copyMatches(1, arenaRegistry.getArenaNames());
                }

                if (helper.equalsAny(0, "stats")) {
                    yield helper.copyMatches(1, helper.playerNames());
                }

                if (helper.equalsAny(0, "kick") && arguments.hasPermission("kotl.admin.kick")) {
                    yield helper.copyMatches(1, helper.playerNames());
                }

                yield helper.empty();
            }
            default -> helper.empty();
        };
    }

    @Debug
    @Completer(
        name = "kotl.debug",
        permission = "kotl.debug.tabcompleter"
    )
    public List<String> debugTabCompleter(CommandArguments arguments, CompleterHelper helper) {
        if (arguments.isSenderConsole()) {
            return helper.empty();
        }

        if (arguments.getLength() == 1) {
            return helper.copyMatches(0, List.of("component", "dump"));
        }

        return helper.empty();
    }

    private List<String> collectAvailableCommands(CommandArguments arguments) {
        List<String> availableCommands = new ArrayList<>(List.of("stats"));

        if (arguments.hasPermission("kotl.command.help")) {
            availableCommands.add("help");
        }

        if (arguments.hasPermission("kotl.arena.create")) {
            availableCommands.add("create");
        }

        if (arguments.hasPermission("kotl.arena.list")) {
            availableCommands.add("list");
        }

        if (arguments.hasPermission("kotl.arena.edit")) {
            availableCommands.add("edit");
        }

        if (arguments.hasPermission("kotl.arena.delete")) {
            availableCommands.add("delete");
        }

        if (arguments.hasPermission("kotl.admin.stop")) {
            availableCommands.add("stop");
        }

        if (arguments.hasPermission("kotl.admin.reload")) {
            availableCommands.add("reload");
        }

        if (arguments.hasPermission("kotl.admin.kick")) {
            availableCommands.add("kick");
        }

        if (BooleanOption.DEBUG.value() && arguments.hasPermission("kotl.debug.tabcompleter")) {
            availableCommands.add("debug");
        }

        return availableCommands;
    }
}
