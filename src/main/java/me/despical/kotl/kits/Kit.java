/*
 * KOTL - Don't let others climb to top of the ladders!
 * Copyright (C) 2025  Berke Akçen
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

import com.cryptomorin.xseries.XEnchantment;
import me.despical.commons.XMaterial;
import me.despical.commons.item.ItemBuilder;
import me.despical.commons.number.NumberUtils;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

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

        for (String armor : config.getStringList(path + "armors")) {
            String[] attributes = armor.split(":");

            ItemBuilder builder = new ItemBuilder(XMaterial.matchXMaterial(attributes[0].toUpperCase()))
                .unbreakable(true)
                .hideTooltip();

            if (attributes.length == 3) {
                builder.enchantment(XEnchantment.of(attributes[1].toUpperCase()).orElseThrow().get(), NumberUtils.getInt(attributes[2], 1));
            }

            armors.add(builder.build());
        }

        for (String item : config.getStringList(path + "items")) {
            String[] attributes = item.split(":");
            ItemBuilder builder = new ItemBuilder(XMaterial.matchXMaterial(attributes[1].toUpperCase()))
                .unbreakable(true)
                .hideTooltip();

            if (attributes.length == 3) {
                int amount = NumberUtils.getInt(attributes[2], 1);

                builder.amount(amount);
            }

            if (attributes.length == 4) {
                builder.enchantment(XEnchantment.of(attributes[2].toUpperCase()).orElseThrow().get(), NumberUtils.getInt(attributes[3], 1));
            }

            int slot = NumberUtils.getInt(attributes[0]);
            items.put(slot, builder.build());
        }
    }

    public void giveKit(Player player) {
        PlayerInventory inventory = player.getInventory();
        inventory.setArmorContents(armors.toArray(ItemStack[]::new));

        items.forEach(inventory::setItem);
    }

    public boolean hasPermission(Player player) {
        return permission.isEmpty() || player.hasPermission(permission);
    }
}
