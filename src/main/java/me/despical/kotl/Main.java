/*
 * KOTL - Don't let others to climb top of the ladders!
 * Copyright (C) 2023 Despical
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

import me.despical.commandframework.CommandFramework;
import me.despical.commons.compat.VersionResolver;
import me.despical.commons.configuration.ConfigUtils;
import me.despical.commons.database.MysqlDatabase;
import me.despical.commons.miscellaneous.AttributeUtils;
import me.despical.commons.scoreboard.ScoreboardLib;
import me.despical.commons.serializer.InventorySerializer;
import me.despical.commons.util.Collections;
import me.despical.commons.util.LogUtils;
import me.despical.commons.util.UpdateChecker;
import me.despical.kotl.api.StatsStorage;
import me.despical.kotl.arena.Arena;
import me.despical.kotl.arena.ArenaRegistry;
import me.despical.kotl.command.AbstractCommand;
import me.despical.kotl.event.ListenerAdapter;
import me.despical.kotl.handler.ChatManager;
import me.despical.kotl.handler.PlaceholderManager;
import me.despical.kotl.handler.language.LanguageManager;
import me.despical.kotl.handler.rewards.RewardsFactory;
import me.despical.kotl.user.User;
import me.despical.kotl.user.UserManager;
import me.despical.kotl.user.data.MysqlManager;
import me.despical.kotl.util.*;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.logging.Logger;

/**
 * @author Despical
 * <p>
 * Created at 20.06.2020
 */
public class Main extends JavaPlugin {

	private boolean forceDisable;

	private ConfigPreferences configPreferences;
	private MysqlDatabase database;
	private UserManager userManager;
	private CommandFramework commandFramework;
	private CuboidSelector cuboidSelector;
	private ChatManager chatManager;
	private RewardsFactory rewardsFactory;
	private LanguageManager languageManager;
	private ArenaRegistry arenaRegistry;

	@Override
	public void onEnable() {
		this.forceDisable = validateIfPluginShouldStart();

		if (forceDisable) {
			getServer().getPluginManager().disablePlugin(this);
			return;
		}

		if ((configPreferences = new ConfigPreferences(this)).getOption(ConfigPreferences.Option.DEBUG_MESSAGES)) {
			LogUtils.enableLogging("KOTL");
			LogUtils.log("Initialization started!");
		}

		long start = System.currentTimeMillis();

		setupFiles();
		initializeClasses();
		checkUpdate();
		initializeMcRankings();

		getLogger().info("Initialization finished. Join our Discord server if you need any help. (https://discord.gg/rVkaGmyszE)");
		LogUtils.log("Initialization finished took {0} ms.", System.currentTimeMillis() - start);
	}
	
	private boolean validateIfPluginShouldStart() {
		final Logger logger = getLogger();

		if (!VersionResolver.isCurrentBetween(VersionResolver.ServerVersion.v1_8_R1, VersionResolver.ServerVersion.v1_19_R3)) {
			logger.warning("Your server version is not supported by King of the Ladder!");
			logger.warning("Sadly, we must shut off. Maybe you consider changing your server version?");
			return true;
		}

		try {
			Class.forName("org.spigotmc.SpigotConfig");
		} catch (Exception e) {
			logger.warning("Your server software is not supported by King of the Ladder!");
			logger.warning("We support only Spigot and Spigot forks only! Shutting off...");
			return true;
		}

		return false;
	}
	
	@Override
	public void onDisable() {
		if (forceDisable) return;

		LogUtils.log("System disable initialized.");
		long start = System.currentTimeMillis();
		
		saveAllUserStatistics();

		if (database != null) {
			database.shutdownConnPool();
		}

		for (Arena arena : arenaRegistry.getArenas()) {
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
				arena.doBarAction(player, 0);
				arena.getScoreboardManager().removeScoreboard(player);

				AttributeUtils.resetAttackCooldown(player);
			}
		}

