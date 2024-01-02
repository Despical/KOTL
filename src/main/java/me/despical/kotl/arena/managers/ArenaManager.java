package me.despical.kotl.arena.managers;

import me.despical.kotl.Main;
import me.despical.kotl.arena.Arena;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class ArenaManager {

	private final boolean async;
	private final int schedulerType, interval;
	private final Main plugin;

	public ArenaManager(Main plugin) {
		this.plugin = plugin;
		this.interval = plugin.getConfig().getInt("Arena-Schedulers.Interval");
		this.async = plugin.getConfig().getBoolean("Arena-Schedulers.Async");

		switch (schedulerType = plugin.getConfig().getInt("Arena-Schedulers.Type")) {
			case 1 -> createGeneralScheduler();
			case 2 -> plugin.getArenaRegistry().getArenas().forEach(this::createSchedulerPerArena);
			default -> registerDefaultEvent();
		}
	}

	private void createGeneralScheduler() {
		var scheduler = plugin.getServer().getScheduler();

		if (async) {
			scheduler.scheduleAsyncRepeatingTask(plugin, this::searchForPlayers0, 1L, interval);
		} else {
			scheduler.scheduleSyncRepeatingTask(plugin, this::searchForPlayers0, 1L, interval);
		}
	}

	public void createSchedulerPerArena(Arena arena) {
		if (schedulerType != 2) return;

		var scheduler = plugin.getServer().getScheduler();

		if (async) {
			scheduler.scheduleAsyncRepeatingTask(plugin, () -> searchForPlayers1(arena), 1L, interval);
		} else {
			scheduler.scheduleSyncRepeatingTask(plugin, () -> searchForPlayers1(arena), 1L, interval);
		}
	}

	private void searchForPlayers0() {
		// Less operation in small outer loop and big inner loops
		for (var arena : plugin.getArenaRegistry().getArenas()) {
			searchForPlayers1(arena);
		}
	}

	private void searchForPlayers1(Arena arena) {
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

	private void registerDefaultEvent() {
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
}