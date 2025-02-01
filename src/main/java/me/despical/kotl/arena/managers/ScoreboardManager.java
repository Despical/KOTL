/*
 * KOTL - Don't let others climb to top of the ladders!
 * Copyright (C) 2024  Berke Akçen
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

package me.despical.kotl.arena.managers;

import me.despical.commons.scoreboard.Scoreboard;
import me.despical.commons.scoreboard.ScoreboardHandler;
import me.despical.commons.scoreboard.ScoreboardLib;
import me.despical.commons.scoreboard.common.Entry;
import me.despical.commons.scoreboard.common.EntryBuilder;
import me.despical.kotl.KOTL;
import me.despical.kotl.api.StatsStorage;
import me.despical.kotl.arena.Arena;
import me.despical.kotl.handlers.ChatManager;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ScoreboardManager {

    private final KOTL plugin;
    private final Arena arena;
    private final ChatManager chatManager;
    private final Map<UUID, Scoreboard> scoreboards;

    public ScoreboardManager(KOTL plugin, Arena arena) {
        this.plugin = plugin;
        this.arena = arena;
        this.chatManager = plugin.getChatManager();
        this.scoreboards = new HashMap<>();
    }

    public void createScoreboard(Player player) {
        Scoreboard scoreboard = ScoreboardLib.createScoreboard(player).setHandler(new ScoreboardHandler() {

            @Override
            public String getTitle(Player player) {
                return chatManager.message("scoreboard.title");
            }

            @Override
            public List<Entry> getEntries(Player player) {
                return formatScoreboard(player);
            }
        });

        scoreboard.activate();
        scoreboards.put(player.getUniqueId(), scoreboard);
    }

    public void removeScoreboard(Player player) {
        Scoreboard scoreboard = scoreboards.remove(player.getUniqueId());

        if (scoreboard != null) {
            scoreboard.deactivate();
        }
    }

    public void stopAllScoreboards() {
        scoreboards.values().forEach(Scoreboard::deactivate);
        scoreboards.clear();
    }

    private List<Entry> formatScoreboard(Player player) {
        EntryBuilder builder = new EntryBuilder();

        for (String line : chatManager.getStringList("scoreboard.content.playing")) {
            builder.next(formatScoreboardLine(line, player));
        }

        return builder.build();
    }

    private String formatScoreboardLine(String line, Player player) {
        line = line.replace("%arena%", arena.getId());
        line = line.replace("%players%", Integer.toString(arena.getPlayers().size()));
        line = line.replace("%king%", arena.getKingName());

        var user = plugin.getUserManager().getUser(player);

        line = line.replace("%score%", Integer.toString(user.getStat(StatsStorage.StatisticType.SCORE)));
        line = line.replace("%tours_played%", Integer.toString(user.getStat(StatsStorage.StatisticType.TOURS_PLAYED)));
        line = chatManager.formatMessage(line, player);
        return line;
    }
}
