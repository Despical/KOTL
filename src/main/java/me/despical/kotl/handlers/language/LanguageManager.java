/*
 * KOTL - Don't let others climb to top of the ladders!
 * Copyright (C) 2024  Berke Akçen
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

package me.despical.kotl.handlers.language;

import me.despical.commons.file.FileUtils;
import me.despical.commons.util.Collections;
import me.despical.kotl.Main;

import java.io.File;
import java.io.IOException;
import java.net.URL;

/**
 * @author Despical
 * <p>
 * Created at 01.11.2018
 */
public class LanguageManager {

	private final Main plugin;
	private Locale pluginLocale;

	public LanguageManager(Main plugin) {
		this.plugin = plugin;

		registerLocales();
		setupLocale();
		init();
	}

	private void init() {
		//noinspection ConfusingArgumentToVarargsMethod
		if (Collections.contains(plugin.getChatManager().message("language"), pluginLocale.aliases)) return;

		try {
			FileUtils.copyURLToFile(new URL("https://raw.githubusercontent.com/Despical/LocaleStorage/main/Minecraft/KOTL/" + pluginLocale.prefix + ".yml"), new File(plugin.getDataFolder(), "messages.yml"));
		} catch (IOException e) {
			plugin.getLogger().warning("Error while connecting to internet!");
		}
	}

	private void registerLocales() {
		Collections.listOf(
			new Locale("English", "en_GB", "default", "english", "en"),
			new Locale("German", "de_DE", "deutsch", "german", "de"),
			new Locale("Turkish", "tr_TR", "turkish", "türkçe", "turkce", "tr"))
			.forEach(LocaleRegistry::registerLocale);
	}

	private void setupLocale() {
		final var localeName = plugin.getConfig().getString("locale", "default").toLowerCase();

		for (final var locale : LocaleRegistry.getRegisteredLocales()) {
			if (locale.prefix.equalsIgnoreCase(localeName)) {
				pluginLocale = locale;
				break;
			}

			for (final var alias : locale.aliases) {
				if (alias.equals(localeName)) {
					pluginLocale = locale;
					break;
				}
			}
		}

		if (pluginLocale == null) {
			pluginLocale = LocaleRegistry.getByName("English");
			plugin.getLogger().warning("Selected locale is invalid! Using default locale.");
			return;
		}

		plugin.getLogger().info("Loaded locale " + pluginLocale.name + " (ID: " + pluginLocale.prefix + ")");
	}

	public Locale getPluginLocale() {
		return pluginLocale;
	}

	public record Locale(String name, String prefix, String... aliases) {
	}
}