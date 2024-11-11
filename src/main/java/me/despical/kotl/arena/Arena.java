/*
 * KOTL - Don't let others climb to top of the ladders!
 * Copyright (C) 2024  Berke Akçen
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

package me.despical.kotl.arena;

import me.despical.commons.compat.XMaterial;
import me.despical.commons.miscellaneous.AttributeUtils;
import me.despical.commons.reflection.XReflection;
import me.despical.commons.serializer.InventorySerializer;
import me.despical.kotl.ConfigPreferences;
import me.despical.kotl.Main;
import me.despical.kotl.api.events.player.KOTLPlayerEnterArenaEvent;
import me.despical.kotl.api.events.player.KOTLPlayerLeaveArenaEvent;
import me.despical.kotl.arena.managers.BossBarManager;
import me.despical.kotl.arena.managers.ScoreboardManager;
import me.despical.kotl.handlers.ChatManager;
import me.despical.kotl.handlers.rewards.Reward.RewardType;
import me.despical.kotl.user.User;
import me.despical.particle.ParticleBuilder;
import me.despical.particle.ParticleEffect;
import org.apache.commons.lang.math.IntRange;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Despical
 * <p>
 * Created at 20.06.2020
 */
public class Arena {

	private static final Main plugin = JavaPlugin.getPlugin(Main.class);

	private boolean ready, showOutlines;
	private BukkitTask particleScheduler;

	private final Set<Player> players;
	private final Map<GameLocation, Location> gameLocations;

	private String king;
	private XMaterial arenaPlate;
	private BossBarManager bossBarManager;

	private final String id;
	private final ScoreboardManager scoreboardManager;

	public Arena(String id) {
		this.id = id;
		this.players = new HashSet<>();
		this.gameLocations = new EnumMap<>(GameLocation.class);
		this.scoreboardManager = new ScoreboardManager(plugin, this);
		this.arenaPlate = XMaterial.OAK_PRESSURE_PLATE;

		if (plugin.getOption(ConfigPreferences.Option.BOSS_BAR_ENABLED)) {
			if (!XReflection.supports(9)) return;

			this.bossBarManager = new BossBarManager(plugin);
		}
	}

	public boolean isReady() {
		return ready;
	}

	public void setReady(boolean ready) {
		this.ready = ready;
	}

	public void setShowOutlines(boolean showOutlines) {
		this.showOutlines = showOutlines;

		if (showOutlines) {
			if (this.particleScheduler != null || getMinCorner() == null || getMaxCorner() == null) return;

			this.particleScheduler = new BukkitRunnable() {
				final Location min = getMinCorner(), max = getMaxCorner();
				final World world = min.getWorld();
				final ParticleEffect particle = ParticleEffect.valueOf(plugin.getConfig().getString("Arena-Outlines.Particle", "flame").toUpperCase());
				final double step = plugin.getConfig().getDouble("Arena-Outlines.Step", .4);
				final ParticleBuilder particleBuilder = new ParticleBuilder(particle);
				final Location location = new Location(world, 0, 0, 0);

				final double[]
					xArr = {Math.min(min.getX(), max.getX()), Math.max(min.getX(), max.getX())},
					yArr = {Math.min(min.getY(), max.getY()), Math.max(min.getY(), max.getY())},
					zArr = {Math.min(min.getZ(), max.getZ()), Math.max(min.getZ(), max.getZ())};

				@Override
				public void run() {
					for (double x = xArr[0]; x < xArr[1]; x += step) for (double y : yArr) for (double z : zArr) {
						particleBuilder.setLocation(setLocation(location, x, y, z)).display();
					}

					for (double y = yArr[0]; y < yArr[1]; y += step) for (double x : xArr) for (double z : zArr) {
						particleBuilder.setLocation(setLocation(location, x, y, z)).display();
					}

					for (double z = zArr[0]; z < zArr[1]; z += step) for (double y : yArr) for (double x : xArr) {
						particleBuilder.setLocation(setLocation(location, x, y, z)).display();
					}
				}
			}.runTaskTimer(plugin, 20, 1);
		} else if (this.particleScheduler != null) {
			this.particleScheduler.cancel();
			this.particleScheduler = null;
		}
	}

	private Location setLocation(Location location, double x, double y, double z) {
		location.setX(x);
		location.setY(y);
		location.setZ(z);
		return location;
	}

