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

package dev.despical.kotl.api.events.player;

import dev.despical.kotl.arena.Arena;
import dev.despical.kotl.game.Game;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Called immediately before a player is removed from a KOTL arena.
 * <p>
 * The player is still registered in the game when this event is fired. Arena
 * membership, statistics, inventory state, scoreboards, and boss bars are
 * therefore still available to listeners.
 * <p>
 * This event is informational and is not cancellable. The {@link LeaveReason}
 * identifies whether the removal came from normal movement, death,
 * disconnection, administration, or game shutdown.
 *
 * @author HappyAreaBean
 * @since 2.7.9
 * <p>
 * Created at 02.01.2024
 */
@Getter
public class PlayerLeaveArenaEvent extends PlayerEvent {

    private static final HandlerList HANDLER_LIST = new HandlerList();

    /**
     * The game the player is leaving.
     */
    private final Game game;

    /**
     * The reason the player is being removed from the arena.
     */
    private final LeaveReason reason;

    /**
     * Constructs an arena leave event with {@link LeaveReason#AREA_EXIT}.
     *
     * @param player the player leaving the arena
     * @param game the game the player is leaving
     */
    public PlayerLeaveArenaEvent(@NotNull Player player, @NotNull Game game) {
        this(player, game, LeaveReason.AREA_EXIT);
    }

    /**
     * Constructs a new arena leave event.
     *
     * @param player the player leaving the arena
     * @param game the game the player is leaving
     * @param reason the reason for the removal
     */
    public PlayerLeaveArenaEvent(@NotNull Player player, @NotNull Game game, @NotNull LeaveReason reason) {
        super(player);
        this.game = game;
        this.reason = reason;
    }

    /**
     * Returns the arena the player is leaving.
     *
     * @return the current arena
     */
    @NotNull
    public Arena getArena() {
        return game.getArena();
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
     * Returns a compact debug representation of the arena departure.
     *
     * @return a string containing the player, arena, and leave reason
     */
    @Override
    public String toString() {
        return "player=%s, arena=%s, reason=%s"
            .formatted(getPlayer().getName(), getArena().getId(), reason);
    }

    /**
     * Describes why a player is being removed from a KOTL arena.
     */
    public enum LeaveReason {

        /**
         * The player crossed out of the configured game area.
         */
        AREA_EXIT,

        /**
         * The player died and is being restored outside the arena.
         */
        DEATH,

        /**
         * An administrator removed the player from the arena.
         */
        KICK,

        /**
         * The player disconnected from the server.
         */
        DISCONNECT,

        /**
         * The arena is being deleted.
         */
        ARENA_DELETED,

        /**
         * The arena is being disabled.
         */
        ARENA_DISABLED,

        /**
         * The plugin or server is reloading.
         */
        SERVER_RELOAD,

        /**
         * The server is shutting down.
         */
        SERVER_SHUTDOWN,

        /**
         * An administrator stopped the game.
         */
        STOP_COMMAND
    }
}
