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
import lombok.Setter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * @author HappyAreaBean
 * @since 2.7.9
 * <p>
 * Created at 02.01.2024
 */
public class PlayerLeaveArenaEvent extends PlayerEvent {

    @Setter
    private boolean cancelled;

    private final Game game;

    public PlayerLeaveArenaEvent(Player player, Game game) {
        super(player);
        this.game = game;
    }

    @NotNull
    public Arena getArena() {
        return game.getArena();
    }

    @Override
    public String toString() {
        return "player=%s, arena=%s, cancelled=%s"
            .formatted(getPlayer().getName(), getArena().getId(), cancelled);
    }
}