	public void handleOutlines() {
		if (this.particleScheduler != null) {
			this.particleScheduler.cancel();
			this.particleScheduler = null;
		}

		this.setShowOutlines(showOutlines);
	}

	public boolean isShowOutlines() {
		return showOutlines;
	}

	/**
	 * Get arena identifier used to get arenas by string.
	 *
	 * @return arena name
	 */
	public String getId() {
		return id;
	}

	/**
	 * Get all players in arena.
	 *
	 * @return set of players in arena
	 */
	public Set<Player> getPlayers() {
		return new HashSet<>(players);
	}

	/**
	  * Get end location of arena.
	  *
	  * @return end location of arena
	  */
	 public Location getEndLocation() {
	   return gameLocations.get(GameLocation.END);
	 }

	/**
	 * Set end location of arena.
	 *
	 * @param endLoc new end location of arena
	 */
	public void setEndLocation(Location endLoc) {
		gameLocations.put(GameLocation.END, endLoc);
	}

	/**
	 * Get arena's plate location.
	 *
	 * @return plate location of arena
	 */
	public Location getPlateLocation() {
		return gameLocations.get(GameLocation.PLATE);
	}

	/**
	 * Set plate location.
	 *
	 * @param plateLoc new plate location of arena
	 */
	public void setPlateLocation(Location plateLoc) {
		gameLocations.put(GameLocation.PLATE, plateLoc);
	}

	public Location getMinCorner() {
		return gameLocations.get(GameLocation.MIN);
	}

	public void setMinCorner(final Location minCorner) {
		gameLocations.put(GameLocation.MIN, minCorner);
	}

	public Location getMaxCorner() {
		return gameLocations.get(GameLocation.MAX);
	}

	public void setMaxCorner(final Location maxCorner) {
		gameLocations.put(GameLocation.MAX, maxCorner);
	}

	public Location getLocation(GameLocation gameLocation) {
		return gameLocations.get(gameLocation);
	}

	public void setKing(String king) {
		this.king = king;
	}

	@Nullable
	public String getKing() {
		return king;
	}

	@NotNull
	public String getKingName() {
		return king == null ? plugin.getChatManager().message("placeholders.there_is_no_king") : king;
	}

	public void setArenaPlate(XMaterial arenaPlate) {
		this.arenaPlate = arenaPlate;
	}

	public XMaterial getArenaPlate() {
		return arenaPlate;
	}

	@Nullable
	public Arena isInArea(final Player player) {
		if (!ready) return null;

		final Location min = getMinCorner(), max = getMaxCorner(), origin = player.getLocation();

		if (!min.getWorld().equals(player.getWorld())) return null;

		if (new IntRange(min.getX(), max.getX()).containsDouble(origin.getX())
			&& new IntRange(min.getY(), max.getY()).containsDouble(origin.getY())
			&& new IntRange(min.getZ(), max.getZ()).containsDouble(origin.getZ())) {

			return this;
		}

		return null;
	}

	/**
	 * Get arena's scoreboard manager
	 *
	 * @return scoreboard manager of arena
	 */
	public ScoreboardManager getScoreboardManager() {
		return scoreboardManager;
	}

	public void broadcastMessage(String message) {
		if (message.isEmpty() || message.equals(plugin.getChatManager().getPrefix())) return;

		for (var player : players) player.sendMessage(message);
	}

