package me.despical.kotl.user.data;

import org.bukkit.configuration.file.FileConfiguration;

import me.despical.commonsbox.configuration.ConfigUtils;
import me.despical.kotl.Main;
import me.despical.kotl.api.StatsStorage;
import me.despical.kotl.user.User;

/**
 * @author Despical
 * <p>
 * Created at 20.06.2020
 */
public class FileStats implements UserDatabase {

	private Main plugin;
	private FileConfiguration config;

	public FileStats(Main plugin) {
		this.plugin = plugin;
		config = ConfigUtils.getConfig(plugin, "stats");
	}

	@Override
	public void saveStatistic(User user, StatsStorage.StatisticType stat) {
		config.set(user.getPlayer().getUniqueId().toString() + "." + stat.getName(), user.getStat(stat));
		ConfigUtils.saveConfig(plugin, config, "stats");
	}

	@Override
	public void loadStatistics(User user) {
		for (StatsStorage.StatisticType stat : StatsStorage.StatisticType.values()) {
			user.setStat(stat, config.getInt(user.getPlayer().getUniqueId().toString() + "." + stat.getName(), 0));
		}
	}
}