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

package dev.despical.kotl.rewards;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Events that can execute configured reward commands.
 *
 * @author Despical
 * <p>
 * Created at 21.08.2026
 */
@Getter
@RequiredArgsConstructor
public enum RewardType {

    GAME_JOIN("game-join"),
    GAME_QUIT("game-quit"),
    BECOME_KING("become-king"),
    PLAYER_KILL("player-kill"),
    PLAYER_DEATH("player-death");

    private final String configurationPath;
}
