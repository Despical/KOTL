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

import dev.despical.kotl.KOTL;
import dev.despical.kotl.api.events.KOTLEvent;
import dev.despical.kotl.user.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * @author Despical
 * <p>
 * Created at 05.06.2026
 */
@Getter
@AllArgsConstructor
public abstract class PlayerEvent extends KOTLEvent {

    /** The Bukkit player associated with this event */
    @NotNull
    private final Player player;

    /**
     * Returns the plugin-specific {@link User} for this player.
     *
     * @return the User object representing the player
     */
    @NotNull
    public final User getUser() {
        return KOTL.getInstance().getUserManager().getUser(player);
    }

    @Override
    public String toString() {
        return "player=%s".formatted(player.getName());
    }
}
