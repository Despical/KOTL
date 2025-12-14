/*
 * KOTL - Don't let others climb to top of the ladders!
 * Copyright (C) 2025  Berke Akçen
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

package dev.despical.kotl.handlers;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import dev.despical.kotl.KOTL;
import dev.despical.kotl.api.StatisticType;
import dev.despical.kotl.arena.Arena;
import dev.despical.kotl.user.User;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * @author Despical
 * <p>
 * Created at 22.06.2020
 */
public class PlaceholderManager extends PlaceholderExpansion {

    private final KOTL plugin;

    public PlaceholderManager(KOTL plugin) {
        this.plugin = plugin;

        register();
    }

    @Override
    public boolean persist() {
        return true;
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

    public String onPlaceholderRequest(Player player, @NotNull String id) {
        if (player == null) return null;

        User user = plugin.getUserManager().getUser(player);

        return switch (id.toLowerCase()) {
            case "score" -> Integer.toString(user.getStat(StatisticType.SCORE));
            case "tours_played" -> Integer.toString(user.getStat(StatisticType.TOURS_PLAYED));
            case "arena" -> Optional.ofNullable(user.getArena())
                .map(Arena::getId)
                .orElseGet(() -> plugin.getChatManager().message("Placeholders.Player-Not-Playing"));
            default -> handleArenaPlaceholderRequest(id);
        };
    }

    private String handleArenaPlaceholderRequest(String id) {
        String[] data = id.split(":");
        Arena arena = plugin.getArenaRegistry().getArena(data[0]);

        if (arena == null) {
            return "No arena with this ID";
        }

        return switch (data[1].toLowerCase()) {
            case "players" -> Integer.toString(arena.getPlayers().size());
            case "king" -> arena.getKingName();
            default -> null;
        };
    }
}
