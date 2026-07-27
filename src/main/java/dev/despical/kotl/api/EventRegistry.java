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

package dev.despical.kotl.api;

import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

/**
 * Provides read-only discovery and type checks for KOTL custom events.
 * <p>
 * The registry is backed by {@link EventType}; no runtime registration is
 * required. It is primarily useful for diagnostics, integrations, and generic
 * event tooling.
 *
 * @author Despical
 * <p>
 * Created at 05.06.2026
 */
public final class EventRegistry {

    private static final Set<EventType> REGISTERED_TYPES =
        Collections.unmodifiableSet(EnumSet.allOf(EventType.class));

    private EventRegistry() {
    }

    /**
     * Resolves the concrete Bukkit event class for an event type.
     *
     * @param type the event type to resolve
     * @return the concrete event class
     */
    @NotNull
    public static Class<? extends Event> getEventClass(@NotNull EventType type) {
        return type.getEventClass();
    }

    /**
     * Tests whether an event instance matches a registered event type.
     *
     * @param type the expected event type
     * @param event the event instance to inspect
     * @return {@code true} when the event is an instance of the registered class
     */
    public static boolean matches(@NotNull EventType type, @NotNull Event event) {
        return type.getEventClass().isInstance(event);
    }

    /**
     * Returns every event type known to this plugin version.
     *
     * @return an unmodifiable set in enum declaration order
     */
    @NotNull
    public static Set<EventType> getRegisteredTypes() {
        return REGISTERED_TYPES;
    }
}
