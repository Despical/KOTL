package me.despical.kotl.util;

import me.despical.kotl.ConfigPreferences;
import me.despical.kotl.Main;
import me.despical.kotl.api.StatsStorage;
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
		if (seconds == 0) return;

		Player player = user.getPlayer();
		boolean showOnRejoin = plugin.getOption(ConfigPreferences.Option.SHOW_COOLDOWN_ON_REJOIN);
		boolean count = plugin.getOption(ConfigPreferences.Option.COUNT_COOLDOWN_OUTSIDE);

		if (!count) {
			user.setStat(StatsStorage.StatisticType.LOCAL_COOLDOWN, 1);
		}

		new BukkitRunnable() {

			int ticks = 0;

			@Override
			public void run() {
				if (user.getStat(StatsStorage.StatisticType.LOCAL_RESET_COOLDOWN) == 1) {
					cancel();

					user.setCooldown("king", 0);
					user.setStat(StatsStorage.StatisticType.LOCAL_RESET_COOLDOWN, 0);
					user.setStat(StatsStorage.StatisticType.LOCAL_COOLDOWN, 0);
					return;
				}

				final var arena = user.getArena();

				if (!count) {
					user.setCooldown("king", seconds - Math.ceil(ticks / 20D));
				} else if (ticks >= 20 * seconds) {
					cancel();
				}

				if (arena == null || !arena.getPlayers().contains(player)) {
					if (!showOnRejoin) {
						cancel();

						user.setStat(StatsStorage.StatisticType.LOCAL_COOLDOWN, 0);
					}

					if (count) {
						ticks += 2;
					}

					return;
				}

				var progress = getProgressBar(ticks, seconds * 20);
				ActionBar.sendActionBar(player, plugin.getChatManager().message("In-Game.Cooldown-Format", player).replace("%progress%", progress).replace("%time%", Double.toString((double) ((seconds * 20) - ticks) / 20)));

				if (ticks >= seconds * 20) {
					cancel();

					user.setStat(StatsStorage.StatisticType.LOCAL_COOLDOWN, 0);
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