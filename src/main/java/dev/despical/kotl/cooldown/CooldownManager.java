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

package dev.despical.kotl.cooldown;

import dev.despical.kotl.user.User;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Stores named, player-specific cooldowns using wall-clock expiration times.
 * <p>
 * Cooldowns are kept in memory and are not persisted across plugin reloads or
 * server restarts. A single player may have multiple independent cooldowns,
 * identified by their names.
 *
 * @author Despical
 * <p>
 * Created at 30.04.2024
 */
public final class CooldownManager {

    private final Map<CooldownKey, Long> cooldowns;

    /**
     * Creates an empty cooldown manager.
     */
    public CooldownManager() {
        this.cooldowns = new ConcurrentHashMap<>();
    }

    /**
     * Starts or replaces a named cooldown for a player.
     * <p>
     * Supplying a duration of {@code 0} or less removes the cooldown.
     *
     * @param user the user who owns the cooldown
     * @param name the stable name of the cooldown
     * @param seconds the cooldown duration in seconds
     */
    public void setCooldown(@NotNull User user, @NotNull String name, double seconds) {
        CooldownKey key = new CooldownKey(user.getUniqueId(), name);

        if (seconds <= 0) {
            cooldowns.remove(key);
            return;
        }

        long durationMillis = (long) Math.ceil(seconds * 1000);
        cooldowns.put(key, System.currentTimeMillis() + durationMillis);
    }

    /**
     * Returns the remaining time for a named player cooldown.
     * <p>
     * Expired entries are removed lazily when queried.
     *
     * @param user the user who owns the cooldown
     * @param name the stable name of the cooldown
     * @return the remaining duration in seconds, or {@code 0} when inactive
     */
    public double getCooldown(@NotNull User user, @NotNull String name) {
        CooldownKey key = new CooldownKey(user.getUniqueId(), name);
        Long expiresAt = cooldowns.get(key);

        if (expiresAt == null) {
            return 0;
        }

        long remainingMillis = expiresAt - System.currentTimeMillis();
        if (remainingMillis <= 0) {
            cooldowns.remove(key, expiresAt);
            return 0;
        }

        return remainingMillis / 1000D;
    }

    private record CooldownKey(UUID uuid, String name) {
    }
}
