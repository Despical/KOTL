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

package me.despical.kotl.handlers.setup.components;

import me.despical.commons.compat.XMaterial;
import me.despical.commons.configuration.ConfigUtils;
import me.despical.commons.item.ItemBuilder;
import me.despical.inventoryframework.GuiItem;
import me.despical.inventoryframework.pane.StaticPane;
import me.despical.kotl.KOTL;
import me.despical.kotl.arena.Arena;
import me.despical.kotl.handlers.setup.AbstractComponent;
import me.despical.kotl.handlers.setup.SetupInventory;
import org.bukkit.configuration.file.FileConfiguration;

/**
 * @author Despical
 * <p>
 * Created at 30.09.2023
 */
public class ArenaOptionComponents extends AbstractComponent {

    public ArenaOptionComponents(KOTL plugin) {
        super(plugin);
    }

    @Override
    public void injectComponents(SetupInventory setup) {
        Arena arena = setup.getArena();
        FileConfiguration config = ConfigUtils.getConfig(plugin, "arenas");
        String path = "instances.%s.".formatted(arena.getId());
        StaticPane arenaOptions = new StaticPane(9, 3);
        ItemBuilder outlineItem = new ItemBuilder(arena.isShowOutlines() ? XMaterial.ENDER_PEARL : XMaterial.ENDER_EYE).name("         " + (arena.isShowOutlines() ? "&c&l Disable" : "&e&l  Enable") + " Outline Particles").lore("&7You can create particles around the arena.");

        arenaOptions.fillWith(new ItemBuilder(XMaterial.BLACK_STAINED_GLASS_PANE).name("&7You can edit additional arena options here.").build());
        arenaOptions.addItem(GuiItem.of(mainMenuItem, event -> setup.restorePage()), 8, 2);
        arenaOptions.addItem(GuiItem.of(outlineItem.build(), e -> {
            arena.setShowOutlines(!arena.isShowOutlines());

            config.set(path + "showOutlines", arena.isShowOutlines());
            ConfigUtils.saveConfig(plugin, config, "arenas");

            setup.closeInventory();
        }), 4, 1);

        setup.getPaginatedPane().addPane(3, arenaOptions);
    }
}
