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

import dev.despical.kotl.game.Game;
import dev.despical.kotl.game.StopReason;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

/**
 * Called after a KOTL game has been stopped and its players have been cleaned
 * up.
 * <p>
 * Scoreboards and boss bars have been removed, inventories and player state
 * have been restored where possible, players have been teleported to the
 * configured end location, and the game's player set is empty.
 * <p>
 * Use {@link #getStoppedPlayers()} to inspect the immutable snapshot of players
 * who were part of the game before cleanup. This event is useful for external
 * logging, administration integrations, and arena lifecycle tracking.
 * <p>
 * This event is informational and is not cancellable.
 *
 * @author Despical
 * <p>
 * Created at 27.07.2026
 */
public class GameStopEvent extends GameEvent {

    private static final HandlerList HANDLER_LIST = new HandlerList();

    /**
     * The reason why the game was stopped.
     */
    @NotNull
    private final StopReason reason;

    /**
     * Immutable snapshot of player UUIDs present before cleanup.
     */
    @NotNull
    private final List<UUID> stoppedPlayers;

    /**
     * Constructs a game stop event without a player snapshot.
     *
     * @param game the game that was stopped
     * @param reason the reason for stopping the game
     */
    public GameStopEvent(@NotNull Game game, @NotNull StopReason reason) {
        this(game, reason, List.of());
    }

    /**
     * Constructs a new game stop event.
     *
     * @param game the game that was stopped
     * @param reason the reason for stopping the game
     * @param stoppedPlayers players that were present before cleanup
     */
    public GameStopEvent(@NotNull Game game, @NotNull StopReason reason, @NotNull List<UUID> stoppedPlayers) {
        super(game);
        this.reason = reason;
        this.stoppedPlayers = List.copyOf(stoppedPlayers);
    }

    /**
     * Returns why the game was stopped.
     *
     * @return the stop reason
     */
    @NotNull
    public StopReason getReason() {
        return reason;
    }

    /**
     * Returns the immutable UUID snapshot captured before player cleanup.
     *
     * @return the players present before the game stopped
     */
    @NotNull
    public List<UUID> getStoppedPlayers() {
        return stoppedPlayers;
    }

    /**
     * Returns the Bukkit handler list for this event type.
     *
     * @return this event's handler list
     */
    @NotNull
    @Override
    public HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    /**
     * Returns the static Bukkit handler list for this event type.
     *
     * @return this event's handler list
     */
    @NotNull
    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }

    /**
     * Returns a compact debug representation of the stopped game.
     *
     * @return a string containing the arena, reason, and player snapshot
     */
    @Override
    public String toString() {
        return "arena=%s, reason=%s, stoppedPlayers=%s"
            .formatted(getArena().getId(), reason, stoppedPlayers);
    }
}
