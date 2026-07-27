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

/**
 * Called when a player's statistic is about to change.
 *
 * <p>This event is fired before the new value is applied, allowing listeners to:</p>
 *
 * <ul>
 *   <li>Cancel the statistic change</li>
 *   <li>Modify the final value (boosters, perks, multipliers, caps)</li>
 *   <li>Detect suspicious or invalid stat changes (anti-cheat)</li>
 * </ul>
 *
 * <p>The {@code newValue} is mutable, meaning listeners can override the final value
 * using {@code setNewValue(...)}.</p>
 *
 * <p>If the event is canceled, the statistic update will NOT be applied.</p>
 *
 * <h2>Example: Apply a 2x booster</h2>
 * <pre>{@code
 * @EventHandler
 * public void onStatChange(PlayerStatisticChangeEvent event) {
 *     if (event.getStat() == Statistics.GAMES_PLAYED) {
 *         event.setNewValue(event.getNewValue() * 2);
 *     }
 * }
 * }</pre>
 *
 * <h2>Example: Cap a statistic value</h2>
 * <pre>{@code
 * @EventHandler
 * public void onStatChange(PlayerStatisticChangeEvent event) {
 *     int cap = 1000;
 *
 *     if (event.getNewValue() > cap) {
 *         event.setNewValue(cap);
 *     }
 * }
 * }</pre>
 *
 * <h2>Example: Block suspicious stat increases</h2>
 * <pre>{@code
 * @EventHandler
 * public void onStatChange(PlayerStatisticChangeEvent event) {
 *     int diff = event.getNewValue() - event.getOldValue();
 *
 *     if (diff > 50) {
 *         event.setCancelled(true);
 *     }
 * }
 * }</pre>
 *
 * <h2>Example: Reduce stat gain in specific conditions</h2>
 * <pre>{@code
 * @EventHandler
 * public void onStatChange(PlayerStatisticChangeEvent event) {
 *     Player player = event.getPlayer();
 *
 *     if (player.hasPermission("stats.half")) {
 *         int reduced = event.getOldValue()
 *             + ((event.getNewValue() - event.getOldValue()) / 2);
 *
 *         event.setNewValue(reduced);
 *     }
 * }
 * }</pre>

 *
 * @author Despical
 * @since 20.06.2020
 */
@Getter
public class PlayerStatisticChangeEvent<T> extends PlayerEvent implements Cancellable {

    @Setter
    private T newValue;

    @Setter
    private boolean cancelled;

    private final StatisticType<T> stat;
    private final T oldValue;

    public PlayerStatisticChangeEvent(Player player, StatisticType<T> stat, T oldValue, T newValue) {
        super(player);
        this.stat = stat;
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

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
