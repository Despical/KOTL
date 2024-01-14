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

package me.despical.kotl.arena.managers.schedulers;

import me.despical.kotl.Main;
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
				scheduler.scheduleAsyncRepeatingTask(plugin, this::run, 1L, options.interval());
			} else {
				scheduler.scheduleSyncRepeatingTask(plugin, this::run, 1L, options.interval());
			}
		}

		private void run() {
			for (var arena : plugin.getArenaRegistry().getArenas()) {
				generalSearchForPlayers(arena);
			}
		}
	},

	PER_ARENA {

		@Override
		public void register(SchedulerOptions options) {
			var scheduler = plugin.getServer().getScheduler();

			for (final var arena : plugin.getArenaRegistry().getArenas()) {
				if (options.async()) {
					scheduler.scheduleAsyncRepeatingTask(plugin, () -> generalSearchForPlayers(arena), 1L, options.interval());
				} else {
					scheduler.scheduleSyncRepeatingTask(plugin, () -> generalSearchForPlayers(arena), 1L, options.interval());
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
					var player = event.getPlayer();
					var arena = isInArea(player);
					var playerArena = plugin.getArenaRegistry().getArena(player);
					var isInArena = playerArena != null;

					if (!isInArena && arena != null) {
						arena.addPlayer(player);
					}

					if (isInArena && arena == null) {
						playerArena.removePlayer(player);
					}
				}

				private Arena isInArea(final Player player) {
					for (var arena : plugin.getArenaRegistry().getArenas()) {
						final var target = arena.isInArea(player);

						if (target != null) return target;
					}

					return null;
				}
			}, plugin);
		}
	};

	final Main plugin = JavaPlugin.getPlugin(Main.class);

	public abstract void register(SchedulerOptions options);

	void generalSearchForPlayers(Arena arena) {
		for (var player : plugin.getServer().getOnlinePlayers()) {
			final var target = arena.isInArea(player);
			final var targetArena = plugin.getArenaRegistry().getArena(player);
			var isInArena = targetArena != null;

			if (!isInArena && target != null) {
				arena.addPlayer(player);
			}

			if (isInArena && target == null) {
				targetArena.removePlayer(player);
			}
		}
	}
}