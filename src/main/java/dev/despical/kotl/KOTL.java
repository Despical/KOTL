/*
 * KOTL - Don't let others climb to top of the ladders!
 * Copyright (C) 2026  Berke Akçen
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

package dev.despical.kotl;

import dev.despical.commandframework.CommandArguments;
import dev.despical.commandframework.CommandErrorMessage;
import dev.despical.commandframework.CommandFramework;
import dev.despical.commandframework.options.FrameworkOption;
import dev.despical.commons.util.UpdateChecker;
import dev.despical.fileitems.ItemManager;
import dev.despical.fileitems.ItemOption;
import dev.despical.kotl.api.EventManager;
import dev.despical.kotl.arena.Arena;
import dev.despical.kotl.arena.ArenaDataSaver;
import dev.despical.kotl.arena.ArenaRegistry;
import dev.despical.kotl.arena.managers.ArenaManager;
import dev.despical.kotl.arena.options.ArenaKeys;
import dev.despical.kotl.chat.ChatManager;
import dev.despical.kotl.command.PlayingCommandPolicy;
import dev.despical.kotl.database.Database;
import dev.despical.kotl.database.DatabaseType;
import dev.despical.kotl.database.FlatFileStorage;
import dev.despical.kotl.database.MySQLStorage;
import dev.despical.kotl.event.ArenaEvents;
import dev.despical.kotl.event.GeneralEvents;
import dev.despical.kotl.event.SetupListener;
import dev.despical.kotl.game.GameManager;
import dev.despical.kotl.handlers.cooldown.CooldownManager;
import dev.despical.kotl.leaderboard.LeaderboardManager;
import dev.despical.kotl.option.BooleanOption;
import dev.despical.kotl.option.ConfigOptions;
import dev.despical.kotl.papi.PlaceholderManager;
import dev.despical.kotl.particle.OutlineManager;
import dev.despical.kotl.scoreboard.ScoreboardManager;
import dev.despical.kotl.stats.offline.StatsCacheManager;
import dev.despical.kotl.user.User;
import dev.despical.kotl.user.UserManager;
import dev.despical.kotl.util.AutoSaveHandler;
import dev.despical.kotl.util.CuboidSelector;
import dev.despical.kotl.util.ShutdownDetector;
import dev.despical.kotl.util.Var;
import lombok.Getter;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;
import org.bstats.charts.SingleLineChart;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

/**
 * @author Despical
 * <p>
 * Created at 20.06.2020
 */
@Getter
public class KOTL extends JavaPlugin {

    @Getter
    private static KOTL instance;

    private ConfigOptions options;
    private UserManager userManager;
    private Database database;
    private CommandFramework commandFramework;
    private CuboidSelector cuboidSelector;
    private ChatManager chatManager;
    private ArenaRegistry arenaRegistry;
    private ArenaManager arenaManager;
    private GameManager gameManager;
    private OutlineManager outlineManager;
    private CooldownManager cooldownManager;
    private LeaderboardManager leaderboardManager;
    private EventManager eventManager;
    private ItemManager itemManager;
    private StatsCacheManager statsCacheManager;
    private PlayingCommandPolicy playingCommandPolicy;
    private ArenaDataSaver arenaDataSaver;
    private Metrics metrics;

    @Override
    public void onEnable() {
        ShutdownDetector.init();

        instance = this;

        createConfigFiles();
        initializeClasses();

        getLogger().info("Initialization finished.");
        getLogger().info("Join our Discord server: https://discord.gg/uXVU8jmtpU");
    }

    @Override
    public void onDisable() {
        arenaManager.handleDisable();
        outlineManager.cancelAll();
        arenaDataSaver.saveAllArenas();
        database.shutdown();
        metrics.shutdown();
    }

    private void createConfigFiles() {
        saveDefaultConfig();
        saveResourceIfMissing("mysql.yml");
    }

    private void initializeClasses() {
        this.loadItemManager();

        options = new ConfigOptions(this);
        playingCommandPolicy = new PlayingCommandPolicy(this);
        chatManager = new ChatManager(this);
        database = this.createDatabase();
        userManager = new UserManager(this);
        statsCacheManager = new StatsCacheManager(this);
        cuboidSelector = new CuboidSelector(this);
        gameManager = new GameManager(this);
        outlineManager = new OutlineManager(this);
        arenaRegistry = new ArenaRegistry(this);
        outlineManager.refreshAll(arenaRegistry.getArenas());
        arenaManager = new ArenaManager(this);
        cooldownManager = new CooldownManager();
        eventManager = new EventManager(this);
        leaderboardManager = new LeaderboardManager(this);
        arenaDataSaver = new ArenaDataSaver(this);

        registerCommands();
        registerEvents();
        registerPlaceholderManager();
        runAutoSave();
        initializeMetrics();
        checkUpdates();

        getServer().getOnlinePlayers().forEach(ScoreboardManager::resetPlayerScoreboard);
    }

