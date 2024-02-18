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

package me.despical.kotl.handlers.setup;

import me.despical.commons.compat.XMaterial;
import me.despical.commons.item.ItemBuilder;
import me.despical.commons.serializer.LocationSerializer;
import me.despical.kotl.Main;
import me.despical.kotl.handlers.ChatManager;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;

/**
 * @author Despical
 * <p>
 * Created at 18.02.2024
 */
public abstract class AbstractComponent {

	protected static final ItemStack mainMenuItem;
	protected static boolean supportsParticle;

	protected final Main plugin;
	protected final ChatManager chatManager;

	static {
		mainMenuItem = new ItemBuilder(XMaterial.REDSTONE).name("&c&lReturn KOTL Menu").lore("&7Click to return last page!").build();

		try {
			Class.forName("org.bukkit.Particle");

			supportsParticle = true;
		} catch (ClassNotFoundException exception) {
			supportsParticle = false;
		}
	}

	public AbstractComponent(Main plugin) {
		this.plugin = plugin;
		this.chatManager = plugin.getChatManager();
	}

	public abstract void injectComponents(SetupInventory inventory);

	protected final String isOptionDoneBool(FileConfiguration config, String path) {
		return config.isSet(path) ? LocationSerializer.isDefaultLocation(config.getString(path)) ? "&c&l✘ Not Completed" : "&a&l✔ Completed" : "&c&l✘ Not Completed";
	}
}
