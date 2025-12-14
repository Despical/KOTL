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

package dev.despical.kotl.util;

import dev.despical.commons.reflection.XReflection;
import dev.despical.fileitems.SpecialItem;
import dev.despical.kotl.KOTL;
import dev.despical.kotl.user.User;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author Despical
 * <p>
 * Created at 24.06.2020
 */
public class CuboidSelector {

    private final KOTL plugin;
    private final SpecialItem wandItem;
    private final Map<UUID, Selection> selections;

    public CuboidSelector(KOTL plugin) {
        this.plugin = plugin;
        this.wandItem = plugin.getItemManager().getItem("area-selector");
        this.selections = new HashMap<>();

        Bukkit.getPluginManager().registerEvents(new SelectorEvents(), plugin);
    }

    public void giveSelectorWand(Player player) {
        Inventory inventory = player.getInventory();
        inventory.addItem(wandItem.getOriginalItemStack());
    }

    public Selection getSelection(Player player) {
        return selections.get(player.getUniqueId());
    }

    public void removeSelection(Player player) {
        selections.remove(player.getUniqueId());
    }

    public record Selection(Location firstPos, Location secondPos) {
    }

    private class SelectorEvents implements Listener {

        @EventHandler
        public void onUsingWand(PlayerInteractEvent event) {
            if (!isMainHand(event)) {
                return;
            }

            ItemStack item = event.getItem();

            if (!wandItem.equals(item)) {
                return;
            }

            event.setCancelled(true);

            User user = plugin.getUserManager().getUser(event.getPlayer());
            UUID uuid = user.getUniqueId();

            switch (event.getAction()) {
                case LEFT_CLICK_BLOCK -> {
                    selections.put(uuid, new Selection(event.getClickedBlock().getLocation(), null));

                    user.sendRawMessage("&a&l✔ &7First position set. &eRight-click &7to select the second position.");
                }

                case RIGHT_CLICK_BLOCK -> {
                    Selection currentSelection = selections.get(uuid);

                    if (currentSelection == null || currentSelection.firstPos() == null) {
                        user.sendRawMessage("&c&l✖ &cYou must set the first position using &eleft-click&c.");
                        return;
                    }

                    selections.put(uuid, new Selection(currentSelection.firstPos(), event.getClickedBlock().getLocation()));

                    user.sendRawMessage("&a&l✔ &7Selection complete. &aNow you can set the area via setup menu!");
                }
            }
        }

        @EventHandler
        public void onQuit(PlayerQuitEvent event) {
            selections.remove(event.getPlayer().getUniqueId());
        }

        private boolean isMainHand(PlayerInteractEvent event) {
            if (XReflection.supports(9)) {
                return event.getHand() == EquipmentSlot.HAND;
            }

            return true;
        }
    }
}
