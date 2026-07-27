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

package dev.despical.kotl.game;

import dev.despical.kotl.KOTL;
import dev.despical.kotl.api.events.player.PlayerLeaveArenaEvent;
import dev.despical.kotl.arena.Arena;
import dev.despical.kotl.arena.options.ArenaKeys;
import dev.despical.kotl.bossbar.BossBarConfig;
import dev.despical.kotl.user.User;
import lombok.AccessLevel;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.List;

/**
 * @author Despical
 * <p>
 * Created at 06.06.2026
 */
@Getter
public class GameManager {

    @Getter(AccessLevel.NONE)
    private final KOTL plugin;
    private final BossBarConfig bossBarConfig;

    public GameManager(KOTL plugin) {
        this.plugin = plugin;
        this.bossBarConfig = new BossBarConfig(plugin);
    }

    public void stopGame(Game game, StopReason reason) {
        game.getScoreboardManager().removeAllScoreboards();
        game.getBossBarManager().removeAll();

        Arena arena = game.getArena();
        Location endLocation = arena.getOption(ArenaKeys.END_LOCATION);
        String messagePath = reason.getMessagePath();
        boolean saveStats = reason != StopReason.SERVER_RELOAD && reason != StopReason.SERVER_SHUTDOWN;

        for (Player player : game.getPlayers()) {
            if (messagePath != null) {
                plugin.getChatManager().sendMessage(player, messagePath);
            }

            if (endLocation != null && endLocation.getWorld() != null) {
                player.teleport(endLocation, PlayerTeleportEvent.TeleportCause.PLUGIN);
            }

            game.removePlayer(player, false, true, saveStats, mapLeaveReason(reason));
        }
    }

    public void stopAllGames(StopReason reason) {
        getGames().forEach(game -> stopGame(game, reason));
    }

    private PlayerLeaveArenaEvent.LeaveReason mapLeaveReason(StopReason reason) {
        return switch (reason) {
            case ARENA_DELETED -> PlayerLeaveArenaEvent.LeaveReason.ARENA_DELETED;
            case ARENA_DISABLED -> PlayerLeaveArenaEvent.LeaveReason.ARENA_DISABLED;
            case SERVER_RELOAD -> PlayerLeaveArenaEvent.LeaveReason.SERVER_RELOAD;
            case SERVER_SHUTDOWN -> PlayerLeaveArenaEvent.LeaveReason.SERVER_SHUTDOWN;
            case STOP_COMMAND -> PlayerLeaveArenaEvent.LeaveReason.STOP_COMMAND;
        };
    }

    public List<Game> getGames() {
        return plugin.getArenaRegistry().getArenas()
            .stream()
            .map(Arena::getGame)
            .toList();
    }

    public Game getGame(User user) {
        Arena arena = plugin.getArenaRegistry().getArena(user);
        if (arena == null) return null;

        return arena.getGame();
    }

    public void reload() {
        bossBarConfig.load();

        getGames().forEach(game -> {
            game.getScoreboardManager().loadContents();
            game.getScoreboardManager().refreshAllScoreboards();
            game.getBossBarManager().update();
        });
    }
}
