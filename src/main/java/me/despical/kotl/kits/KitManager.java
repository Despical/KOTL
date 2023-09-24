package me.despical.kotl.kits;

import me.despical.commons.configuration.ConfigUtils;
import me.despical.kotl.Main;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;

public class KitManager {

	private boolean isEnabled;

	private final Main plugin;
	private final Set<Kit> kits;

	public KitManager(final Main plugin) {
		this.plugin = plugin;
		this.kits = new HashSet<>();

		this.loadKits();
	}

	public void loadKits() {
		this.kits.clear();

		final var config = ConfigUtils.getConfig(plugin, "kits");

		this.isEnabled = config.getBoolean("kits-enabled");

		for (final var path : config.getConfigurationSection("kits").getKeys(false)) {
			this.kits.add(new Kit(plugin, "kits." + path + "."));
		}
	}

	public void giveKit(final Player player) {
		if (!isEnabled) return;

		final var kit = kits.stream().filter(k -> k.hasPermission(player)).findFirst().orElse(null);

		if (kit == null) return;

		kit.giveKit(player);
	}
}