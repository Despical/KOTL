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
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Called when a player attempts to enter a KOTL arena.
 * <p>
 * This event is fired before the player is added to the target game and before
 * inventory, game mode, scoreboard, boss bar, or potion state is changed.
 * Cancelling the event prevents every part of the arena entry.
 * <p>
 * Typical use cases include permission checks, party restrictions, custom
 * cooldowns, maintenance locks, and external queue integrations.
 *
 * @author HappyAreaBean
 * @since 2.7.9
 * <p>
 * Created at 02.01.2024
 */
@Getter
public class PlayerEnterArenaEvent extends PlayerEvent implements Cancellable {

    private static final HandlerList HANDLER_LIST = new HandlerList();

    /**
     * Whether this arena entry has been cancelled.
     */
    @Setter
    private boolean cancelled;

    /**
     * The game the player is attempting to enter.
     */
    private final Game game;

    /**
     * Constructs a new player arena entry event.
     *
     * @param player the player attempting to enter
     * @param game the target game
     */
    public PlayerEnterArenaEvent(@NotNull Player player, @NotNull Game game) {
        super(player);
        this.game = game;
    }

    /**
     * Returns the arena the player is attempting to enter.
     *
     * @return the target arena
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
     * Returns a compact debug representation of the arena entry attempt.
     *
     * @return a string containing the player, arena, and cancellation state
     */
    @Override
    public String toString() {
        return "player=%s, arena=%s, cancelled=%s"
            .formatted(getPlayer().getName(), getArena().getId(), cancelled);
    }
}
