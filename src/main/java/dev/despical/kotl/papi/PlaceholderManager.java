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
import dev.despical.kotl.arena.options.ArenaKeys;
import dev.despical.kotl.leaderboard.Leaderboard;
import dev.despical.kotl.leaderboard.LeaderboardEntry;
import dev.despical.kotl.stats.Statistics;
import dev.despical.kotl.user.User;
import dev.despical.kotl.util.Utils;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

/**
 * @author Despical
 * <p>
 * Created at 05.06.2026
 */
public class PlaceholderManager extends PlaceholderExpansion {

    private static final String NO_ARENA = "none";
    private static final String NO_DATA = "--:--.---";

    private final KOTL plugin;

    public PlaceholderManager(KOTL plugin) {
        this.plugin = plugin;
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
        if (id.equals("arenas_total")) {
            return Integer.toString(plugin.getArenaRegistry().getArenas().size());
        }

        if (id.equals("arenas_ready")) {
            return Integer.toString((int) plugin.getArenaRegistry().getArenas().stream().filter(arena -> arena.getOption(ArenaKeys.READY)).count());
        }

        if (id.equals("active_games")) {
            return Integer.toString(plugin.getGameManager().getGames().size());
        }

        if (id.equals("active_players")) {
            return Integer.toString(plugin.getGameManager().getGames().stream().mapToInt(game -> game.getPlayers().size()).sum());
        }

        if (id.startsWith("arena")) {
            return handleArenaPlaceholders(id);
        }

        if (id.startsWith("leaderboard")) {
            return handleLeaderboardPlaceholders(id);
        }

        if (id.startsWith("stat:")) {
            return player != null ? handleStatPlaceholder(plugin.getUserManager().getUser(player), id) : "";
        }

        if (player == null) {
            return "";
        }

        User user = plugin.getUserManager().getUser(player);
        if (user == null) {
            return "";
        }

        if (id.startsWith("stat:")) {
            return handleStatPlaceholder(user, id);
        }

        return switch (id) {
            case "name" -> user.getName();
            case "uuid" -> user.getUUID().toString();
//            case "is_playing" -> Boolean.toString(user.isInArena());
            case "current_arena" -> getCurrentArenaId(user);
            case "games_played" -> String.valueOf(user.getStatistic(Statistics.TOURS_PLAYED));
//            case "current_arena_players" -> Integer.toString(getCurrentArenaPlayers(user));
//            case "current_arena_state" -> getCurrentArenaState(user);
            default -> "No general placeholder like that";
        };
    }

    // %kotl_leaderboard:<stat>:<pos>:name%
    // %kotl_leaderboard:wins:1:name%
    // %kotl_leaderboard:arena_time_arena1:1:value%
    private String handleLeaderboardPlaceholders(String id) {
        String[] splitted = id.split(":");
        if (splitted.length < 4) {
            return "";
        }

        String statName = splitted[1];
        int pos;

        try {
            pos = Integer.parseInt(splitted[2]);
        } catch (NumberFormatException e) {
            return "Invalid position: " + splitted[2];
        }

        String asked = splitted[3];
        Leaderboard<?> leaderboard = plugin.getLeaderboardManager().getLeaderboard(statName);

        if (leaderboard == null) {
            return "Invalid statistic name";
        }

        LeaderboardEntry<?> entry = leaderboard.getEntryAtPosition(pos);

        return switch (asked) {
            case "name" -> entry.name();
            case "uuid" -> entry.uuid().toString();
            case "value" -> String.valueOf(entry.value());
            case "formatted_value" -> formatLeaderboardValue(statName, entry.value());
            default -> "";
        };
    }

