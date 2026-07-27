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
import dev.despical.kotl.arena.options.ArenaKeys;
import dev.despical.kotl.game.Game;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Called immediately before a player becomes king in a KOTL arena.
 * <p>
 * The player has already passed the built-in repeated-king and cooldown checks
 * when this event is fired. Cancelling it prevents the crown change, cooldown,
 * score updates, announcements, fireworks, and plate knockback.
 * <p>
 * The previous king name may be {@code null} when the arena has not yet had a
 * king during the current runtime. A repeated claim means the same player is
 * claiming the plate again while consecutive king claims are enabled.
 *
 * @author Despical
 * @since 2.8.1
 * <p>
 * Created at 2.02.2024
 */
@Getter
public class PlayerBecomeKingEvent extends PlayerEvent implements Cancellable {

    private static final HandlerList HANDLER_LIST = new HandlerList();

    /**
     * Whether this king change has been cancelled.
     */
    @Setter
    private boolean cancelled;

    /**
     * The game in which the player is attempting to become king.
     */
    private final Game game;

    /**
     * The player name stored as king before this event, if one exists.
     */
    @Nullable
    private final String previousKing;

    /**
     * Whether the player is reclaiming the crown consecutively.
     */
    private final boolean repeatedClaim;

    /**
     * Constructs a king change event using the current arena state.
     *
     * @param player the player attempting to become king
     * @param game the game in which the claim is occurring
     */
    public PlayerBecomeKingEvent(@NotNull Player player, @NotNull Game game) {
        this(player, game, game.getArena().getOption(ArenaKeys.KING));
    }

    /**
     * Constructs a king change event with an explicit previous king.
     *
     * @param player the player attempting to become king
     * @param game the game in which the claim is occurring
     * @param previousKing the previous king name, or {@code null} when absent
     */
    public PlayerBecomeKingEvent(@NotNull Player player, @NotNull Game game, @Nullable String previousKing) {
        super(player);
        this.game = game;
        this.previousKing = previousKing;
        this.repeatedClaim = previousKing != null && previousKing.equals(player.getName());
    }

    /**
     * Returns the arena in which the crown claim is occurring.
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
     * Returns a compact debug representation of the crown claim.
     *
     * @return a string containing the player, arena, previous king, and cancellation state
     */
    @Override
    public String toString() {
        return "player=%s, arena=%s, previousKing=%s, repeatedClaim=%s, cancelled=%s"
            .formatted(getPlayer().getName(), getArena().getId(), previousKing, repeatedClaim, cancelled);
    }
}
