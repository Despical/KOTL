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

import dev.despical.kotl.stats.StatisticType;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Called before a player's statistic value is stored.
 * <p>
 * The event exposes the statistic key, the previous value, and a mutable new
 * value. Listeners may cancel the update or replace the new value before it is
 * written to the player's statistic map.
 * <p>
 * Common use cases:
 * <ul>
 *   <li>Applying boosters or multipliers</li>
 *   <li>Clamping values to a maximum or minimum</li>
 *   <li>Rejecting invalid statistic changes</li>
 *   <li>Mirroring statistic updates to an external service</li>
 * </ul>
 *
 * @param <T> the value type used by the statistic
 * @author Despical
 * @since 20.06.2020
 */
@Getter
public class PlayerStatisticChangeEvent<T> extends PlayerEvent implements Cancellable {

    private static final HandlerList HANDLER_LIST = new HandlerList();

    /**
     * The value that will be stored if the event is not cancelled.
     * <p>
     * Listeners may replace this value to modify the statistic update.
     */
    @Setter
    private T newValue;

    /**
     * Whether this statistic update has been cancelled.
     */
    @Setter
    private boolean cancelled;

    /**
     * The statistic key being updated.
     */
    private final StatisticType<T> stat;

    /**
     * The value stored before this update was requested.
     */
    private final T oldValue;

    /**
     * Constructs a new player statistic change event.
     *
     * @param player the player whose statistic is changing
     * @param stat the statistic key being updated
     * @param oldValue the value stored before the update
     * @param newValue the value that will be stored unless modified or cancelled
     */
    public PlayerStatisticChangeEvent(@NotNull Player player, @NotNull StatisticType<T> stat, T oldValue, T newValue) {
        super(player);
        this.stat = stat;
        this.oldValue = oldValue;
        this.newValue = newValue;
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
     * Returns a compact debug representation of the statistic transition.
     *
     * @return a string containing the player, statistic, values, and cancellation state
     */
    @Override
    public String toString() {
        return "player=%s, stat=%s, oldValue=%s, newValue=%s, cancelled=%s".formatted(
            getPlayer().getName(),
            stat.getKey(),
            String.valueOf(oldValue),
            String.valueOf(newValue),
            cancelled
        );
    }
}
