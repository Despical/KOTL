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

		boolean showOnRejoin = plugin.getOption(ConfigPreferences.Option.SHOW_COOLDOWN_ON_REJOIN);
		boolean count = plugin.getOption(ConfigPreferences.Option.COUNT_COOLDOWN_OUTSIDE);
		boolean separateCooldowns = plugin.getOption(ConfigPreferences.Option.SEPARATE_COOLDOWNS);
		String arenaId = user.getArena().getId();
		String cooldownName = (separateCooldowns ? arenaId : "") + "king";
		String localCooldownName = (separateCooldowns ? arenaId : "") + "local_cooldown";

		if (!count) {
			user.set(localCooldownName, true);
		}

		new BukkitRunnable() {

			int ticks = 0;

			@Override
			public void run() {
				Player player = user.getPlayer();

				if (user.getStat(StatsStorage.StatisticType.LOCAL_RESET_COOLDOWN) == 1) {
					cancel();

					plugin.getCooldownManager().setCooldown(user, cooldownName, 0);

					user.set(localCooldownName, false);
					user.setStat(StatsStorage.StatisticType.LOCAL_RESET_COOLDOWN, 0);
					return;
				}

				final var arena = user.getArena();

				if (separateCooldowns && arena != null && !arenaId.equals(arena.getId())) {
					return;
				}

				if (!count) {
					plugin.getCooldownManager().setCooldown(user, cooldownName, seconds - Math.ceil(ticks / 20D));
				} else if (ticks >= 20 * seconds) {
					cancel();
				}

				if (arena == null || !arena.getPlayers().contains(player)) {
					if (!showOnRejoin) {
						cancel();

						user.set(localCooldownName, false);
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

					user.set(localCooldownName, false);
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