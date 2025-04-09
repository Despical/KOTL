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

package me.despical.kotl.events;

import me.despical.commons.serializer.InventorySerializer;
import me.despical.kotl.KOTL;
import me.despical.kotl.arena.Arena;
import me.despical.kotl.handlers.ChatManager;
import me.despical.kotl.options.ConfigOptions;
import me.despical.kotl.options.Option;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * @author Despical
 * <p>
 * Created at 22.06.2020
 */
public non-sealed class Events extends EventListener {

    private final Map<UUID, Arena> teleportToEnd;

    public Events(KOTL plugin) {
        super(plugin);
        this.teleportToEnd = new HashMap<>();
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        plugin.getUserManager().addUser(player);

        Arena arena = teleportToEnd.get(player.getUniqueId());

        if (arena != null) {
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                arena.teleportToEndLocation(player);

                teleportToEnd.remove(player.getUniqueId());
            });
        }

        if (plugin.getConfigOptions().isEnabled(Option.INVENTORY_MANAGER_ENABLED)) {
            InventorySerializer.loadInventory(plugin, player);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        Arena arena = plugin.getArenaRegistry().getArena(player);

        if (arena != null) {
            chatManager.broadcastAction(arena, player, ChatManager.ActionType.LEAVE);

            arena.quitPlayer(player);

            teleportToEnd.put(player.getUniqueId(), arena);
        }

        plugin.getUserManager().removeUser(player);
    }

    @EventHandler
    public void onCommandExecute(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();

        if (!plugin.getArenaRegistry().isInArena(player)) {
            return;
        }

        if (!plugin.getConfigOptions().isEnabled(Option.BLOCK_COMMANDS)) {
            return;
        }

        String message = event.getMessage();

        if (plugin.getConfig().getStringList("Whitelisted-Commands").contains(message)) {
            return;
        }

        if (player.isOp() || player.hasPermission("kotl.command.override")) {
            return;
        }

        if (message.startsWith("/kotl") || message.startsWith("/kingoftheladder") || message.contains("top") || message.contains("stats")) {
            return;
        }

        event.setCancelled(true);
        player.sendMessage(chatManager.prefixedMessage("in_game.only_command_is_leave"));
    }

    @EventHandler
    public void onFallDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player victim)) return;
        if (!plugin.getArenaRegistry().isInArena(victim)) return;

        switch (event.getCause()) {
            case BLOCK_EXPLOSION -> event.setCancelled(true);
            case FALL -> {
                if (plugin.getConfigOptions().isEnabled(Option.DISABLE_FALL_DAMAGE)) {
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onFireworkDamage(EntityDamageByEntityEvent event) {
        if (!plugin.getConfigOptions().isEnabled(Option.FIREWORKS_ON_NEW_KING)) return;
        if (!(event.getEntity() instanceof Player player)) return;
        if (!plugin.getArenaRegistry().isInArena(player)) return;

        if (event.getDamager() instanceof Firework) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (!plugin.getArenaRegistry().isInArena(player)) return;

        if (!plugin.getConfigOptions().isEnabled(Option.UPDATE_HUNGER)) {
            event.setFoodLevel(20);
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();

        if (!plugin.getArenaRegistry().isInArena(player)) {
            return;
        }

        if (player.getGameMode() != GameMode.CREATIVE) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();

        if (!plugin.getArenaRegistry().isInArena(player)) {
            return;
        }

        if (player.getGameMode() != GameMode.CREATIVE) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {
        if (plugin.getArenaRegistry().isInArena(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPickUpItem(PlayerPickupItemEvent event) {
        if (!plugin.getArenaRegistry().isInArena(event.getPlayer())) {
            return;
        }

        if (!plugin.getConfigOptions().isEnabled(Option.PICK_UP_ITEMS)) {
            event.setCancelled(true);
            event.getItem().remove();
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onChatInGame(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        Arena arena = plugin.getArenaRegistry().getArena(player);
        ConfigOptions options = plugin.getConfigOptions();

        if (arena == null) {
            if (!options.isEnabled(Option.DISABLE_SEPARATE_CHAT)) {
                plugin.getArenaRegistry().getArenas().forEach(loopArena -> loopArena.getPlayers().forEach(p -> event.getRecipients().remove(p)));
            }

            return;
        }

        if (options.isEnabled(Option.CHAT_FORMAT_ENABLED)) {
            String message = formatChatPlaceholders(chatManager.message("in_game.chat_format"), player, event.getMessage().replaceAll(Pattern.quote("[$\\]"), ""));

            if (!options.isEnabled(Option.DISABLE_SEPARATE_CHAT)) {
                event.setCancelled(true);

                arena.broadcastMessage(message);

                plugin.getServer().getConsoleSender().sendMessage(message);
            } else {
                event.setMessage(message);
            }
        }
    }

    private String formatChatPlaceholders(String message, Player player, String saidMessage) {
        String formatted = message;

        formatted = formatted.replace("%player%", player.getName());
        formatted = formatted.replace("%message%", ChatColor.stripColor(saidMessage));
        formatted = chatManager.formatMessage(formatted, player);
        return chatManager.coloredRawMessage(formatted);
    }
}
