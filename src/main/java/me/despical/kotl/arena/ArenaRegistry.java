/*
 * KOTL - Don't let others to climb top of the ladders!
 * Copyright (C) 2022 Despical
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
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package me.despical.kotl.arena;

import me.despical.commons.compat.XMaterial;
import me.despical.commons.configuration.ConfigUtils;
import me.despical.commons.serializer.LocationSerializer;
import me.despical.commons.util.LogUtils;
import me.despical.kotl.Main;
import me.despical.kotl.handler.hologram.Hologram;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Despical
 * <p>
 * Created at 22.06.2020
 */
public class ArenaRegistry {

	private static final Main plugin = JavaPlugin.getPlugin(Main.class);
	private static final Set<Arena> arenas = new HashSet<>();

	public static boolean isInArena(Player player) {
		return getArena(player) != null;
	}

	public static boolean isArena(String id) {
		return getArena(id) != null;
	}

	public static Arena getArena(Player p) {
		return p == null || !p.isOnline() ? null : arenas.stream().filter(arena -> arena.getPlayers().stream().anyMatch(player -> player.getUniqueId().equals(p.getUniqueId()))).findFirst().orElse(null);
	}

	public static Arena getArena(String id) {
		return arenas.stream().filter(loopArena -> loopArena.getId().equalsIgnoreCase(id)).findFirst().orElse(null);
	}

	public static void registerArena(Arena arena) {
		LogUtils.log("Registering new game instance {0}", arena.getId());
		arenas.add(arena);
	}

	public static void unregisterArena(Arena arena) {
		LogUtils.log("Unregistering game instance {0}", arena.getId());
		arenas.remove(arena);
	}

	public static void registerArenas() {
		LogUtils.log("Arena registration started.");
		FileConfiguration config = ConfigUtils.getConfig(plugin, "arenas");
		long start = System.currentTimeMillis();
		
		arenas.clear();

		if (!config.contains("instances")) {
			LogUtils.sendConsoleMessage(plugin.getChatManager().message("validator.no_instances_created"));
			return;
		}

		ConfigurationSection section = config.getConfigurationSection("instances");

		if (section == null) {
			LogUtils.sendConsoleMessage(plugin.getChatManager().message("validator.no_instances_created"));
			return;
		}

		for (String id : section.getKeys(false)) {
			Arena arena;
			String path = "instances." + id + ".";

			if (path.contains("default")) {
				continue;
			}

			arena = new Arena(id);
			arena.setEndLocation(LocationSerializer.fromString(config.getString(path + "endLocation")));
			arena.setPlateLocation(LocationSerializer.fromString(config.getString(path + "plateLocation")));

			Hologram hologram = new Hologram(LocationSerializer.fromString(config.getString(path + "hologramLocation")));
			hologram.appendLine(plugin.getChatManager().message("in_game.last_king_hologram").replace("%king%", arena.getKingName()));

			arena.setHologram(hologram);
			arena.setHologramLocation(hologram.getLocation());

			if (LocationSerializer.fromString(config.getString(path + "plateLocation")).getBlock().getType() != XMaterial.OAK_PRESSURE_PLATE.parseMaterial()) {
				LogUtils.sendConsoleMessage(plugin.getChatManager().message("validator.invalid_arena_configuration").replace("%arena%", id).replace("%error%", "MISSING PLATE LOCATION"));
				config.set(path + "plateLocation", LocationSerializer.SERIALIZED_LOCATION);
				config.set(path + "isdone", false);
				arena.setReady(false);

				registerArena(arena);
				ConfigUtils.saveConfig(plugin, config, "arenas");
				continue;
			}

			if (!config.getBoolean(path + "isdone")) {
				LogUtils.sendConsoleMessage(plugin.getChatManager().message("validator.invalid_arena_configuration").replace("%arena%", id).replace("%error%", "NOT VALIDATED"));
				config.set(path + "isdone", false);
				arena.setReady(false);

				registerArena(arena);
				ConfigUtils.saveConfig(plugin, config, "arenas");
				continue;
			}

			registerArena(arena);
			LogUtils.sendConsoleMessage(plugin.getChatManager().message("validator.instance_started").replace("%arena%", id));
			ConfigUtils.saveConfig(plugin, config, "arenas");
		}

		LogUtils.log("Arenas registration completed, took {0} ms.", System.currentTimeMillis() - start);
	}

	public static Set<Arena> getArenas() {
		return arenas;
	}
}