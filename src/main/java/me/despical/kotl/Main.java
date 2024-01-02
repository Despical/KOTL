/*
 * KOTL - Don't let others climb to top of the ladders!
 * Copyright (C) 2024  Berke Akçen
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
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
import me.despical.kotl.arena.ArenaRegistry;
import me.despical.kotl.arena.managers.ArenaManager;
import me.despical.kotl.commands.AbstractCommand;
import me.despical.kotl.events.ListenerAdapter;
import me.despical.kotl.handlers.ChatManager;
import me.despical.kotl.handlers.PlaceholderManager;
import me.despical.kotl.handlers.language.LanguageManager;
import me.despical.kotl.handlers.rewards.RewardsFactory;
import me.despical.kotl.kits.KitManager;
import me.despical.kotl.user.User;
import me.despical.kotl.user.UserManager;
import me.despical.kotl.user.data.MysqlManager;
import me.despical.kotl.util.CuboidSelector;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;

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
	private KitManager kitManager;
	private ArenaManager arenaManager;

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

		for (final var arena : arenaRegistry.getArenas()) {
			for (final var player : arena.getPlayers()) {
				player.getInventory().clear();
				player.getInventory().setArmorContents(null);

				if (getOption(ConfigPreferences.Option.INVENTORY_MANAGER_ENABLED)) {
					InventorySerializer.loadInventory(this, player);
				} else {
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

		if (getOption(ConfigPreferences.Option.DATABASE_ENABLED)) database = new MysqlDatabase(this, "mysql");

		chatManager = new ChatManager(this);
		languageManager = new LanguageManager(this);
		userManager = new UserManager(this);
		commandFramework = new CommandFramework(this);
		cuboidSelector = new CuboidSelector(this);
		rewardsFactory = new RewardsFactory(this);
		arenaRegistry = new ArenaRegistry(this);
		kitManager = new KitManager(this);
		arenaManager = new ArenaManager(this);

		ListenerAdapter.registerEvents(this);
		AbstractCommand.registerCommands(this);
		ScoreboardLib.setPluginInstance(this);
		User.cooldownHandlerTask();

		if (chatManager.isPapiEnabled()) new PlaceholderManager(this);

		final var metrics = new Metrics(this, 7938);
		metrics.addCustomChart(new SimplePie("locale_used", () -> languageManager.getPluginLocale().prefix()));
		metrics.addCustomChart(new SimplePie("database_enabled", () -> getOption(ConfigPreferences.Option.DATABASE_ENABLED) ? "Enabled" : "Disabled"));
		metrics.addCustomChart(new SimplePie("update_notifier", () -> getOption(ConfigPreferences.Option.UPDATE_NOTIFIER_ENABLED) ? "Enabled" : "Disabled"));
	}

	private void checkUpdate() {
		if (!getOption(ConfigPreferences.Option.UPDATE_NOTIFIER_ENABLED)) return;

		UpdateChecker.init(this, 80686).requestUpdateCheck().whenComplete((result, exception) -> {
			if (result.requiresUpdate()) {
				final var logger = getLogger();

				logger.info("Found a new version available: v" + result.getNewestVersion());
				logger.info("Download it on SpigotMC:");
				logger.info("https://spigotmc.org/resources/80686");
			}
		});
	}

	private void setupConfigurationFiles() {
		Collections.streamOf("arenas", "rewards", "stats", "mysql", "messages", "kits").filter(name -> !new File(getDataFolder(),name + ".yml").exists()).forEach(name -> saveResource(name + ".yml", false));
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

	@NotNull
	public KitManager getKitManager() {
		return kitManager;
	}

	@NotNull
	public ArenaManager getArenaManager() {
		return arenaManager;
	}

	public boolean getOption(ConfigPreferences.Option option) {
		return configPreferences.getOption(option);
	}

	public void reload() {
		kitManager.loadKits();
		chatManager.reload();
		configPreferences.loadOptions();

		reloadConfig();
	}

	private void saveAllUserStatistics() {
		for (final var player : getServer().getOnlinePlayers()) {
			final var user = userManager.getUser(player);

			if (userManager.getDatabase() instanceof MysqlManager mysqlManager) {
				final var update = new StringBuilder(" SET ");

				for (final var stat : StatsStorage.StatisticType.values()) {
					if (!stat.isPersistent()) continue;

					final var val = user.getStat(stat);
					final var statName = stat.getName();

					if (update.toString().equalsIgnoreCase(" SET ")) {
						update.append(statName).append("=").append(val);
					}

					update.append(", ").append(statName).append("=").append(val);
				}

				final var finalUpdate = update.toString();

				mysqlManager.getDatabase().executeUpdate("UPDATE %s%s WHERE UUID='%s';".formatted(mysqlManager.getTable(), finalUpdate, user.getUniqueId().toString()));
				continue;
			}

			userManager.getDatabase().saveStatistics(user);
		}
	}
}