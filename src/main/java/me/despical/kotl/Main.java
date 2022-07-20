/*
 * KOTL - Don't let others to climb top of the ladders!
 * Copyright (C) 2022 Despical
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
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package me.despical.kotl;

import me.despical.commons.compat.VersionResolver;
import me.despical.commons.database.MysqlDatabase;
import me.despical.commons.miscellaneous.AttributeUtils;
import me.despical.commons.scoreboard.ScoreboardLib;
import me.despical.commons.serializer.InventorySerializer;
import me.despical.commons.exception.ExceptionLogHandler;
import me.despical.commons.util.Collections;
import me.despical.commons.util.JavaVersion;
import me.despical.commons.util.LogUtils;
import me.despical.commons.util.UpdateChecker;
import me.despical.kotl.api.StatsStorage;
import me.despical.kotl.arena.Arena;
import me.despical.kotl.arena.ArenaEvents;
import me.despical.kotl.arena.ArenaRegistry;
import me.despical.kotl.command.CommandHandler;
import me.despical.kotl.event.ChatEvents;
import me.despical.kotl.event.Events;
import me.despical.kotl.handler.ChatManager;
import me.despical.kotl.handler.PlaceholderManager;
import me.despical.kotl.handler.language.LanguageManager;
import me.despical.kotl.handler.rewards.RewardsFactory;
import me.despical.kotl.user.User;
import me.despical.kotl.user.UserManager;
import me.despical.kotl.user.data.MysqlManager;
import me.despical.kotl.util.*;
import org.bstats.bukkit.Metrics;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

/**
 * @author Despical
 * <p>
 * Created at 20.06.2020
 */
public class Main extends JavaPlugin {

	private boolean forceDisable;

	private ExceptionLogHandler exceptionLogHandler;
	private ConfigPreferences configPreferences;
	private MysqlDatabase database;
	private UserManager userManager;
	private CommandHandler commandHandler;
	private CuboidSelector cuboidSelector;
	private ChatManager chatManager;
	private RewardsFactory rewardsFactory;
	private LanguageManager languageManager;

	@Override
	public void onEnable() {
		if ((forceDisable = !validateIfPluginShouldStart())) {
			getServer().getPluginManager().disablePlugin(this);
			return;
		}

		if (getConfig().getBoolean("Debug-Messages")) {
			LogUtils.setLoggerName("KOTL");
			LogUtils.enableLogging();
			LogUtils.log("Initialization started!");
		}

		exceptionLogHandler = new ExceptionLogHandler(this);
		exceptionLogHandler.setMainPackage("me.despical");
		exceptionLogHandler.addBlacklistedClass("me.despical.kotl.user.data.MysqlManager", "me.despical.commons.database.MysqlDatabase");
		exceptionLogHandler.setRecordMessage("[KOTL] We have found a bug in the code. Contact us at our official Discord server (link: https://discord.gg/rVkaGmyszE) with the following error given above!");

		configPreferences = new ConfigPreferences(this);

		long start = System.currentTimeMillis();

		setupFiles();
		initializeClasses();
		checkUpdate();

		LogUtils.sendConsoleMessage("[KOTL] &aInitialization finished. Join our Discord server if you need any help. (https://discord.gg/rVkaGmyszE)");
		LogUtils.log("Initialization finished took {0} ms.", System.currentTimeMillis() - start);
	}
	
	private boolean validateIfPluginShouldStart() {
		if (!VersionResolver.isCurrentBetween(VersionResolver.ServerVersion.v1_8_R1, VersionResolver.ServerVersion.v1_19_R1)) {
			LogUtils.sendConsoleMessage("&cYour server version is not supported by King of the Ladder!");
			LogUtils.sendConsoleMessage("&cSadly, we must shut off. Maybe you consider changing your server version?");
			return false;
		}

		if (JavaVersion.getCurrentVersion().isAt(JavaVersion.JAVA_8)) {
			LogUtils.sendConsoleMessage("&cThis plugin won't support Java 8 in future updates.");
			LogUtils.sendConsoleMessage("&cSo, maybe consider to update your version, right?");
		}

		try {
			Class.forName("org.spigotmc.SpigotConfig");
		} catch (Exception e) {
			LogUtils.sendConsoleMessage("&cYour server software is not supported by King of the Ladder!");
			LogUtils.sendConsoleMessage("&cWe support only Spigot and Spigot forks only! Shutting off...");
			return false;
		}

		return true;
	}
	
