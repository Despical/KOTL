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

package dev.despical.kotl.user;

import dev.despical.kotl.KOTL;
import dev.despical.kotl.api.events.player.PlayerStatisticChangeEvent;
import dev.despical.kotl.arena.Arena;
import dev.despical.kotl.stats.StatisticType;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Despical
 * <p>
 * Created at 20.06.2020
 */
public class User {

    private static final KOTL plugin = JavaPlugin.getPlugin(KOTL.class);

    private final UUID uuid;

    @Getter
    private final String name;
    private final Map<StatisticType<?>, Object> stats;

    public User(Player player) {
        this.uuid = player.getUniqueId();
        this.name = player.getName();
        this.stats = new ConcurrentHashMap<>();
    }

    public Arena getArena() {
        return plugin.getArenaRegistry().getArena(getPlayer());
    }

    public Player getPlayer() {
        return Bukkit.getPlayer(uuid);
    }

    public UUID getUniqueId() {
        return uuid;
    }

    public UUID getUUID() {
        return uuid;
    }

    public void sendRawMessage(String message) {
        Player player = getPlayer();
        player.sendMessage(message);
    }

    @SuppressWarnings("unchecked")
    public <T> T getStatistic(StatisticType<T> type) {
        return (T) stats.computeIfAbsent(type, stat -> {
            if (stat.getDefaultValue() instanceof Map) {
                return new HashMap<>();
            }

            return stat.getDefaultValue();
        });
    }

    public <T> void setStatistic(StatisticType<T> type, T newValue) {
        setStatisticInternal(type, newValue, true);
    }

    public <T> void loadStatistic(StatisticType<T> type, T newValue) {
        setStatisticInternal(type, newValue, false);
    }

    private <T> void setStatisticInternal(StatisticType<T> type, T newValue, boolean callEvent) {
        T oldValue = getStatistic(type);
        if (oldValue != null && oldValue.equals(newValue)) {
            return;
        }

        T finalValue = newValue;

        if (callEvent) {
            PlayerStatisticChangeEvent<T> event =
                plugin.getEventManager().statChange(getPlayer(), type, oldValue, newValue);

            if (event.isCancelled()) {
                return;
            }

            finalValue = event.getNewValue();
        }

        if (oldValue != null && oldValue.equals(finalValue)) {
            return;
        }

        stats.put(type, finalValue);
    }

    public void addStat(StatisticType<Integer> type, int amount) {
        setStatistic(type, getStatistic(type) + amount);
    }

    @SafeVarargs
    public final void addStat(StatisticType<Integer> type, StatisticType<Integer>... types) {
        addStat(type, 1);

        for (StatisticType<Integer> statisticType : types) {
            addStat(statisticType, 1);
        }
    }

    public void setStatisticIfHigher(StatisticType<Integer> type, int amount) {
        setStatistic(type, Math.max(getStatistic(type), amount));
    }

    public int getArenaScore(String arenaId) {
        Map<String, Integer> arenaScores = getStatistic(dev.despical.kotl.stats.Statistics.ARENA_SCORES);
        return arenaScores.getOrDefault(arenaId, 0);
    }

    public void addArenaScore(String arenaId, int amount) {
        Map<String, Integer> arenaScores = new HashMap<>(getStatistic(dev.despical.kotl.stats.Statistics.ARENA_SCORES));
        arenaScores.put(arenaId, arenaScores.getOrDefault(arenaId, 0) + amount);

        setStatistic(dev.despical.kotl.stats.Statistics.ARENA_SCORES, arenaScores);
    }

    public void resetArenaStats(String arenaId) {
        Map<String, Integer> arenaScores = new HashMap<>(getStatistic(dev.despical.kotl.stats.Statistics.ARENA_SCORES));

        if (arenaScores.remove(arenaId) != null) {
            setStatistic(dev.despical.kotl.stats.Statistics.ARENA_SCORES, arenaScores);
        }
    }
}