	public void addPlayer(Player player) {
		players.add(player);

		AttributeUtils.setAttackCooldown(player, plugin.getConfig().getDouble("Hit-Cooldown-Delay", 4));

		final var user = plugin.getUserManager().getUser(player);

		if (plugin.getOption(ConfigPreferences.Option.INVENTORY_MANAGER_ENABLED)) {
			InventorySerializer.saveInventoryToFile(plugin, player);
		}

		if (plugin.getOption(ConfigPreferences.Option.HEAL_PLAYER)) {
			AttributeUtils.healPlayer(player);
		}

		if (plugin.getOption(ConfigPreferences.Option.SCOREBOARD_ENABLED)) {
			user.cacheScoreboard();

			plugin.getServer().getScheduler().runTask(plugin, () -> scoreboardManager.createScoreboard(player));
		}

		if (plugin.getOption(ConfigPreferences.Option.CLEAR_INVENTORY)) {
			player.getInventory().clear();
		}

		if (plugin.getOption(ConfigPreferences.Option.CLEAR_EFFECTS)) {
			plugin.getServer().getScheduler().runTask(plugin, () -> player.getActivePotionEffects().forEach(effect -> player.removePotionEffect(effect.getType())));
		}

		plugin.getServer().getScheduler().runTask(plugin, () -> {
			if (plugin.getOption(ConfigPreferences.Option.UPDATE_GAME_MODE)) {
				player.setGameMode(GameMode.valueOf(plugin.getConfig().getString("Game-Mode", "SURVIVAL")));
			}

			if (plugin.getOption(ConfigPreferences.Option.UPDATE_HUNGER)) {
				player.setFoodLevel(20);
			}
		});

		doBarAction(player, 1);

		if (plugin.getOption(ConfigPreferences.Option.JOIN_NOTIFY)) {
			plugin.getChatManager().broadcastAction(this, player, ChatManager.ActionType.JOIN);
		}

		if (plugin.getOption(ConfigPreferences.Option.REMOVE_COOLDOWN_ON_JOIN)) {
			plugin.getCooldownManager().setCooldown(user, (plugin.getOption(ConfigPreferences.Option.SEPARATE_COOLDOWNS) ? id : "") + "king", 0);
		}

		user.giveKit();
		user.performReward(RewardType.JOIN, this);

		plugin.getServer().getScheduler().runTask(plugin, () -> plugin.getServer().getPluginManager().callEvent(new KOTLPlayerEnterArenaEvent(this, player)));
	}

	public void removePlayer(Player player) {
		this.removePlayer(player, false);
	}

	public void quitPlayer(Player player) {
		this.removePlayer(player, true);
	}

	private void removePlayer(Player player, boolean quit) {
		if (player == null) return;

		User user = plugin.getUserManager().getUser(player);
		user.performReward(RewardType.LEAVE, this);

		players.remove(player);

		if (plugin.getOption(ConfigPreferences.Option.CLEAR_INVENTORY)) {
			player.getInventory().clear();
		}

		if (plugin.getOption(ConfigPreferences.Option.CLEAR_EFFECTS)) {
			plugin.getServer().getScheduler().runTask(plugin, () -> player.getActivePotionEffects().forEach(effect -> player.removePotionEffect(effect.getType())));
		}

		if (plugin.getOption(ConfigPreferences.Option.INVENTORY_MANAGER_ENABLED) && !quit) {
			plugin.getServer().getScheduler().runTask(plugin, () -> InventorySerializer.loadInventory(plugin, player));
		}

		if (plugin.getOption(ConfigPreferences.Option.SCOREBOARD_ENABLED)) {
			scoreboardManager.removeScoreboard(player);

			user.removeScoreboard();
		}

		if (plugin.getOption(ConfigPreferences.Option.LEAVE_NOTIFY) && user.getCooldown("death") == 0) {
			plugin.getChatManager().broadcastAction(this, player, ChatManager.ActionType.LEAVE);
		}

		if (plugin.getOption(ConfigPreferences.Option.REMOVE_COOLDOWN_ON_LEAVE)) {
			plugin.getCooldownManager().setCooldown(user, (plugin.getOption(ConfigPreferences.Option.SEPARATE_COOLDOWNS) ? id : "") + "king", 0);
		}

		doBarAction(player, 0);

		plugin.getUserManager().getDatabase().saveStatistics(user);

		AttributeUtils.resetAttackCooldown(player);

		plugin.getServer().getScheduler().runTask(plugin, () -> plugin.getServer().getPluginManager().callEvent(new KOTLPlayerLeaveArenaEvent(this, player)));
	}

	public void teleportToEndLocation(Player player) {
		final Location location = getEndLocation();

		if (location == null) {
			plugin.getLogger().warning("Couldn't teleport " + player.getName() + " to end location!");
			return;
		}

		player.teleport(location);
	}

	public void teleportAllToEndLocation() {
		players.forEach(this::teleportToEndLocation);
	}

	public void doBarAction(Player player, int action) {
		if (bossBarManager == null) return;

		if (action == 1) {
			bossBarManager.addPlayer(player);
		} else {
			bossBarManager.removePlayer(player);
		}
	}

	public enum GameLocation {
		MIN, MAX, END, PLATE;
	}
}