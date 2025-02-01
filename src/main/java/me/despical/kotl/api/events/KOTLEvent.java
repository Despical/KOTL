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

package me.despical.kotl.api.events;

import me.despical.kotl.arena.Arena;
import org.bukkit.event.Event;

/**
 * @author Despical
 * @since 1.0.0
 * <p>
 * Created at 20.06.2020
 */
public abstract class KOTLEvent extends Event {

    protected Arena arena;

    public KOTLEvent(Arena eventArena) {
        arena = eventArena;
    }

    public Arena getArena() {
        return arena;
    }
}
