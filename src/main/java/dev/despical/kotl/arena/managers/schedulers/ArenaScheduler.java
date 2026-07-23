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

package dev.despical.kotl.arena.managers.schedulers;

import dev.despical.kotl.KOTL;
import dev.despical.kotl.arena.Arena;
import dev.despical.kotl.user.User;
import dev.despical.kotl.util.Schedulers;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * @author Despical
 * <p>
 * Created at 14.01.2024
 */
public enum ArenaScheduler {

    GENERAL {
        @Override
        public void register(SchedulerOptions options) {
            Schedulers.runTaskTimer(this::run, 1L, options.interval());
        }

        private void run() {
            for (Player player : Bukkit.getOnlinePlayers()) {
                Arena currentArena = plugin.getArenaRegistry().getArena(player);
                Arena targetArena = findTargetArena(player);

                handleArenaTransition(player, currentArena, targetArena);
            }
        }
    },

    EVENT {
        @Override
        public void register(SchedulerOptions options) {
            Bukkit.getPluginManager().registerEvents(new Listener() {

                @EventHandler
                public void onMove(PlayerMoveEvent event) {
                    Location from = event.getFrom(), to = event.getTo();

                    if (from.getBlockX() == to.getBlockX() &&
                        from.getBlockY() == to.getBlockY() &&
                        from.getBlockZ() == to.getBlockZ()) {
                        return;
                    }

                    Player player = event.getPlayer();
                    Arena currentArena = plugin.getArenaRegistry().getArena(player);
                    Arena targetArena = findTargetArena(player);

                    handleArenaTransition(player, currentArena, targetArena);
                }
            }, plugin);
        }
    };

    protected final KOTL plugin = JavaPlugin.getPlugin(KOTL.class);

    public abstract void register(SchedulerOptions options);

    protected void handleArenaTransition(Player player, Arena current, Arena target) {
        User user = plugin.getUserManager().getUser(player);

        if (current == null && target != null && !target.getGame().getPlayers().contains(player)) {
            plugin.getArenaManager().joinAttempt(user, target);
            return;
        }

        if (current != null && target == null && current.getGame().getPlayers().contains(player)) {
            plugin.getArenaManager().leaveAttempt(user);
            return;
        }

        if (current != null && target != null && !current.equals(target)) {
            plugin.getArenaManager().leaveAttempt(user);
            plugin.getArenaManager().joinAttempt(user, target);
        }
    }

    protected Arena findTargetArena(Player player) {
        return plugin.getArenaRegistry().findTargetArena(player);
    }
}
