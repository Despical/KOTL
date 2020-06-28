package me.despical.kotl.handler.setup.components;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import com.github.stefvanschie.inventoryframework.GuiItem;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;

import me.despical.commonsbox.compat.XMaterial;
import me.despical.commonsbox.configuration.ConfigUtils;
import me.despical.commonsbox.item.ItemBuilder;
import me.despical.commonsbox.serializer.LocationSerializer;
import me.despical.kotl.Main;
import me.despical.kotl.arena.Arena;
import me.despical.kotl.arena.ArenaRegistry;
import me.despical.kotl.handler.setup.SetupInventory;

/**
 * @author Despical
 * <p>
 *  Created at 22.06.2020
 */
public class ArenaRegisterComponents implements SetupComponent {

	private SetupInventory setupInventory;

	@Override
	public void prepare(SetupInventory setupInventory) {
		this.setupInventory = setupInventory;
	}

	@Override
	public void injectComponents(StaticPane pane) {
		FileConfiguration config = setupInventory.getConfig();
		Main plugin = setupInventory.getPlugin();
		ItemStack registeredItem;
		if (!config.getBoolean("instances." + setupInventory.getArena().getId() + ".isdone", false)) {
			registeredItem = new ItemBuilder(XMaterial.FIREWORK_ROCKET.parseItem())
				.name(plugin.getChatManager().colorRawMessage("&e&lRegister Arena - Finish Setup"))
				.lore(ChatColor.GRAY + "Click this when you're done with configuration.")
				.lore(ChatColor.GRAY + "It will validate and register arena.")
				.build();
		} else {
			registeredItem = new ItemBuilder(Material.BARRIER)
				.name(plugin.getChatManager().colorRawMessage("&a&lArena Registered - Congratulations"))
				.lore(ChatColor.GRAY + "This arena is already registered!")
				.lore(ChatColor.GRAY + "Good job, you went through whole setup!")
				.enchantment(Enchantment.ARROW_DAMAGE)
				.flag(ItemFlag.HIDE_ENCHANTS)
				.build();
		}

		pane.addItem(new GuiItem(registeredItem, e -> {
			Arena arena = setupInventory.getArena();
			e.getWhoClicked().closeInventory();
			if (config.getBoolean("instances." + arena.getId() + ".isdone", false)) {
				e.getWhoClicked().sendMessage(plugin.getChatManager().colorRawMessage("&a&l✔ &aThis arena was already validated and is ready to use!"));
				return;
			}
			String[] locations = new String[] {"plateLocation", "hologramLocation", "endLocation", "areaMin", "areaMax"};
			for (String s : locations) {
				if (!config.isSet("instances." + arena.getId() + "." + s) || config.getString("instances." + arena.getId() + "." + s).equals(
					LocationSerializer.locationToString(Bukkit.getWorlds().get(0).getSpawnLocation()))) {
					e.getWhoClicked().sendMessage(plugin.getChatManager().colorRawMessage("&c&l✘ &cArena validation failed! Please configure following spawn properly: " + s + " (cannot be world spawn location)"));
					return;
				}
			}
			if (setupInventory.getArena().getHologram() != null) setupInventory.getArena().getHologram().delete();
			arena = new Arena(setupInventory.getArena().getId());
			arena.setReady(true);
			arena.setEndLocation(LocationSerializer.locationFromString(config.getString("instances." + arena.getId() + ".endLocation")));
			arena.setPlateLocation(LocationSerializer.locationFromString(config.getString("instances." + arena.getId() + ".plateLocation")));
			
			Hologram hologram = HologramsAPI.createHologram(plugin, LocationSerializer.locationFromString(config.getString("instances." + arena.getId() + ".hologramLocation")));
			hologram.setAllowPlaceholders(true);
			hologram.appendTextLine(plugin.getChatManager().colorMessage("In-Game.Last-King-Hologram").replace("%king%", arena.getKing() == null ? "Nobody" : arena.getKing().getName()));
			arena.setHologram(hologram);
			arena.setHologramLocation(hologram.getLocation());
			ArenaRegistry.unregisterArena(setupInventory.getArena());
			e.getWhoClicked().sendMessage(plugin.getChatManager().colorRawMessage("&a&l✔ &aValidation succeeded! Registering new arena instance: " + arena.getId()));
			config.set("instances." + arena.getId() + ".isdone", true);
			ConfigUtils.saveConfig(plugin, config, "arenas");
			ArenaRegistry.registerArena(arena);
		}), 8, 0);
	}
}