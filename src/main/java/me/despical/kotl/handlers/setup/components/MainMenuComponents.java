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
import me.despical.commons.configuration.ConfigUtils;
import me.despical.commons.item.ItemBuilder;
import me.despical.commons.reflection.XReflection;
import me.despical.commons.serializer.LocationSerializer;
import me.despical.inventoryframework.GuiItem;
import me.despical.kotl.Main;
import me.despical.kotl.arena.managers.schedulers.ArenaScheduler;
import me.despical.kotl.handlers.setup.AbstractComponent;
import me.despical.kotl.handlers.setup.SetupInventory;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;

import java.util.Optional;

/**
 * @author Despical
 * <p>
 * Created at 22.06.2020
 */
public class MainMenuComponents extends AbstractComponent {

	public MainMenuComponents(Main plugin) {
		super(plugin);
	}

	@Override
	public void injectComponents(SetupInventory setup) {
		final var player = setup.getPlayer();
		final var config = ConfigUtils.getConfig(plugin, "arenas");
		final var arena = setup.getArena();
		final var path = "instances.%s.".formatted(arena.getId());
		final var pane = setup.getPane();
		final var optionsItem = new ItemBuilder(XMaterial.CLOCK).name("&e&l        Additional Options").lore("&7Click to open additional options menu.").enchantment(Enchantment.DAMAGE_ALL).flag(ItemFlag.HIDE_ENCHANTS);

		pane.addItem(GuiItem.of(optionsItem.build(), event -> setup.setPage("   Set Additional Arena Options", 3, 3)), 4, 2);

		pane.addItem(new GuiItem(new ItemBuilder(XMaterial.REDSTONE_BLOCK)
			.name("&e&l       Set Ending Location       ")
			.lore("&7Click to set ending location on")
			.lore("&7the place where you are standing.")
			.lore("&8(location where players will be")
			.lore("&8teleported after the reloading)")
			.lore("", isOptionDoneBool(config, path + "endLocation"))
			.build(), e -> {

			setup.closeInventory();

			var location = player.getLocation();
			player.sendMessage(chatManager.coloredRawMessage("&e✔ Completed | &aEnding location for arena &e" + arena.getId() + " &aset at your location!"));

			arena.setEndLocation(location);

			config.set(path + "endLocation", LocationSerializer.toString(location));
			ConfigUtils.saveConfig(plugin, config, "arenas");
		}), 1, 1);
		
		pane.addItem(GuiItem.of(new ItemBuilder(arena.getArenaPlate())
			.name("&e&l        Set Plate Location        ")
			.lore("&7Click to set plate location on")
			.lore("&7the place where you are standing.")
			.lore("&8(location where players will try to")
			.lore("&8reach)")
			.lore("", isOptionDoneBool(config, path + "plateLocation"))
			.build(), e -> {

			setup.closeInventory();

			final var plateMaterial = arena.getArenaPlate().parseMaterial();

			// Remove the old one if present.
			Optional.ofNullable(arena.getPlateLocation()).ifPresent(location -> {
				var block = location.getBlock();

				if (block.getType() == arena.getArenaPlate().parseMaterial()) {
					block.setType(Material.AIR);
				}
			});

			final var location = player.getLocation();
			location.getBlock().setType(plateMaterial);

			arena.setPlateLocation(location);
			player.sendMessage(chatManager.coloredRawMessage("&e✔ Completed | &aPlate location for arena &e" + arena.getId() + " &aset at your location!"));

			config.set(path + "plateLocation", LocationSerializer.toString(location.getBlock().getLocation()));
			ConfigUtils.saveConfig(plugin, config, "arenas");
		}), 5, 1);

		pane.addItem(GuiItem.of(new ItemBuilder(XMaterial.BLAZE_ROD.parseItem())
			.name("&e&l         Set Arena Region         ")
			.lore("&7Click to set arena's region with the")
			.lore("&7cuboid selector.")
			.lore("&8(area where the game will be playing)")
			.lore("", isOptionDoneBool(config, path + "areaMax"))
			.build(), e -> {

			setup.closeInventory();

			final var selector =  plugin.getCuboidSelector();
			final var selection = selector.getSelection(player);

			if (selector.giveSelectorWand(player)) return;

			if (selection.secondPos() == null) {
				player.sendMessage(chatManager.coloredRawMessage("&c&l✖ &cWarning | Please select top corner using right click!"));
				return;
			}

			config.set(path + "areaMin", LocationSerializer.toString(selection.firstPos()));
			config.set(path + "areaMax", LocationSerializer.toString(selection.secondPos()));
			ConfigUtils.saveConfig(plugin, config, "arenas");

			arena.setMinCorner(selection.firstPos());
			arena.setMaxCorner(selection.secondPos());

			player.sendMessage(chatManager.coloredRawMessage("&e✔ Completed | &aGame area of arena &e" + arena.getId() + " &aset as you selection!"));
			selector.removeSelection(player);

			arena.handleOutlines();
		}), 3, 1);

		pane.addItem(GuiItem.of(new ItemBuilder(XMaterial.ENCHANTED_BOOK)
			.name("&e&l       Change Arena Plate    ")
			.lore("&7Click here to change arena plate.")
			.lore("&8(opens arena plate changer menu)")
			.build(), e -> {

			setup.getPaginatedPane().setPage(2);

			final var gui = setup.getGui();
			gui.setRows(XReflection.supports(13) ? 6 : 3);
			gui.setTitle("         Arena Plate Editor");
			gui.update();
		}), 7, 1);

		final ItemBuilder registeredItem;

		if (!arena.isReady()) {
			registeredItem = new ItemBuilder(XMaterial.FIREWORK_ROCKET)
				.name("&e&l     Register Arena - Finish Setup")
				.lore("&7Click this when you're done with configuration.")
				.lore("&7It will validate and register arena.");
		} else {
			registeredItem = new ItemBuilder(Material.BARRIER)
				.name("&a&lArena Registered - Congratulations")
				.lore("&7This arena is already registered!")
				.lore("&7Good job, you went through whole setup!")
				.enchantment(Enchantment.ARROW_DAMAGE)
				.flag(ItemFlag.HIDE_ENCHANTS);
		}

		pane.addItem(GuiItem.of(registeredItem.build(), e -> {
			setup.closeInventory();

			if (config.getBoolean(path + "isdone")) {
				player.sendMessage(chatManager.coloredRawMessage("&a&l✔ &aThis arena was already validated and is ready to use!"));
				return;
			}

			String[] locations = {"plateLocation", "endLocation", "areaMin", "areaMax"};

			for (var loc : locations) {
				if (!config.isSet(path + loc) || LocationSerializer.isDefaultLocation(config.getString(path + loc))) {
					player.sendMessage(chatManager.coloredRawMessage("&c&l✘ &cArena validation failed! Please configure following spawn properly: %s (cannot be world spawn location)".formatted(loc)));
					return;
				}
			}

			arena.setReady(true);
			arena.setEndLocation(LocationSerializer.fromString(config.getString(path + "endLocation")));
			arena.setPlateLocation(LocationSerializer.fromString(config.getString(path + "plateLocation")));
			arena.setMinCorner(LocationSerializer.fromString(config.getString(path + "areaMin")));
			arena.setMaxCorner(LocationSerializer.fromString(config.getString(path + "areaMax")));
			arena.setArenaPlate(XMaterial.valueOf(config.getString(path + "arenaPlate")));

			var scheduler = plugin.getArenaManager().getArenaScheduler();

			if (scheduler == ArenaScheduler.PER_ARENA) {
				scheduler.register(plugin.getArenaManager().getOptions());
			}

			player.sendMessage(chatManager.coloredRawMessage("&a&l✔ &aValidation succeeded! Registering new arena instance: &e" + arena.getId()));

			config.set(path + "isdone", true);
			ConfigUtils.saveConfig(plugin, config, "arenas");
		}), 8, 3);
	}
}