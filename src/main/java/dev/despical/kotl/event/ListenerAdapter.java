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

package dev.despical.kotl.event;

import dev.despical.fileitems.ItemManager;
import dev.despical.kotl.KOTL;
import dev.despical.kotl.arena.ArenaRegistry;
import dev.despical.kotl.arena.managers.ArenaManager;
import dev.despical.kotl.chat.ChatManager;
import dev.despical.kotl.game.GameManager;
import dev.despical.kotl.option.ConfigOptions;
import dev.despical.kotl.user.UserManager;
import org.bukkit.event.Listener;

/**
 * @author Despical
 * <p>
 * Created at 12.07.2022
 */
public abstract class ListenerAdapter implements Listener {

    protected static final KOTL plugin = KOTL.getInstance();

    protected final ConfigOptions options;
    protected final ArenaManager arenaManager;
    protected final ArenaRegistry arenaRegistry;
    protected final UserManager userManager;
    protected final ItemManager itemManager;
    protected final ChatManager chatManager;
    protected final GameManager gameManager;

    public ListenerAdapter() {
        this.options = plugin.getOptions();
        this.arenaManager = plugin.getArenaManager();
        this.arenaRegistry = plugin.getArenaRegistry();
        this.userManager = plugin.getUserManager();
        this.itemManager = plugin.getItemManager();
        this.chatManager = plugin.getChatManager();
        this.gameManager = plugin.getGameManager();

        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }
}
