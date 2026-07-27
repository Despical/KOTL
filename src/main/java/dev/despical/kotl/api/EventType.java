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

package dev.despical.kotl.api;

import dev.despical.kotl.api.events.game.GameStopEvent;
import dev.despical.kotl.api.events.player.PlayerBecomeKingEvent;
import dev.despical.kotl.api.events.player.PlayerEnterArenaEvent;
import dev.despical.kotl.api.events.player.PlayerLeaveArenaEvent;
import dev.despical.kotl.api.events.player.PlayerStatisticChangeEvent;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;

/**
 * Enumerates every custom Bukkit event dispatched through the KOTL event API.
 * <p>
 * The enum is used by {@link EventManager} to validate event factories and by
 * {@link EventRegistry} for discovery. Bukkit listeners should still register
 * against the concrete event classes.
 *
 * @author Despical
 * <p>
 * Created at 05.06.2026
 */
public enum EventType {

    /**
     * Fired after a game is stopped and its players are cleaned up.
     */
    GAME_STOP(GameStopEvent.class),

    /**
     * Fired before a player enters an arena.
     */
    PLAYER_ENTER_ARENA(PlayerEnterArenaEvent.class),

    /**
     * Fired before a player leaves an arena.
     */
    PLAYER_LEAVE_ARENA(PlayerLeaveArenaEvent.class),

    /**
     * Fired before a player becomes king.
     */
    PLAYER_BECOME_KING(PlayerBecomeKingEvent.class),

    /**
     * Fired before a player statistic is stored.
     */
    PLAYER_STAT_CHANGE(PlayerStatisticChangeEvent.class);

    private final Class<? extends Event> eventClass;

    EventType(Class<? extends Event> eventClass) {
        this.eventClass = eventClass;
    }

    /**
     * Returns the concrete Bukkit event class represented by this type.
     *
     * @return the registered event class
     */
    @NotNull
    public Class<? extends Event> getEventClass() {
        return eventClass;
    }
}
