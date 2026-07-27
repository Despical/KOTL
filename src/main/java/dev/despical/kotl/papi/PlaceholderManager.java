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

package dev.despical.kotl.papi;

import dev.despical.kotl.KOTL;
import dev.despical.kotl.arena.Arena;
import dev.despical.kotl.arena.ArenaRegistry;
import dev.despical.kotl.arena.options.ArenaOption;
import dev.despical.kotl.arena.options.ArenaKeys;
import dev.despical.kotl.game.Game;
import dev.despical.kotl.game.GameManager;
import dev.despical.kotl.leaderboard.Leaderboard;
import dev.despical.kotl.leaderboard.LeaderboardEntry;
import dev.despical.kotl.stats.Statistics;
import dev.despical.kotl.user.User;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

/**
 * PlaceholderAPI expansion for King of the Ladder placeholders.
 *
 * @author Despical
 * <p>
 * Created at 05.06.2026
 */
public class PlaceholderManager extends PlaceholderExpansion {

    private static final String NO_ARENA = "none";

    private final KOTL plugin;
    private final ArenaRegistry arenaRegistry;
    private final GameManager gameManager;

    public PlaceholderManager(KOTL plugin) {
        this.plugin = plugin;
        this.arenaRegistry = plugin.getArenaRegistry();
        this.gameManager = plugin.getGameManager();
    }

    @NotNull
    @Override
    public String getIdentifier() {
        return "kotl";
    }

    @NotNull
    @Override
    public String getAuthor() {
        return "Despical";
    }

    @NotNull
    @Override
    public String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Nullable
    @Override
    public String onPlaceholderRequest(Player player, @NotNull String id) {
        String normalized = id.toLowerCase(Locale.ENGLISH);

        switch (normalized) {
            case "arenas_total" -> {
                return Integer.toString(arenaRegistry.getArenas().size());
            }
            case "arenas_ready" -> {
                return Long.toString(arenaRegistry.getArenas().stream()
                    .filter(arena -> arena.getOption(ArenaKeys.READY))
                    .count());
            }
            case "active_games" -> {
                return Long.toString(gameManager.getGames().stream()
                    .filter(game -> !game.getPlayers().isEmpty())
                    .count());
            }
            case "active_players" -> {
                return Integer.toString(gameManager.getGames().stream()
                    .mapToInt(game -> game.getPlayers().size())
                    .sum());
            }
        }

        if (normalized.startsWith("arena:")) {
            return handleArenaPlaceholder(id);
        }

        if (normalized.startsWith("leaderboard:")) {
            return handleLeaderboardPlaceholder(id);
        }

        if (player == null) {
            return "";
        }

        User user = plugin.getUserManager().getUser(player);
        if (user == null) {
            return "";
        }

        if (normalized.startsWith("stat:")) {
            return handleStatPlaceholder(user, id);
        }

        return switch (normalized) {
            case "name" -> user.getName();
            case "uuid" -> user.getUUID().toString();
            case "is_playing" -> Boolean.toString(user.getArena() != null);
            case "current_arena" -> getCurrentArenaId(user);
            case "current_arena_players" -> Integer.toString(getCurrentArenaPlayers(user));
            case "current_arena_king" -> getCurrentArenaKing(user);
            case "current_arena_last_king" -> getCurrentArenaOption(user, ArenaKeys.LAST_KING);
            case "current_arena_top_king" -> getCurrentArenaOption(user, ArenaKeys.TOP_KING);
            case "current_arena_top_king_score" -> Integer.toString(getCurrentArenaTopKingScore(user));
            case "current_arena_score" -> Integer.toString(getCurrentArenaScore(user));
            case "games_played", "tours_played" -> Integer.toString(user.getStatistic(Statistics.TOURS_PLAYED));
            case "score" -> Integer.toString(user.getStatistic(Statistics.SCORE));
            case "kills" -> Integer.toString(user.getStatistic(Statistics.KILL));
            case "deaths" -> Integer.toString(user.getStatistic(Statistics.DEATH));
            default -> "";
        };
    }

