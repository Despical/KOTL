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

package dev.despical.kotl.arena;

import dev.despical.commons.XMaterial;
import dev.despical.commons.configuration.ConfigUtils;
import dev.despical.commons.serializer.LocationSerializer;
import dev.despical.kotl.KOTL;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

/**
 * @author Despical
 * <p>
 * Created at 22.06.2020
 */
public class ArenaRegistry {

    private final KOTL plugin;
    private final Map<String, Arena> arenas;

    public ArenaRegistry(KOTL plugin) {
        this.plugin = plugin;
        this.arenas = new HashMap<>();
        this.registerArenas();
    }

    public void registerArena(Arena arena) {
        arenas.put(arena.getId(), arena);
    }

    public void unregisterArena(Arena arena) {
        arenas.remove(arena.getId());
    }

    public Set<Arena> getArenas() {
        return Set.copyOf(arenas.values());
    }

    public Set<String> getArenaNames() {
        return arenas.keySet();
    }

    public Arena getArena(String id) {
        return arenas.get(id);
    }

    public Arena getArena(Player player) {
        if (player == null) return null;

        return arenas.values()
            .stream()
            .filter(arena -> arena.getPlayers().contains(player))
            .findFirst()
            .orElse(null);
    }

    public boolean isArena(String arenaId) {
        return getArena(arenaId) != null;
    }

    public boolean isInArena(Player player) {
        return getArena(player) != null;
    }

    public void registerArenas() {
        arenas.clear();

        FileConfiguration config = ConfigUtils.getConfig(plugin, "arenas");
        ConfigurationSection section = config.getConfigurationSection("instances");

        if (section == null) {
            return;
        }

        for (String id : section.getKeys(false)) {
            if (id.equals("default")) continue;

            String path = "instances." + id + ".";
            Arena arena = new Arena(id);

            arenas.put(id, arena);

            arena.setReady(config.getBoolean(path + "isdone"));
            arena.setEndLocation(LocationSerializer.fromString(config.getString(path + "endLocation")));
            arena.setPlateLocation(LocationSerializer.fromString(config.getString(path + "plateLocation")));
            arena.setMinCorner(LocationSerializer.fromString(config.getString(path + "areaMin")));
            arena.setMaxCorner(LocationSerializer.fromString(config.getString(path + "areaMax")));
            arena.setArenaPlate(XMaterial.matchXMaterial(config.getString(path + "arenaPlate")).orElse(XMaterial.OAK_PRESSURE_PLATE));
            arena.setShowOutlines(config.getBoolean(path + "showOutlines"));

            if (arena.isReady() && arena.getPlateLocation().getBlock().getType() != arena.getArenaPlate().parseMaterial()) {
                arena.setPlateLocation(LocationSerializer.DEFAULT_LOCATION);
                arena.setReady(false);

                plugin.getLogger().log(Level.WARNING, "The pressure plate material is not the same type as you set on setup for the arena ''{0}''!", id);
                continue;
            }

            if (!arena.isReady()) {
                plugin.getLogger().log(Level.WARNING, "Setup of arena ''{0}'' is not finished yet!", id);
                return;
            }
        }
    }
}