	@Override
	public void onDisable() {
		if (forceDisable) return;

		LogUtils.log("System disable initialized.");
		long start = System.currentTimeMillis();
		
		getServer().getLogger().removeHandler(exceptionLogHandler);
		saveAllUserStatistics();

		if (database != null) {
			database.shutdownConnPool();
		}

		for (Arena arena : ArenaRegistry.getArenas()) {
			for (Player player : arena.getPlayers()) {
				if (getConfigPreferences().getOption(ConfigPreferences.Option.INVENTORY_MANAGER_ENABLED)) {
					InventorySerializer.loadInventory(this, player);
				} else {
					player.getInventory().clear();
					player.getInventory().setArmorContents(null);
					player.getActivePotionEffects().forEach(effect -> player.removePotionEffect(effect.getType()));

					AttributeUtils.healPlayer(player);
				}

				arena.teleportToEndLocation(player);
				arena.doBarAction(Arena.BarAction.REMOVE, player);
				arena.getScoreboardManager().removeScoreboard(player);

				AttributeUtils.resetAttackCooldown(player);
			}

			arena.deleteHologram();
		}

		LogUtils.log("System disable finished took {0} ms.", System.currentTimeMillis() - start);
		LogUtils.disableLogging();
	}
	
	private void initializeClasses() {
		ScoreboardLib.setPluginInstance(this);

		if (configPreferences.getOption(ConfigPreferences.Option.DATABASE_ENABLED)) {
			database = new MysqlDatabase(this, "mysql");
		}

		chatManager = new ChatManager(this);
		languageManager = new LanguageManager(this);
		userManager = new UserManager(this);
		commandHandler = new CommandHandler(this);
		cuboidSelector = new CuboidSelector(this);
		rewardsFactory = new RewardsFactory(this);

		ArenaRegistry.registerArenas();

		new ChatEvents(this);
		new Events(this);
		new ArenaEvents(this);

		registerSoftDependencies();
	}
	
	private void registerSoftDependencies() {
		LogUtils.log("Hooking into soft dependencies.");

		startPluginMetrics();

		if (chatManager.isPapiEnabled()) {
			LogUtils.log("Hooking into PlaceholderAPI.");
			new PlaceholderManager(this);
		}

		LogUtils.log("Hooked into soft dependencies.");
	}
	
	private void startPluginMetrics() {
		Metrics metrics = new Metrics(this, 7938);

		if (!metrics.isEnabled()) return;

		metrics.addCustomChart(new Metrics.SimplePie("locale_used", () -> languageManager.getPluginLocale().prefix));
		metrics.addCustomChart(new Metrics.SimplePie("database_enabled", () -> configPreferences.getOption(ConfigPreferences.Option.DATABASE_ENABLED) ? "Enabled" : "Disabled"));
		metrics.addCustomChart(new Metrics.SimplePie("update_notifier", () -> configPreferences.getOption(ConfigPreferences.Option.UPDATE_NOTIFIER_ENABLED) ? "Enabled" : "Disabled"));
	}
	
	private void checkUpdate() {
		if (!configPreferences.getOption(ConfigPreferences.Option.UPDATE_NOTIFIER_ENABLED)) return;

		UpdateChecker.init(this, 80686).requestUpdateCheck().whenComplete((result, exception) -> {
			if (result.requiresUpdate()) {
				LogUtils.sendConsoleMessage("[KOTL] Found a new version available: v" + result.getNewestVersion());
				LogUtils.sendConsoleMessage("[KOTL] Download it on SpigotMC:");
				LogUtils.sendConsoleMessage("[KOTL] spigotmc.org/resources/king-of-the-ladder.80686/");
			}
		});
	}
	
	private void setupFiles() {
		Collections.streamOf("arenas", "rewards", "stats", "mysql", "messages").filter(name -> !new File(getDataFolder(),name + ".yml").exists()).forEach(name -> saveResource(name + ".yml", false));
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

	public UserManager getUserManager() {
		return userManager;
	}

	private void saveAllUserStatistics() {
		for (Player player : getServer().getOnlinePlayers()) {
			final User user = userManager.getUser(player);

			if (userManager.getDatabase() instanceof MysqlManager) {
				StringBuilder update = new StringBuilder(" SET ");

				for (StatsStorage.StatisticType stat : StatsStorage.StatisticType.values()) {
					if (!stat.isPersistent()) continue;

					int val = user.getStat(stat);

					if (update.toString().equalsIgnoreCase(" SET ")) {
						update.append(stat.getName()).append("'='").append(val);
					}

					update.append(", ").append(stat.getName()).append("'='").append(val);
				}

				final String finalUpdate = update.toString();
				final MysqlManager database = ((MysqlManager) userManager.getDatabase());
				database.getDatabase().executeUpdate("UPDATE " + database.getTableName() + finalUpdate + " WHERE UUID='" + user.getUniqueId().toString() + "';");
				continue;
			}

			userManager.getDatabase().saveAllStatistic(user);
		}
	}
}