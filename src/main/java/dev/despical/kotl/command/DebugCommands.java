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
import dev.despical.commandframework.debug.Debug;
import net.kyori.adventure.text.Component;

/**
 * @author Despical
 * <p>
 * Created at 05.06.2026
 */
@Debug
public final class DebugCommands extends CommandCategory {

    @Command(
        name = "kotl.debug.component",
        permission = "kotl.debug.component",
        usage = "/%label% debug component <message>",
        min = 1,
        senderType = Command.SenderType.PLAYER
    )
    public void debugComponentCommand(CommandArguments arguments) {
        Component component = chatManager.parseMessage(arguments.concatArguments());
        arguments.sendMessage(component);
    }

    @Command(
        name = "kotl.debug.dump",
        permission = "kotl.debug.dump",
        usage = "/%label% debug dump"
    )
    public void debugDumpTimingsCommand(CommandArguments arguments) {
        plugin.getEventManager().sendTimingsReport(arguments.getSender());
    }
}