    private String handleArenaPlaceholders(String id) {
        String[] data = id.split(":");
        if (data.length < 3) {
            return "";
        }

        Arena arena = plugin.getArenaRegistry().getArena(data[1]);
        if (arena == null) {
            return "No arena with this ID";
        }

        String key = data[2].toLowerCase(Locale.ROOT);
//        Game game = arena.getGame();

        return switch (key) {
            case "players" -> Integer.toString(arena.getGame().getPlayers().size());
            case "ready" -> Boolean.toString(arena.getOption(ArenaKeys.READY));
            case "enabled" -> Boolean.toString(arena.getOption(ArenaKeys.ENABLED));
            case "king" -> arena.getOption(ArenaKeys.KING) == null ? "None" : arena.getOption(ArenaKeys.KING);
            case "last_king", "last-king" -> arena.getOption(ArenaKeys.LAST_KING);
            case "top_king", "top-king" -> arena.getOption(ArenaKeys.TOP_KING);
            case "top_king_score", "top-king-score" -> Integer.toString(arena.getOption(ArenaKeys.TOP_KING_SCORE));
//            case "state" -> game != null ? game.getCurrentState().name().toLowerCase(Locale.ROOT) : "disabled";
//            case "checkpoint_count", "checkpoints" -> Integer.toString(arena.getOption(ArenaKeys.CHECKPOINTS).size());
            case "gamemode" -> arena.getOption(ArenaKeys.ARENA_GAMEMODE).name();
            case "scoreboard_enabled" -> Boolean.toString(arena.getOption(ArenaKeys.ARENA_SCOREBOARD_ENABLED));
            case "bossbar_enabled" -> Boolean.toString(arena.getOption(ArenaKeys.ARENA_BOSSBAR_ENABLED));
//            case "potion_effects_count" -> Integer.toString(arena.getOption(ArenaKeys.ARENA_POTION_EFFECTS).size());
//            case "has_potion_effects" -> Boolean.toString(!arena.getOption(ArenaKeys.ARENA_POTION_EFFECTS).isEmpty());
            default -> "No arena placeholder like that";
        };
    }

    private String handleStatPlaceholder(User user, String id) {
        if (user == null) {
            return "";
        }

        String[] data = id.split(":");
        if (data.length < 2) {
            return "";
        }

        return switch (data[1].toLowerCase(Locale.ROOT)) {
            case "kill" -> String.valueOf(user.getStatistic(Statistics.KILL));
            case "death" -> String.valueOf(user.getStatistic(Statistics.DEATH));
            case "score" -> String.valueOf(user.getStatistic(Statistics.SCORE));
            case "arena_score" -> {
                if (data.length < 3) yield "";
                yield String.valueOf(user.getArenaScore(data[2]));
            }
            case "games_played" -> String.valueOf(user.getStatistic(Statistics.TOURS_PLAYED));
            default -> "";
        };
    }

    private String getCurrentArenaId(User user) {
        Arena arena = user.getArena();
        return arena != null ? arena.getId() : NO_ARENA;
    }

//    private int getCurrentArenaPlayers(User user) {
//        Arena arena = user.getArena();
//        if (arena == null || arena.getGame() == null) {
//            return 0;
//        }
//
//        return arena.getGame().getUsers().size();
//    }

//    private String getCurrentArenaState(User user) {
//        Arena arena = user.getArena();
//        if (arena == null || arena.getGame() == null) {
//            return "none";
//        }
//
//        return arena.getGame().getCurrentState().name().toLowerCase(Locale.ROOT);
//    }

      private String formatLeaderboardValue(String statName, Object value) {
        if (value instanceof Number number) {
            return formatTimeValue(number.longValue());
        }

        return String.valueOf(value);
    }

    private String formatPercentage(int numerator, int denominator) {
        if (denominator <= 0) {
            return "0.00";
        }

        return String.format(Locale.US, "%.2f", (numerator * 100.0D) / denominator);
    }

    private String formatTimeValue(long millis) {
        return millis >= 0 ? Utils.formatTime(millis) : NO_DATA;
    }

    private String nullableString(String value) {
        return value == null || value.isBlank() ? "none" : value;
    }
}
