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

package dev.despical.kotl.api;

import dev.despical.kotl.KOTL;
import dev.despical.kotl.api.events.player.PlayerBecomeKingEvent;
import dev.despical.kotl.api.events.player.PlayerEnterArenaEvent;
import dev.despical.kotl.api.events.player.PlayerLeaveArenaEvent;
import dev.despical.kotl.api.events.player.PlayerStatisticChangeEvent;
import dev.despical.kotl.game.Game;
import dev.despical.kotl.stats.StatisticType;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;

import java.util.function.Supplier;

/**
 * @author Despical
 * <p>
 * Created at 05.06.2026
 */
public final class EventManager {

    private final EventProfiler profiler;

    public EventManager(KOTL plugin) {
        this.profiler = new EventProfiler(plugin);
    }

    public <T extends Event> T call(T event) {
        long start = System.nanoTime();
        Bukkit.getPluginManager().callEvent(event);

        long duration = System.nanoTime() - start;
        profiler.record(event, duration);
        return event;
    }

    public <T extends Event> T callByType(EventType type, Supplier<T> supplier) {
        T event = supplier.get();
        Class<? extends Event> expected = EventRegistry.getEventClass(type);

        if (!expected.isInstance(event)) {
            String message = "EventType mismatch! Expected: %s but got: %s"
                .formatted(expected.getSimpleName(), event.getClass().getSimpleName());
            throw new IllegalArgumentException(message);
        }

        return call(event);
    }

    public void sendTimingsReport(CommandSender sender) {
        profiler.sendReport(sender);
    }

    public void reload() {
        profiler.reload();
    }

    public PlayerEnterArenaEvent playerEnterArena(Player player, Game game) {
        return callByType(EventType.PLAYER_ENTER_ARENA, () -> new PlayerEnterArenaEvent(player, game));
    }

    public PlayerLeaveArenaEvent playerLeaveArena(Player player, Game game) {
        return callByType(EventType.PLAYER_LEAVE_ARENA, () -> new PlayerLeaveArenaEvent(player, game));
    }

    public PlayerLeaveArenaEvent playerLeaveArena(Player player, Game game, PlayerLeaveArenaEvent.LeaveReason reason) {
        return callByType(EventType.PLAYER_LEAVE_ARENA,
            () -> new PlayerLeaveArenaEvent(player, game, reason));
    }

    public PlayerBecomeKingEvent playerBecomeKing(Player player, Game game) {
        return callByType(EventType.PLAYER_BECOME_KING, () -> new PlayerBecomeKingEvent(player, game));
    }

    public PlayerBecomeKingEvent playerBecomeKing(Player player, Game game, String previousKing) {
        return callByType(EventType.PLAYER_BECOME_KING,
            () -> new PlayerBecomeKingEvent(player, game, previousKing));
    }

    public <T> PlayerStatisticChangeEvent<T> statChange(Player player, StatisticType<T> stat, T oldValue, T newValue) {
        return callByType(EventType.PLAYER_STAT_CHANGE,
            () -> new PlayerStatisticChangeEvent<>(player, stat, oldValue, newValue));
    }
}
