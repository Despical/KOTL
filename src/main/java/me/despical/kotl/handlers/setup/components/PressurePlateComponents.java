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

import me.despical.commons.ReflectionUtils;
import me.despical.commons.compat.XMaterial;
import me.despical.commons.item.ItemBuilder;
import me.despical.commons.serializer.LocationSerializer;
import me.despical.inventoryframework.GuiItem;
import me.despical.inventoryframework.pane.StaticPane;
import me.despical.kotl.handlers.setup.SetupInventory;
import org.bukkit.block.BlockFace;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Despical
 * <p>
 * Created at 30.09.2022
 */
public class PressurePlateComponents implements SetupInventory.SetupComponent {

	@Override
	public void injectComponents(SetupInventory setup, StaticPane pane) {
		final var player = setup.getPlayer();
		final var arena = setup.getArena();
		final var path = "instances.%s.".formatted(arena.getId());

		final var pressurePlatesPane = new StaticPane(9, 6);
		pressurePlatesPane.fillWith(new ItemBuilder(XMaterial.BLACK_STAINED_GLASS_PANE).name("&7Current plate: &a" + arena.getArenaPlate().toString()).build());

		setup.getPaginatedPane().addPane(2, pressurePlatesPane);

		final var pressurePlates = new ArrayList<XMaterial>() {{
			add(XMaterial.OAK_PRESSURE_PLATE);
			add(XMaterial.STONE_PRESSURE_PLATE); // 1.8

			if (ReflectionUtils.supports(13)) {
				add(XMaterial.ACACIA_PRESSURE_PLATE);
				add(XMaterial.BIRCH_PRESSURE_PLATE);
				add(XMaterial.SPRUCE_PRESSURE_PLATE);
				add(XMaterial.DARK_OAK_PRESSURE_PLATE);
				add(XMaterial.JUNGLE_PRESSURE_PLATE); // 1.13
			}

			if (ReflectionUtils.supports(16)) {
				add(XMaterial.CRIMSON_PRESSURE_PLATE);
				add(XMaterial.POLISHED_BLACKSTONE_PRESSURE_PLATE);
				add(XMaterial.WARPED_PRESSURE_PLATE); // 1.16
			}

			if (ReflectionUtils.supports(20)) add(XMaterial.BAMBOO_PRESSURE_PLATE);
		}};

		final var slots = getSlots(pressurePlates.size());

		for (int i = 0; i < pressurePlates.size(); i++) {
			final int slot = slots.get(i);
			final var plate = pressurePlates.get(i);
			final var itemBuilder = new ItemBuilder(plate);

			if (arena.getArenaPlate().equals(plate)) {
				itemBuilder.name("&7This plate is the current arena plate.").enchantment(Enchantment.ARROW_DAMAGE).flag(ItemFlag.HIDE_ENCHANTS);
			} else {
				itemBuilder.name("&7Click to change plate!");
			}

			pressurePlatesPane.addItem(GuiItem.of(itemBuilder.build(), inventoryClickEvent -> {
				player.closeInventory();

				config.set(path + "arenaPlate", plate.name());
				saveConfig();

				player.sendMessage(chatManager.coloredRawMessage("&e✔ Completed | &aArena plate for arena &e" + arena.getId() + " &achanged to &e" + plate));

				arena.setArenaPlate(plate);

				final var plateLoc = arena.getPlateLocation();

				if (!LocationSerializer.isDefaultLocation(plateLoc)) plateLoc.getBlock().getRelative(BlockFace.DOWN).setType(plate.parseMaterial());
			}), slot % 9, slot / 9);
		}

		pressurePlatesPane.addItem(GuiItem.of(mainMenuItem, event -> setup.restorePage()), 8, 5);
	}

	private List<Integer> getSlots(int size) {
		final var slots = me.despical.commons.util.Collections.listOf(10, 12, 14, 16);

		if (size == 9) {
			slots.addAll(Arrays.asList(28, 30, 32, 34, 40));
		} else {
			slots.addAll(Arrays.asList(20, 24, 28, 30, 32, 34, 38, 42, 44));
		}

		return slots;
	}
}