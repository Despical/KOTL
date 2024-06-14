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

package me.despical.kotl.kits;

import me.despical.commons.compat.XMaterial;
import me.despical.commons.item.ItemBuilder;
import me.despical.commons.number.NumberUtils;
import me.despical.commons.reflection.XReflection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class Kit {

	private final String permission;
	private final Set<ItemStack> armors;
	private final Map<Integer, ItemStack> items;

	public Kit(FileConfiguration config, String path) {
		this.armors = new LinkedHashSet<>();
		this.items = new HashMap<>();

		this.permission = config.getString(path + "permission");

		for (var armor : config.getStringList(path + "armors")) {
			armors.add(XMaterial.valueOf(armor).parseItem());
		}

		for (final var item : config.getStringList(path + "items")) {
			var array = item.split(":");
			var builder = new ItemBuilder(XMaterial.valueOf(array[1].toUpperCase()));

			if (XReflection.supports(9))
				builder = builder.unbreakable(true);

			if (array.length == 4) {
				builder.enchantment(Enchantment.getByName(array[2].toUpperCase()), NumberUtils.getInt(array[3], 1));
			}

			items.put(NumberUtils.getInt(array[0]), builder.build());
		}
	}

	public void giveKit(Player player) {
		final var inventory = player.getInventory();

		inventory.setArmorContents(armors.toArray(new ItemStack[0]));
		this.items.forEach(inventory::setItem);
	}

	public boolean hasPermission(Player player) {
		return permission.isEmpty() || player.hasPermission(permission);
	}
}