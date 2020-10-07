/*
 * KOTL - Don't let others to climb top of the ladders!
 * Copyright (C) 2020 Despical
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package me.despical.kotl;

import me.despical.commonsbox.compat.VersionResolver;
import me.despical.commonsbox.configuration.ConfigUtils;
import me.despical.commonsbox.database.MysqlDatabase;
import me.despical.commonsbox.scoreboard.ScoreboardLib;
import me.despical.commonsbox.serializer.InventorySerializer;
import me.despical.kotl.api.StatsStorage;
import me.despical.kotl.arena.Arena;
import me.despical.kotl.arena.ArenaEvents;
import me.despical.kotl.arena.ArenaRegistry;
import me.despical.kotl.commands.CommandHandler;
import me.despical.kotl.events.ChatEvents;
import me.despical.kotl.events.Events;
import me.despical.kotl.events.JoinEvent;
import me.despical.kotl.events.QuitEvent;
import me.despical.kotl.handlers.ChatManager;
import me.despical.kotl.handlers.PlaceholderManager;
import me.despical.kotl.handlers.hologram.HologramManager;
import me.despical.kotl.handlers.rewards.RewardsFactory;
import me.despical.kotl.user.User;
import me.despical.kotl.user.UserManager;
import me.despical.kotl.user.data.MysqlManager;
import me.despical.kotl.utils.*;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.Arrays;

/**
 * @author Despical
 * <p>
 * Created at 20.06.2020
 */
public class Main extends JavaPlugin {

	private ExceptionLogHandler exceptionLogHandler;
	private boolean forceDisable = false;
	private ConfigPreferences configPreferences;
	private MysqlDatabase database;
	private UserManager userManager;
	private CommandHandler commandHandler;
	private CuboidSelector cuboidSelector;
	private ChatManager chatManager;
	private RewardsFactory rewardsFactory;
	private HologramManager hologramManager;

	@Override
	public void onEnable() {
		if (!validateIfPluginShouldStart()) {
			return;
		}

		exceptionLogHandler = new ExceptionLogHandler(this);
		saveDefaultConfig();

		Debugger.setEnabled(getDescription().getVersion().contains("d") || getConfig().getBoolean("Debug-Messages", false));
		Debugger.debug("Initialization start");

		long start = System.currentTimeMillis();
		
		configPreferences = new ConfigPreferences(this);
		setupFiles();
		initializeClasses();
		checkUpdate();

		Debugger.debug("Initialization finished took {0} ms", System.currentTimeMillis() - start);
	}
	
	private boolean validateIfPluginShouldStart() {
		if (VersionResolver.isCurrentLower(VersionResolver.ServerVersion.v1_8_R1)) {
			MessageUtils.thisVersionIsNotSupported();
			Debugger.sendConsoleMessage("&cYour server version is not supported by King of the Ladder!");
			Debugger.sendConsoleMessage("&cSadly, we must shut off. Maybe you consider changing your server version?");
			forceDisable = true;
			getServer().getPluginManager().disablePlugin(this);
			return false;
		}
		try {
			Class.forName("org.spigotmc.SpigotConfig");
		} catch (Exception e) {
			MessageUtils.thisVersionIsNotSupported();
			Debugger.sendConsoleMessage("&cYour server software is not supported by King of the Ladder!");
			Debugger.sendConsoleMessage("&cWe support only Spigot and Spigot forks only! Shutting off...");
			forceDisable = true;
			getServer().getPluginManager().disablePlugin(this);
			return false;
		}

		return true;
	}
	
	@Override
	public void onDisable() {
		if (forceDisable) {
			return;
		}

		Debugger.debug("System disable initialized");
		long start = System.currentTimeMillis();
		
		Bukkit.getLogger().removeHandler(exceptionLogHandler);
		saveAllUserStatistics();

		if (configPreferences.getOption(ConfigPreferences.Option.DATABASE_ENABLED)) {
			getMysqlDatabase().shutdownConnPool();
		}

		for (Arena arena : ArenaRegistry.getArenas()) {
			for (Player player : arena.getPlayers()) {
				if (getConfigPreferences().getOption(ConfigPreferences.Option.INVENTORY_MANAGER_ENABLED)) {
					InventorySerializer.loadInventory(this, player);
				} else {
					player.getInventory().clear();
					player.getInventory().setArmorContents(null);
				}

				player.teleport(arena.getEndLocation());
				arena.doBarAction(Arena.BarAction.REMOVE, player);
				arena.getScoreboardManager().removeScoreboard(getUserManager().getUser(player));
				if (!isBefore1_9_R1()) player.getAttribute(Attribute.GENERIC_ATTACK_SPEED).setBaseValue(4);
			}

			arena.getHologram().delete();
			arena.getPlayers().clear();
		}

		Debugger.debug("System disable finished took {0} ms", System.currentTimeMillis() - start);
	}
	
