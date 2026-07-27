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
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

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

    public void broadcastMessage(String messagePath, Var... variables) {
        players.forEach(player -> plugin.getChatManager().sendMessage(player, messagePath, variables));
    }

    public boolean addPlayer(Player player) {
        if (players.contains(player)) {
            return false;
        }

        var enterEvent = plugin.getEventManager().playerEnterArena(player, this);
        if (enterEvent.isCancelled()) {
            return false;
        }

        players.add(player);
        if (BooleanOption.JOIN_NOTIFY.value()) {
            broadcastMessage("game.player-joined", Var.of("%player%", player.getName()));
        }

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

        if (IntOption.COOLDOWN.value() > 0
            && BooleanOption.APPLY_KING_DELAY_BAR.value()
            && BooleanOption.SHOW_COOLDOWN_ON_REJOIN.value()
            && plugin.getCooldownManager().getCooldown(user, Utils.kingCooldownName(arena.getId())) > 0) {
            Utils.applyActionBarCooldown(user, arena.getId(), IntOption.COOLDOWN.value());
        }

        return true;
    }

    public void removePlayer(Player player, boolean quit) {
        removePlayer(player, quit, false);
    }

    public void removePlayer(Player player, boolean quit, boolean restoreImmediately) {
        removePlayer(player, quit, restoreImmediately, true);
    }

    public void removePlayer(Player player, boolean quit, boolean restoreImmediately, boolean saveStats) {
        if (player == null) return;

        User user = plugin.getUserManager().getUser(player);

        players.remove(player);
        if (BooleanOption.LEAVE_NOTIFY.value()) {
            broadcastMessage("game.player-left", Var.of("%player%", player.getName()));
        }

        if (BooleanOption.CLEAR_INVENTORY_ON_JOIN.value()) {
            player.getInventory().clear();
        }

        if (BooleanOption.CLEAR_EFFECTS_ON_JOIN.value()) {
            Schedulers.runInTheNextTick(() -> player.getActivePotionEffects().forEach(effect -> player.removePotionEffect(effect.getType())));
        }

        if (!quit) {
            if (restoreImmediately) {
                InventorySerializer.loadInventory(plugin, player);
            } else {
                Schedulers.runInTheNextTick(() -> InventorySerializer.loadInventory(plugin, player));
            }
        }

        if (arena.getOption(ArenaKeys.ARENA_SCOREBOARD_ENABLED)) {
            scoreboardManager.removeScoreboard(player);
        }

        bossBarManager.removePlayer(player);

        if (saveStats) {
            plugin.getDatabase().saveData(user);
        }

        plugin.getEventManager().playerLeaveArena(player, this);
    }

    public boolean becomeKing(Player player) {
        int size = players.size();
        String currentKing = arena.getOption(ArenaKeys.KING);
        boolean isSameKing = currentKing != null && currentKing.equals(player.getName());

        if (isSameKing && !BooleanOption.BECOME_KING_IN_A_ROW.value()) {
            return false;
        }

        int cooldown = IntOption.COOLDOWN.value();
        String cooldownName = Utils.kingCooldownName(arena.getId());
        User user = plugin.getUserManager().getUser(player);
        boolean shouldApplyCooldown = cooldown > 0
            && (size > 1 || BooleanOption.COOLDOWN_WHEN_ALONE.value());
        String overridePermission = plugin.getConfig().getString("king-settings.cooldown-override-permission", "");
        boolean bypassCooldown = !overridePermission.isEmpty() && player.hasPermission(overridePermission);

        if (shouldApplyCooldown
            && !bypassCooldown
            && plugin.getCooldownManager().getCooldown(user, cooldownName) > 0) {
            return false;
        }

        if (shouldApplyCooldown && !bypassCooldown) {
            plugin.getCooldownManager().setCooldown(user, cooldownName, cooldown);
            if (BooleanOption.APPLY_KING_DELAY_BAR.value()) {
                Utils.applyActionBarCooldown(user, arena.getId(), cooldown);
            }
        }

        plugin.getEventManager().playerBecomeKing(player, this);

        arena.setOption(ArenaKeys.KING, player.getName());
        broadcastMessage(
            isSameKing ? "game.king-retained" : "game.new-king",
            Var.of("%king%", player.getName())
        );

        user.addStat(Statistics.SCORE, 1);
        user.addStat(Statistics.TOURS_PLAYED, 1);
        user.addArenaScore(arena.getId(), 1);
        updateTopKing(user.getName(), user.getArenaScore(arena.getId()));

        Set<Player> targets = getPlayers();
        targets.remove(player);

        spawnCelebrationFireworks();

        for (Player p : targets) {
            User loser = plugin.getUserManager().getUser(p);
            loser.addStat(Statistics.TOURS_PLAYED, 1);
        }

        getScoreboardManager().updateAllScoreboards();
        return true;
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

    private void spawnCelebrationFireworks() {
        if (!BooleanOption.FIREWORKS_ON_NEW_KING.value()) return;

        Location plate = arena.getOption(ArenaKeys.PLATE_LOCATION);
        if (plate != null && plate.getWorld() != null) {
            spawnFirework(findOpenLocationAbovePlate(plate));
        }

        for (int index = 1; index <= 6; index++) {
            long delay = index * 5L;
            Schedulers.runTaskLater(() -> spawnFirework(findRandomFireworkLocation()), delay);
        }
    }

    private Location findRandomFireworkLocation() {
        Location min = arena.getOption(ArenaKeys.MIN_CORNER);
        Location max = arena.getOption(ArenaKeys.MAX_CORNER);
        Location plate = arena.getOption(ArenaKeys.PLATE_LOCATION);

        if (min == null || max == null || min.getWorld() == null || !min.getWorld().equals(max.getWorld())) {
            return plate == null ? null : findOpenLocationAbovePlate(plate);
        }

        World world = min.getWorld();
        int minX = Math.min(min.getBlockX(), max.getBlockX());
        int maxX = Math.max(min.getBlockX(), max.getBlockX());
        int minY = Math.min(min.getBlockY(), max.getBlockY());
        int maxY = Math.max(min.getBlockY(), max.getBlockY());
        int minZ = Math.min(min.getBlockZ(), max.getBlockZ());
        int maxZ = Math.max(min.getBlockZ(), max.getBlockZ());
        ThreadLocalRandom random = ThreadLocalRandom.current();

        for (int attempt = 0; attempt < 12; attempt++) {
            Location candidate = new Location(
                world,
                random.nextInt(minX, maxX + 1) + 0.5,
                random.nextInt(minY, maxY + 1) + 0.5,
                random.nextInt(minZ, maxZ + 1) + 0.5
            );

            boolean awayFromPlayers = players.stream()
                .filter(player -> player.getWorld().equals(world))
                .allMatch(player -> player.getLocation().distanceSquared(candidate) >= 4);

            if (isOpenForFirework(candidate) && awayFromPlayers) {
                return candidate;
            }
        }

        return plate == null ? min.clone().add(0.5, 0.5, 0.5) : findOpenLocationAbovePlate(plate);
    }

    private Location findOpenLocationAbovePlate(Location plate) {
        Location candidate = plate.clone().add(0.5, 2.5, 0.5);

        for (int offset = 0; offset < 5; offset++) {
            if (isOpenForFirework(candidate)) {
                return candidate;
            }

            candidate.add(0, 1, 0);
        }

        return plate.clone().add(0.5, 2.5, 0.5);
    }

    private boolean isOpenForFirework(Location location) {
        return location.getBlock().isPassable()
            && location.clone().add(0, 1, 0).getBlock().isPassable();
    }

    private void spawnFirework(Location location) {
        if (location == null || location.getWorld() == null) {
            return;
        }

        ThreadLocalRandom random = ThreadLocalRandom.current();
        Firework firework = location.getWorld().spawn(location, Firework.class);
        FireworkMeta meta = firework.getFireworkMeta();
        FireworkEffect.Type[] types = FireworkEffect.Type.values();

        meta.addEffect(FireworkEffect.builder()
            .with(types[random.nextInt(types.length)])
            .withColor(Color.fromRGB(random.nextInt(256), random.nextInt(256), random.nextInt(256)))
            .withFade(Color.fromRGB(random.nextInt(256), random.nextInt(256), random.nextInt(256)))
            .flicker(random.nextBoolean())
            .trail(true)
            .build());
        meta.setPower(0);

        firework.setFireworkMeta(meta);
        firework.setVelocity(new org.bukkit.util.Vector(0, 0.6, 0));
        firework.setInvulnerable(true);
        firework.setPersistent(false);
    }

    public Set<Player> getPlayers() {
        return new HashSet<>(players);
    }
}
