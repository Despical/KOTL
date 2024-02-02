/*
 * KOTL - Don't let others climb to top of the ladders!
 * Copyright (C) 2024  Berke Akçen
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

package me.despical.kotl.api.events.arena;

import me.despical.kotl.api.events.KOTLEvent;
import me.despical.kotl.arena.Arena;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * @author Despical
 * @since 2.8.1
 * <p>
 * Created at 2.02.2024
 */
public class KOTLNewKingEvent extends KOTLEvent {

	private static final HandlerList handlers = new HandlerList();

	private final Player king;
	private final boolean sameKing;

	public KOTLNewKingEvent(Arena eventArena, Player king, boolean sameKing) {
		super(eventArena);
		this.king = king;
		this.sameKing = sameKing;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

	@Override
	public @NotNull HandlerList getHandlers() {
		return handlers;
	}

	/**
	 * Current king of the arena.
	 *
	 * @return current king player.
	 */
	public Player getKing() {
		return king;
	}

	/**
	 * Returns last king equals to new king.
	 *
	 * @return true if last king equals to
	 *         new king, otherwise false.
	 */
	public boolean isSameKing() {
		return sameKing;
	}
}