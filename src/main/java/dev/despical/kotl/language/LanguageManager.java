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

package dev.despical.kotl.language;

import dev.despical.commons.file.FileUtils;
import dev.despical.kotl.KOTL;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;

/**
 * @author Despical
 * <p>
 * Created at 01.11.2018
 */
public class LanguageManager {

    private final KOTL plugin;
    private Locale currentLocale;

    public LanguageManager(KOTL plugin) {
        this.plugin = plugin;

        setupLocale();
        downloadLocaleFile();
    }

    private void downloadLocaleFile() {
        String language = plugin.getChatManager().message("Language");

        if (currentLocale.isSameWith(language)) {
            return;
        }

        try {
            URL fileURL = URI.create("https://raw.githubusercontent.com/Despical/LocaleStorage/main/Minecraft/KOTL/%s.yml".formatted(currentLocale.getPrefix())).toURL();
            FileUtils.copyURLToFile(fileURL, new File(plugin.getDataFolder(), "messages.yml"));
        } catch (IOException exception) {
            plugin.getLogger().warning("An exception occurred while downloading the language content!");
        }
    }

    private void setupLocale() {
        String localeName = plugin.getConfig().getString("locale", "default");

        currentLocale = Locale.getByName(localeName);

        if (currentLocale == null) {
            currentLocale = Locale.getByName("English");

            plugin.getLogger().warning("Selected locale is invalid! Using the default one.");
            return;
        }

        plugin.getLogger().info("Locale successfully loaded: %s (%s).".formatted(currentLocale.getName(), currentLocale.getPrefix()));
    }

    public Locale getCurrentLocale() {
        return currentLocale;
    }
}
