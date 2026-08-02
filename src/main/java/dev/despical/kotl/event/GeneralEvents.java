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

package dev.despical.kotl.event;

import dev.despical.kotl.arena.Arena;
import dev.despical.kotl.arena.options.ArenaKeys;
import dev.despical.kotl.game.Game;
import dev.despical.kotl.option.BooleanOption;
import dev.despical.kotl.scoreboard.ScoreboardManager;
import dev.despical.kotl.user.User;
import dev.despical.kotl.util.ItemUtils;
import dev.despical.kotl.util.Schedulers;
import dev.despical.kotl.util.Var;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.PlayerInventory;

import java.util.*;

/**
 * @author Despical
 * <p>
 * Created at 22.06.2020
 */
public class GeneralEvents extends ListenerAdapter {

    private final Map<UUID, Arena> quitPlayers = new HashMap<>();

    @EventHandler(priority = EventPriority.LOWEST)
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        ScoreboardManager.resetPlayerScoreboard(player);

        userManager.createNewUser(player);

        Arena arena = quitPlayers.remove(player.getUniqueId());
        if (!plugin.getPlayerInventoryManager().hasSnapshot(player)) {
            return;
        }

        Schedulers.runInTheNextTick(() -> {
            Optional.ofNullable(arena)
                .map(target -> target.getOption(ArenaKeys.END_LOCATION))
                .ifPresent(player::teleport);

            PlayerInventory inventory = player.getInventory();
            inventory.clear();
            inventory.setArmorContents(ItemUtils.EMPTY_ARMORS);

            plugin.getPlayerInventoryManager().restore(player);
        });
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        ScoreboardManager.resetPlayerScoreboard(player);

        arenaRegistry.getArenas().stream()
            .map(Arena::getGame)
            .filter(Objects::nonNull)
            .forEach(game -> game.getScoreboardManager().removeScoreboard(player));

        User user = userManager.getUser(player);
        UUID uuid = user.getUUID();

        Arena arena = user.getArena();
        if (arena != null) {
            quitPlayers.put(uuid, arena);
            arenaManager.quitPlayer(user, arena);
        } else {
            plugin.getDatabase().saveData(user);
        }

        userManager.removeUser(user);

        plugin.getStatsCacheManager().invalidate(uuid);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDeath(PlayerDeathEvent event) {
        if (!arenaRegistry.isInArena(event.getEntity())) {
            return;
        }

        event.getDrops().clear();
        event.setDroppedExp(0);
        event.setShowDeathMessages(false);
        event.setShouldDropExperience(false);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        User user = userManager.getUser(player);
        Game game = gameManager.getGame(user);

        if (game == null) {
            return;
        }

        Schedulers.runInTheNextTick(() -> game.restoreAfterRespawn(user));
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        Arena arena = arenaRegistry.getArena(player);
        Set<Player> recipients = event.getRecipients();

        boolean separateChat = BooleanOption.SEPARATE_CHAT.value();
        boolean blockOutsideChat = BooleanOption.BLOCK_OUTSIDE_CHAT.value();

        if (arena == null) {
            if (blockOutsideChat || separateChat) {
                arenaRegistry.getArenas().stream()
                    .map(Arena::getGame)
                    .flatMap(game -> game.getPlayers().stream())
                    .forEach(recipients::remove);
            }

            return;
        }

        boolean disableChatInGame = BooleanOption.DISABLE_CHAT_IN_GAME.value();
        if (disableChatInGame) {
            event.setCancelled(true);
            chatManager.sendMessage(player, "game.chat-disabled-in-game");
            return;
        }

        boolean enableFormatting = BooleanOption.ENABLE_CHAT_FORMATTING.value();

        if (separateChat) {
            Set<Player> arenaPlayers = arena.getGame().getPlayers();
            recipients.removeIf(recipient -> !arenaPlayers.contains(recipient));
        }

        if (!enableFormatting) {
            return;
        }

        event.setCancelled(true);

        Component formattedMessage = chatManager.getMessageComponent(
            "chat-format",
            Var.of("%sender%", player.getName()),
            Var.of("%message%", event.getMessage())
        );

        recipients.forEach(recipient -> chatManager.sendRawComponent(recipient, formattedMessage));
        plugin.getServer().getConsoleSender().sendMessage(formattedMessage);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player damagedPlayer)) {
            return;
        }

        Player attackingPlayer = getAttackingPlayer(event);
        Arena damagedArena = arenaRegistry.getArena(damagedPlayer);
        Arena attackingArena = attackingPlayer == null ? null : arenaRegistry.getArena(attackingPlayer);

        if (damagedArena == null && attackingArena == null) {
            return;
        }

        boolean sameArena = damagedArena != null && damagedArena == attackingArena;
        if (sameArena) {
            event.setCancelled(false);
            event.setDamage(0d);
            return;
        }

        event.setCancelled(true);
    }

    private Player getAttackingPlayer(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player player) {
            return player;
        }

        if (event.getDamager() instanceof Projectile projectile && projectile.getShooter() instanceof Player player) {
            return player;
        }

        return null;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        cancelIfTaskIsNull(event);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        Arena arena = arenaRegistry.getArena(player);
        if (arena == null) {
            return;
        }

        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockBreak(BlockBreakEvent event) {
        if (arenaRegistry.isInArena(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (arenaRegistry.isInArena(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    private <T extends EntityEvent & Cancellable> void cancelIfTaskIsNull(T event) {
        if (!(event.getEntity() instanceof Player player)) return;

        Arena arena = arenaRegistry.getArena(player);
        if (arena == null) {
            return;
        }

        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onFallDamage(EntityDamageEvent event) {
        if (!BooleanOption.DISABLE_FALL_DAMAGE.value()) return;
        if (!(event.getEntity() instanceof Player player)) return;

        var cause = event.getCause();
        if (cause != EntityDamageEvent.DamageCause.FALL) return;

        Arena arena = arenaRegistry.getArena(player);
        if (arena == null) return;

        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDrop(PlayerDropItemEvent event) {
        Player player = event.getPlayer();

        Arena arena = arenaRegistry.getArena(player);
        if (arena == null) {
            return;
        }

        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onItemPickup(EntityPickupItemEvent event) {
        if (BooleanOption.PICK_UP_ITEMS.value()) return;
        if (!(event.getEntity() instanceof Player player)) return;

        Arena arena = arenaRegistry.getArena(player);
        if (arena == null) {
            return;
        }

        event.getItem().remove();
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onArrowPickup(PlayerPickupArrowEvent event) {
        if (BooleanOption.PICK_UP_ITEMS.value()) return;
        if (!arenaRegistry.isInArena(event.getPlayer())) return;

        event.getArrow().remove();
        event.setCancelled(true);
    }
}
