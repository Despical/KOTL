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

package me.despical.kotl.handlers.setup.components;

import me.despical.commonsbox.compat.XMaterial;
import me.despical.commonsbox.configuration.ConfigUtils;
import me.despical.commonsbox.item.ItemBuilder;
import me.despical.commonsbox.serializer.LocationSerializer;
import me.despical.inventoryframework.GuiItem;
import me.despical.inventoryframework.pane.StaticPane;
import me.despical.kotl.Main;
import me.despical.kotl.arena.Arena;
import me.despical.kotl.handlers.setup.SetupInventory;
import me.despical.kotl.utils.CuboidSelector;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

/**
 * @author Despical
 * <p>
 * Created at 22.06.2020
 */
public class SpawnComponents implements SetupComponent {

	private SetupInventory setupInventory;

	@Override
	public void prepare(SetupInventory setupInventory) {
		this.setupInventory = setupInventory;
	}

	@Override
	public void injectComponents(StaticPane pane) {
		Player player = setupInventory.getPlayer();
		FileConfiguration config = setupInventory.getConfig();
		Arena arena = setupInventory.getArena();
		Main plugin = setupInventory.getPlugin();
		String s = "instances." + arena.getId() + ".";

		pane.addItem(new GuiItem(new ItemBuilder(Material.REDSTONE_BLOCK)
			.name("&e&lSet Ending Location")
			.lore("&7Click to set ending location on")
			.lore("&7the place where you are standing.")
			.lore("&8(location where players will be")
			.lore("&8teleported after the reloading)")
			.lore("", setupInventory.getSetupUtilities()
			.isOptionDoneBool(s + "endLocation"))
			.build(), e -> {
			e.getWhoClicked().closeInventory();
			config.set(s + "endLocation", LocationSerializer.locationToString(player.getLocation()));
			arena.setEndLocation(player.getLocation());
			player.sendMessage(plugin.getChatManager().colorRawMessage("&e✔ Completed | &aEnding location for arena " + arena.getId() + " set at your location!"));

			ConfigUtils.saveConfig(plugin, config, "arenas");
		}), 0, 0);
		
		pane.addItem(new GuiItem(new ItemBuilder(XMaterial.OAK_PRESSURE_PLATE.parseMaterial())
			.name("&e&lSet Plate Location")
			.lore("&7Click to set plate location on")
			.lore("&7the place where you are standing.")
			.lore("&8(location where players will try to")
			.lore("&8reach)")
			.lore("", setupInventory.getSetupUtilities().isOptionDoneBool(s + "plateLocation"))
			.build(), e -> {
			e.getWhoClicked().closeInventory();
			player.getLocation().getBlock().getRelative(BlockFace.DOWN).setType(XMaterial.OAK_PRESSURE_PLATE.parseMaterial());
			config.set(s + "plateLocation", LocationSerializer.locationToString(player.getLocation().getBlock().getRelative(BlockFace.DOWN).getLocation()));
			arena.setPlateLocation(player.getLocation());
			player.sendMessage(plugin.getChatManager().colorRawMessage("&e✔ Completed | &aPlate location for arena " + arena.getId() + " set at your location!"));

			ConfigUtils.saveConfig(plugin, config, "arenas");
		}), 1, 0);

		pane.addItem(new GuiItem(new ItemBuilder(XMaterial.BLAZE_ROD.parseItem())
			.name("&e&lSet Arena Region")
			.lore("&7Click to set arena's region")
			.lore("&7with the cuboid selector.")
			.lore("&8(area where game will be playing)")
			.lore("", setupInventory.getSetupUtilities().isOptionDoneBool(s + "areaMax"))
			.build(), e -> {
			e.getWhoClicked().closeInventory();

			CuboidSelector.Selection selection = plugin.getCuboidSelector().getSelection(player);

			if (selection == null) {
				plugin.getCuboidSelector().giveSelectorWand(player);
				return;
			}

			if (selection.getSecondPos() == null) {
				player.sendMessage(plugin.getChatManager().colorRawMessage("&c&l✖ &cWarning | Please select top corner using right click!"));
				return;
			}

			config.set(s + "areaMin", LocationSerializer.locationToString(selection.getFirstPos()));
			config.set(s + "areaMax", LocationSerializer.locationToString(selection.getSecondPos()));
			player.sendMessage(plugin.getChatManager().colorRawMessage("&e✔ Completed | &aGame area of arena " + arena.getId() + " set as you selection!"));
			plugin.getCuboidSelector().removeSelection(player);

			ConfigUtils.saveConfig(plugin, config, "arenas");
		}), 2, 0);
	}
}