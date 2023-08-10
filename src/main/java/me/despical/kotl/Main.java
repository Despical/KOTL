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
import me.despical.commons.database.MysqlDatabase;
import me.despical.commons.miscellaneous.AttributeUtils;
import me.despical.commons.scoreboard.ScoreboardLib;
import me.despical.commons.serializer.InventorySerializer;
import me.despical.commons.util.Collections;
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
import me.despical.kotl.util.CuboidSelector;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;
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
		initializeClasses();
		checkUpdate();

		getLogger().info("Initialization finished. Consider donating: https://buymeacoffee.com/despical");
	}

	@Override
	public void onDisable() {
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
	}
	
	private void initializeClasses() {
		this.setupConfigurationFiles();

		configPreferences = new ConfigPreferences(this);
		chatManager = new ChatManager(this);
		languageManager = new LanguageManager(this);
		userManager = new UserManager(this);
		commandFramework = new CommandFramework(this);
		cuboidSelector = new CuboidSelector(this);
		rewardsFactory = new RewardsFactory(this);
		arenaRegistry = new ArenaRegistry(this);

		if (configPreferences.getOption(ConfigPreferences.Option.DATABASE_ENABLED)) database = new MysqlDatabase(this, "mysql");
		if (chatManager.isPapiEnabled()) new PlaceholderManager(this);

		ListenerAdapter.registerEvents(this);
		AbstractCommand.registerCommands(this);
		ScoreboardLib.setPluginInstance(this);
		User.cooldownHandlerTask();

		final var metrics = new Metrics(this, 7938);
		metrics.addCustomChart(new SimplePie("locale_used", () -> languageManager.getPluginLocale().prefix()));
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
				logger.info("https://spigotmc.org/resources/80686");
			}
		});
	}
	
	private void setupConfigurationFiles() {
		Collections.streamOf("arenas", "rewards", "stats", "mysql", "messages").filter(name -> !new File(getDataFolder(),name + ".yml").exists()).forEach(name -> saveResource(name + ".yml", false));
	}

	@NotNull
	public ConfigPreferences getConfigPreferences() {
		return configPreferences;
	}

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
		for (final var player : getServer().getOnlinePlayers()) {
			final var user = userManager.getUser(player);

			if (userManager.getDatabase() instanceof MysqlManager mysqlDatabase) {
				final var update = new StringBuilder(" SET ");

				for (final var stat : StatsStorage.StatisticType.values()) {
					if (!stat.isPersistent()) continue;

					final var val = user.getStat(stat);
					final var statName = stat.getName();

					if (update.toString().equalsIgnoreCase(" SET ")) {
						update.append(statName).append("'='").append(val);
					}

					update.append(", ").append(statName).append("'='").append(val);
				}

				final var finalUpdate = update.toString();
				mysqlDatabase.getDatabase().executeUpdate("UPDATE playerstats" + finalUpdate + " WHERE UUID='" + user.getUniqueId().toString() + "';");
				continue;
			}

			userManager.getDatabase().saveStatistics(user);
		}
	}
}