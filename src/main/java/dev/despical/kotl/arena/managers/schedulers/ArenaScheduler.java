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

package dev.despical.kotl.arena.managers.schedulers;

import dev.despical.kotl.KOTL;
import dev.despical.kotl.arena.Arena;
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
            Bukkit.getScheduler().runTaskTimer(plugin, this::run, 1L, options.interval());
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
        if (current == null && target != null && !target.getPlayers().contains(player)) {
            target.addPlayer(player);
            return;
        }

        if (current != null && target == null && current.getPlayers().contains(player)) {
            current.removePlayer(player);
            return;
        }

        if (current != null && target != null && !current.equals(target)) {
            current.removePlayer(player);
            target.addPlayer(player);
        }
    }

    protected Arena findTargetArena(Player player) {
        for (Arena arena : plugin.getArenaRegistry().getArenas()) {
            if (arena.isInArea(player) != null) {
                return arena;
            }
        }

        return null;
    }
}
