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

package dev.despical.kotl.arena.managers;

import dev.despical.kotl.KOTL;
import dev.despical.kotl.arena.Arena;
import dev.despical.kotl.arena.managers.schedulers.ArenaScheduler;
import dev.despical.kotl.arena.managers.schedulers.SchedulerOptions;
import dev.despical.kotl.game.Game;
import dev.despical.kotl.game.StopReason;
import dev.despical.kotl.user.User;
import dev.despical.kotl.util.ShutdownDetector;
import lombok.Getter;

/**
 * @author Despical
 * <p>
 * Created at 06.06.2026
 */
@Getter
public class ArenaManager {

    private final KOTL plugin;
    private final SchedulerOptions options;
    private final ArenaScheduler arenaScheduler;

    public ArenaManager(KOTL plugin) {
        this.plugin = plugin;
        final var config = plugin.getConfig();

        this.arenaScheduler = switch (config.getInt("arena-schedulers.type")) {
            case 1 -> ArenaScheduler.GENERAL;
            default -> ArenaScheduler.EVENT;
        };

        final int interval = config.getInt("arena-schedulers.interval");
        final boolean async = config.getBoolean("arena-schedulers.async");

        this.options = new SchedulerOptions(async, interval);

        arenaScheduler.register(options);
    }

    public boolean joinAttempt(User user, Arena arena) {
        Game game = arena.getGame();

        if (game == null) {
            return false;
        }

        game.addPlayer(user.getPlayer());
        return true;
    }

    public void leaveAttempt(User user) {
        Arena arena = user.getArena();

        if (arena == null) {
            return;
        }

        Game game = arena.getGame();
        game.removePlayer(user.getPlayer(), false);
    }

    public void quitPlayer(User user, Arena arena) {
        if (arena == null) {
            return;
        }

        Game game = arena.getGame();
        game.removePlayer(user.getPlayer(), true);
    }

    public void handleDisable() {
        StopReason reason = resolveStopReason();
        plugin.getGameManager().stopAllGames(reason);
    }

    private StopReason resolveStopReason() {
        return ShutdownDetector.isShutdown()
            ? StopReason.SERVER_SHUTDOWN
            : StopReason.SERVER_RELOAD;
    }
}
