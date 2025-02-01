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
import me.despical.commons.string.StringUtils;
import me.despical.inventoryframework.GuiItem;
import me.despical.inventoryframework.pane.StaticPane;
import me.despical.kotl.KOTL;
import me.despical.kotl.handlers.setup.AbstractComponent;
import me.despical.kotl.handlers.setup.SetupInventory;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * @author Despical
 * <p>
 * Created at 30.09.2022
 */
public class PressurePlateComponents extends AbstractComponent {

    public PressurePlateComponents(KOTL plugin) {
        super(plugin);
    }

    @Override
    public void injectComponents(SetupInventory setup) {
        var player = setup.getPlayer();
        var arena = setup.getArena();
        var path = "instances.%s.".formatted(arena.getId());
        var config = ConfigUtils.getConfig(plugin, "arenas");

        var pressurePlatesPane = new StaticPane(9, XReflection.supports(13) ? 6 : 3);
        pressurePlatesPane.fillWith(new ItemBuilder(XMaterial.BLACK_STAINED_GLASS_PANE).name("&7Current plate: &a" + formatPlateName(arena.getArenaPlate())).build());

        setup.getPaginatedPane().addPane(2, pressurePlatesPane);

        var pressurePlates = new ArrayList<XMaterial>() {{
            add(XMaterial.OAK_PRESSURE_PLATE);
            add(XMaterial.STONE_PRESSURE_PLATE);
            add(XMaterial.LIGHT_WEIGHTED_PRESSURE_PLATE);
            add(XMaterial.HEAVY_WEIGHTED_PRESSURE_PLATE); // 1.8

            if (XReflection.supports(13)) {
                add(XMaterial.ACACIA_PRESSURE_PLATE);
                add(XMaterial.BIRCH_PRESSURE_PLATE);
                add(XMaterial.SPRUCE_PRESSURE_PLATE);
                add(XMaterial.DARK_OAK_PRESSURE_PLATE);
                add(XMaterial.JUNGLE_PRESSURE_PLATE); // 1.13
            }

            if (XReflection.supports(16)) {
                add(XMaterial.CRIMSON_PRESSURE_PLATE);
                add(XMaterial.POLISHED_BLACKSTONE_PRESSURE_PLATE);
                add(XMaterial.WARPED_PRESSURE_PLATE); // 1.16
            }

            if (XReflection.supports(20)) add(XMaterial.BAMBOO_PRESSURE_PLATE);
        }};

        var slots = getSlots(pressurePlates.size());

        for (int i = 0; i < pressurePlates.size(); i++) {
            int slot = slots.get(i);
            var plate = pressurePlates.get(i);
            var itemBuilder = new ItemBuilder(plate);

            if (arena.getArenaPlate().equals(plate)) {
                itemBuilder.name("&7This plate is the current arena plate.").enchantment(Enchantment.ARROW_DAMAGE).flag(ItemFlag.HIDE_ENCHANTS);
            } else {
                itemBuilder.name("&7Click to change plate!");
            }

            pressurePlatesPane.addItem(GuiItem.of(itemBuilder.build(), inventoryClickEvent -> {
                setup.closeInventory();

                config.set(path + "arenaPlate", plate.name());
                ConfigUtils.saveConfig(plugin, config, "arenas");

                player.sendMessage(chatManager.coloredRawMessage("&e✔ Completed | &aArena plate for arena &e" + arena.getId() + " &achanged to &e" + formatPlateName(plate)));

                arena.setArenaPlate(plate);

                var plateLoc = arena.getPlateLocation();

                if (!LocationSerializer.isDefaultLocation(plateLoc)) plateLoc.getBlock().setType(plate.parseMaterial());
            }), slot % 9, slot / 9);
        }

        pressurePlatesPane.addItem(GuiItem.of(mainMenuItem, event -> setup.restorePage()), 8, 5);
    }

    private List<Integer> getSlots(int size) {
        var slots = me.despical.commons.util.Collections.listOf(10, 12, 14, 16);

        if (size == 9) {
            slots.addAll(Arrays.asList(28, 30, 32, 34, 40));
        } else {
            slots.add(20);

            if (XReflection.supports(20)) {
                slots.add(22);
            }

            slots.addAll(Arrays.asList(24, 28, 30, 32, 34, 38, 42, 44));
        }

        return slots;
    }

    private String formatPlateName(XMaterial plate) {
        return StringUtils.capitalize(plate.name().toLowerCase(Locale.ENGLISH).replace("_", " "), ' ');
    }
}
