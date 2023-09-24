package me.despical.kotl.kits;

import me.despical.commons.compat.XMaterial;
import me.despical.commons.configuration.ConfigUtils;
import me.despical.commons.item.ItemBuilder;
import me.despical.kotl.Main;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class Kit {

	private final String permission;
	private final Set<ItemStack> armors;
	private final Map<Integer, ItemStack> items;

	public Kit(Main plugin, String path) {
		this.armors = new LinkedHashSet<>();
		this.items = new HashMap<>();

		final var config = ConfigUtils.getConfig(plugin, "kits");

		this.permission = config.getString(path + "permission");

		for (final var armor : config.getStringList(path + "armors")) {
			armors.add(XMaterial.valueOf(armor).parseItem());
		}

		for (final var item : config.getStringList(path + "items")) {
			final var array = item.split(":");

			items.put(Integer.parseInt(array[0]), new ItemBuilder(XMaterial.valueOf(array[1].toUpperCase())).unbreakable(true).build());
		}
	}

	public void giveKit(Player player) {
		final var inventory = player.getInventory();

		inventory.setArmorContents(armors.toArray(new ItemStack[0]));

		for (final var entry : this.items.entrySet()) {
			inventory.setItem(entry.getKey(), entry.getValue());
		}
	}

	public boolean hasPermission(Player player) {
		return permission.isEmpty() || player.hasPermission(permission);
	}
}