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

import dev.despical.kotl.KOTL;
import dev.despical.kotl.arena.options.ArenaKeys;
import dev.despical.kotl.game.Game;
import dev.despical.kotl.stats.Statistics;
import dev.despical.kotl.user.User;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

/**
 * Immutable values captured when a reward trigger occurs.
 *
 * @author Despical
 * <p>
 * Created at 21.08.2026
 */
record RewardContext(Player player, Map<String, String> placeholders) {

    static RewardContext from(KOTL plugin, Player player, Game game, Map<String, String> triggerPlaceholders) {
        if (player == null || game == null) {
            return null;
        }

        User user = plugin.getUserManager().getUser(player);
        if (user == null) {
            return null;
        }

        String arenaId = game.getArena().getId();
        String king = game.getArena().getOption(ArenaKeys.KING);

        Map<String, String> placeholders = new HashMap<>(20);
        placeholders.put("%player%", player.getName());
        placeholders.put("%uuid%", player.getUniqueId().toString());
        placeholders.put("%arena%", arenaId);
        placeholders.put("%king%", king == null ? "NONE" : king);
        placeholders.put("%score%", Integer.toString(user.getStatistic(Statistics.SCORE)));
        placeholders.put("%kills%", Integer.toString(user.getStatistic(Statistics.KILL)));
        placeholders.put("%deaths%", Integer.toString(user.getStatistic(Statistics.DEATH)));
        placeholders.put("%tours-played%", Integer.toString(user.getStatistic(Statistics.TOURS_PLAYED)));
        placeholders.put("%arena-score%", Integer.toString(user.getArenaScore(arenaId)));
        placeholders.put("%player-count%", Integer.toString(game.getPlayers().size()));
        placeholders.put("%previous-king%", "NONE");
        placeholders.put("%target%", "NONE");
        placeholders.put("%target-uuid%", "NONE");
        placeholders.put("%reason%", "NONE");
        placeholders.putAll(triggerPlaceholders);

        return new RewardContext(player, Map.copyOf(placeholders));
    }

    String format(String command) {
        String formatted = command;

        for (var placeholder : placeholders.entrySet()) {
            formatted = formatted.replace(placeholder.getKey(), placeholder.getValue());
        }

        return formatted;
    }
}
