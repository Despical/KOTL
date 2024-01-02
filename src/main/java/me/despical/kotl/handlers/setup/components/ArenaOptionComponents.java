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

package me.despical.kotl.handlers.setup.components;

import me.despical.commons.compat.XMaterial;
import me.despical.commons.item.ItemBuilder;
import me.despical.inventoryframework.GuiItem;
import me.despical.inventoryframework.pane.StaticPane;
import me.despical.kotl.handlers.setup.SetupInventory;

/**
 * @author Despical
 * <p>
 * Created at 30.09.2023
 */
public class ArenaOptionComponents implements SetupInventory.SetupComponent {

	private static boolean supportsParticle;

	@Override
	public void injectComponents(SetupInventory setup, StaticPane pane) {
		final var arena = setup.getArena();
		final var path = "instances.%s.".formatted(arena.getId());
		final var arenaOptions = new StaticPane(9, 3);

		final var outlineItem = supportsParticle ? new ItemBuilder(arena.isShowOutlines() ? XMaterial.ENDER_PEARL : XMaterial.ENDER_EYE).name("           " + (arena.isShowOutlines() ? "&c&lDisable" : "&e&lEnable") + " Outline Particles           ").lore("&7You can create particles around the game arena.") : new ItemBuilder(XMaterial.BARREL).name("&c&lYour server does not support Particles!");

		arenaOptions.fillWith(new ItemBuilder(XMaterial.BLACK_STAINED_GLASS_PANE).name("&7Current plate: &a" + arena.getArenaPlate().toString()).build());

		setup.getPaginatedPane().addPane(3, arenaOptions);

		arenaOptions.addItem(GuiItem.of(outlineItem.build(), e -> {
			arena.setShowOutlines(!arena.isShowOutlines());

			config.set(path + "showOutlines", arena.isShowOutlines());
			saveConfig();

			setup.getPlayer().closeInventory();
		}), 4, 1);

		arenaOptions.addItem(GuiItem.of(mainMenuItem, event -> setup.restorePage()), 8, 2);
	}

	static {
		try {
			Class.forName("org.bukkit.Particle");

			supportsParticle = true;
		} catch (ClassNotFoundException exception) {
			supportsParticle = false;
		}
	}
}