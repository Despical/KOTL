package me.despical.kotl.arena;

import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.gmail.filoghost.holographicdisplays.api.Hologram;

import me.despical.commonsbox.serializer.InventorySerializer;
import me.despical.kotl.ConfigPreferences;
import me.despical.kotl.Main;

/**
 * @author Despical
 * <p>
 * Created at 20.06.2020
 */
public class Arena {
	
	private static final Main plugin = JavaPlugin.getPlugin(Main.class);
	private final String id;
	
	private Set<Player> players = new HashSet<>();
	private Map<GameLocation, Location> gameLocations = new EnumMap<>(GameLocation.class);
	
	private Player king;
	
	private Hologram hologram;
	
	private BossBar gameBar;

	private boolean ready = true;
	
	public Arena(String id) {
		this.id = id;
		if (plugin.getConfigPreferences().getOption(ConfigPreferences.Option.BOSSBAR_ENABLED)) {
			if (plugin.isBefore1_9_R1()) { /** Not implemented yet*/
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
	
	void addPlayer(Player player) {
		players.add(player);
		if (plugin.getConfigPreferences().getOption(ConfigPreferences.Option.INVENTORY_MANAGER_ENABLED)) {
			InventorySerializer.saveInventoryToFile(plugin, player);
		}
		player.getInventory().clear();
	}
	
	void removePlayer(Player player) {
		if (player == null) {
			return;
		}
		player.getInventory().clear();
		players.remove(player);
		if (plugin.getConfigPreferences().getOption(ConfigPreferences.Option.INVENTORY_MANAGER_ENABLED)) {
			InventorySerializer.loadInventory(plugin, player);
		}
	}
	
	public void teleportAllToEndLocation() {
		Location location = getEndLocation();

		if (location == null) {
			System.out.print("End location for arena " + getId() + " isn't intialized!");
			return;
		}
		for (Player player : getPlayers()) {
			player.teleport(location);
		}
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