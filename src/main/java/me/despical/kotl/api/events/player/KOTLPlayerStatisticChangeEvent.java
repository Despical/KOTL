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

package me.despical.kotl.api.events.player;

import me.despical.kotl.api.StatisticType;
import me.despical.kotl.api.events.KOTLEvent;
import me.despical.kotl.arena.Arena;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * @author Despical
 * @since 1.0.0
 * <p>
 * Created at 20.06.2020
 */
public class KOTLPlayerStatisticChangeEvent extends KOTLEvent {

    private static final HandlerList handlers = new HandlerList();

    private final Player player;
    private final StatisticType statisticType;
    private final int value;

    public KOTLPlayerStatisticChangeEvent(Arena eventArena, Player player, StatisticType statisticType, int value) {
        super(eventArena);
        this.player = player;
        this.statisticType = statisticType;
        this.value = value;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public Player getPlayer() {
        return player;
    }

    public StatisticType getStatisticType() {
        return statisticType;
    }

    public int getNewValue() {
        return value;
    }
}
