package me.despical.kotl.user;

import me.despical.kotl.ConfigPreferences;
import me.despical.kotl.Main;
import me.despical.kotl.api.StatsStorage;
import me.despical.kotl.user.data.FileStats;
import me.despical.kotl.user.data.MysqlManager;
import me.despical.kotl.user.data.UserDatabase;
import me.despical.kotl.utils.Debugger;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Despical
 * <p>
 * Created at 20.06.2020
 */
public class UserManager {

	private final UserDatabase database;
	private final List<User> users = new ArrayList<>();

	public UserManager(Main plugin) {
		if (plugin.getConfigPreferences().getOption(ConfigPreferences.Option.DATABASE_ENABLED)) {
			database = new MysqlManager(plugin);

			Debugger.debug("MySQL Stats enabled");
		} else {
			database = new FileStats(plugin);

			Debugger.debug("File Stats enabled");
		}

		loadStatsForPlayersOnline();
	}

	private void loadStatsForPlayersOnline() {
		Bukkit.getServer().getOnlinePlayers().stream().map(this::getUser).forEach(this::loadStatistics);
	}

	public User getUser(Player player) {
		for (User user : users) {
			if (user.getPlayer().equals(player)) {
				return user;
			}
		}

		Debugger.debug("Registering new user {0} ({1})", player.getUniqueId(), player.getName());

		User user = new User(player);
		users.add(user);
		return user;
	}

	public void saveStatistic(User user, StatsStorage.StatisticType stat) {
		if (!stat.isPersistent()) {
			return;
		}

		database.saveStatistic(user, stat);
	}

	public void loadStatistics(User user) {
		database.loadStatistics(user);
	}

	public void removeUser(User user) {
		users.remove(user);
	}

	public UserDatabase getDatabase() {
		return database;
	}
}