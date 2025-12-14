/*
 * KOTL - Don't let others climb to top of the ladders!
 * Copyright (C) 2025  Berke Akçen
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

import dev.despical.commandframework.CommandFramework;
import dev.despical.commons.miscellaneous.AttributeUtils;
import dev.despical.commons.scoreboard.ScoreboardLib;
import dev.despical.commons.serializer.InventorySerializer;
import dev.despical.commons.util.UpdateChecker;
import dev.despical.fileitems.ItemManager;
import dev.despical.kotl.api.StatisticType;
import dev.despical.kotl.api.events.KOTLEvent;
import dev.despical.kotl.arena.Arena;
import dev.despical.kotl.arena.ArenaRegistry;
import dev.despical.kotl.arena.managers.ArenaManager;
import dev.despical.kotl.commands.AdminCommands;
import dev.despical.kotl.commands.PlayerCommands;
import dev.despical.kotl.commands.TabCompleters;
import dev.despical.kotl.database.Database;
import dev.despical.kotl.database.DatabaseType;
import dev.despical.kotl.database.FlatFileStorage;
import dev.despical.kotl.database.MySQLStorage;
import dev.despical.kotl.events.ArenaEvents;
import dev.despical.kotl.events.Events;
import dev.despical.kotl.handlers.ChatManager;
import dev.despical.kotl.handlers.PlaceholderManager;
import dev.despical.kotl.handlers.cooldown.CooldownManager;
import dev.despical.kotl.handlers.rewards.RewardsFactory;
import dev.despical.kotl.kits.KitManager;
import dev.despical.kotl.language.LanguageManager;
import dev.despical.kotl.options.ConfigOptions;
import dev.despical.kotl.options.Option;
import dev.despical.kotl.user.User;
import dev.despical.kotl.user.UserManager;
import dev.despical.kotl.util.CuboidSelector;
import lombok.AccessLevel;
import lombok.Getter;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.function.Predicate;
import java.util.function.Supplier;
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

    private ConfigOptions configOptions;
    private UserManager userManager;
    private Database database;
    private CommandFramework commandFramework;
    private CuboidSelector cuboidSelector;
    private ChatManager chatManager;
    private RewardsFactory rewardsFactory;
    private LanguageManager languageManager;
    private ArenaRegistry arenaRegistry;
    private KitManager kitManager;
    private ArenaManager arenaManager;
    private CooldownManager cooldownManager;
    private ItemManager itemManager;

    @Getter(AccessLevel.NONE)
    private boolean initializeFinished;

    @Override
    public void onEnable() {
        initializeClasses();
        checkUpdate();

        getLogger().info("Initialization finished.");
        getLogger().info("Join our Discord server: https://discord.gg/uXVU8jmtpU");
    }

    @Override
    public void onDisable() {
        saveAllUserStatistics();

        for (Arena arena : arenaRegistry.getArenas()) {
            for (Player player : arena.getPlayers()) {
                player.getInventory().clear();
                player.getInventory().setArmorContents(null);
                player.getActivePotionEffects().forEach(effect -> player.removePotionEffect(effect.getType()));

                if (configOptions.isEnabled(Option.INVENTORY_MANAGER_ENABLED)) {
                    InventorySerializer.loadInventory(this, player);
                } else {
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
        instance = this;

        this.setupConfigurationFiles();
        this.initializeItemManager();

        configOptions = new ConfigOptions(this);
        chatManager = new ChatManager(this);
        languageManager = new LanguageManager(this);
        userManager = new UserManager(this);
        database = this.createDatabase();
        cuboidSelector = new CuboidSelector(this);
        rewardsFactory = new RewardsFactory(this);
        arenaRegistry = new ArenaRegistry(this);
        kitManager = new KitManager(this);
        arenaManager = new ArenaManager(this);
        cooldownManager = new CooldownManager(this);

        ScoreboardLib.setPluginInstance(this);

        this.registerCommands();
        this.registerEvents();

        if (chatManager.isPapiEnabled()) {
            new PlaceholderManager(this);
        }

        this.initializeMetrics();
        this.initializeFinished = true;
    }

    private void checkUpdate() {
        if (!configOptions.isEnabled(Option.UPDATE_NOTIFIER_ENABLED)) {
            return;
        }

        UpdateChecker.init(this, 80686).onNewUpdate(result -> getLogger().info("Found a new version available: v" + result.getNewestVersion()));
    }

    private void setupConfigurationFiles() {
        saveDefaultConfig();

        Stream.of("arenas", "rewards", "stats", "mysql", "messages", "kits")
            .map(fileName -> new File(getDataFolder(), fileName + ".yml"))
            .filter(Predicate.not(File::exists))
            .forEach(file -> saveResource(file.getName(), false));
    }

    private void initializeItemManager() {
        itemManager = new ItemManager(this);
        itemManager.registerItems("items", "items");
    }

    private void initializeMetrics() {
        Metrics metrics = new Metrics(this, 7938);
        metrics.addCustomChart(new SimplePie("locale_used", () -> languageManager.getCurrentLocale().getPrefix()));
        metrics.addCustomChart(new SimplePie("database_enabled", () -> configOptions.isEnabled(Option.DATABASE_ENABLED) ? "Enabled" : "Disabled"));
        metrics.addCustomChart(new SimplePie("update_notifier", () -> configOptions.isEnabled(Option.UPDATE_NOTIFIER_ENABLED) ? "Enabled" : "Disabled"));
    }

    private Database createDatabase() {
        String databaseName = getConfig().getString("Database");

        return switch (DatabaseType.getByName(databaseName)) {
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

        new PlayerCommands();
        new AdminCommands();
        new TabCompleters(this);
    }

    private void registerEvents() {
        new Events(this);
        new ArenaEvents(this);
    }

    public void reload() {
        reloadConfig();

        kitManager.loadKits();
        chatManager.reload();
        configOptions.loadOptions();
    }

    public void callEvent(KOTLEvent event) {
        this.callEvent(() -> event);
    }

    public void callEvent(Supplier<KOTLEvent> eventSupplier) {
        if (initializeFinished && isEnabled()) {
            getServer().getScheduler().runTask(this, () -> getServer().getPluginManager().callEvent(eventSupplier.get()));
        }
    }

    private void saveAllUserStatistics() {
        if (database instanceof MySQLStorage mySQLStorage) {
            for (User user : userManager.getUsers()) {
                StringBuilder update = new StringBuilder(" SET ");

                for (StatisticType stat : StatisticType.getPersistentStats()) {
                    int value = user.getStat(stat);
                    String statName = stat.getName();

                    if (update.toString().equalsIgnoreCase(" SET ")) {
                        update.append(statName).append("=").append(value);
                    }

                    update.append(", ").append(statName).append("=").append(value);
                }

                mySQLStorage.getDatabase().executeUpdate("UPDATE %s%s WHERE UUID='%s';".formatted(mySQLStorage.getStatsTable(), update.toString(), user.getUniqueId().toString()));
            }
        }

        database.shutdown();
    }
}
