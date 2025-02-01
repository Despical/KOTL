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

package me.despical.kotl.events;

import me.despical.kotl.KOTL;
import me.despical.kotl.handlers.ChatManager;
import org.bukkit.event.Listener;

/**
 * @author Despical
 * <p>
 * Created at 12.07.2022
 */
public sealed class EventListener implements Listener permits Events, ArenaEvents {

    protected final KOTL plugin;
    protected final ChatManager chatManager;

    public EventListener(KOTL plugin) {
        this.plugin = plugin;
        this.chatManager = plugin.getChatManager();
        this.plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }
}
