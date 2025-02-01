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

package me.despical.kotl.util;

import me.despical.commons.compat.XMaterial;
import me.despical.commons.item.ItemBuilder;
import me.despical.commons.item.ItemUtils;
import me.despical.kotl.KOTL;
import me.despical.kotl.handlers.ChatManager;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * @author Despical
 * <p>
 * Created at 24.06.2020
 */
public class CuboidSelector {

    private final KOTL plugin;
    private final ItemStack wandItem;
    private final Map<UUID, Selection> selections;

    public CuboidSelector(KOTL plugin) {
        this.plugin = plugin;
        this.wandItem = new ItemBuilder(XMaterial.BLAZE_ROD)
            .name("&6&lArea selector")
            .lore("&eLEFT CLICK to select first corner.")
            .lore("&eRIGHT CLICK to select second corner.")
            .build();
        this.selections = new HashMap<>();

        plugin.getServer().getPluginManager().registerEvents(new SelectorEvents(), plugin);
    }

    public void giveSelectorWand(Player player) {
        Inventory inventory = player.getInventory();
        inventory.addItem(wandItem);
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
            ItemStack item = event.getItem();

            if (!ItemUtils.isNamed(item) || !Objects.equals(item.getItemMeta().getDisplayName(), wandItem.getItemMeta().getDisplayName())) {
                return;
            }

            event.setCancelled(true);

            Player player = event.getPlayer();
            UUID uuid = player.getUniqueId();
            ChatManager chatManager = plugin.getChatManager();

            switch (event.getAction()) {
                case LEFT_CLICK_BLOCK -> {
                    selections.put(uuid, new Selection(event.getClickedBlock().getLocation(), null));

                    player.sendMessage(chatManager.coloredRawMessage("&e✔ Completed | &aNow select the other corner using right click!"));
                }

                case RIGHT_CLICK_BLOCK -> {
                    if (!selections.containsKey(uuid)) {
                        player.sendMessage(chatManager.coloredRawMessage("&c&l✖ &cWarning | Please select a corner using the left click first!"));
                        return;
                    }

                    selections.replace(uuid, new Selection(selections.get(uuid).firstPos, event.getClickedBlock().getLocation()));

                    player.sendMessage(chatManager.coloredRawMessage("&e✔ Completed | &aNow you can set the area via setup menu!"));
                }

                default ->
                    player.sendMessage(chatManager.coloredRawMessage("&c&l✖ &cWarning | Please select solid block, not air!"));
            }
        }
    }
}
