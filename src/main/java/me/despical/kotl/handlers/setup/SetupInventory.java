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

package me.despical.kotl.handlers.setup;

import me.despical.commons.XMaterial;
import me.despical.commons.item.ItemBuilder;
import me.despical.inventoryframework.Gui;
import me.despical.inventoryframework.pane.PaginatedPane;
import me.despical.inventoryframework.pane.StaticPane;
import me.despical.kotl.KOTL;
import me.despical.kotl.arena.Arena;
import me.despical.kotl.handlers.setup.components.ArenaOptionComponents;
import me.despical.kotl.handlers.setup.components.MainMenuComponents;
import me.despical.kotl.handlers.setup.components.PressurePlateComponents;
import org.bukkit.entity.Player;

import java.util.stream.Stream;

/**
 * @author Despical
 * <p>
 * Created at 22.06.2020
 */
public class SetupInventory {

    private final KOTL plugin;
    private final Arena arena;
    private final Player player;
    private Gui gui;
    private PaginatedPane paginatedPane;
    private StaticPane pane;

    public SetupInventory(KOTL plugin, Arena arena, Player player) {
        this.plugin = plugin;
        this.arena = arena;
        this.player = player;

        prepareGui();
    }

    private void prepareGui() {
        this.gui = new Gui(plugin, 4, "         KOTL Arena Editor");
        this.gui.setOnGlobalClick(e -> e.setCancelled(true));
        this.gui.setOnDrag(e -> e.setCancelled(true));
        this.paginatedPane = new PaginatedPane(9, 4);

        this.pane = new StaticPane(9, 4);
        final ItemBuilder registeredItem = new ItemBuilder(XMaterial.GREEN_STAINED_GLASS_PANE).name("&aArena Validation Successful"), notRegisteredItem = new ItemBuilder(XMaterial.BLACK_STAINED_GLASS_PANE).name("&cArena Validation Not Finished Yet");
        pane.fillWith(arena.isReady() ? registeredItem.build() : notRegisteredItem.build());

        paginatedPane.addPane(0, pane);
        this.gui.addPane(paginatedPane);

        Stream.of(new MainMenuComponents(plugin), new PressurePlateComponents(plugin), new ArenaOptionComponents(plugin)).forEach(component -> component.injectComponents(this));
    }

    public void openInventory() {
        gui.show(player);
    }

    public KOTL getPlugin() {
        return plugin;
    }

    public Arena getArena() {
        return arena;
    }

    public Player getPlayer() {
        return player;
    }

    public Gui getGui() {
        return gui;
    }

    public PaginatedPane getPaginatedPane() {
        return paginatedPane;
    }

    public StaticPane getPane() {
        return pane;
    }

    public void closeInventory() {
        plugin.getServer().getScheduler().runTask(plugin, () -> player.closeInventory());
    }

    public void setPage(String title, int rows, int page) {
        this.gui.setTitle(title != null ? title : this.gui.getTitle());
        this.gui.setRows(rows);
        this.paginatedPane.setPage(page);
        this.gui.update();
    }

    public void restorePage() {
        this.setPage("         KOTL Arena Editor", 4, 0);
    }
}
