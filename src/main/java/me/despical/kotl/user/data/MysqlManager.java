package me.despical.kotl.user.data;

import me.despical.commonsbox.configuration.ConfigUtils;
import me.despical.commonsbox.database.MysqlDatabase;
import me.despical.kotl.Main;
import me.despical.kotl.api.StatsStorage;
import me.despical.kotl.user.User;
import me.despical.kotl.utils.Debugger;
import me.despical.kotl.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;

/**
 * @author Despical
 * <p>
 * Created at 20.06.2020
 */
public class MysqlManager implements UserDatabase {

	private final Main plugin;
	private final MysqlDatabase database;

	public MysqlManager(Main plugin) {
		this.plugin = plugin;
		database = plugin.getMysqlDatabase();
		Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
			try (Connection connection = database.getConnection()) {
				Statement statement = connection.createStatement();
				statement.executeUpdate("CREATE TABLE IF NOT EXISTS `" + getTableName() + "` (\n"
					+ "  `UUID` char(36) NOT NULL PRIMARY KEY,\n"
					+ "  `name` varchar(32) NOT NULL,\n"
					+ "  `score` int(11) NOT NULL DEFAULT '0',\n"
					+ "  `toursplayed` int(11) NOT NULL DEFAULT '0',\n"
					+ ");");
			} catch (SQLException e) {
				e.printStackTrace();
				MessageUtils.errorOccurred();
				Bukkit.getConsoleSender().sendMessage("Cannot save contents to MySQL database!");
				Bukkit.getConsoleSender().sendMessage("Check configuration of mysql.yml file or disable mysql option in config.yml");
			}
		});
	}

	@Override
	public void saveStatistic(User user, StatsStorage.StatisticType stat) {
		Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
			database.executeUpdate("UPDATE " + getTableName() + " SET " + stat.getName() + "=" + user.getStat(stat) + " WHERE UUID='" + user.getPlayer().getUniqueId().toString() + "';");
			Debugger.debug(Level.INFO, "Executed MySQL: " + "UPDATE " + getTableName() + " SET " + stat.getName() + "=" + user.getStat(stat) + " WHERE UUID='" + user.getPlayer().getUniqueId().toString() + "';");
		});
	}

	@Override
	public void loadStatistics(User user) {
		Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
			String uuid = user.getPlayer().getUniqueId().toString();
			try (Connection connection = database.getConnection()) {
				Statement statement = connection.createStatement();
				ResultSet rs = statement.executeQuery("SELECT * from " + getTableName() + " WHERE UUID='" + uuid + "';");
				if (rs.next()) {
					Debugger.debug(Level.INFO, "MySQL Stats | Player {0} already exist. Getting Stats...", user.getPlayer().getName());
					for (StatsStorage.StatisticType stat : StatsStorage.StatisticType.values()) {
						if (stat.isPersistent())
							continue;
						int val = rs.getInt(stat.getName());
						user.setStat(stat, val);
					}
				} else {
					Debugger.debug(Level.INFO, "MySQL Stats | Player {0} does not exist. Creating new one...", user.getPlayer().getName());
					statement.executeUpdate("INSERT INTO " + getTableName() + " (UUID,name) VALUES ('" + uuid + "','" + user.getPlayer().getName() + "');");
					for (StatsStorage.StatisticType stat : StatsStorage.StatisticType.values()) {
						user.setStat(stat, 0);
					}
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		});
	}

	public String getTableName() {
		FileConfiguration config = ConfigUtils.getConfig(plugin, "mysql");
		return config.getString("table", "playerstats");
	}

	public MysqlDatabase getDatabase() {
		return database;
	}
}