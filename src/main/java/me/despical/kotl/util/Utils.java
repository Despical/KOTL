package me.despical.kotl.util;

import me.despical.kotl.Main;
import me.despical.kotl.user.User;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * @author Despical
 * <p>
 * Created at 18.04.2024
 */
public class Utils {

	private static final Main plugin = JavaPlugin.getPlugin(Main.class);

	private Utils() {
	}

	public static void applyActionBarCooldown(User user, int seconds) {
		if (seconds == 0 || user.getCooldown("king") > 0) return;

		Player player = user.getPlayer();

		new BukkitRunnable() {
			int ticks = 0;

			@Override
			public void run() {
				final var arena = user.getArena();

				if (arena == null || !arena.getPlayers().contains(player)) {
					cancel();
					return;
				}

				var progress = getProgressBar(ticks, seconds * 20);
				ActionBar.sendActionBar(player, plugin.getChatManager().message("In-Game.Cooldown-Format", player).replace("%progress%", progress).replace("%time%", Double.toString((double) ((seconds * 20) - ticks) / 20)));

				if (ticks >= seconds * 20) {
					cancel();
					return;
				}

				ticks += 2;
			}
		}.runTaskTimer(plugin, 0, 2);
	}

	private static String getProgressBar(int current, int max) {
		float percent = (float) current / max;
		int progressBars = (int) (10 * percent), leftOver = (10 - progressBars);

		return "§a" +
			"■".repeat(Math.max(0, progressBars)) +
			"§c" +
			"■".repeat(Math.max(0, leftOver));
	}
}