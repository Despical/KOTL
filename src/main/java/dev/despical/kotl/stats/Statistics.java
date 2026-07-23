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

package dev.despical.kotl.stats;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Despical
 * <p>
 * Created at 04.06.2026
 */
public final class Statistics {

    private static final Gson gson = new GsonBuilder().create();

    public static final StatisticType<Integer> KILL = createIntStat("kill");
    public static final StatisticType<Integer> DEATH = createIntStat("death");
    public static final StatisticType<Integer> SCORE = createIntStat("score");
    public static final StatisticType<Integer> TOURS_PLAYED = createIntStat("tours_played");

    public static final StatisticType<Map<String, Integer>> ARENA_SCORES = new StatisticType<>("arena_scores", new HashMap<>(), (Class<Map<String, Integer>>) (Class<?>) Map.class) {

        @Override
        public Object serialize(Map<String, Integer> value) {
            return gson.toJson(value);
        }

        @Override
        protected Map<String, Integer> parse(String value) {
            Map<String, Integer> parsed = gson.fromJson(value, new TypeToken<Map<String, Integer>>() {}.getType());
            return parsed != null ? parsed : new HashMap<>();
        }
    };

    public static final StatisticType<Integer> LOCAL_RESET_COOLDOWN = createLocalIntStat("local_reset_cooldown");

    private static StatisticType<Integer> createIntStat(String key) {
        return new StatisticType<>(key, 0, Integer.class) {

            @Override
            protected Integer parse(String value) {
                try {
                    return Integer.parseInt(value);
                } catch (NumberFormatException _) {
                    return 0;
                }
            }
        };
    }

    private static StatisticType<Integer> createLocalIntStat(String key) {
        return new StatisticType<>(key, 0, Integer.class) {

            @Override
            public boolean isPersistent() {
                return false;
            }
        };
    }

    public static List<StatisticType<?>> getAllStats() {
        return StatsHolder.ALL_STATS;
    }

    public static List<StatisticType<?>> getPersistentStats() {
        return StatsHolder.PERSISTENT_STATS;
    }

    private static class StatsHolder {

        private static final List<StatisticType<?>> ALL_STATS = List.of(KILL, DEATH, SCORE, TOURS_PLAYED, ARENA_SCORES, LOCAL_RESET_COOLDOWN);
        private static final List<StatisticType<?>> PERSISTENT_STATS = ALL_STATS.stream().filter(StatisticType::isPersistent).toList();
    }
}
