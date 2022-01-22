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
import me.despical.commons.configuration.ConfigUtils;
import me.despical.commons.item.ItemBuilder;
import me.despical.commons.serializer.LocationSerializer;
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
public class SpawnComponents implements SetupComponent {

	@Override
	public void injectComponents(SetupInventory setupInventory, StaticPane pane) {
		Player player = setupInventory.getPlayer();
		Arena arena = setupInventory.getArena();
		String path = "instances." + arena.getId() + ".";

		pane.addItem(new GuiItem(new ItemBuilder(XMaterial.REDSTONE_BLOCK)
			.name("&e&lSet Ending Location")
			.lore("&7Click to set ending location on")
			.lore("&7the place where you are standing.")
			.lore("&8(location where players will be")
			.lore("&8teleported after the reloading)")
			.lore("", setupInventory.getSetupUtilities().isOptionDoneBool(path + "endLocation"))
			.build(), e -> {

			player.closeInventory();

			Location location = player.getLocation();
			player.sendMessage(chatManager.coloredRawMessage("&e✔ Completed | &aEnding location for arena " + arena.getId() + " set at your location!"));

			arena.setEndLocation(location);

			config.set(path + "endLocation", LocationSerializer.toString(location));
			ConfigUtils.saveConfig(plugin, config, "arenas");
		}), 1, 1);
		
		pane.addItem(GuiItem.of(new ItemBuilder(XMaterial.OAK_PRESSURE_PLATE)
			.name("&e&lSet Plate Location")
			.lore("&7Click to set plate location on")
			.lore("&7the place where you are standing.")
			.lore("&8(location where players will try to")
			.lore("&8reach)")
			.lore("", setupInventory.getSetupUtilities().isOptionDoneBool(path + "plateLocation"))
			.build(), e -> {

			player.closeInventory();

			Location location = player.getLocation();
			location.getBlock().getRelative(BlockFace.DOWN).setType(XMaterial.OAK_PRESSURE_PLATE.parseMaterial());

			arena.setPlateLocation(location);
			player.sendMessage(chatManager.coloredRawMessage("&e✔ Completed | &aPlate location for arena " + arena.getId() + " set at your location!"));

			config.set(path + "plateLocation", LocationSerializer.toString(location.getBlock().getRelative(BlockFace.DOWN).getLocation()));
			ConfigUtils.saveConfig(plugin, config, "arenas");
		}), 2, 1);

		pane.addItem(GuiItem.of(new ItemBuilder(XMaterial.BLAZE_ROD.parseItem())
			.name("&e&lSet Arena Region")
			.lore("&7Click to set arena's region")
			.lore("&7with the cuboid selector.")
			.lore("&8(area where game will be playing)")
			.lore("", setupInventory.getSetupUtilities().isOptionDoneBool(path + "areaMax"))
			.build(), e -> {

			player.closeInventory();

			CuboidSelector.Selection selection = plugin.getCuboidSelector().getSelection(player);

			if (selection == null) {
				plugin.getCuboidSelector().giveSelectorWand(player);
				return;
			}

			if (selection.secondPos == null) {
				player.sendMessage(chatManager.coloredRawMessage("&c&l✖ &cWarning | Please select top corner using right click!"));
				return;
			}

			config.set(path + "areaMin", LocationSerializer.toString(selection.firstPos));
			config.set(path + "areaMax", LocationSerializer.toString(selection.secondPos));
			player.sendMessage(chatManager.coloredRawMessage("&e✔ Completed | &aGame area of arena " + arena.getId() + " set as you selection!"));
			plugin.getCuboidSelector().removeSelection(player);

			ConfigUtils.saveConfig(plugin, config, "arenas");
		}), 3, 1);
	}
}