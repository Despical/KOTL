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

package dev.despical.kotl.arena;

import dev.despical.commons.configuration.ConfigUtils;
import dev.despical.kotl.KOTL;
import dev.despical.kotl.arena.options.ArenaKeys;
import dev.despical.kotl.arena.options.ArenaOption;
import dev.despical.kotl.user.User;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import java.util.*;

/**
 * @author Despical
 * <p>
 * Created at 22.06.2020
 */
public class ArenaRegistry {

    private final KOTL plugin;
    @Getter
    private final FileConfiguration config;
    private final Map<String, Arena> arenas;

    public ArenaRegistry(KOTL plugin) {
        this.plugin = plugin;
        this.config = ConfigUtils.getConfig(plugin, "arenas");
        this.arenas = new HashMap<>();
        this.registerArenas();
    }

    public Arena getArena(User user) {
        Player player = user.getPlayer();
        if (player == null) {
            return null;
        }

        return arenas.values()
            .stream()
            .filter(arena -> arena.getGame().getPlayers().contains(player))
            .findFirst()
            .orElse(null);
    }

    public Arena getArena(Player player) {
        User user = plugin.getUserManager().getUser(player);
        return getArena(user);
    }

    public boolean isInArena(Player player) {
        return getArena(player) != null;
    }

    public Arena getArena(String id) {
        return findArena(id).orElse(null);
    }

    public Optional<Arena> findArena(String id) {
        return id == null ? Optional.empty() : Optional.ofNullable(arenas.get(ArenaIdValidator.normalize(id)));
    }

    public Optional<Arena> findArena(Player player) {
        return Optional.ofNullable(getArena(player));
    }

    public boolean isArenaExists(String id) {
        return id != null && arenas.containsKey(ArenaIdValidator.normalize(id));
    }

    public boolean registerNewArena(String id) {
        if (!ArenaIdValidator.isValid(id) || isArenaExists(id)) {
            return false;
        }

        arenas.put(ArenaIdValidator.normalize(id), new Arena(id));
        return true;
    }

    public void unregisterArena(Arena arena) {
        plugin.getOutlineManager().hideOutlines(arena);
        arenas.remove(ArenaIdValidator.normalize(arena.getId()));
        config.set(arena.getId(), null);
    }

    public Set<Arena> getArenas() {
        return Set.copyOf(arenas.values());
    }

    public Set<String> getArenaNames() {
        return arenas.values().stream().map(Arena::getId).collect(java.util.stream.Collectors.toUnmodifiableSet());
    }

    public void registerArenas() {
        arenas.clear();

        for (String id : config.getKeys(false)) {
            if (!config.isConfigurationSection(id)) {
                continue;
            }

            if (!ArenaIdValidator.isValid(id)) {
                plugin.getLogger().warning("Skipping arena with invalid ID in arenas.yml: " + id);
                continue;
            }

            String normalizedId = ArenaIdValidator.normalize(id);
            if (arenas.containsKey(normalizedId)) {
                plugin.getLogger().warning("Skipping duplicate arena ID in arenas.yml (case-insensitive): " + id);
                continue;
            }

            Arena arena = new Arena(id);
            loadOptionsFor(arena, config);
            validateReadyState(arena);

            arenas.put(normalizedId, arena);
        }
    }

    private void loadOptionsFor(Arena arena, FileConfiguration config) {
        for (ArenaOption<?> option : ArenaKeys.getPersistentKeys()) {
            loadSingleOption(arena, config, option);
        }
    }

    private <T> void loadSingleOption(Arena arena, FileConfiguration config, ArenaOption<T> option) {
        String path = "%s.%s".formatted(arena.getId(), option.getKey());

        if (config.contains(path)) {
            Object rawValue = config.get(path);
            T value = option.deserialize(rawValue);

            arena.setOption(option, value);
        } else {
            arena.setOption(option, option.getDefaultValue());
        }
    }

    private void validateReadyState(Arena arena) {
        if (!arena.getOption(ArenaKeys.READY)) {
            return;
        }

        Location end = arena.getOption(ArenaKeys.END_LOCATION);
        Location plate = arena.getOption(ArenaKeys.PLATE_LOCATION);
        boolean invalidLocation = end == null
            || end.getWorld() == null
            || !isLocationInArea(arena, plate);

        if (!invalidLocation) {
            return;
        }

        arena.setOption(ArenaKeys.READY, false);
        config.set(arena.getId() + "." + ArenaKeys.READY.getKey(), false);
        plugin.getLogger().warning("Arena '" + arena.getId()
            + "' was marked not ready because one or more required locations are missing or invalid.");
    }

    public Arena findTargetArena(Player player) {
        for (Arena arena : arenas.values()) {
            if (isInArea(arena, player)) {
                return arena;
            }
        }
        return null;
    }

    public boolean isInArea(Arena arena, Player player) {
        if (!arena.getOption(ArenaKeys.READY) || !arena.getOption(ArenaKeys.ENABLED)) return false;

        return isLocationInArea(arena, player.getLocation());
    }

    public boolean hasValidArea(Arena arena) {
        Location min = arena.getOption(ArenaKeys.MIN_CORNER);
        Location max = arena.getOption(ArenaKeys.MAX_CORNER);

        return min != null
            && max != null
            && min.getWorld() != null
            && min.getWorld().equals(max.getWorld());
    }

    public boolean isLocationInArea(Arena arena, Location origin) {
        if (!hasValidArea(arena) || origin == null || origin.getWorld() == null) return false;

        Location min = arena.getOption(ArenaKeys.MIN_CORNER);
        Location max = arena.getOption(ArenaKeys.MAX_CORNER);

        if (!min.getWorld().equals(origin.getWorld())) return false;

        double minX = Math.min(min.getX(), max.getX()), maxX = Math.max(min.getX(), max.getX());
        double minY = Math.min(min.getY(), max.getY()), maxY = Math.max(min.getY(), max.getY());
        double minZ = Math.min(min.getZ(), max.getZ()), maxZ = Math.max(min.getZ(), max.getZ());

        return origin.getX() >= minX && origin.getX() <= maxX
            && origin.getY() >= minY && origin.getY() <= maxY
            && origin.getZ() >= minZ && origin.getZ() <= maxZ;
    }
}
