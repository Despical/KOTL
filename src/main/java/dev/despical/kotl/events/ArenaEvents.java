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

package dev.despical.kotl.events;

import dev.despical.commons.miscellaneous.MiscUtils;
import dev.despical.kotl.KOTL;
import dev.despical.kotl.api.StatisticType;
import dev.despical.kotl.api.events.arena.KOTLNewKingEvent;
import dev.despical.kotl.arena.Arena;
import dev.despical.kotl.handlers.ChatManager.ActionType;
import dev.despical.kotl.handlers.rewards.Reward.RewardType;
import dev.despical.kotl.options.ConfigOptions;
import dev.despical.kotl.options.Option;
import dev.despical.kotl.user.User;
import dev.despical.kotl.util.Utils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Despical
 * <p>
 * Created at 22.06.2020
 */
public final class ArenaEvents extends EventListener {

    public ArenaEvents(KOTL plugin) {
        super(plugin);
    }

    @EventHandler
    public void onInteractWithPlate(PlayerInteractEvent event) {
        if (event.getAction() != Action.PHYSICAL) return;

        Player player = event.getPlayer();
        Arena arena = plugin.getArenaRegistry().getArena(player);

        if (arena == null) return;

        if (event.getClickedBlock().getType() == arena.getArenaPlate().parseMaterial()) {
            ConfigOptions options = plugin.getConfigOptions();
            int size = arena.getPlayers().size();
            boolean isSameKing = arena.getKing() != null && arena.getKing().equals(player.getName());

            if (isSameKing && size == 1 && !options.isEnabled(Option.BECOME_KING_IN_A_ROW)) return;

            int cooldown = options.getValue(Option.COOLDOWN);
            String cooldownName = (options.isEnabled(Option.SEPARATE_COOLDOWNS) ? arena.getId() : "") + "king";
            User user = plugin.getUserManager().getUser(player);

            if (plugin.getCooldownManager().getCooldown(user, cooldownName) > 0 || user.get((options.isEnabled(Option.SEPARATE_COOLDOWNS) ? arena.getId() : "") + "local_cooldown")) {
                return;
            }

            cooldown_perm_check:
            if (size > 1 || (size == 1 && options.isEnabled(Option.COOLDOWN_WHEN_ALONE))) {
                String permission = plugin.getConfig().getString("King-Settings.Cooldown-Override-Perm", "");

                if (!permission.isEmpty() && player.hasPermission(permission)) {
                    break cooldown_perm_check;
                }

                if (options.isEnabled(Option.APPLY_KING_DELAY_BAR)) {
                    Utils.applyActionBarCooldown(user, cooldown);
                }

                plugin.getCooldownManager().setCooldown(user, cooldownName, cooldown);
            }

            plugin.callEvent(new KOTLNewKingEvent(arena, player, isSameKing));

            arena.setKing(player.getName());

            if (options.isEnabled(Option.RESET_COOLDOWNS_ON_NEW_KING)) {
                var players = new HashSet<>(arena.getPlayers());
                players.remove(player);

                players.stream().map(plugin.getUserManager()::getUser).forEach(pUser -> pUser.setStat(StatisticType.LOCAL_RESET_COOLDOWN, 1));
            }

            chatManager.broadcastAction(arena, player, ActionType.NEW_KING);

            user.addStat(StatisticType.SCORE, 1);
            user.addStat(StatisticType.TOURS_PLAYED, 1);
            user.performReward(RewardType.WIN, arena);

            Set<Player> players = arena.getPlayers();
            players.remove(player);

            spawnFireworks(arena, player);

            for (Player p : players) {
                User u = plugin.getUserManager().getUser(p);
                u.addStat(StatisticType.TOURS_PLAYED, 1);
                u.performReward(RewardType.LOSE, arena);

                spawnFireworks(arena, p);
            }
        }
    }

    @EventHandler
    public void onInteractWithDeathBlocks(PlayerInteractEvent event) {
        var player = event.getPlayer();

        if (!plugin.getConfigOptions().isEnabled(Option.DEATH_BLOCKS_ENABLED)) {
            return;
        }

        User user = plugin.getUserManager().getUser(player);
        Arena arena = user.getArena();

        if (arena == null) return;

        if (event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.LEFT_CLICK_BLOCK) {
            for (String material : plugin.getConfig().getStringList("Death-Blocks.Blacklisted-Blocks")) {
                if (event.getClickedBlock().getType() == Material.valueOf(material.toUpperCase())) {
                    arena.doBarAction(player, 0);
                    arena.broadcastMessage(chatManager.prefixedMessage("in_game.clicked_death_block").replace("%player%", player.getName()));
                    arena.removePlayer(player);
                    arena.teleportToEndLocation(player);

                    user.performReward(RewardType.LOSE, arena);
                    return;
                }
            }
        }
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player entity && event.getDamager() instanceof Player damager)) {
            return;
        }

        if (plugin.getArenaRegistry().isInArena(entity) && plugin.getArenaRegistry().isInArena(damager)) {
            if (!plugin.getConfigOptions().isEnabled(Option.DAMAGE_ENABLED)) {
                event.setCancelled(false);
                event.setDamage(0d);
            }
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        Player deadPlayer = event.getEntity();
        Arena arena = plugin.getArenaRegistry().getArena(deadPlayer);

        if (arena == null) return;

        event.getDrops().clear();
        event.setDroppedExp(0);
        event.setKeepLevel(true);
        event.setDeathMessage("");

        plugin.getServer().getScheduler().runTaskLater(plugin, () -> deadPlayer.spigot().respawn(), 5);
        plugin.getUserManager().getUser(deadPlayer).setCooldown("death", 2);

        Player killer = deadPlayer.getKiller();
        boolean killerFound = killer != null;

        arena.broadcastMessage(chatManager.prefixedMessage("in_game." + (killerFound ? "killed_player" : "kill_command")).replace("%player%", killerFound ? killer.getName() : "").replace("%victim%", deadPlayer.getName()));

        if (killerFound) {
            User killerUser = plugin.getUserManager().getUser(killer);
            killerUser.addStat(StatisticType.KILLS, 1);
            killerUser.performReward(RewardType.KILL, arena);
        }
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        Arena arena = plugin.getArenaRegistry().getArena(player);

        if (arena == null) return;

        arena.removePlayer(player);
        event.setRespawnLocation(arena.getEndLocation());

        User user = plugin.getUserManager().getUser(player);
        user.performReward(RewardType.DEATH, arena);
        user.addStat(StatisticType.DEATHS, 1);
    }

    private void spawnFireworks(Arena arena, Player player) {
        if (!plugin.getConfigOptions().isEnabled(Option.FIREWORKS_ON_NEW_KING)) return;

        new BukkitRunnable() {

            private int i = 0;

            public void run() {
                if (i == 2 || !arena.getPlayers().contains(player)) {
                    cancel();
                }

                MiscUtils.spawnRandomFirework(player.getLocation());
                i++;
            }
        }.runTaskTimer(plugin, 10, 20);
    }
}
