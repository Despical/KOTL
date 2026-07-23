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

package dev.despical.kotl.scoreboard;

import dev.despical.commons.configuration.ConfigUtils;
import dev.despical.commons.scoreboard.Scoreboard;
import dev.despical.commons.scoreboard.ScoreboardHandler;
import dev.despical.commons.scoreboard.ScoreboardLib;
import dev.despical.commons.scoreboard.common.Entry;
import dev.despical.commons.scoreboard.common.EntryBuilder;
import dev.despical.kotl.KOTL;
import dev.despical.kotl.arena.options.ArenaKeys;
import dev.despical.kotl.game.Game;
import dev.despical.kotl.scoreboard.formatter.GlobalFormatter;
import dev.despical.kotl.user.User;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ScoreboardManager {

    private static final KOTL plugin = KOTL.getInstance();

    private ScoreboardContent contents;

    private final Game game;
    private final Map<UUID, Scoreboard> scoreboards;

    public ScoreboardManager(Game game) {
        this.game = game;
        this.scoreboards = new HashMap<>();
        this.loadContents();
    }

    public void createScoreboard(Player player) {
        if (player == null) {
            return;
        }

        removeScoreboard(player);

        if (!isEnabled()) {
            resetPlayerScoreboard(player);
            return;
        }

        Scoreboard scoreboard = ScoreboardLib.createScoreboard(player);
        scoreboard.setHandler(new ScoreboardHandler() {

            @Override
            public Component getTitle(Player player) {
                return plugin.getChatManager().parseMessage(contents.title);
            }

            @Override
            public List<Entry> getEntries(Player player) {
                return getLines(player);
            }
        });

        scoreboard.disableAutoUpdate();
        scoreboard.activate();
        scoreboard.update();

        scoreboards.put(player.getUniqueId(), scoreboard);
    }

    public void removeScoreboard(Player player) {
        if (player == null) {
            return;
        }

        Scoreboard scoreboard = scoreboards.remove(player.getUniqueId());

        if (scoreboard != null) {
            scoreboard.deactivate();
        }

        resetPlayerScoreboard(player);
    }

    public void removeAllScoreboards() {
        scoreboards.forEach((uuid, scoreboard) -> {
            scoreboard.deactivate();

            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                resetPlayerScoreboard(player);
            }
        });
        scoreboards.clear();
    }

    public void updateScoreboard(Player player) {
        Scoreboard scoreboard = scoreboards.get(player.getUniqueId());

        if (scoreboard != null) {
            scoreboard.update();
        }
    }

    public void updateAllScoreboards() {
        game.getPlayers().forEach(this::updateScoreboard);
    }

    private List<Entry> getLines(Player player) {
        EntryBuilder builder = new EntryBuilder();

        User user = plugin.getUserManager().getUser(player);

        for (String line : contents.lines) {
            builder.next(formatLine(line, user));
        }

        return builder.build();
    }

    private String formatLine(String line, User user) {
        return GlobalFormatter.INSTANCE.format(user, game, line);
    }

    public void loadContents() {
        FileConfiguration config = ConfigUtils.getConfig(plugin, "scoreboard");
        String title = config.getString("title");
        List<String> lines = config.getStringList("lines");

        contents = new ScoreboardContent(title, lines);
    }

    public void refreshAllScoreboards() {
        if (!isEnabled()) {
            removeAllScoreboards();
            return;
        }

        game.getPlayers().forEach(this::createScoreboard);
        updateAllScoreboards();
    }

    private boolean isEnabled() {
        return game.getArena().getOption(ArenaKeys.ARENA_SCOREBOARD_ENABLED);
    }

    public static void resetPlayerScoreboard(Player player) {
        if (player == null) {
            return;
        }

        var scoreboardManager = Bukkit.getScoreboardManager();
        player.setScoreboard(scoreboardManager.getMainScoreboard());
    }

    private record ScoreboardContent(String title, List<String> lines) {
    }
}
