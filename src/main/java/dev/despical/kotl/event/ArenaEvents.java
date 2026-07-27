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
import dev.despical.kotl.option.BooleanOption;
import dev.despical.kotl.stats.Statistics;
import dev.despical.kotl.user.User;
import dev.despical.kotl.util.Schedulers;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.util.Vector;

/**
 * @author Despical
 * <p>
 * Created at 22.06.2020
 */
public final class ArenaEvents extends ListenerAdapter {

    @EventHandler
    public void onInteractWithPlate(PlayerInteractEvent event) {
        if (event.getAction() != Action.PHYSICAL) return;

        Player player = event.getPlayer();
        Arena arena = arenaRegistry.getArena(player);

        if (arena == null) {
            return;
        }

        Block block = event.getClickedBlock();
        if (block == null) return;

        if (block.getLocation().equals(arena.getOption(ArenaKeys.PLATE_LOCATION))
            && arena.getGame().becomeKing(player)) {
            knockPlayerFromPlate(player, block.getLocation());
        }
    }

    private void knockPlayerFromPlate(Player player, Location plateLocation) {
        if (!BooleanOption.KING_PLATE_KNOCKBACK_ENABLED.value()) {
            return;
        }

        Location plateCenter = plateLocation.clone().add(0.5, 0, 0.5);
        Vector direction = player.getLocation().toVector().subtract(plateCenter.toVector()).setY(0);

        if (direction.lengthSquared() < 0.01) {
            direction = player.getLocation().getDirection().setY(0).multiply(-1);
        }

        if (direction.lengthSquared() < 0.01) {
            direction = new Vector(1, 0, 0);
        }

        double horizontalStrength = plugin.getConfig().getDouble("king-settings.plate-knockback.horizontal-strength", 1.25);
        double verticalStrength = plugin.getConfig().getDouble("king-settings.plate-knockback.vertical-strength", 0.45);

        direction.normalize().multiply(horizontalStrength).setY(verticalStrength);
        player.setVelocity(direction);
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        Player deadPlayer = event.getEntity();
        Arena arena = arenaRegistry.getArena(deadPlayer);

        if (arena == null) return;

        event.getDrops().clear();
        event.setKeepLevel(true);
        event.setShowDeathMessages(false);
        event.setShouldDropExperience(false);

        Schedulers.runTaskLater(() -> deadPlayer.spigot().respawn(), 5);

        User user = userManager.getUser(deadPlayer);
        plugin.getCooldownManager().setCooldown(user, "death", 2);

        Player killer = deadPlayer.getKiller();
        boolean killerFound = killer != null;

        if (killerFound) {
            User killerUser = userManager.getUser(killer);
            killerUser.addStat(Statistics.KILL, 1);
        }
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        Arena arena = arenaRegistry.getArena(player);

        if (arena == null) return;

        plugin.getArenaManager().leaveAttempt(userManager.getUser(player));
        event.setRespawnLocation(arena.getOption(ArenaKeys.END_LOCATION));

        User user = userManager.getUser(player);
        user.addStat(Statistics.DEATH, 1);
    }
}
