/*
 * KOTL - Don't let others climb to top of the ladders!
 * Copyright (C) 2026  Berke Akcen
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package dev.despical.kotl.util;

import dev.despical.commons.reflection.XReflection;
import dev.despical.fileitems.SpecialItem;
import dev.despical.kotl.KOTL;
import dev.despical.kotl.arena.Arena;
import dev.despical.kotl.arena.options.ArenaKeys;
import dev.despical.kotl.setup.pages.SetupHomePage;
import dev.despical.kotl.user.User;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

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
    private final NamespacedKey arenaKey;
    private final Map<UUID, Selection> selections;

    public CuboidSelector(KOTL plugin) {
        this.plugin = plugin;
        this.wandItem = plugin.getItemManager().getItem("area-selector");
        this.arenaKey = new NamespacedKey(plugin, "setup_area_selector");
        this.selections = new HashMap<>();

        Bukkit.getPluginManager().registerEvents(new SelectorEvents(), plugin);
    }

    public void giveSelectorWand(Player player, Arena arena) {
        ItemStack item = wandItem.getOriginalItemStack().clone();
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.getPersistentDataContainer().set(arenaKey, PersistentDataType.STRING, arena.getId());
            item.setItemMeta(meta);
        }

        player.getInventory().setItemInMainHand(item);
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

            String arenaId = getArenaId(item);
            if (arenaId == null) {
                return;
            }

            event.setCancelled(true);

            User user = plugin.getUserManager().getUser(event.getPlayer());
            UUID uuid = user.getUniqueId();
            Arena arena = plugin.getArenaRegistry().getArena(arenaId);

            if (arena == null) {
                return;
            }

            switch (event.getAction()) {
                case LEFT_CLICK_BLOCK -> {
                    Location first = event.getClickedBlock().getLocation();
                    selections.put(uuid, new Selection(first, null));

                    arena.setOption(ArenaKeys.MIN_CORNER, first);
                    plugin.getOutlineManager().handleOutlines(arena);

                    plugin.getChatManager().sendMessage(event.getPlayer(), "setup.area-first-position-set", locationVars("%first", first));
                }

                case RIGHT_CLICK_BLOCK -> {
                    Selection currentSelection = selections.get(uuid);

                    if (currentSelection == null || currentSelection.firstPos() == null) {
                        plugin.getChatManager().sendMessage(event.getPlayer(), "setup.area-first-position-required");
                        return;
                    }

                    Location second = event.getClickedBlock().getLocation();
                    Selection selection = new Selection(currentSelection.firstPos(), second);
                    selections.put(uuid, selection);

                    arena.setOption(ArenaKeys.MIN_CORNER, selection.firstPos());
                    arena.setOption(ArenaKeys.MAX_CORNER, selection.secondPos());

                    plugin.getOutlineManager().handleOutlines(arena);

                    plugin.getChatManager().sendMessage(event.getPlayer(), "setup.area-selection-complete",
                        locationVars("%first", selection.firstPos()),
                        locationVars("%second", selection.secondPos()),
                        Var.of("%blocks%", countBlocks(selection))
                    );
                    SetupHomePage.warnIfKingPlateOutsideArea(event.getPlayer(), arena);
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

        private String getArenaId(ItemStack item) {
            if (item == null) {
                return null;
            }

            ItemMeta meta = item.getItemMeta();
            if (meta == null) {
                return null;
            }

            return meta.getPersistentDataContainer().get(arenaKey, PersistentDataType.STRING);
        }

        private Var locationVars(String prefix, Location location) {
            return Var.of(prefix + "_xyz%", "%d, %d, %d".formatted(location.getBlockX(), location.getBlockY(), location.getBlockZ()));
        }

        private long countBlocks(Selection selection) {
            Location first = selection.firstPos();
            Location second = selection.secondPos();

            long x = Math.abs(first.getBlockX() - second.getBlockX()) + 1L;
            long y = Math.abs(first.getBlockY() - second.getBlockY()) + 1L;
            long z = Math.abs(first.getBlockZ() - second.getBlockZ()) + 1L;

            return x * y * z;
        }
    }
}
