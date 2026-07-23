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

import lombok.Getter;

/**
 * @author Despical
 * <p>
 * Created at 05.06.2026
 */
@Getter
public abstract class ArenaOption<T> {

    private final String key;
    private final T defaultValue;
    private final Class<T> type;

    public ArenaOption(String key, T defaultValue, Class<T> type) {
        this.key = key;
        this.defaultValue = defaultValue;
        this.type = type;
    }

    public Object serialize(T value) {
        return value;
    }

    public T deserialize(Object value) {
        try {
            if (type.isInstance(value)) {
                return type.cast(value);
            }

            return parse(String.valueOf(value));
        } catch (Exception exception) {
            exception.printStackTrace();
            return defaultValue;
        }
    }

    protected T parse(String value) {
        throw new UnsupportedOperationException("Parse method not implemented for option: " + key);
    }

    public boolean isPersistent() {
        return key != null && !key.isEmpty();
    }
}
