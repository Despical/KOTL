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

package dev.despical.kotl.game;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * @author Despical
 * <p>
 * Created at 06.06.2026
 */
@Getter
@RequiredArgsConstructor
public enum StopReason {

    ARENA_DELETED("game-stopped-due-to-arena-deletion"),
    SERVER_RELOAD("server-reload-detected"),
    SERVER_SHUTDOWN("server-shutdown-detected"),
    STOP_COMMAND("game-stopped-by-command");

    private final String messagePath;
}
