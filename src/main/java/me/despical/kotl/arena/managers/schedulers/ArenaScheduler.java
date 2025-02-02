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

package me.despical.kotl.arena.managers.schedulers;

import me.despical.kotl.KOTL;
import me.despical.kotl.arena.Arena;
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
            var scheduler = plugin.getServer().getScheduler();

            if (options.async()) {
                scheduler.runTaskTimerAsynchronously(plugin, this::run, 1L, options.interval());
            } else {
                scheduler.runTaskTimer(plugin, this::run, 1L, options.interval());
            }
        }

        private void run() {
            for (Arena arena : plugin.getArenaRegistry().getArenas()) {
                generalSearchForPlayers(arena);
            }
        }
    },

    PER_ARENA {
        @Override
        public void register(SchedulerOptions options) {
            var scheduler = plugin.getServer().getScheduler();

            for (Arena arena : plugin.getArenaRegistry().getArenas()) {
                if (options.async()) {
                    scheduler.runTaskTimerAsynchronously(plugin, () -> generalSearchForPlayers(arena), 1L, options.interval());
                } else {
                    scheduler.runTaskTimer(plugin, () -> generalSearchForPlayers(arena), 1L, options.interval());
                }
            }
        }
    },

    EVENT {
        @Override
        public void register(SchedulerOptions options) {
            plugin.getServer().getPluginManager().registerEvents(new Listener() {

                @EventHandler
                public void onEnterAndLeaveGameArea(PlayerMoveEvent event) {
                    Player player = event.getPlayer();
                    Arena arena = isInArea(player);
                    Arena playerArena = plugin.getArenaRegistry().getArena(player);
                    boolean isInArena = playerArena != null;

                    if (!isInArena && arena != null) {
                        arena.addPlayer(player);
                    }

                    if (isInArena && arena == null) {
                        playerArena.removePlayer(player);
                    }
                }

                private Arena isInArea(final Player player) {
                    for (Arena arena : plugin.getArenaRegistry().getArenas()) {
                        Arena target = arena.isInArea(player);

                        if (target != null) {
                            return target;
                        }
                    }

                    return null;
                }
            }, plugin);
        }
    };

    protected final KOTL plugin = JavaPlugin.getPlugin(KOTL.class);

    public abstract void register(SchedulerOptions options);

    protected void generalSearchForPlayers(Arena arena) {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            Arena target = arena.isInArea(player);
            Arena current = plugin.getArenaRegistry().getArena(player);

            if (current == null && target != null && !arena.getPlayers().contains(player)) {
                arena.addPlayer(player);
                continue;
            }

            if (arena.equals(current) && target == null && arena.getPlayers().contains(player)) {
                current.removePlayer(player);
            }
        }
    }
}