    private void runAutoSave() {
        new AutoSaveHandler(this).runTaskTimerAsynchronously(this, 20, 20 * 60 * 5);
    }

    private void loadItemManager() {
        itemManager = new ItemManager(this, _ -> ItemOption.enableOptions(ItemOption.GLOW, ItemOption.AMOUNT));

        registerItems();
    }

    public void registerItems() {
        itemManager.registerItems("menu/setup-menu", "items");
        itemManager.registerItems("stats-menu-items", "items", "menu/stats-menu");
    }

    private Database createDatabase() {
        String databaseType = getConfig().getString("database");

        return switch (DatabaseType.getByName(databaseType)) {
            case FLAT_FILE -> new FlatFileStorage();
            case MYSQL -> new MySQLStorage();
            case null -> {
                getLogger().warning("Invalid database type. Using flat file storage.");
                yield new FlatFileStorage();
            }
        };
    }

    private void registerCommands() {
        commandFramework = new CommandFramework(this);

        if (BooleanOption.DEBUG.value()) {
            commandFramework.options().enableOptions(FrameworkOption.DEBUG);
        }

        commandFramework.addCustomParameter(Player.class, CommandArguments::getSender);
        commandFramework.addCustomParameter(User.class, args -> userManager.getUser(args.<Player>getSender()));
        commandFramework.addCustomParameter(Arena.class, args -> arenaRegistry.getArena(args.getFirst()));
        commandFramework.registerAllInPackage("dev.despical.kotl.command");

        var messages = Stream.of(CommandErrorMessage.SHORT_ARG_SIZE, CommandErrorMessage.LONG_ARG_SIZE);
        messages.forEach(message -> message.setHandler((cmd, args) -> {
            chatManager.sendMessage(args, "correct-usage", Var.of("%usage%", cmd.usage().replace("%label%", args.getLabel())));
            return true;
        }));
    }

    private void registerEvents() {
        new GeneralEvents();
        new ArenaEvents();
        new SetupListener();
    }

    private void registerPlaceholderManager() {
        if (!isPluginEnabled("PlaceholderAPI")) {
            return;
        }

        PlaceholderManager manager = new PlaceholderManager(this);
        manager.register();
    }

    private void initializeMetrics() {
        metrics = new Metrics(this, 7938);
        metrics.addCustomChart(new SimplePie("database_type", this::resolveMetricsDatabaseType));
        metrics.addCustomChart(new SimplePie("placeholderapi_enabled", () -> isPluginEnabled("PlaceholderAPI") ? "yes" : "no"));
        metrics.addCustomChart(new SingleLineChart("arenas_total", () -> arenaRegistry.getArenas().size()));
        metrics.addCustomChart(new SingleLineChart("arenas_ready", () -> (int) arenaRegistry.getArenas().stream().filter(arena -> arena.getOption(ArenaKeys.READY)).count()));
    }

    private void checkUpdates() {
        if (!BooleanOption.UPDATE_NOTIFIER.value()) {
            return;
        }

        UpdateChecker.init(this, 80686).onNewUpdate(_ -> {
            Logger logger = getLogger();
            logger.log(Level.INFO, "An update for Kig of the Ladder ({0}) is available at:", getDescription().getVersion());
            logger.log(Level.INFO, "https://www.spigotmc.org/resources/king-of-the-ladder.80686/");
        });
    }

    private String resolveMetricsDatabaseType() {
        String configured = getConfig().getString("database", "flat");
        DatabaseType type = DatabaseType.getByName(configured);
        return type != null ? type.name().toLowerCase(Locale.ENGLISH) : configured.toLowerCase(Locale.ENGLISH);
    }

    private boolean isPluginEnabled(String pluginName) {
        return getServer().getPluginManager().isPluginEnabled(pluginName);
    }

    private void saveResourceIfMissing(String resourcePath) {
        File targetFile = new File(getDataFolder(), resourcePath);

        if (targetFile.exists()) {
            return;
        }

        saveResource(resourcePath, false);
    }
}
