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

import dev.despical.commandframework.Message;
import dev.despical.commons.configuration.ConfigUtils;
import dev.despical.commons.string.StringUtils;
import dev.despical.commons.util.Strings;
import me.clip.placeholderapi.PlaceholderAPI;
import dev.despical.kotl.KOTL;
import dev.despical.kotl.arena.Arena;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.stream.Stream;

/**
 * @author Despical
 * <p>
 * Created at 22.06.2020
 */
public class ChatManager {

    private final KOTL plugin;
    private final boolean papiEnabled;
    private String prefix;
    private FileConfiguration config;

    public ChatManager(KOTL plugin) {
        this.plugin = plugin;
        this.papiEnabled = plugin.getServer().getPluginManager().isPluginEnabled("PlaceholderAPI");
        this.config = ConfigUtils.getConfig(plugin, "messages");
        this.prefix = message("in_game.plugin_prefix");
    }

    public String coloredRawMessage(String message) {
        return Strings.format(message);
    }

    public String prefixedRawMessage(String message) {
        return prefix + coloredRawMessage(message);
    }

    public String message(String path) {
        path = StringUtils.capitalize(path.replace('_', '-'), '-', '.');
        return coloredRawMessage(config.getString(path));
    }

    public String prefixedMessage(String path) {
        return prefix + message(path);
    }

    public boolean isPapiEnabled() {
        return papiEnabled;
    }

    public String message(String path, Player player) {
        String message = message(path);
        message = message.replace("%player%", player.getName());
        message = formatMessage(message, player);

        return message;
    }

    public String formatMessage(String message, Player player) {
        if (papiEnabled) {
            message = PlaceholderAPI.setPlaceholders(player, message);
        }

        return Strings.format(message);
    }

    private String formatMessage(Arena arena, String message, Player player) {
        message = message.replace("%player%", player.getName());
        message = formatMessage(message, arena);
        message = formatMessage(message, player);

        return coloredRawMessage(message);
    }

    private String formatMessage(String message, Arena arena) {
        message = message.replace("%arena%", arena.getId());
        message = message.replace("%players%", Integer.toString(arena.getPlayers().size()));
        message = message.replace("%king%", arena.getKingName());
        return message;
    }

    public List<String> getStringList(String path) {
        path = StringUtils.capitalize(path.replace('_', '-'), '-', '.');
        return config.getStringList(path);
    }

    public void broadcastAction(Arena arena, Player player, ActionType action) {
        String path = switch (action) {
            case JOIN -> "join";
            case LEAVE -> "leave";
            default -> "new_king";
        };

        arena.broadcastMessage(prefix + formatMessage(arena, message("in_game." + path), player));
    }

    public String getPrefix() {
        return prefix;
    }

    public void reload() {
        config = ConfigUtils.getConfig(plugin, "messages");
        prefix = message("in_game.plugin_prefix");

        Stream.of(Message.SHORT_ARG_SIZE, Message.LONG_ARG_SIZE).forEach(message -> message.setMessage((command, arguments) -> {
            arguments.sendMessage(prefixedMessage("commands.correct_usage").replace("%usage%", command.usage()));
            return true;
        }));
    }

    public enum ActionType {
        JOIN, LEAVE, NEW_KING
    }
}
