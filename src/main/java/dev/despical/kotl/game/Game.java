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

package dev.despical.kotl.game;

import dev.despical.commons.miscellaneous.MiscUtils;
import dev.despical.commons.serializer.InventorySerializer;
import dev.despical.kotl.KOTL;
import dev.despical.kotl.arena.Arena;
import dev.despical.kotl.arena.options.ArenaKeys;
import dev.despical.kotl.bossbar.BossBarManager;
import dev.despical.kotl.option.BooleanOption;
import dev.despical.kotl.option.IntOption;
import dev.despical.kotl.scoreboard.ScoreboardManager;
import dev.despical.kotl.stats.Statistics;
import dev.despical.kotl.user.User;
import dev.despical.kotl.util.ItemUtils;
import dev.despical.kotl.util.Schedulers;
import dev.despical.kotl.util.Utils;
import dev.despical.kotl.util.Var;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Despical
 * <p>
 * Created at 05.06.2026
 */
@Getter
public class Game {

    private static final KOTL plugin = JavaPlugin.getPlugin(KOTL.class);

    private final Arena arena;
    private final Set<Player> players;
    private final BossBarManager bossBarManager;
    private final ScoreboardManager scoreboardManager;

    public Game(Arena arena) {
        this.arena = arena;
        this.players = new HashSet<>();
        this.bossBarManager = new BossBarManager(this);
        this.scoreboardManager = new ScoreboardManager(this);
    }

    public void broadcastRawComponent(Component message, Var... variables) {
        players.forEach(player -> plugin.getChatManager().sendRawComponent(player, message, variables));
    }

    public void addPlayer(Player player) {
        players.add(player);

        InventorySerializer.saveInventoryToFile(plugin, player);

        player.setHealth(20);

        if (arena.getOption(ArenaKeys.ARENA_SCOREBOARD_ENABLED)) {
            Schedulers.runInTheNextTick(() -> getScoreboardManager().createScoreboard(player));
        }

        if (BooleanOption.CLEAR_INVENTORY_ON_JOIN.value()) {
            player.getInventory().clear();
        }

        if (BooleanOption.CLEAR_EFFECTS_ON_JOIN.value()) {
            Schedulers.runInTheNextTick(() -> player.getActivePotionEffects().forEach(effect -> player.removePotionEffect(effect.getType())));
        }

        Schedulers.runInTheNextTick(() -> {
            GameMode gamemode = arena.getOption(ArenaKeys.ARENA_GAMEMODE);

            if (gamemode != null) {
                player.setGameMode(gamemode);
            }

            player.setFoodLevel(20);
        });

        bossBarManager.addPlayer(player);

        User user = plugin.getUserManager().getUser(player);

        if (BooleanOption.REMOVE_COOLDOWN_ON_JOIN.value()) {
            plugin.getCooldownManager().setCooldown(user, (BooleanOption.SEPARATE_COOLDOWNS.value() ? arena.getId() : "") + "king", 0);
        }

        plugin.getEventManager().playerEnterArena(player, this);
    }

    public void removePlayer(Player player, boolean quit) {
        if (player == null) return;

        User user = plugin.getUserManager().getUser(player);

        players.remove(player);

        if (BooleanOption.CLEAR_INVENTORY_ON_JOIN.value()) {
            player.getInventory().clear();
        }

        if (BooleanOption.CLEAR_EFFECTS_ON_JOIN.value()) {
            Schedulers.runInTheNextTick(() -> player.getActivePotionEffects().forEach(effect -> player.removePotionEffect(effect.getType())));
        }

        if (!quit) {
            Schedulers.runInTheNextTick(() -> InventorySerializer.loadInventory(plugin, player));
        }

        if (arena.getOption(ArenaKeys.ARENA_SCOREBOARD_ENABLED)) {
            scoreboardManager.removeScoreboard(player);
        }

        if (BooleanOption.REMOVE_COOLDOWN_ON_LEAVE.value()) {
            plugin.getCooldownManager().setCooldown(user, (BooleanOption.SEPARATE_COOLDOWNS.value() ? arena.getId() : "") + "king", 0);
        }

        bossBarManager.removePlayer(player);

        plugin.getDatabase().saveData(user);
        plugin.getEventManager().playerLeaveArena(player, this);
    }