    // %kotl_arena:<arena>:players%
    private String handleArenaPlaceholder(String id) {
        String[] data = id.split(":");
        if (data.length < 3) {
            return "";
        }

        Arena arena = arenaRegistry.getArena(data[1]);
        if (arena == null) {
            return "";
        }

        String key = data[2].toLowerCase(Locale.ENGLISH);
        Game game = arena.getGame();

        return switch (key) {
            case "players" -> Integer.toString(game != null ? game.getPlayers().size() : 0);
            case "ready" -> Boolean.toString(arena.getOption(ArenaKeys.READY));
            case "enabled" -> Boolean.toString(arena.getOption(ArenaKeys.ENABLED));
            case "king" -> nullableString(arena.getOption(ArenaKeys.KING));
            case "last_king", "last-king" -> nullableString(arena.getOption(ArenaKeys.LAST_KING));
            case "top_king", "top-king" -> nullableString(arena.getOption(ArenaKeys.TOP_KING));
            case "top_king_score", "top-king-score" -> Integer.toString(arena.getOption(ArenaKeys.TOP_KING_SCORE));
            case "gamemode" -> arena.getOption(ArenaKeys.ARENA_GAMEMODE).name();
            case "scoreboard_enabled" -> Boolean.toString(arena.getOption(ArenaKeys.ARENA_SCOREBOARD_ENABLED));
            case "bossbar_enabled" -> Boolean.toString(arena.getOption(ArenaKeys.ARENA_BOSSBAR_ENABLED));
            default -> "";
        };
    }

    // %kotl_leaderboard:<stat>:<position>:name%
    // %kotl_leaderboard:score:1:name%
    // %kotl_leaderboard:arena_score_arena1:1:formatted_value%
    private String handleLeaderboardPlaceholder(String id) {
        String[] data = id.split(":");
        if (data.length < 4) {
            return "";
        }

        int position;
        try {
            position = Integer.parseInt(data[2]);
        } catch (NumberFormatException _) {
            return "";
        }

        String statName = data[1];
        Leaderboard<?> leaderboard = plugin.getLeaderboardManager().getLeaderboard(statName);
        if (leaderboard == null) {
            return "";
        }

        LeaderboardEntry<?> entry = leaderboard.getEntryAtPosition(position);
        String key = data[3].toLowerCase(Locale.ENGLISH);

        return switch (key) {
            case "name" -> entry.name();
            case "uuid" -> entry.uuid().toString();
            case "value", "formatted_value" -> String.valueOf(entry.value());
            default -> "";
        };
    }

    private String handleStatPlaceholder(User user, String id) {
        String[] data = id.split(":");
        if (data.length < 2) {
            return "";
        }

        return switch (data[1].toLowerCase(Locale.ENGLISH)) {
            case "kill", "kills" -> Integer.toString(user.getStatistic(Statistics.KILL));
            case "death", "deaths" -> Integer.toString(user.getStatistic(Statistics.DEATH));
            case "score" -> Integer.toString(user.getStatistic(Statistics.SCORE));
            case "arena_score" -> data.length >= 3 ? Integer.toString(user.getArenaScore(data[2])) : "";
            case "games_played", "tours_played" -> Integer.toString(user.getStatistic(Statistics.TOURS_PLAYED));
            default -> "";
        };
    }

    private String getCurrentArenaId(User user) {
        Arena arena = user.getArena();
        return arena != null ? arena.getId() : NO_ARENA;
    }

    private int getCurrentArenaPlayers(User user) {
        Arena arena = user.getArena();
        return arena != null && arena.getGame() != null ? arena.getGame().getPlayers().size() : 0;
    }

    private String getCurrentArenaKing(User user) {
        Arena arena = user.getArena();
        return arena != null ? nullableString(arena.getOption(ArenaKeys.KING)) : NO_ARENA;
    }

    private String getCurrentArenaOption(User user, ArenaOption<String> option) {
        Arena arena = user.getArena();
        return arena != null ? nullableString(arena.getOption(option)) : NO_ARENA;
    }

    private int getCurrentArenaTopKingScore(User user) {
        Arena arena = user.getArena();
        return arena != null ? arena.getOption(ArenaKeys.TOP_KING_SCORE) : 0;
    }

    private int getCurrentArenaScore(User user) {
        Arena arena = user.getArena();
        return arena != null ? user.getArenaScore(arena.getId()) : 0;
    }

    private String nullableString(String value) {
        return value == null || value.isBlank() ? NO_ARENA : value;
    }
}
