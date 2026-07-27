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

package dev.despical.kotl.api.events.game;

import dev.despical.kotl.api.events.KOTLEvent;
import dev.despical.kotl.arena.Arena;
import dev.despical.kotl.bossbar.BossBarManager;
import dev.despical.kotl.game.Game;
import dev.despical.kotl.scoreboard.ScoreboardManager;
import org.jetbrains.annotations.NotNull;

/**
 * Base class for events associated with one KOTL game instance.
 * <p>
 * The related {@link Game} provides access to the active players and runtime
 * managers used by its {@link Arena}, including:
 * <ul>
 *     <li>{@link ScoreboardManager} for arena scoreboards</li>
 *     <li>{@link BossBarManager} for arena boss bars</li>
 *     <li>The current player membership snapshot</li>
 * </ul>
 *
 * @author Despical
 * <p>
 * Created at 27.07.2026
 */
public abstract class GameEvent extends KOTLEvent {

    /**
     * The game instance associated with this event.
     */
    @NotNull
    protected final Game game;

    /**
     * Constructs a new game event.
     *
     * @param game the game associated with the event
     */
    protected GameEvent(@NotNull Game game) {
        this.game = game;
    }

    /**
     * Returns the game instance associated with this event.
     *
     * @return the related game
     */
    @NotNull
    public Game getGame() {
        return game;
    }

    /**
     * Returns the arena represented by the associated game.
     *
     * @return the related arena
     */
    @NotNull
    public Arena getArena() {
        return game.getArena();
    }

    /**
     * Returns a compact debug representation containing the arena identifier.
     *
     * @return a string containing the event arena
     */
    @Override
    public String toString() {
        return "arena=%s".formatted(getArena().getId());
    }
}
