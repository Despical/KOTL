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

package me.despical.kotl.handlers.setup;

import me.despical.commons.compat.XMaterial;
import me.despical.commons.configuration.ConfigUtils;
import me.despical.commons.item.ItemBuilder;
import me.despical.commons.serializer.LocationSerializer;
import me.despical.inventoryframework.Gui;
import me.despical.inventoryframework.pane.PaginatedPane;
import me.despical.inventoryframework.pane.StaticPane;
import me.despical.kotl.Main;
import me.despical.kotl.arena.Arena;
import me.despical.kotl.handlers.ChatManager;
import me.despical.kotl.handlers.setup.components.ArenaOptionComponents;
import me.despical.kotl.handlers.setup.components.PressurePlateComponents;
import me.despical.kotl.handlers.setup.components.MainMenuComponents;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * @author Despical
 * <p>
 * Created at 22.06.2020
 */
public class SetupInventory {

	private Gui gui;
	private final Main plugin;
	private final Arena arena;
	private final Player player;

	private PaginatedPane paginatedPane;
	private StaticPane pane;

	public static final String TUTORIAL_VIDEO = "https://www.youtube.com/watch?v=O_vkf_J4OgY";

	public SetupInventory(Main plugin, Arena arena, Player player) {
		this.plugin = plugin;
		this.arena = arena;
		this.player = player;

		prepareGui();
	}

	private void prepareGui() {
		this.gui = new Gui(plugin, 4, "         KOTL Arena Editor");
		this.gui.setOnGlobalClick(e -> e.setCancelled(true));
		this.paginatedPane = new PaginatedPane(9, 4);

		final var pane = new StaticPane(9, 4);
		final ItemBuilder registeredItem = new ItemBuilder(XMaterial.GREEN_STAINED_GLASS_PANE).name("&aArena Validation Successful"), notRegisteredItem = new ItemBuilder(XMaterial.BLACK_STAINED_GLASS_PANE).name("&cArena Validation Not Finished Yet");
		pane.fillWith(arena.isReady() ? registeredItem.build() : notRegisteredItem.build());

		paginatedPane.addPane(0, pane);
		this.gui.addPane(paginatedPane);

		prepareComponents(pane);
	}

	private void prepareComponents(StaticPane pane) {
		AbstractComponent spawnComponents = new MainMenuComponents(plugin);
		spawnComponents.injectComponents(this);

		AbstractComponent pressurePlateComponents = new PressurePlateComponents(plugin);
		pressurePlateComponents.injectComponents(this);

		AbstractComponent arenaOptionComponents = new ArenaOptionComponents(plugin);
		arenaOptionComponents.injectComponents(this);
	}

	public void openInventory() {
		gui.show(player);
	}

	public Main getPlugin() {
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