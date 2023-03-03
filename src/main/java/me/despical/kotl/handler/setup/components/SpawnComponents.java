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

package me.despical.kotl.handler.setup.components;

import me.despical.commons.compat.VersionResolver;
import me.despical.commons.compat.XMaterial;
import me.despical.commons.item.ItemBuilder;
import me.despical.commons.serializer.LocationSerializer;
import me.despical.inventoryframework.Gui;
import me.despical.inventoryframework.GuiItem;
import me.despical.inventoryframework.pane.StaticPane;
import me.despical.kotl.arena.Arena;
import me.despical.kotl.handler.setup.SetupInventory;
import me.despical.kotl.util.CuboidSelector;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

/**
 * @author Despical
 * <p>
 * Created at 22.06.2020
 */
public class SpawnComponents implements SetupInventory.SetupComponent {

	@Override
	public void injectComponents(SetupInventory setupInventory, StaticPane pane) {
		final Player player = setupInventory.getPlayer();
		final Arena arena = setupInventory.getArena();
		final String path = "instances." + arena.getId() + ".";

		pane.addItem(new GuiItem(new ItemBuilder(XMaterial.REDSTONE_BLOCK)
			.name("&e&lSet Ending Location")
			.lore("&7Click to set ending location on")
			.lore("&7the place where you are standing.")
			.lore("&8(location where players will be")
			.lore("&8teleported after the reloading)")
			.lore("", isOptionDoneBool(path + "endLocation"))
			.build(), e -> {

			player.closeInventory();

			Location location = player.getLocation();
			player.sendMessage(chatManager.coloredRawMessage("&e✔ Completed | &aEnding location for arena &e" + arena.getId() + " &aset at your location!"));

			arena.setEndLocation(location);

			config.set(path + "endLocation", LocationSerializer.toString(location));
			saveConfig();
		}), 1, 1);
		
		pane.addItem(GuiItem.of(new ItemBuilder(arena.getArenaPlate())
			.name("&e&lSet Plate Location")
			.lore("&7Click to set plate location on")
			.lore("&7the place where you are standing.")
			.lore("&8(location where players will try to")
			.lore("&8reach)")
			.lore("", isOptionDoneBool(path + "plateLocation"))
			.build(), e -> {

			player.closeInventory();

			final Location location = player.getLocation();
			location.getBlock().getRelative(BlockFace.DOWN).setType(arena.getArenaPlate().parseMaterial());

			arena.setPlateLocation(location);
			player.sendMessage(chatManager.coloredRawMessage("&e✔ Completed | &aPlate location for arena &e" + arena.getId() + " &aset at your location!"));

			config.set(path + "plateLocation", LocationSerializer.toString(location.getBlock().getRelative(BlockFace.DOWN).getLocation()));
			saveConfig();
		}), 5, 1);

		pane.addItem(GuiItem.of(new ItemBuilder(XMaterial.BLAZE_ROD.parseItem())
			.name("&e&lSet Arena Region")
			.lore("&7Click to set arena's region")
			.lore("&7with the cuboid selector.")
			.lore("&8(area where game will be playing)")
			.lore("", isOptionDoneBool(path + "areaMax"))
			.build(), e -> {

			player.closeInventory();

			final CuboidSelector.Selection selection = plugin.getCuboidSelector().getSelection(player);

			if (plugin.getCuboidSelector().giveSelectorWand(player)) return;

			if (selection.secondPos == null) {
				player.sendMessage(chatManager.coloredRawMessage("&c&l✖ &cWarning | Please select top corner using right click!"));
				return;
			}

			config.set(path + "areaMin", LocationSerializer.toString(selection.firstPos));
			config.set(path + "areaMax", LocationSerializer.toString(selection.secondPos));

			arena.setMinCorner(selection.firstPos);
			arena.setMaxCorner(selection.secondPos);

			player.sendMessage(chatManager.coloredRawMessage("&e✔ Completed | &aGame area of arena &e" + arena.getId() + " &aset as you selection!"));
			plugin.getCuboidSelector().removeSelection(player);

			saveConfig();
		}), 3, 1);

		pane.addItem(GuiItem.of(new ItemBuilder(XMaterial.ENCHANTED_BOOK)
			.name(chatManager.coloredRawMessage("&e&lChange Arena Plate"))
			.lore("&7Click here to change arena plate.")
			.lore("&8(opens arena plate changer menu)")
			.build(), e -> {

			setupInventory.getPaginatedPane().setPage(2);

			final Gui gui = setupInventory.getGui();
			gui.setRows(VersionResolver.isCurrentEqualOrHigher(VersionResolver.ServerVersion.v1_13_R1) ? 6 : 4);
			gui.setTitle("Arena Plate Editor");
			gui.update();
		}), 7, 1);
	}
}