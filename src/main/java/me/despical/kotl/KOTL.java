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

package me.despical.kotl;

import me.despical.commandframework.CommandFramework;
import me.despical.commons.miscellaneous.AttributeUtils;
import me.despical.commons.scoreboard.ScoreboardLib;
import me.despical.commons.serializer.InventorySerializer;
import me.despical.commons.util.UpdateChecker;
import me.despical.kotl.api.StatsStorage;
import me.despical.kotl.api.events.KOTLEvent;
import me.despical.kotl.arena.Arena;
import me.despical.kotl.arena.ArenaRegistry;
import me.despical.kotl.arena.managers.ArenaManager;
import me.despical.kotl.commands.AdminCommands;
import me.despical.kotl.commands.PlayerCommands;
import me.despical.kotl.events.ArenaEvents;
import me.despical.kotl.events.Events;
import me.despical.kotl.handlers.ChatManager;
import me.despical.kotl.handlers.PlaceholderManager;
import me.despical.kotl.handlers.cooldown.CooldownManager;
import me.despical.kotl.handlers.rewards.RewardsFactory;
import me.despical.kotl.kits.KitManager;
import me.despical.kotl.language.LanguageManager;
import me.despical.kotl.options.ConfigOptions;
import me.despical.kotl.options.Option;
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
import java.util.Locale;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * @author Despical
 * <p>
 * Created at 20.06.2020
 */
public class KOTL extends JavaPlugin {

    private ConfigOptions configOptions;
    private UserManager userManager;
    private CommandFramework commandFramework;
    private CuboidSelector cuboidSelector;
    private ChatManager chatManager;
    private RewardsFactory rewardsFactory;
    private LanguageManager languageManager;
    private ArenaRegistry arenaRegistry;
    private KitManager kitManager;
    private ArenaManager arenaManager;
    private CooldownManager cooldownManager;
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
        Locale.setDefault(Locale.ENGLISH);

        this.setupConfigurationFiles();

        configOptions = new ConfigOptions(this);
        chatManager = new ChatManager(this);
        languageManager = new LanguageManager(this);
        userManager = new UserManager(this);
        commandFramework = new CommandFramework(this);
        cuboidSelector = new CuboidSelector(this);
        rewardsFactory = new RewardsFactory(this);
        arenaRegistry = new ArenaRegistry(this);
        kitManager = new KitManager(this);
        arenaManager = new ArenaManager(this);
        cooldownManager = new CooldownManager(this);

        ScoreboardLib.setPluginInstance(this);
        User.cooldownHandlerTask();

        new Events(this);
        new ArenaEvents(this);

        new PlayerCommands(this);
        new AdminCommands(this);

        if (chatManager.isPapiEnabled()) {
            new PlaceholderManager(this);
        }

        Metrics metrics = new Metrics(this, 7938);
        metrics.addCustomChart(new SimplePie("locale_used", () -> languageManager.getCurrentLocale().getPrefix()));
        metrics.addCustomChart(new SimplePie("database_enabled", () -> configOptions.isEnabled(Option.DATABASE_ENABLED) ? "Enabled" : "Disabled"));
        metrics.addCustomChart(new SimplePie("update_notifier", () -> configOptions.isEnabled(Option.UPDATE_NOTIFIER_ENABLED) ? "Enabled" : "Disabled"));

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

        Stream.of("arenas", "rewards", "stats", "mysql", "messages", "kits").filter(name -> !new File(getDataFolder(), name + ".yml").exists()).forEach(name -> saveResource(name + ".yml", false));
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

    @NotNull
    public CooldownManager getCooldownManager() {
        return cooldownManager;
    }

    public ConfigOptions getConfigOptions() {
        return configOptions;
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
        for (User user : userManager.getUsers()) {
            if (userManager.getDatabase() instanceof MysqlManager mysqlManager) {
                StringBuilder update = new StringBuilder(" SET ");

                for (StatsStorage.StatisticType stat : StatsStorage.StatisticType.PERSISTENT_STATS) {
                    int value = user.getStat(stat);
                    String statName = stat.getName();

                    if (update.toString().equalsIgnoreCase(" SET ")) {
                        update.append(statName).append("=").append(value);
                    }

                    update.append(", ").append(statName).append("=").append(value);
                }

                String finalUpdate = update.toString();

                mysqlManager.getDatabase().executeUpdate("UPDATE %s%s WHERE UUID='%s';".formatted(mysqlManager.getTable(), finalUpdate, user.getUniqueId().toString()));
                continue;
            }

            userManager.getDatabase().saveStatistics(user);
        }

        userManager.getDatabase().shutdown();
    }
}
