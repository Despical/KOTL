/*
 * KOTL - Don't let others to climb top of the ladders!
 * Copyright (C) 2023 Despical
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package me.despical.kotl.event;

import me.despical.kotl.ConfigPreferences;
import me.despical.kotl.Main;
import me.despical.kotl.arena.Arena;
import me.despical.kotl.arena.ArenaRegistry;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.regex.Pattern;

/**
 * @author Despical
 * <p>
 * Created at 22.06.2020
 */
public class ChatEvents extends ListenerAdapter {

	private final boolean disabledSeparateChat, chatFormatEnabled;

	public ChatEvents(Main plugin) {
		super (plugin);
		this.disabledSeparateChat = plugin.getConfigPreferences().getOption(ConfigPreferences.Option.DISABLE_SEPARATE_CHAT);
		this.chatFormatEnabled = plugin.getConfigPreferences().getOption(ConfigPreferences.Option.CHAT_FORMAT_ENABLED);
	}

	@EventHandler(ignoreCancelled = true)
	public void onChatInGame(AsyncPlayerChatEvent event) {
		Player player = event.getPlayer();
		Arena arena = ArenaRegistry.getArena(player);

		if (arena == null) {
			if (!disabledSeparateChat) {
				ArenaRegistry.getArenas().forEach(loopArena -> loopArena.getPlayers().forEach(p -> event.getRecipients().remove(p)));
			}

			return;
		}

		if (chatFormatEnabled) {
			String message = formatChatPlaceholders(chatManager.message("in_game.chat_format"), player, event.getMessage().replaceAll(Pattern.quote("[$\\]"), ""));

			if (!disabledSeparateChat) {
				event.setCancelled(true);

				for (Player p : arena.getPlayers()) {
					p.sendMessage(message);
				}

				plugin.getServer().getConsoleSender().sendMessage(message);
			} else {
				event.setMessage(message);
			}
		}
	}

	private String formatChatPlaceholders(String message, Player player, String saidMessage) {
		String formatted = message;

		formatted = StringUtils.replace(formatted, "%player%", player.getName());
		formatted = StringUtils.replace(formatted, "%message%", ChatColor.stripColor(saidMessage));
		formatted = chatManager.formatMessage(formatted, player);
		return chatManager.coloredRawMessage(formatted);
	}
}