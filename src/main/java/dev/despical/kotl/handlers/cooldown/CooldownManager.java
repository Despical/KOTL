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

package dev.despical.kotl.handlers.cooldown;

import dev.despical.kotl.user.User;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Despical
 * <p>
 * Created at 30.04.2024
 */
public class CooldownManager {

    private final Map<CooldownKey, Long> cooldowns;

    public CooldownManager() {
        this.cooldowns = new ConcurrentHashMap<>();
    }

    public void setCooldown(User user, String name, double seconds) {
        CooldownKey key = new CooldownKey(user.getUniqueId(), name);

        if (seconds <= 0) {
            cooldowns.remove(key);
            return;
        }

        long durationMillis = (long) Math.ceil(seconds * 1000);
        cooldowns.put(key, System.currentTimeMillis() + durationMillis);
    }

    public double getCooldown(User user, String name) {
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
