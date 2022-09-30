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
import me.despical.commons.configuration.ConfigUtils;
import me.despical.commons.item.ItemBuilder;
import me.despical.inventoryframework.Gui;
import me.despical.inventoryframework.GuiItem;
import me.despical.inventoryframework.pane.StaticPane;
import me.despical.kotl.arena.Arena;
import me.despical.kotl.handler.setup.SetupInventory;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Despical
 * <p>
 * Created at 30.09.2022
 */
public class PressurePlateComponents implements SetupComponent {

	@Override
	public void injectComponents(SetupInventory setupInventory, StaticPane pane) {
		final Player player = setupInventory.getPlayer();
		final Arena arena = setupInventory.getArena();
		final String path = "instances." + arena.getId() + ".";

		final StaticPane pressurePlatesPane = new StaticPane(9, 6);
		pressurePlatesPane.fillWith(new ItemBuilder(XMaterial.BLACK_STAINED_GLASS_PANE)
			.name("&eClick to change plate!")
			.lore("", "&7Current plate: &a" + arena.getArenaPlate().name()).build());

		setupInventory.getPaginatedPane().addPane(2, pressurePlatesPane);

		final List<XMaterial> pressurePlates = new ArrayList<XMaterial>() {{
			add(XMaterial.OAK_PRESSURE_PLATE);
			add(XMaterial.STONE_PRESSURE_PLATE);
			add(XMaterial.LIGHT_WEIGHTED_PRESSURE_PLATE);
			add(XMaterial.HEAVY_WEIGHTED_PRESSURE_PLATE); // 1.8

			if (VersionResolver.isCurrentEqualOrHigher(VersionResolver.ServerVersion.v1_13_R1)) {
				add(XMaterial.ACACIA_PRESSURE_PLATE);
				add(XMaterial.BIRCH_PRESSURE_PLATE);
				add(XMaterial.SPRUCE_PRESSURE_PLATE);
				add(XMaterial.DARK_OAK_PRESSURE_PLATE);
				add(XMaterial.JUNGLE_PRESSURE_PLATE); // 1.13
			}

			if (VersionResolver.isCurrentEqualOrHigher(VersionResolver.ServerVersion.v1_16_R1)) {
				add(XMaterial.CRIMSON_PRESSURE_PLATE);
				add(XMaterial.POLISHED_BLACKSTONE_PRESSURE_PLATE);
				add(XMaterial.WARPED_PRESSURE_PLATE); // 1.16
			}
		}};

		final List<Integer> slots = getSlots(pressurePlates.size());

		for (int i = 0; i < pressurePlates.size(); i++) {
			final int slot = slots.get(i);
			final XMaterial plate = pressurePlates.get(i);
			final ItemBuilder itemBuilder = new ItemBuilder(plate);

			if (arena.getArenaPlate().equals(plate)) {
				itemBuilder.name("&7This plate is the current arena plate.").enchantment(Enchantment.ARROW_DAMAGE).flag(ItemFlag.HIDE_ENCHANTS);
			} else {
				itemBuilder.name("&7Click to change plate!");
			}

			pressurePlatesPane.addItem(GuiItem.of(itemBuilder.build(), inventoryClickEvent -> {
				player.closeInventory();
				player.sendMessage(chatManager.coloredRawMessage("&e✔ Completed | &aArena plate for arena &e" + arena.getId() + " &achanged to &e" + plate.name()));

				arena.setArenaPlate(plate);

				config.set(path + "arenaPlate", plate.name());
				ConfigUtils.saveConfig(plugin, config, "arenas");
			}), slot % 9, slot / 9);
		}

		pressurePlatesPane.addItem(GuiItem.of(new ItemBuilder(XMaterial.REDSTONE)
			.name("&e&lRestore menu")
			.lore("&7Click here to go back.")
			.build(), e -> {

			setupInventory.getPaginatedPane().setPage(0);

			final Gui gui = setupInventory.getGui();
			gui.setRows(5);
			gui.setTitle("Arena Setup Menu");
			gui.update();
		}), 8, 5);
	}

	private List<Integer> getSlots(int size) {
		final List<Integer> slots = new ArrayList<>(Arrays.asList(10, 12, 14, 16));

		if (size == 9) {
			slots.addAll(Arrays.asList(28, 30, 32, 34, 40));
		} else {
			slots.addAll(Arrays.asList(20, 24, 28, 30, 32, 34, 38, 42));
		}

		return slots;
	}
}