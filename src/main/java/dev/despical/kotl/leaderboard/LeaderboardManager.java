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

package dev.despical.kotl.leaderboard;

import dev.despical.kotl.KOTL;
import dev.despical.kotl.stats.StatisticType;
import dev.despical.kotl.stats.Statistics;
import dev.despical.kotl.stats.offline.OfflineStats;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;

/**
 * @author Despical
 * <p>
 * Created at 04.06.2026
 */
public class LeaderboardManager {

    private final KOTL plugin;
    private final Map<String, Leaderboard<?>> leaderboards;

    public LeaderboardManager(KOTL plugin) {
        this.plugin = plugin;
        this.leaderboards = new HashMap<>();
    }

    public void refreshAllLeaderboards() {
        Set<OfflineStats> allPlayersCache = plugin.getDatabase().getAllPlayers();

        for (StatisticType<?> type : Statistics.getPersistentStats()) {
            if (type.getType() == Integer.class) {
                @SuppressWarnings("unchecked")
                StatisticType<Integer> intType = (StatisticType<Integer>) type;

                createLeaderboard(
                    intType.getKey(),
                    allPlayersCache,
                    stats -> stats.getStat(intType),
                    Comparator.<Integer>naturalOrder().reversed(),
                    0
                );
            }
        }

        for (String arenaName : plugin.getArenaRegistry().getArenaNames()) {
            createLeaderboard(
                "arena_score_" + arenaName,
                allPlayersCache,
                stats -> {
                    Map<String, Integer> scores = stats.getStat(Statistics.ARENA_SCORES);
                    return scores != null ? scores.getOrDefault(arenaName, 0) : 0;
                },
                Comparator.<Integer>naturalOrder().reversed(),
                0
            );
        }
    }

    private <T extends Comparable<T>> void createLeaderboard(String id, Set<OfflineStats> allPlayers, Function<OfflineStats, T> valueExtractor, Comparator<T> comparator, T fallbackValue) {
        List<LeaderboardEntry<T>> entries = allPlayers.stream()
            .map(stats -> new LeaderboardEntry<>(stats.getUuid(), stats.getName(), valueExtractor.apply(stats)))
            .filter(entry -> {
                if (entry.value() instanceof Number num) {
                    return num.doubleValue() > 0;
                }

                return true;
            })
            .sorted((e1, e2) -> comparator.compare(e1.value(), e2.value()))
            .toList();

        Leaderboard<T> leaderboard = new Leaderboard<>(id, entries, fallbackValue);
        leaderboards.put(id, leaderboard);
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public <T extends Comparable<T>> Leaderboard<T> getLeaderboard(String id) {
        return (Leaderboard<T>) leaderboards.get(id);
    }

    @Nullable
    public <T extends Comparable<T>> Leaderboard<T> getLeaderboard(StatisticType<T> type) {
        return getLeaderboard(type.getKey());
    }
}
