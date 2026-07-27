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
import dev.despical.kotl.api.events.game.GameStopEvent;
import dev.despical.kotl.api.events.player.PlayerBecomeKingEvent;
import dev.despical.kotl.api.events.player.PlayerEnterArenaEvent;
import dev.despical.kotl.api.events.player.PlayerLeaveArenaEvent;
import dev.despical.kotl.api.events.player.PlayerStatisticChangeEvent;
import dev.despical.kotl.game.Game;
import dev.despical.kotl.game.StopReason;
import dev.despical.kotl.stats.StatisticType;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * Central dispatcher for KOTL custom events.
 * <p>
 * The manager invokes Bukkit listeners synchronously, records optional timing
 * information, and validates named event factories against {@link EventType}.
 * External plugins should listen to the concrete event classes rather than
 * manually invoking the lifecycle factory methods in this class.
 *
 * @author Despical
 * <p>
 * Created at 05.06.2026
 */
@ApiStatus.Internal
public final class EventManager {

    private final EventProfiler profiler;

    /**
     * Creates the event dispatcher used by the plugin runtime.
     *
     * @param plugin the owning KOTL instance
     */
    public EventManager(@NotNull KOTL plugin) {
        this.profiler = new EventProfiler(plugin);
    }

    /**
     * Dispatches a Bukkit event and records its listener execution time.
     *
     * @param event the event to dispatch
     * @param <T> the concrete event type
     * @return the same event instance after listeners have run
     */
    @NotNull
    public <T extends Event> T call(@NotNull T event) {
        long start = System.nanoTime();
        Bukkit.getPluginManager().callEvent(event);

        long duration = System.nanoTime() - start;
        profiler.record(event, duration);
        return event;
    }

    /**
     * Creates and dispatches an event after validating its registered type.
     *
     * @param type the expected event type
     * @param supplier the factory that creates the event
     * @param <T> the concrete event type
     * @return the event after listeners have run
     * @throws IllegalArgumentException if the factory returns the wrong event class
     */
    @NotNull
    public <T extends Event> T callByType(@NotNull EventType type, @NotNull Supplier<T> supplier) {
        T event = supplier.get();
        Class<? extends Event> expected = EventRegistry.getEventClass(type);

        if (!expected.isInstance(event)) {
            String message = "EventType mismatch! Expected: %s but got: %s"
                .formatted(expected.getSimpleName(), event.getClass().getSimpleName());
            throw new IllegalArgumentException(message);
        }

        return call(event);
    }

    /**
     * Sends the current event timing report to a command sender.
     *
     * @param sender the report recipient
     */
    public void sendTimingsReport(@NotNull CommandSender sender) {
        profiler.sendReport(sender);
    }

    /**
     * Reloads event profiling options from the plugin configuration.
     */
    public void reload() {
        profiler.reload();
    }

    /**
     * Dispatches a completed game stop notification.
     *
     * @param game the game that was stopped
     * @param reason the reason for stopping the game
     * @param stoppedPlayers the player UUID snapshot captured before cleanup
     */
    public void gameStop(@NotNull Game game, @NotNull StopReason reason, @NotNull List<UUID> stoppedPlayers) {
        callByType(EventType.GAME_STOP, () -> new GameStopEvent(game, reason, stoppedPlayers));
    }

    /**
     * Dispatches a player arena entry attempt.
     *
     * @param player the player attempting to enter
     * @param game the target game
     * @return the event after listeners have run
     */
    @NotNull
    public PlayerEnterArenaEvent playerEnterArena(@NotNull Player player, @NotNull Game game) {
        return callByType(EventType.PLAYER_ENTER_ARENA, () -> new PlayerEnterArenaEvent(player, game));
    }

    /**
     * Dispatches a normal arena area-exit notification.
     *
     * @param player the player leaving the arena
     * @param game the game being left
     * @return the event after listeners have run
     */
    @NotNull
    public PlayerLeaveArenaEvent playerLeaveArena(@NotNull Player player, @NotNull Game game) {
        return callByType(EventType.PLAYER_LEAVE_ARENA, () -> new PlayerLeaveArenaEvent(player, game));
    }

    /**
     * Dispatches an arena departure with an explicit reason.
     *
     * @param player the player leaving the arena
     * @param game the game being left
     * @param reason the reason for the removal
     * @return the event after listeners have run
     */
    @NotNull
    public PlayerLeaveArenaEvent playerLeaveArena(@NotNull Player player, @NotNull Game game,
                                                  @NotNull PlayerLeaveArenaEvent.LeaveReason reason) {
        return callByType(EventType.PLAYER_LEAVE_ARENA,
            () -> new PlayerLeaveArenaEvent(player, game, reason));
    }

    /**
     * Dispatches a crown claim using the game's current king state.
     *
     * @param player the player attempting to become king
     * @param game the active game
     * @return the event after listeners have run
     */
    @NotNull
    public PlayerBecomeKingEvent playerBecomeKing(@NotNull Player player, @NotNull Game game) {
        return callByType(EventType.PLAYER_BECOME_KING, () -> new PlayerBecomeKingEvent(player, game));
    }

    /**
     * Dispatches a crown claim with an explicit previous king snapshot.
     *
     * @param player the player attempting to become king
     * @param game the active game
     * @param previousKing the previous king name, or {@code null} when absent
     * @return the event after listeners have run
     */
    @NotNull
    public PlayerBecomeKingEvent playerBecomeKing(@NotNull Player player, @NotNull Game game,
                                                  @Nullable String previousKing) {
        return callByType(EventType.PLAYER_BECOME_KING,
            () -> new PlayerBecomeKingEvent(player, game, previousKing));
    }

    /**
     * Dispatches a mutable player statistic change.
     *
     * @param player the player whose statistic is changing
     * @param stat the statistic key being updated
     * @param oldValue the value stored before the update
     * @param newValue the requested new value
     * @param <T> the statistic value type
     * @return the event after listeners have run
     */
    @NotNull
    public <T> PlayerStatisticChangeEvent<T> statChange(@NotNull Player player, @NotNull StatisticType<T> stat,
                                                        T oldValue, T newValue) {
        return callByType(EventType.PLAYER_STAT_CHANGE,
            () -> new PlayerStatisticChangeEvent<>(player, stat, oldValue, newValue));
    }
}