		LogUtils.log("System disable finished took {0} ms.", System.currentTimeMillis() - start);
		LogUtils.disableLogging();
	}
	
	private void initializeClasses() {
		if (configPreferences.getOption(ConfigPreferences.Option.DATABASE_ENABLED)) database = new MysqlDatabase(this, "mysql");

		chatManager = new ChatManager(this);
		languageManager = new LanguageManager(this);
		userManager = new UserManager(this);
		commandFramework = new CommandFramework(this);
		cuboidSelector = new CuboidSelector(this);
		rewardsFactory = new RewardsFactory(this);
		arenaRegistry = new ArenaRegistry(this);

		ListenerAdapter.registerEvents(this);
		AbstractCommand.registerCommands(this);
		ScoreboardLib.setPluginInstance(this);
		User.cooldownHandlerTask();

		registerSoftDependencies();
	}
	
	private void registerSoftDependencies() {
		LogUtils.log("Hooking into soft dependencies.");

		startPluginMetrics();

		if (chatManager.isPapiEnabled()) {
			LogUtils.log("Hooking into PlaceholderAPI.");
			new PlaceholderManager(this);
		} else LogUtils.log("PlaceholderAPI not found skipped hooking.");
	}
	
	private void startPluginMetrics() {
		Metrics metrics = new Metrics(this, 7938);

		metrics.addCustomChart(new SimplePie("locale_used", () -> languageManager.getPluginLocale().prefix));
		metrics.addCustomChart(new SimplePie("database_enabled", () -> configPreferences.getOption(ConfigPreferences.Option.DATABASE_ENABLED) ? "Enabled" : "Disabled"));
		metrics.addCustomChart(new SimplePie("update_notifier", () -> configPreferences.getOption(ConfigPreferences.Option.UPDATE_NOTIFIER_ENABLED) ? "Enabled" : "Disabled"));
	}
	
	private void checkUpdate() {
		if (!configPreferences.getOption(ConfigPreferences.Option.UPDATE_NOTIFIER_ENABLED)) return;

		UpdateChecker.init(this, 80686).requestUpdateCheck().whenComplete((result, exception) -> {
			if (result.requiresUpdate()) {
				final Logger logger = getLogger();

				logger.info("Found a new version available: v" + result.getNewestVersion());
				logger.info("Download it on SpigotMC:");
				logger.info("spigotmc.org/resources/king-of-the-ladder.80686/");
			}
		});
	}
	
	private void setupFiles() {
		Collections.streamOf("arenas", "rewards", "stats", "mysql", "messages").filter(name -> !new File(getDataFolder(),name + ".yml").exists()).forEach(name -> saveResource(name + ".yml", false));
	}

	private void initializeMcRankings() {
		final McRankings mcRankings = new McRankings(this);
		final McRankings.Leaderboard scoresLeaderboard = mcRankings.getLeaderboard(0, "King of the Ladder Top Scorers", "Score", true);
		final McRankings.Leaderboard gamesLeaderboard = mcRankings.getLeaderboard(0, "King of the Ladder Top Game Players", "Games Played", true);

		final FileConfiguration config = ConfigUtils.getConfig(this, "stats");

		for (final OfflinePlayer offlinePlayer : getServer().getOfflinePlayers()) {
			final String uuid = offlinePlayer.getUniqueId().toString();

			if (config.contains(uuid)) {
				scoresLeaderboard.setScore(offlinePlayer, config.getInt(String.format("%s.score", uuid)));
				gamesLeaderboard.setScore(offlinePlayer, config.getInt(String.format("%s.toursplayed", uuid)));
			}
		}

		getLogger().info("Check out your top scorer leaderboard at: " + scoresLeaderboard.getUrl());
		getLogger().info("Check out top game players leaderboard at: " + gamesLeaderboard.getUrl());
	}

	@NotNull
	public ConfigPreferences getConfigPreferences() {
		return configPreferences;
	}

	@NotNull
	public MysqlDatabase getMysqlDatabase() {
		return database;
	}

	@NotNull
	public CommandFramework getCommandFramework() {
		return commandFramework;
	}

	@NotNull
	public CuboidSelector getCuboidSelector() {
		return cuboidSelector;
	}

	@NotNull
	public ChatManager getChatManager() {
		return chatManager;
	}

	@NotNull
	public RewardsFactory getRewardsFactory() {
		return rewardsFactory;
	}

	@NotNull
	public UserManager getUserManager() {
		return userManager;
	}

	@NotNull
	public ArenaRegistry getArenaRegistry() {
		return arenaRegistry;
	}

	private void saveAllUserStatistics() {
		for (Player player : getServer().getOnlinePlayers()) {
			final User user = userManager.getUser(player);

			if (userManager.getDatabase() instanceof MysqlManager) {
				final StringBuilder update = new StringBuilder(" SET ");
				final MysqlManager database = ((MysqlManager) userManager.getDatabase());

				for (StatsStorage.StatisticType stat : StatsStorage.StatisticType.values()) {
					if (!stat.isPersistent()) continue;

					final int val = user.getStat(stat);
					final String statName = stat.getName();

					if (update.toString().equalsIgnoreCase(" SET ")) {
						update.append(statName).append("'='").append(val);
					}

					update.append(", ").append(statName).append("'='").append(val);
				}

				final String finalUpdate = update.toString();
				database.getDatabase().executeUpdate("UPDATE " + database.getTableName() + finalUpdate + " WHERE UUID='" + user.getUniqueId().toString() + "';");
				continue;
			}

			userManager.getDatabase().saveAllStatistic(user);
		}
	}
}