    public void becomeKing(Player player) {
        int size = players.size();
        String currentKing = arena.getOption(ArenaKeys.KING);
        boolean isSameKing = currentKing != null && currentKing.equals(player.getName());

        if (isSameKing && size == 1 && !BooleanOption.BECOME_KING_IN_A_ROW.value()) return;

        int cooldown = IntOption.COOLDOWN.value();
        String cooldownName = (BooleanOption.SEPARATE_COOLDOWNS.value() ? arena.getId() : "") + "king";
        User user = plugin.getUserManager().getUser(player);

        if (plugin.getCooldownManager().getCooldown(user, cooldownName) > 0 || user.getStatistic(Statistics.LOCAL_RESET_COOLDOWN) == 1) {
            return;
        }

        cooldown_perm_check:
        if (size > 1 || (size == 1 && BooleanOption.COOLDOWN_WHEN_ALONE.value())) {
            String permission = plugin.getConfig().getString("King-Settings.Cooldown-Override-Perm", "");

            if (!permission.isEmpty() && player.hasPermission(permission)) {
                break cooldown_perm_check;
            }

            if (BooleanOption.APPLY_KING_DELAY_BAR.value()) {
                Utils.applyActionBarCooldown(user, cooldown);
            }

            plugin.getCooldownManager().setCooldown(user, cooldownName, cooldown);
        }

        plugin.getEventManager().playerBecomeKing(player, this);

        arena.setOption(ArenaKeys.KING, player.getName());

        if (BooleanOption.RESET_COOLDOWNS_ON_NEW_KING.value()) {
            Set<Player> targets = new HashSet<>(players);
            targets.remove(player);

            targets.stream().map(plugin.getUserManager()::getUser).forEach(pUser -> pUser.setStatistic(Statistics.LOCAL_RESET_COOLDOWN, 1));
        }

        user.addStat(Statistics.SCORE, 1);
        user.addStat(Statistics.TOURS_PLAYED, 1);
        user.addArenaScore(arena.getId(), 1);
        updateTopKing(user.getName(), user.getArenaScore(arena.getId()));

        Set<Player> targets = getPlayers();
        targets.remove(player);

        spawnFireworks(player);

        for (Player p : targets) {
            User loser = plugin.getUserManager().getUser(p);
            loser.addStat(Statistics.TOURS_PLAYED, 1);

            spawnFireworks(p);
        }

        getScoreboardManager().updateAllScoreboards();
    }

    public void updateTopKing(String playerName, int score) {
        arena.setOption(ArenaKeys.LAST_KING, playerName);

        int topKingScore = arena.getOption(ArenaKeys.TOP_KING_SCORE);
        if (score > topKingScore) {
            arena.setOption(ArenaKeys.TOP_KING, playerName);
            arena.setOption(ArenaKeys.TOP_KING_SCORE, score);
        }
    }

    public void restoreAfterRespawn(User user) {
        Player player = user.getPlayer();
        if (player == null || !player.isOnline()) return;

        PlayerInventory inventory = player.getInventory();
        inventory.clear();
        inventory.setArmorContents(ItemUtils.EMPTY_ARMORS);

        Utils.restoreSavedPlayerState(player);

        player.teleport(arena.getOption(ArenaKeys.END_LOCATION));

        scoreboardManager.removeScoreboard(player);
        bossBarManager.removePlayer(player);
    }

    private void spawnFireworks(Player player) {
        if (!BooleanOption.FIREWORKS_ON_NEW_KING.value()) return;

        new BukkitRunnable() {

            private int i = 0;

            public void run() {
                if (i == 2 || !players.contains(player)) {
                    cancel();
                }

                MiscUtils.spawnRandomFirework(player.getLocation());
                i++;
            }
        }.runTaskTimer(plugin, 10, 20);
    }

    public Set<Player> getPlayers() {
        return new HashSet<>(players);
    }
}
