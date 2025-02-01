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

package me.despical.kotl.arena.managers;

import me.despical.commons.number.NumberUtils;
import me.despical.kotl.KOTL;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

/**
 * @author Despical
 * <p>
 * Created at 30.09.2022
 */
public class BossBarManager extends BukkitRunnable {

    private final KOTL plugin;
    private final BossBar bossBar;
    private final List<String> messages;

    private int queue = 0;

    public BossBarManager(KOTL plugin) {
        this.plugin = plugin;
        this.bossBar = plugin.getServer().createBossBar("", BarColor.valueOf(plugin.getChatManager().message("boss_bar.color")), BarStyle.valueOf(plugin.getChatManager().message("boss_bar.style")));
        this.messages = plugin.getChatManager().getStringList("boss_bar.messages");

        this.runTaskTimer(plugin, 20, NumberUtils.getInt(plugin.getChatManager().message("boss_bar.interval"), 300));
    }

    public void addPlayer(Player player) {
        this.bossBar.addPlayer(player);
    }

    public void removePlayer(Player player) {
        this.bossBar.removePlayer(player);
    }

    @Override
    public void run() {
        if (queue + 1 > messages.size()) queue = 0;

        this.bossBar.setTitle(plugin.getChatManager().coloredRawMessage(messages.get(queue++)));
        this.bossBar.setVisible(!this.bossBar.getTitle().isEmpty());
    }
}
