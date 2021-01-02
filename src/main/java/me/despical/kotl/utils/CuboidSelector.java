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

package me.despical.kotl.utils;

import java.util.HashMap;
import java.util.Map;

import me.despical.kotl.handlers.ChatManager;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import me.despical.commonsbox.item.ItemBuilder;
import me.despical.commonsbox.item.ItemUtils;
import me.despical.kotl.Main;

/**
 * @author Despical
 * <p>
 * Created at 24.06.2020
 */
public class CuboidSelector implements Listener {

	private final Main plugin;
	private final ChatManager chatManager;
	private final Map<Player, Selection> selections = new HashMap<>();

	public CuboidSelector(Main plugin) {
		this.plugin = plugin;
		this.chatManager = plugin.getChatManager();

		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}

	public void giveSelectorWand(Player p) {
		ItemStack stack = new ItemBuilder(Material.BLAZE_ROD)
			.name("&6&lArea selector")
			.lore("&eLEFT CLICK to select first corner.")
			.lore("&eRIGHT CLICK to select second corner.")
			.build();
		p.getInventory().addItem(stack);
		
		p.sendMessage(chatManager.getPrefix() + chatManager.colorRawMessage("&eYou received area selector wand!"));
		p.sendMessage(chatManager.getPrefix() + chatManager.colorRawMessage("&eSelect bottom corner using left click!"));
	}

	public Selection getSelection(Player p) {
		return selections.get(p);
	}

	public void removeSelection(Player p) {
		selections.remove(p);
	}

	@EventHandler
	public void onWandUse(PlayerInteractEvent e) {
		if (!ItemUtils.isNamed(e.getItem()) || !e.getItem().getItemMeta().getDisplayName().equals(plugin.getChatManager().colorRawMessage("&6&lArea selector"))) {
			return;
		}

		e.setCancelled(true);

		switch (e.getAction()) {
			case LEFT_CLICK_BLOCK:
				selections.put(e.getPlayer(), new Selection(e.getClickedBlock().getLocation(), null));
				e.getPlayer().sendMessage(chatManager.colorRawMessage("&e✔ Completed | &aNow select top corner using right click!"));
				break;
			case RIGHT_CLICK_BLOCK:
				if (!selections.containsKey(e.getPlayer())) {
					e.getPlayer().sendMessage(chatManager.colorRawMessage("&c&l✖ &cWarning | Please select bottom corner using left click first!"));
					break;
				}

				selections.put(e.getPlayer(), new Selection(selections.get(e.getPlayer()).getFirstPos(), e.getClickedBlock().getLocation()));

				e.getPlayer().sendMessage(chatManager.colorRawMessage("&e✔ Completed | &aNow you can set the area via setup menu!"));
				break;
			case LEFT_CLICK_AIR:
			case RIGHT_CLICK_AIR:
				e.getPlayer().sendMessage(chatManager.colorRawMessage("&c&l✖ &cWarning | Please select solid block (not air)!"));
				break;
			default:
				break;
		}
	}

	public static class Selection {

		private final Location firstPos;
		private final Location secondPos;

		public Selection(Location firstPos, Location secondPos) {
			this.firstPos = firstPos;
			this.secondPos = secondPos;
		}

		public Location getFirstPos() {
			return firstPos;
		}

		public Location getSecondPos() {
			return secondPos;
		}
	}
}