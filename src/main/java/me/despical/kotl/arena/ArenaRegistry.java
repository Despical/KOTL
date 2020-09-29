package me.despical.kotl.arena;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import me.despical.kotl.HookManager;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;

import me.despical.commonsbox.compat.XMaterial;
import me.despical.commonsbox.configuration.ConfigUtils;
import me.despical.commonsbox.serializer.LocationSerializer;
import me.despical.kotl.Main;
import me.despical.kotl.utils.Debugger;

/**
 * @author Despical
 * <p>
 * Created at 22.06.2020
 */
public class ArenaRegistry {

	private static final Main plugin = JavaPlugin.getPlugin(Main.class);
	private static final List<Arena> arenas = new ArrayList<>();

	/**
	 * Checks if player is in any arena
	 *
	 * @param player player to check
	 * @return [b]true[/b] when player is in arena, [b]false[/b] if otherwise
	 */
	public static boolean isInArena(Player player) {
		for (Arena arena : arenas) {
			if (arena.getPlayers().contains(player)) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Returns arena where the player is
	 *
	 * @param p target player
	 * @return Arena or null if not playing
	 * @see #isInArena(Player) to check if player is playing
	 */
	public static Arena getArena(Player p) {
		if (p == null || !p.isOnline()) {
			return null;
		}

		for (Arena arena : arenas) {
			for (Player player : arena.getPlayers()) {
				if (player.getUniqueId().equals(p.getUniqueId())) {
					return arena;
				}
			}
		}

		return null;
	}

	/**
	 * Returns arena based by ID
	 *
	 * @param id name of arena
	 * @return Arena or null if not found
	 */
	public static Arena getArena(String id) {
		for (Arena loopArena : arenas) {
			if (loopArena.getId().equalsIgnoreCase(id)) {
				return loopArena;
			}
		}

		return null;
	}

	public static void registerArena(Arena arena) {
		Debugger.debug(Level.INFO, "Registering new game instance {0}", arena.getId());
		arenas.add(arena);
	}

	public static void unregisterArena(Arena arena) {
		Debugger.debug(Level.INFO, "Unregistering game instance {0}", arena.getId());
		arenas.remove(arena);
	}

	public static void registerArenas() {
		Debugger.debug(Level.INFO, "Initial arenas registration");
		FileConfiguration config = ConfigUtils.getConfig(plugin, "arenas");
		long start = System.currentTimeMillis();
		
		if (arenas.size() > 0) arenas.clear();

		if (!config.contains("instances")) {
			Bukkit.getConsoleSender().sendMessage(plugin.getChatManager().colorMessage("Validator.No-Instances-Created"));
			return;
		}

		ConfigurationSection section = config.getConfigurationSection("instances");

		if (section == null) {
			Bukkit.getConsoleSender().sendMessage(plugin.getChatManager().colorMessage("Validator.No-Instances-Created"));
			return;
		}

		for (String id : section.getKeys(false)) {
			Arena arena;
			String s = "instances." + id + ".";
			if (s.contains("default")) {
				continue;
			}

			arena = new Arena(id);
			arena.setEndLocation(LocationSerializer.locationFromString(config.getString(s + "endLocation", "world, -224.000, 4.000, -583.000, 0.000, 0.000")));
			arena.setPlateLocation(LocationSerializer.locationFromString(config.getString(s + "plateLocation", "world, -224.000, 4.000, -583.000, 0.000, 0.000")));

			if (plugin.getHookManager().isFeatureEnabled(HookManager.HookFeature.HOLOGRAPHIC_DISPLAYS)) {
				Hologram hologram = HologramsAPI.createHologram(plugin, LocationSerializer.locationFromString(config.getString(s + "hologramLocation")));
				hologram.setAllowPlaceholders(true);
				hologram.appendTextLine(plugin.getChatManager().colorMessage("In-Game.Last-King-Hologram").replace("%king%", arena.getKing() == null ? plugin.getChatManager().colorMessage("In-Game.There-Is-No-King") : arena.getKing().getName()));
				arena.setHologram(hologram);
				arena.setHologramLocation(hologram.getLocation());
			}

			if (LocationSerializer.locationFromString(config.getString(s + "plateLocation")).getBlock().getType() != XMaterial.OAK_PRESSURE_PLATE.parseMaterial()) {
				Bukkit.getConsoleSender().sendMessage(plugin.getChatManager().colorMessage("Validator.Invalid-Arena-Configuration").replace("%arena%", id).replace("%error%", "MISSING PLATE LOCATION"));
				config.set(s + "plateLocation", LocationSerializer.locationToString(Bukkit.getWorlds().get(0).getSpawnLocation()));
				config.set(s + "isdone", false);
				arena.setReady(false);
				ArenaRegistry.registerArena(arena);
				ConfigUtils.saveConfig(plugin, config, "arenas");
				continue;
			}

			if (!config.getBoolean(s + "isdone", false)) {
				Bukkit.getConsoleSender().sendMessage(plugin.getChatManager().colorMessage("Validator.Invalid-Arena-Configuration").replace("%arena%", id).replace("%error%", "NOT VALIDATED"));
				config.set(s + "isdone", false);
				arena.setReady(false);
				ArenaRegistry.registerArena(arena);
				ConfigUtils.saveConfig(plugin, config, "arenas");
				continue;
			}

			ArenaRegistry.registerArena(arena);
			Bukkit.getConsoleSender().sendMessage(plugin.getChatManager().colorMessage("Validator.Instance-Started").replace("%arena%", id));
			ConfigUtils.saveConfig(plugin, config, "arenas");
		}

		Debugger.debug(Level.INFO, "Arenas registration completed, took {0} ms", System.currentTimeMillis() - start);
	}

	public static List<Arena> getArenas() {
		return arenas;
	}
}