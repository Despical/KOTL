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

import me.despical.commons.compat.VersionResolver;
import me.despical.commons.compat.XMaterial;
import me.despical.commons.item.ItemBuilder;
import me.despical.commons.serializer.LocationSerializer;
import me.despical.inventoryframework.Gui;
import me.despical.inventoryframework.GuiItem;
import me.despical.inventoryframework.pane.StaticPane;
import me.despical.kotl.arena.Arena;
import me.despical.kotl.handler.hologram.Hologram;
import me.despical.kotl.handler.setup.SetupInventory;
import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 * @author Despical
 * <p>
 * Created at 22.06.2020
 */
public class MiscComponents implements SetupComponent {

	@Override
	public void injectComponents(SetupInventory setupInventory, StaticPane pane) {
		final Player player = setupInventory.getPlayer();
		final Arena arena = setupInventory.getArena();
		final String path = "instances." + arena.getId() + ".";

		pane.addItem(GuiItem.of(new ItemBuilder(XMaterial.ARMOR_STAND)
			.name("&e&lSet King Hologram")
			.lore("&7Click to set king's hologram location")
			.lore("&7on the place where you are standing.")
			.lore("&8(where the last king displays)")
			.lore("")
			.lore("&8Holograms may be buggy with some servers.")
			.lore("&8We're going to add support for popular plugins.")
			.lore("", setupInventory.getSetupUtilities().isOptionDoneBool(path + "hologramLocation"))
			.build(), e -> {
			
			player.closeInventory();

			final Location location = player.getLocation();
			player.sendMessage(chatManager.coloredRawMessage("&e✔ Completed | &aHologram location for arena &e" + arena.getId() + " &aset at your location!"));

			final Hologram hologram = new Hologram(location, chatManager.message("In-Game.Last-King-Hologram").replace("%king%", arena.getKingName()));
			arena.setHologram(hologram);
			arena.setHologramLocation(location);

			config.set(path + "hologramLocation", LocationSerializer.toString(location));
			saveConfig();
		}), 4, 1);

		pane.addItem(GuiItem.of(new ItemBuilder(XMaterial.ENCHANTED_BOOK)
			.name(chatManager.coloredRawMessage("&e&lChange Arena Plate"))
			.lore("&7Click here to change arena plate.")
			.lore("&8(Opens arena plate changer menu)")
			.build(), e -> {

			setupInventory.getPaginatedPane().setPage(2);

			final Gui gui = setupInventory.getGui();
			gui.setRows(VersionResolver.isCurrentEqualOrHigher(VersionResolver.ServerVersion.v1_13_R1) ? 6 : 4);
			gui.setTitle("Arena Plate Editor");
			gui.update();
		}), 5, 1);
		
		pane.addItem(GuiItem.of(new ItemBuilder(XMaterial.FILLED_MAP)
			.name(chatManager.coloredRawMessage("&e&lView Setup Video"))
			.lore("&7Having problems with setup or wanna know")
			.lore("&7some useful tips? Click to get video link!")
			.build(), e -> {
			
			player.closeInventory();
			player.sendMessage(chatManager.prefixedRawMessage("&aCheck out this video: &7" + SetupInventory.TUTORIAL_VIDEO));
		}), 6, 3);
	}
}