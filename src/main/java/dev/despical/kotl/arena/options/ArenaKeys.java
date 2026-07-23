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

package dev.despical.kotl.arena.options;

import dev.despical.commons.serializer.LocationSerializer;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;

import java.util.List;
import java.util.Locale;

/**
 * @author Despical
 * <p>
 * Created at 05.06.2026
 */
public final class ArenaKeys {

    public static final ArenaOption<Boolean> READY = new ArenaOption<>("ready", false, Boolean.class) {

        @Override
        protected Boolean parse(String value) {
            return Boolean.parseBoolean(value);
        }
    };

    public static final ArenaOption<Location> END_LOCATION = new ArenaOption<>("end", null, Location.class) {

        @Override
        public Object serialize(Location value) {
            return LocationSerializer.toString(value);
        }

        @Override
        protected Location parse(String value) {
            return LocationSerializer.fromString(value);
        }
    };

    public static final ArenaOption<Location> PLATE_LOCATION = new ArenaOption<>("plate", null, Location.class) {

        @Override
        public Object serialize(Location value) {
            return LocationSerializer.toString(value);
        }

        @Override
        protected Location parse(String value) {
            return LocationSerializer.fromString(value);
        }
    };

    public static final ArenaOption<Location> MIN_CORNER = new ArenaOption<>("area-min", null, Location.class) {

        @Override
        public Object serialize(Location value) {
            return LocationSerializer.toString(value);
        }

        @Override
        protected Location parse(String value) {
            return LocationSerializer.fromString(value);
        }
    };

    public static final ArenaOption<Location> MAX_CORNER = new ArenaOption<>("area-max", null, Location.class) {

        @Override
        public Object serialize(Location value) {
            return LocationSerializer.toString(value);
        }

        @Override
        protected Location parse(String value) {
            return LocationSerializer.fromString(value);
        }
    };

    public static final ArenaOption<Material> ARENA_PLATE = new ArenaOption<>("arena-plate", Material.OAK_PRESSURE_PLATE, Material.class) {

        @Override
        public Object serialize(Material value) {
            return value.name();
        }

        @Override
        protected Material parse(String value) {
            try {
                return Material.valueOf(value);
            } catch (IllegalArgumentException ignored) {
                return Material.OAK_PRESSURE_PLATE;
            }
        }
    };

    public static final ArenaOption<GameMode> ARENA_GAMEMODE = new ArenaOption<>("arena-gamemode", GameMode.SURVIVAL, GameMode.class) {

        @Override
        public Object serialize(GameMode value) {
            return value.name();
        }

        @Override
        protected GameMode parse(String value) {
            try {
                return GameMode.valueOf(value.trim().toUpperCase(Locale.ENGLISH));
            } catch (IllegalArgumentException ignored) {
                return GameMode.SURVIVAL;
            }
        }
    };

    public static final ArenaOption<Boolean> ARENA_SCOREBOARD_ENABLED = new ArenaOption<>("arena-scoreboard-enabled", true, Boolean.class) {

        @Override
        protected Boolean parse(String value) {
            return Boolean.parseBoolean(value);
        }
    };

    public static final ArenaOption<Boolean> ARENA_BOSSBAR_ENABLED = new ArenaOption<>("arena-bossbar-enabled", true, Boolean.class) {

        @Override
        protected Boolean parse(String value) {
            return Boolean.parseBoolean(value);
        }
    };

    public static final ArenaOption<Boolean> SHOW_OUTLINES = new ArenaOption<>("show-outlines", false, Boolean.class) {

        @Override
        protected Boolean parse(String value) {
            return Boolean.parseBoolean(value);
        }
    };

    public static final ArenaOption<String> KING = new ArenaOption<>("king", null, String.class) {

        @Override
        protected String parse(String value) {
            return value;
        }

        @Override
        public boolean isPersistent() {
            return false;
        }
    };

    public static final ArenaOption<String> LAST_KING = new ArenaOption<>("last-king", "None", String.class) {

        @Override
        protected String parse(String value) {
            return value;
        }
    };

    public static final ArenaOption<String> TOP_KING = new ArenaOption<>("top-king", "None", String.class) {

        @Override
        protected String parse(String value) {
            return value;
        }
    };

    public static final ArenaOption<Integer> TOP_KING_SCORE = new ArenaOption<>("top-king-score", 0, Integer.class) {

        @Override
        protected Integer parse(String value) {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException ignored) {
                return 0;
            }
        }
    };

    public static List<ArenaOption<?>> getAllKeys() {
        return List.of(
            READY,
            END_LOCATION,
            PLATE_LOCATION,
            MIN_CORNER,
            MAX_CORNER,
            ARENA_PLATE,
            ARENA_GAMEMODE,
            ARENA_SCOREBOARD_ENABLED,
            ARENA_BOSSBAR_ENABLED,
            SHOW_OUTLINES,
            KING,
            LAST_KING,
            TOP_KING,
            TOP_KING_SCORE
        );
    }

    public static List<ArenaOption<?>> getPersistentKeys() {
        return getAllKeys().stream().filter(ArenaOption::isPersistent).toList();
    }
}
