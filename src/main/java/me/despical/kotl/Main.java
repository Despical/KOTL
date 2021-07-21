/*
 * KOTL - Don't let others to climb top of the ladders!
 * Copyright (C) 2021 Despical and contributors
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
import me.despical.commons.exception.ExceptionLogHandler;
import me.despical.commons.util.Collections;
import me.despical.commons.util.JavaVersion;
import me.despical.kotl.api.StatsStorage;
import me.despical.kotl.arena.Arena;
import me.despical.kotl.arena.ArenaEvents;
import me.despical.kotl.arena.ArenaRegistry;
import me.despical.kotl.commands.TabCompletion;
import me.despical.kotl.commands.admin.AdminCommands;
import me.despical.kotl.commands.player.PlayerCommands;
import me.despical.kotl.events.ChatEvents;
import me.despical.kotl.events.Events;
import me.despical.kotl.events.JoinEvent;
import me.despical.kotl.events.QuitEvent;
import me.despical.kotl.handlers.ChatManager;
import me.despical.kotl.handlers.PlaceholderManager;
import me.despical.kotl.handlers.hologram.HologramManager;
import me.despical.kotl.handlers.language.LanguageManager;
import me.despical.kotl.handlers.rewards.RewardsFactory;
import me.despical.kotl.user.User;
import me.despical.kotl.user.UserManager;
import me.despical.kotl.user.data.MysqlManager;
import me.despical.kotl.utils.*;
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
	private CommandFramework commandFramework;
	private CuboidSelector cuboidSelector;
	private ChatManager chatManager;
	private RewardsFactory rewardsFactory;
	private LanguageManager languageManager;
	private HologramManager hologramManager;

	@Override
	public void onEnable() {
		Debugger.setEnabled(this);

		if ((forceDisable = !validateIfPluginShouldStart())) {
			getServer().getPluginManager().disablePlugin(this);
			return;
		}

		exceptionLogHandler = new ExceptionLogHandler(this);
		exceptionLogHandler.setMainPackage("me.despical.kotl");
		exceptionLogHandler.addBlacklistedClass("me.despical.kotl.user.data.MysqlManager", "me.despical.commons.database.MysqlDatabase");
		exceptionLogHandler.setRecordMessage("[KOTL] We have found a bug in the code. Contact us at our official Discord server (Invite link: https://discordapp.com/invite/Vhyy4HA) with the following error given above!");
		saveDefaultConfig();

		Debugger.debug("Initialization started!");

		long start = System.currentTimeMillis();
		
		configPreferences = new ConfigPreferences(this);
		setupFiles();
		initializeClasses();
		checkUpdate();

		Debugger.debug("Initialization finished took {0} ms.", System.currentTimeMillis() - start);
	}
	
	private boolean validateIfPluginShouldStart() {
		if (VersionResolver.isCurrentLower(VersionResolver.ServerVersion.v1_8_R1)) {
			Debugger.sendConsoleMessage("&cYour server version is not supported by King of the Ladder!");
			Debugger.sendConsoleMessage("&cSadly, we must shut off. Maybe you consider changing your server version?");
			return false;
		}

		if (JavaVersion.getCurrentVersion().isAt(JavaVersion.JAVA_8)) {
			Debugger.sendConsoleMessage("&cThis plugin won't support Java 8 in future updates.");
			Debugger.sendConsoleMessage("&cSo, maybe consider to update your version, right?");
		}

		try {
			Class.forName("org.spigotmc.SpigotConfig");
		} catch (Exception e) {
			Debugger.sendConsoleMessage("&cYour server software is not supported by King of the Ladder!");
			Debugger.sendConsoleMessage("&cWe support only Spigot and Spigot forks only! Shutting off...");
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
		
		getServer().getLogger().removeHandler(exceptionLogHandler);
		saveAllUserStatistics();

		if (configPreferences.getOption(ConfigPreferences.Option.DATABASE_ENABLED)) {
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

		Debugger.debug("System disable finished took {0} ms", System.currentTimeMillis() - start);
	}
	
	private void initializeClasses() {
		ScoreboardLib.setPluginInstance(this);

		if (configPreferences.getOption(ConfigPreferences.Option.DATABASE_ENABLED)) {
			database = new MysqlDatabase(ConfigUtils.getConfig(this, "mysql"));
		}

		chatManager = new ChatManager(this);
		languageManager = new LanguageManager(this);
		userManager = new UserManager(this);
		commandFramework = new CommandFramework(this);
		cuboidSelector = new CuboidSelector(this);
		rewardsFactory = new RewardsFactory(this);
		hologramManager = new HologramManager();

		ArenaRegistry.registerArenas();

		new AdminCommands(this);
		new PlayerCommands(this);
		new TabCompletion(commandFramework);

		new ChatEvents(this);
		new Events(this);
		new ArenaEvents(this);
		new JoinEvent(this);
		new QuitEvent(this);

		registerSoftDependencies();
	}
	
	private void registerSoftDependencies() {
		Debugger.debug("Hooking into soft dependencies");

		startPluginMetrics();

		if (getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
			Debugger.debug("Hooking into PlaceholderAPI.");
			new PlaceholderManager(this);
		}

		Debugger.debug("Hooked into soft dependencies.");
	}
	
	private void startPluginMetrics() {
		Metrics metrics = new Metrics(this, 7938);

		if (!metrics.isEnabled()) {
			return;
		}

		metrics.addCustomChart(new Metrics.SimplePie("database_enabled", () -> String.valueOf(configPreferences.getOption(ConfigPreferences.Option.DATABASE_ENABLED))));
		metrics.addCustomChart(new Metrics.SimplePie("locale_used", () -> languageManager.getPluginLocale().prefix));
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
					Debugger.sendConsoleMessage("[KOTL] spigotmc.org/resources/king-of-the-ladder.80686/");
				}

				return;
			}

			Debugger.sendConsoleMessage("[KOTL] Found a new version available: v" + result.getNewestVersion());
			Debugger.sendConsoleMessage("[KOTL] Download it SpigotMC:");
			Debugger.sendConsoleMessage("[KOTL] spigotmc.org/resources/king-of-the-ladder.80686/");
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

	public CommandFramework getCommandFramework() {
		return commandFramework;
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
						update.append(stat.getName()).append("'='").append(user.getStat(stat));
					}

					update.append(", ").append(stat.getName()).append("'='").append(user.getStat(stat));
				}

				String finalUpdate = update.toString();
				MysqlManager database = ((MysqlManager) userManager.getDatabase());
				database.getDatabase().executeUpdate("UPDATE " + database.getTableName() + finalUpdate + " WHERE UUID='" + user.getUniqueId().toString() + "';");
				continue;
			}

			userManager.getDatabase().saveAllStatistic(user);
		}
	}
}