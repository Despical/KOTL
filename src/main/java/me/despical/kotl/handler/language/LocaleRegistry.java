/*
 * KOTL - Don't let others to climb top of the ladders!
 * Copyright (C) 2023 Despical
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

package me.despical.kotl.handler.language;

import java.util.HashSet;
import java.util.Set;

import me.despical.kotl.handler.language.LanguageManager.Locale;

/**
 * @author Despical
 * <p>
 * Created at 01.11.2020
 */
public class LocaleRegistry {

	private static final Set<LanguageManager.Locale> registeredLocales = new HashSet<>();

	public static void registerLocale(Locale locale) {
		registeredLocales.removeIf(l -> l.prefix().equals(locale.prefix()));

		registeredLocales.add(locale);
	}

	public static Set<Locale> getRegisteredLocales() {
		return new HashSet<>(registeredLocales);
	}

	public static Locale getByName(String name) {
		for (final var locale : registeredLocales) {
			if (locale.name().equals(name)) {
				return locale;
			}
		}

		return null;
	}
}