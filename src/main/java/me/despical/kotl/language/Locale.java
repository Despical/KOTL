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

package me.despical.kotl.language;

import java.util.List;

/**
 * @author Despical
 * <p>
 * Created at 31.01.2025
 */
public enum Locale {

    ENGLISH("English", "en_GB", "default", "english", "en"),
    TURKISH("Turkish", "tr_TR", "turkish", "türkçe", "turkce", "tr"),
    GERMAN("German", "de_DE", "deutsch", "german", "de");

    private final String name;
    private final String prefix;
    private final List<String> aliases;

    Locale(String name, String prefix, String... aliases) {
        this.name = name;
        this.prefix = prefix;
        this.aliases = List.of(aliases);
    }

    public static Locale getByName(String name) {
        for (Locale locale : values()) {
            if (locale.isSameWith(name)) {
                return locale;
            }
        }

        return null;
    }

    public String getName() {
        return name;
    }

    public String getPrefix() {
        return prefix;
    }

    public List<String> getAliases() {
        return aliases;
    }

    public boolean isSameWith(String name) {
        return name.equalsIgnoreCase(this.name) || aliases.contains(name);
    }
}
