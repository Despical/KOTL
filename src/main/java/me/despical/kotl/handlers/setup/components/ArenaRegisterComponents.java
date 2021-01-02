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

package me.despical.kotl.handlers.setup.components;

import me.despical.commonsbox.compat.XMaterial;
import me.despical.commonsbox.configuration.ConfigUtils;
import me.despical.commonsbox.item.ItemBuilder;
import me.despical.commonsbox.serializer.LocationSerializer;
import me.despical.inventoryframework.GuiItem;
import me.despical.inventoryframework.pane.StaticPane;
import me.despical.kotl.Main;
import me.despical.kotl.arena.Arena;
import me.despical.kotl.arena.ArenaRegistry;
import me.despical.kotl.handlers.hologram.Hologram;
import me.despical.kotl.handlers.setup.SetupInventory;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

/**
 * @author Despical
 * <p>
 *  Created at 22.06.2020
 */
public class ArenaRegisterComponents implements SetupComponent {

	private SetupInventory setupInventory;

	@Override
	public void prepare(SetupInventory setupInventory) {
		this.setupInventory = setupInventory;
	}

	@Override
	public void injectComponents(StaticPane pane) {
		FileConfiguration config = setupInventory.getConfig();
		Main plugin = setupInventory.getPlugin();
		ItemStack registeredItem;

		if (!setupInventory.getArena().isReady()) {
			registeredItem = new ItemBuilder(XMaterial.FIREWORK_ROCKET.parseItem())
				.name("&e&lRegister Arena - Finish Setup")
				.lore("&7Click this when you're done with configuration.")
				.lore("&7It will validate and register arena.")
				.build();
		} else {
			registeredItem = new ItemBuilder(Material.BARRIER)
				.name("&a&lArena Registered - Congratulations")
				.lore("&7This arena is already registered!")
				.lore("&7Good job, you went through whole setup!")
				.enchantment(Enchantment.ARROW_DAMAGE)
				.flag(ItemFlag.HIDE_ENCHANTS)
				.build();
		}

		pane.addItem(new GuiItem(registeredItem, e -> {
			Arena arena = setupInventory.getArena();
			String s = "instances." + arena.getId() + ".";

			e.getWhoClicked().closeInventory();

			if (config.getBoolean(s + "isdone")) {
				e.getWhoClicked().sendMessage(plugin.getChatManager().colorRawMessage("&a&l✔ &aThis arena was already validated and is ready to use!"));
				return;
			}

			String[] locations = {"plateLocation", "hologramLocation", "endLocation", "areaMin", "areaMax"};

			for (String loc : locations) {
				if (!config.isSet(s + loc) || config.getString(s + loc).equals(LocationSerializer.locationToString(Bukkit.getWorlds().get(0).getSpawnLocation()))) {
					e.getWhoClicked().sendMessage(plugin.getChatManager().colorRawMessage("&c&l✘ &cArena validation failed! Please configure following spawn properly: " + loc + " (cannot be world spawn location)"));
					return;
				}
			}

			if (arena.getHologram() != null) arena.getHologram().delete();
			arena = new Arena(setupInventory.getArena().getId());
			arena.setReady(true);
			arena.setEndLocation(LocationSerializer.locationFromString(config.getString(s + "endLocation")));
			arena.setPlateLocation(LocationSerializer.locationFromString(config.getString(s + "plateLocation")));

			Hologram hologram = new Hologram(LocationSerializer.locationFromString(config.getString(s + "hologramLocation")), plugin.getChatManager().colorMessage("In-Game.Last-King-Hologram").replace("%king%", arena.getKing() == null ? plugin.getChatManager().colorMessage("In-Game.There-Is-No-King") : arena.getKing().getName()));

			arena.setHologram(hologram);
			arena.setHologramLocation(hologram.getLocation());

			ArenaRegistry.unregisterArena(setupInventory.getArena());

			e.getWhoClicked().sendMessage(plugin.getChatManager().colorRawMessage("&a&l✔ &aValidation succeeded! Registering new arena instance: " + arena.getId()));

			config.set(s + "isdone", true);
			ConfigUtils.saveConfig(plugin, config, "arenas");
			ArenaRegistry.registerArena(arena);
		}), 8, 0);
	}
}