	private void initializeClasses() {
		ScoreboardLib.setPluginInstance(this);

		if (configPreferences.getOption(ConfigPreferences.Option.DATABASE_ENABLED)) {
			FileConfiguration config = ConfigUtils.getConfig(this, "mysql");
			database = new MysqlDatabase(config.getString("user"), config.getString("password"), config.getString("address"));
		}

		userManager = new UserManager(this);
		registerSoftDependenciesAndServices();
		commandHandler = new CommandHandler(this);
		chatManager = new ChatManager(this);
		cuboidSelector = new CuboidSelector(this);
		rewardsFactory = new RewardsFactory(this);
		hologramManager = new HologramManager();

		ArenaRegistry.registerArenas();

		new ChatEvents(this);
		new Events(this);
		new ArenaEvents(this);
		new JoinEvent(this);
		new QuitEvent(this);
	}
	
	private void registerSoftDependenciesAndServices() {
		Debugger.debug("Hooking into soft dependencies");
		long start = System.currentTimeMillis();

		startPluginMetrics();

		if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
			Debugger.debug("Hooking into PlaceholderAPI");
			new PlaceholderManager().register();
		}

		Debugger.debug("Hooked into soft dependencies took {0} ms", System.currentTimeMillis() - start);
	}
	
	private void startPluginMetrics() {
		Metrics metrics = new Metrics(this, 7938);

		if (!metrics.isEnabled()) {
			return;
		}

		metrics.addCustomChart(new Metrics.SimplePie("database_enabled", () -> String.valueOf(configPreferences.getOption(ConfigPreferences.Option.DATABASE_ENABLED))));
		metrics.addCustomChart(new Metrics.SimplePie("update_notifier", () -> {
			if (getConfig().getBoolean("Update-Notifier.Enabled", true)) {
				return getConfig().getBoolean("Update-Notifier.Notify-Beta-Versions", true) ? "Enabled with beta notifier" : "Enabled";
			}

			return getConfig().getBoolean("Update-Notifier.Notify-Beta-Versions", true) ? "Beta notifier only" : "Disabled";
		}));
	}
	
	private void checkUpdate() {
		if (!getConfig().getBoolean("Update-Notifier.Enabled", true)) {
			return;
		}

		UpdateChecker.init(this, 80686).requestUpdateCheck().whenComplete((result, exception) -> {
			if (!result.requiresUpdate()) {
				return;
			}

			if (result.getNewestVersion().contains("b")) {
				if (getConfig().getBoolean("Update-Notifier.Notify-Beta-Versions", true)) {
					Debugger.sendConsoleMessage("[KOTL] Found a new beta version available: v" + result.getNewestVersion());
					Debugger.sendConsoleMessage("[KOTL] Download it on SpigotMC:");
					Debugger.sendConsoleMessage("[KOTL] spigotmc.org/resources/king-of-the-ladder-1-8-1-16-3.80686/");
				}

				return;
			}

			MessageUtils.updateIsHere();
			Debugger.sendConsoleMessage("[KOTL] Found a new version available: v" + result.getNewestVersion());
			Debugger.sendConsoleMessage("[KOTL] Download it SpigotMC:");
			Debugger.sendConsoleMessage("[KOTL] spigotmc.org/resources/king-of-the-ladder-1-8-1-16-3.80686/");
		});
	}
	
	private void setupFiles() {
		for (String fileName : Arrays.asList("arenas", "stats", "mysql", "rewards")) {
			File file = new File(getDataFolder() + File.separator + fileName + ".yml");

			if (!file.exists()) {
				saveResource(fileName + ".yml", false);
			}
		}
	}

	public boolean isBefore1_9_R1() {
		return VersionResolver.isCurrentLower(VersionResolver.ServerVersion.v1_9_R1);
	}

	public ConfigPreferences getConfigPreferences() {
		return configPreferences;
	}
	
	public MysqlDatabase getMysqlDatabase() {
		return database;
	}
	
	public CommandHandler getCommandHandler() {
		return commandHandler;
	}
	
	public CuboidSelector getCuboidSelector() {
		return cuboidSelector;
	}
	
	public ChatManager getChatManager() {
		return chatManager;
	}
	
	public RewardsFactory getRewardsFactory() {
		return rewardsFactory;
	}

	public HologramManager getHologramManager() {
		return hologramManager;
	}

	public UserManager getUserManager() {
		return userManager;
	}

	private void saveAllUserStatistics() {
		for (Player player : getServer().getOnlinePlayers()) {
			User user = userManager.getUser(player);

			if (userManager.getDatabase() instanceof MysqlManager) {
				StringBuilder update = new StringBuilder(" SET ");

				for (StatsStorage.StatisticType stat : StatsStorage.StatisticType.values()) {
					if (!stat.isPersistent()) continue;
					if (update.toString().equalsIgnoreCase(" SET ")) {
						update.append(stat.getName()).append("=").append(user.getStat(stat));
					}

					update.append(", ").append(stat.getName()).append("=").append(user.getStat(stat));
				}

				String finalUpdate = update.toString();
				((MysqlManager) userManager.getDatabase()).getDatabase().executeUpdate("UPDATE " + ((MysqlManager) getUserManager().getDatabase()).getTableName() + finalUpdate + " WHERE UUID='" + user.getPlayer().getUniqueId().toString() + "';");
				continue;
			}

			Arrays.stream(StatsStorage.StatisticType.values()).forEach(stat -> userManager.getDatabase().saveStatistic(user, stat));
		}
	}
}