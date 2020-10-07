/*
 * KOTL - Don't let others to climb top of the ladders!
 * Copyright (C) 2020 Despical
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package me.despical.kotl.handlers.setup.components;

import com.github.despical.inventoryframework.GuiItem;
import com.github.despical.inventoryframework.pane.StaticPane;
import me.despical.commonsbox.compat.XMaterial;
import me.despical.commonsbox.configuration.ConfigUtils;
import me.despical.commonsbox.item.ItemBuilder;
import me.despical.commonsbox.serializer.LocationSerializer;
import me.despical.kotl.Main;
import me.despical.kotl.arena.Arena;
import me.despical.kotl.handlers.hologram.Hologram;
import me.despical.kotl.handlers.setup.SetupInventory;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

/**
 * @author Despical
 * <p>
 * Created at 22.06.2020
 */
public class MiscComponents implements SetupComponent {

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

		pane.addItem(new GuiItem(new ItemBuilder(XMaterial.ARMOR_STAND.parseItem())
			.name("&e&lSet King Hologram")
			.lore("&7Click to set king's hologram location")
			.lore("&7on the place where you are standing.")
			.lore("&8(where the last king displays)")
			.lore("", setupInventory.getSetupUtilities()
			.isOptionDoneBool("instances." + arena.getId() + ".hologramLocation"))
			.build(), e -> {
				e.getWhoClicked().closeInventory();

				if(arena.getHologram() != null) {
					arena.getHologram().delete();
				}

				config.set("instances." + arena.getId() + ".hologramLocation", LocationSerializer.locationToString(player.getLocation()));
				player.sendMessage(plugin.getChatManager().colorRawMessage("&e✔ Completed | &aHologram location for arena " + arena.getId() + " set at your location!"));

				Hologram hologram = new Hologram(player.getLocation(), plugin.getChatManager().colorMessage("In-Game.Last-King-Hologram").replace("%king%", arena.getKing() == null ? plugin.getChatManager().colorMessage("In-Game.There-Is-No-King") : arena.getKing().getName()));

				arena.setHologram(hologram);
				arena.setHologramLocation(hologram.getLocation());
				ConfigUtils.saveConfig(plugin, config, "arenas");
		}), 3, 0);		
		
		pane.addItem(new GuiItem(new ItemBuilder(XMaterial.FILLED_MAP.parseItem())
			.name(plugin.getChatManager().colorRawMessage("&e&lView Wiki Page"))
			.lore("&7Having problems with setup or wanna know")
			.lore("&7some useful tips? Click to get wiki link!")
			.build(), e -> {
				e.getWhoClicked().closeInventory();

				player.sendMessage(plugin.getChatManager().getPrefix()+ plugin.getChatManager().colorRawMessage("&aCheck out our wiki: https://github.com/Despical/KOTL/wiki"));
		}), 7, 0);
	}
}