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

package dev.despical.kotl.rewards;

import dev.despical.commons.configuration.ConfigUtils;
import dev.despical.commons.util.Strings;
import dev.despical.kotl.KOTL;
import dev.despical.kotl.game.Game;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Loads and executes commands for reward triggers.
 *
 * @author Despical
 * <p>
 * Created at 21.08.2026
 */
public final class RewardManager {

    private volatile RewardConfiguration configuration;
    private final KOTL plugin;

    public RewardManager(KOTL plugin) {
        this.plugin = plugin;
        this.reload();
    }

    public void reload() {
        FileConfiguration config = ConfigUtils.getConfig(plugin, "rewards");

        if (!config.getBoolean("enabled", true)) {
            configuration = new RewardConfiguration(false, Map.of());
            return;
        }

        EnumMap<RewardType, List<String>> commands = new EnumMap<>(RewardType.class);

        for (RewardType type : RewardType.values()) {
            commands.put(type, List.copyOf(config.getStringList(type.getConfigurationPath())));
        }

        configuration = new RewardConfiguration(true, Map.copyOf(commands));
    }

    /**
     * Executes the commands configured for a reward type.
     *
     * @param type reward trigger
     * @param player player receiving the reward
     * @param game active game containing the player and statistics
     */
    public void dispatch(RewardType type, Player player, Game game) {
        dispatch(type, player, game, Map.of());
    }

    /**
     * Executes configured commands with additional trigger placeholders.
     *
     * @param type reward trigger
     * @param player player receiving the reward
     * @param game active game containing the player and statistics
     * @param triggerPlaceholders placeholders specific to this trigger
     */
    public void dispatch(RewardType type, Player player, Game game, Map<String, String> triggerPlaceholders) {
        RewardConfiguration snapshot = configuration;
        List<String> commands = snapshot.commands().getOrDefault(type, List.of());

        if (!snapshot.enabled() || commands.isEmpty()) {
            return;
        }

        Map<String, String> placeholders = Map.copyOf(triggerPlaceholders);
        Runnable execution = () -> {
            RewardContext context = RewardContext.from(plugin, player, game, placeholders);

            if (context != null) {
                commands.forEach(command -> execute(command, context));
            }
        };

        if (Bukkit.isPrimaryThread()) {
            execution.run();
        } else {
            Bukkit.getScheduler().runTask(plugin, execution);
        }
    }

    private void execute(String configuredCommand, RewardContext context) {
        String command = configuredCommand.trim();
        if (command.isEmpty()) {
            return;
        }

        boolean playerCommand = command.regionMatches(true, 0, "p:", 0, 2);
        if (playerCommand) {
            command = command.substring(2).trim();
        }

        command = Strings.format(context.format(command));
        if (command.startsWith("/")) {
            command = command.substring(1);
        }

        if (command.isBlank()) {
            return;
        }

        if (playerCommand) {
            context.player().performCommand(command);
        } else {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
        }
    }

    private record RewardConfiguration(boolean enabled, Map<RewardType, List<String>> commands) {
    }
}
