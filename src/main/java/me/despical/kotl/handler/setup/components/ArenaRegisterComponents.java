/*
 * KOTL - Don't let others to climb top of the ladders!
 * Copyright (C) 2022 Despical
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

package me.despical.kotl.handler.setup.components;

import me.despical.commons.compat.XMaterial;
import me.despical.commons.item.ItemBuilder;
import me.despical.commons.serializer.LocationSerializer;
import me.despical.inventoryframework.GuiItem;
import me.despical.inventoryframework.pane.StaticPane;
import me.despical.kotl.arena.Arena;
import me.despical.kotl.arena.ArenaRegistry;
import me.despical.kotl.handler.hologram.Hologram;
import me.despical.kotl.handler.setup.SetupInventory;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;

/**
 * @author Despical
 * <p>
 *  Created at 22.06.2020
 */
public class ArenaRegisterComponents implements SetupComponent {

	@Override
	public void injectComponents(SetupInventory setupInventory, StaticPane pane) {
		final Player player = setupInventory.getPlayer();
		final Arena arena = setupInventory.getArena();
		final ItemBuilder registeredItem;

		if (!arena.isReady()) {
			registeredItem = new ItemBuilder(XMaterial.FIREWORK_ROCKET)
				.name("&e&lRegister Arena - Finish Setup")
				.lore("&7Click this when you're done with configuration.")
				.lore("&7It will validate and register arena.");
		} else {
			registeredItem = new ItemBuilder(Material.BARRIER)
				.name("&a&lArena Registered - Congratulations")
				.lore("&7This arena is already registered!")
				.lore("&7Good job, you went through whole setup!")
				.enchantment(Enchantment.ARROW_DAMAGE)
				.flag(ItemFlag.HIDE_ENCHANTS);
		}

		pane.addItem(GuiItem.of(registeredItem.build(), e -> {
			final String path = "instances." + arena.getId() + ".";

			player.closeInventory();

			if (config.getBoolean(path + "isdone")) {
				player.sendMessage(chatManager.coloredRawMessage("&a&l✔ &aThis arena was already validated and is ready to use!"));
				return;
			}

			String[] locations = {"plateLocation", "endLocation", "areaMin", "areaMax"};

			for (String loc : locations) {
				if (!config.isSet(path + loc) || LocationSerializer.isDefaultLocation(config.getString(path + loc))) {
					player.sendMessage(chatManager.coloredRawMessage("&c&l✘ &cArena validation failed! Please configure following spawn properly: " + loc + " (cannot be world spawn location)"));
					return;
				}
			}

			ArenaRegistry.unregisterArena(arena);

			arena.deleteHologram();
			arena.setReady(true);
			arena.setEndLocation(LocationSerializer.fromString(config.getString(path + "endLocation")));
			arena.setPlateLocation(LocationSerializer.fromString(config.getString(path + "plateLocation")));
			arena.setArenaPlate(XMaterial.valueOf(config.getString(path + "arenaPlate")));

			if (!config.isSet(path + "hologramLocation") || LocationSerializer.isDefaultLocation(config.getString(path + "hologramLocation"))) {
				player.sendMessage(chatManager.coloredRawMessage("&a&l✔ &aNo hologram location found skipping hologram creation."));
			} else {
				final Hologram hologram = new Hologram(LocationSerializer.fromString(config.getString(path + "hologramLocation")), chatManager.message("In-Game.Last-King-Hologram").replace("%king%", arena.getKingName()));
				arena.setHologram(hologram);
			}

			player.sendMessage(chatManager.coloredRawMessage("&a&l✔ &aValidation succeeded! Registering new arena instance: &e" + arena.getId()));

			config.set(path + "isdone", true);
			saveConfig();

			ArenaRegistry.registerArena(arena);
		}), 7, 3);
	}
}