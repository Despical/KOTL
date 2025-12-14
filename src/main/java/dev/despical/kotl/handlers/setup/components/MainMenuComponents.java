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

package dev.despical.kotl.handlers.setup.components;

import dev.despical.commons.XMaterial;
import dev.despical.commons.configuration.ConfigUtils;
import dev.despical.commons.item.ItemBuilder;
import dev.despical.commons.reflection.XReflection;
import dev.despical.commons.serializer.LocationSerializer;
import dev.despical.inventoryframework.Gui;
import dev.despical.inventoryframework.GuiItem;
import dev.despical.inventoryframework.pane.StaticPane;
import dev.despical.kotl.KOTL;
import dev.despical.kotl.arena.Arena;
import dev.despical.kotl.handlers.setup.AbstractComponent;
import dev.despical.kotl.handlers.setup.SetupInventory;
import dev.despical.kotl.util.CuboidSelector;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;

import java.util.Optional;

/**
 * @author Despical
 * <p>
 * Created at 22.06.2020
 */
public class MainMenuComponents extends AbstractComponent {

    public MainMenuComponents(KOTL plugin) {
        super(plugin);
    }

    @Override
    public void injectComponents(SetupInventory setup) {
        Player player = setup.getPlayer();
        FileConfiguration config = ConfigUtils.getConfig(plugin, "arenas");
        Arena arena = setup.getArena();
        String path = "instances.%s.".formatted(arena.getId());
        StaticPane pane = setup.getPane();
        ItemBuilder optionsItem = new ItemBuilder(XMaterial.CLOCK).name("&e&l        Additional Options").lore("&7Click to open additional options menu.");

        pane.addItem(GuiItem.of(optionsItem.build(), event -> setup.setPage("   Set Additional Arena Options", 3, 3)), 4, 2);

        pane.addItem(new GuiItem(new ItemBuilder(XMaterial.REDSTONE_BLOCK)
            .name("&e&l      Set Ending Location")
            .lore("&7Click to set the ending location on")
            .lore("&7the place where you are standing.")
            .lore("", isLocationSet(arena.getEndLocation()))
            .build(), e -> {

            setup.closeInventory();

            Location location = player.getLocation();
            player.sendMessage(chatManager.coloredRawMessage("&e✔ Completed | &aEnding location for arena &e" + arena.getId() + " &aset at your location!"));

            arena.setEndLocation(location);

            config.set(path + "endLocation", LocationSerializer.toString(location));
            ConfigUtils.saveConfig(plugin, config, "arenas");
        }), 1, 1);

        pane.addItem(GuiItem.of(new ItemBuilder(arena.getArenaPlate())
            .name("&e&l         Set Plate Location")
            .lore("&7Click to set plate location on the place")
            .lore("&7           where you are standing.")
            .lore("", isLocationSet(arena.getPlateLocation()))
            .build(), e -> {

            setup.closeInventory();

            Material plateMaterial = arena.getArenaPlate().parseMaterial();

            Optional.ofNullable(arena.getPlateLocation()).ifPresent(location -> {
                Block block = location.getBlock();

                if (block.getType() == arena.getArenaPlate().parseMaterial()) {
                    block.setType(Material.AIR);
                }
            });

            Location location = player.getLocation();
            location.getBlock().setType(plateMaterial);

            arena.setPlateLocation(location);
            player.sendMessage(chatManager.coloredRawMessage("&e✔ Completed | &aPlate location for arena &e" + arena.getId() + " &aset at your location!"));

            config.set(path + "plateLocation", LocationSerializer.toString(location.getBlock().getLocation()));
            ConfigUtils.saveConfig(plugin, config, "arenas");
        }), 5, 1);

        pane.addItem(GuiItem.of(new ItemBuilder(XMaterial.BLAZE_ROD.parseItem())
            .name("&e&l        Set Arena Region")
            .lore("&7Click to set arena's region with the")
            .lore("&7            cuboid selector.")
            .lore("", isLocationSet(arena.getMaxCorner()))
            .build(), e -> {

            setup.closeInventory();

            CuboidSelector selector = plugin.getCuboidSelector();
            var selection = selector.getSelection(player);

            if (selection == null) {
                selector.giveSelectorWand(player);

                player.sendMessage(chatManager.prefixedRawMessage("&eYou received area selector wand!"));
                player.sendMessage(chatManager.prefixedRawMessage("&eSelect bottom corner using left click!"));
                return;
            }

            if (selection.secondPos() == null) {
                player.sendMessage(chatManager.coloredRawMessage("&c&l✖ &cWarning | Please select the other corner using the right click!"));
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
            .name("&e&l     Change Arena Plate")
            .lore("&7Click here to change arena plate.")
            .build(), e -> {

            setup.getPaginatedPane().setPage(2);

            Gui gui = setup.getGui();
            gui.setRows(XReflection.supports(13) ? 6 : 3);
            gui.setTitle("         Arena Plate Editor");
            gui.update();
        }), 7, 1);

        ItemBuilder registerItem;

        if (arena.isReady()) {
            registerItem = new ItemBuilder(XMaterial.BARRIER)
                .name("&a&l           Arena Registered")
                .lore("&7Good job, you went through whole setup!")
                .lore("&7      You can play on this arena now!")
                .enchantment(Enchantment.DURABILITY)
                .hideTooltip();
        } else {
            registerItem = new ItemBuilder(XMaterial.FIREWORK_ROCKET)
                .name("       &e&lFinish Arena Setup")
                .lore("&7  Click this when you are done.")
                .lore("&7You'll still be able to edit arena.")
                .hideTooltip();
        }

        pane.addItem(GuiItem.of(registerItem.build(), e -> {
            setup.closeInventory();

            if (arena.isReady()) {
                player.sendMessage(chatManager.coloredRawMessage("&a&l✔ &aThis arena was already validated and is ready to use!"));
                return;
            }

            for (Arena.GameLocation gameLocation : Arena.GameLocation.values()) {
                if (LocationSerializer.isDefaultLocation(arena.getLocation(gameLocation))) {
                    player.sendMessage(chatManager.coloredRawMessage("&c&l✘ &cArena validation failed! Please configure following spawn properly: %s (cannot be world spawn location)".formatted(gameLocation)));
                    return;
                }
            }

            arena.setReady(true);

            player.sendMessage(chatManager.coloredRawMessage("&a&l✔ &aValidation succeeded! Registering new arena instance: &e" + arena.getId()));

            config.set(path + "isdone", true);
            ConfigUtils.saveConfig(plugin, config, "arenas");
        }), 8, 3);
    }
}
