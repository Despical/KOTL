/*
 *  KOTL - Don't let others to climb top of the ladders!
 *  Copyright (C) 2020 Despical and contributors
 *  
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *  
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License
 *  along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package me.despical.kotl.handlers.language;

import java.util.List;

/**
 * @author Despical
 * <p>
 * Created at 04.11.2020
 */
public class Locale {

	private final String name;
	private final String originalName;
	private final String prefix;
	private final String author;
	private final List<String> aliases;

	public Locale(String name, String originalName, String prefix, String author, List<String> aliases) {
		this.prefix = prefix;
		this.name = name;
		this.originalName = originalName;
		this.author = author;
		this.aliases = aliases;
	}

	/**
	 * Gets name of locale, ex. English or Turkish
	 *
	 * @return name of locale
	 */
	public String getName() {
		return name;
	}

	/**
	 * Gets original name of locale ex. for German it will return Deutsch, Polish returns Polski etc.
	 *
	 * @return name of locale in its language
	 */
	public String getOriginalName() {
		return originalName;
	}

	/**
	 * @return authors of locale
	 */
	public String getAuthor() {
		return author;
	}

	/**
	 * Language code ex. en_GB, de_DE, tr_TR etc.
	 *
	 * @return language code of locale
	 */
	public String getPrefix() {
		return prefix;
	}

	/**
	 * Valid aliases of locale ex. for German - deutsch, de, german; Turkish - tr etc.
	 *
	 * @return aliases for locale
	 */
	public List<String> getAliases() {
		return aliases;
	}
}