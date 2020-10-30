/*
 * KOTL - Don't let others to climb top of the ladders!
 * Copyright (C) 2020 Despical
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package me.despical.kotl.arena;

import me.despical.commonsbox.miscellaneous.AttributeUtils;
import me.despical.commonsbox.serializer.InventorySerializer;
import me.despical.kotl.ConfigPreferences;
import me.despical.kotl.Main;
import me.despical.kotl.arena.managers.ScoreboardManager;
import me.despical.kotl.handlers.hologram.Hologram;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
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
	
	private final Main plugin = JavaPlugin.getPlugin(Main.class);
	private final String id;
	
	private final Set<Player> players = new HashSet<>();
	private final Map<GameLocation, Location> gameLocations = new EnumMap<>(GameLocation.class);
	
	private Player king;
	private Hologram hologram;
	private BossBar gameBar;
	private final ScoreboardManager scoreboardManager;
	private boolean ready = true;

	public Arena(String id) {
		this.id = id;

		scoreboardManager = new ScoreboardManager(this);

		if (plugin.getConfigPreferences().getOption(ConfigPreferences.Option.BOSSBAR_ENABLED)) {
			if (plugin.isBefore1_9_R1()) {
				return;
			}

			gameBar = Bukkit.createBossBar(plugin.getChatManager().colorMessage("Bossbar.Game-Info"), BarColor.BLUE, BarStyle.SOLID);
		}
	}
	
	public boolean isReady() {
		return ready;
	}

	public void setReady(boolean ready) {
		this.ready = ready;
	}
	
	/**
	 * Get arena identifier used to get arenas by string.
	 *
	 * @return arena name
	 * @see ArenaRegistry#getArena(String)
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
		return players;
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
	 * Get last king hologram's location of arena.
	 * 
	 * @return hologram location of last king
	 */
	public Location getHologramLocation() {
		return gameLocations.get(GameLocation.HOLOGRAM);
	}
	
	/**
	 * Set last king's hologram location.
	 * 
	 * @param hologramLoc new hologram location of arena
	 */
	public void setHologramLocation(Location hologramLoc) {
		gameLocations.put(GameLocation.HOLOGRAM, hologramLoc);
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
	
	/**
	 * Set new king of arena.
	 * 
	 * @param player new king of arena
	 */
	public void setKing(Player player) {
		this.king = player;
	}
	
	/**
	 * 
	 * @return null if king is not online
	 */
	@Nullable
	public Player getKing() {
		return king;
	}
	
	/**
	 * Set hologram of last king.
	 *
	 * @param hologram last king's hologram
	 */
	public void setHologram(Hologram hologram) {
		this.hologram = hologram;
	}

	/**
	 * Get last king's hologram.
	 *
	 * @return last king's hologram
	 */
	public Hologram getHologram() {
		return hologram;
	}
	
	/**
	 * Get arena's scoreboard manager
	 * 
	 * @return scoreboard manager of arena
	 */
	public ScoreboardManager getScoreboardManager() {
		return scoreboardManager;
	}
	
	void addPlayer(Player player) {
		players.add(player);

		AttributeUtils.setAttackCooldown(player, plugin.getConfig().getDouble("Hit-Cooldown-Delay", 4));

		if (plugin.getConfigPreferences().getOption(ConfigPreferences.Option.INVENTORY_MANAGER_ENABLED)) {
			InventorySerializer.saveInventoryToFile(plugin, player);
		}

		if (plugin.getConfigPreferences().getOption(ConfigPreferences.Option.SCOREBOARD_ENABLED)) {
			scoreboardManager.createScoreboard(plugin.getUserManager().getUser(player));
		}

		if (plugin.getConfigPreferences().getOption(ConfigPreferences.Option.CLEAR_INVENTORY)) {
			player.getInventory().clear();
		}

		if (plugin.getConfigPreferences().getOption(ConfigPreferences.Option.CLEAR_EFFECTS)) {
			player.getActivePotionEffects().forEach(effect -> player.removePotionEffect(effect.getType()));
		}
	}
	
	void removePlayer(Player player) {
		if (player == null) {
			return;
		}

		players.remove(player);

		if (plugin.getConfigPreferences().getOption(ConfigPreferences.Option.CLEAR_INVENTORY)) {
			player.getInventory().clear();
		}

		if (plugin.getConfigPreferences().getOption(ConfigPreferences.Option.INVENTORY_MANAGER_ENABLED)) {
			InventorySerializer.loadInventory(plugin, player);
		}

		if (plugin.getConfigPreferences().getOption(ConfigPreferences.Option.SCOREBOARD_ENABLED)) {
			scoreboardManager.removeScoreboard(plugin.getUserManager().getUser(player));
			plugin.getUserManager().getUser(player).removeScoreboard();
		}

		AttributeUtils.resetAttackCooldown(player);
	}
	
	public void teleportAllToEndLocation() {
		Location location = getEndLocation();

		if (location == null) {
			System.out.print("End location for arena " + getId() + " isn't initialized!");
			return;
		}

		getPlayers().forEach(player -> player.teleport(location));
	}
	
	/**
	 * Executes boss bar action for arena
	 *
	 * @param action add or remove a player from boss bar
	 * @param p player
	 */
	public void doBarAction(BarAction action, Player p) {
		if (!plugin.getConfigPreferences().getOption(ConfigPreferences.Option.BOSSBAR_ENABLED)) {
			return;
		}

		if (plugin.isBefore1_9_R1()) {
			return;
		}

		switch (action) {
			case ADD:
				gameBar.addPlayer(p);
				break;
			case REMOVE:
				gameBar.removePlayer(p);
				break;
			default:
				break;
		}
	}

	public enum BarAction {
		ADD, REMOVE
	}
	
	public enum GameLocation {
		END, HOLOGRAM, PLATE
	}
}