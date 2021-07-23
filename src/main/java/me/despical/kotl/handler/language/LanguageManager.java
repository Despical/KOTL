/*
 * KOTL - Don't let others to climb top of the ladders!
 * Copyright (C) 2021 Despical and contributors
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

import me.despical.commons.file.FileUtils;
import me.despical.commons.util.Collections;
import me.despical.kotl.Main;
import me.despical.kotl.util.Debugger;

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
		try {
			FileUtils.copyURLToFile(new URL("https://raw.githubusercontent.com/Despical/LocaleStorage/main/Minecraft/KOTL/" + pluginLocale.prefix + ".yml"), new File(plugin.getDataFolder(), "messages.yml"));
		} catch (IOException e) {
			Debugger.sendConsoleMessage("&c[KOTL] Error while connecting to internet!");
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
		if (Debugger.isEnabled()) {
			Debugger.sendConsoleMessage("&c[KOTL] Debug mode doesn't support languages, using default one.");
			pluginLocale = LocaleRegistry.getByName("English");
			return;
		}

		String localeName = plugin.getConfig().getString("locale", "default").toLowerCase();

		for (Locale locale : LocaleRegistry.getRegisteredLocales()) {
			if (locale.prefix.equalsIgnoreCase(localeName)) {
				pluginLocale = locale;
				break;
			}

			for (String alias : locale.aliases) {
				if (alias.equals(localeName)) {
					pluginLocale = locale;
					break;
				}
			}
		}

		if (pluginLocale == null) {
			Debugger.sendConsoleMessage("&c[KOTL] Plugin locale is invalid! Using default one.");
			pluginLocale = LocaleRegistry.getByName("English");
			return;
		}

		Debugger.sendConsoleMessage("[KOTL] Loaded locale " + pluginLocale.name + " (ID: " + pluginLocale.prefix + ")");
	}

	public Locale getPluginLocale() {
		return pluginLocale;
	}
}