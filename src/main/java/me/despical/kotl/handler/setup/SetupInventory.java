/*
 * KOTL - Don't let others to climb top of the ladders!
 * Copyright (C) 2022 Despical
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package me.despical.kotl.handler.setup;

import me.despical.commons.compat.XMaterial;
import me.despical.inventoryframework.Gui;
import me.despical.inventoryframework.GuiItem;
import me.despical.inventoryframework.pane.StaticPane;
import me.despical.kotl.Main;
import me.despical.kotl.arena.Arena;
import me.despical.kotl.handler.ChatManager;
import me.despical.kotl.handler.setup.components.ArenaRegisterComponents;
import me.despical.kotl.handler.setup.components.MiscComponents;
import me.despical.kotl.handler.setup.components.SpawnComponents;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.ThreadLocalRandom;

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
	private final SetupUtilities setupUtilities;

	public static final String TUTORIAL_VIDEO = "https://www.youtube.com/watch?v=O_vkf_J4OgY";

	public SetupInventory(Arena arena, Player player) {
		this.arena = arena;
		this.player = player;
		this.plugin = JavaPlugin.getPlugin(Main.class);
		this.setupUtilities = new SetupUtilities(plugin);

		prepareGui();
	}

	private void prepareGui() {
		this.gui = new Gui(plugin, 3, "King of the Ladder Arena Setup");
		this.gui.setOnGlobalClick(e -> e.setCancelled(true));

		StaticPane pane = new StaticPane(9, 3);
		pane.fillProgressBorder(GuiItem.of(XMaterial.GREEN_STAINED_GLASS_PANE.parseItem()), GuiItem.of(XMaterial.BLACK_STAINED_GLASS_PANE.parseItem()), arena.isReady() ? 100 : 0);
		this.gui.addPane(pane);

		prepareComponents(pane);
	}

	private void prepareComponents(StaticPane pane) {
		SpawnComponents spawnComponents = new SpawnComponents();
		spawnComponents.injectComponents(this, pane);
		
		MiscComponents miscComponents = new MiscComponents();
		miscComponents.injectComponents(this, pane);
		
		ArenaRegisterComponents arenaRegistryComponents = new ArenaRegisterComponents();
		arenaRegistryComponents.injectComponents(this, pane);
	}

	private void sendProTip(Player player) {
		ChatManager chatManager = plugin.getChatManager();
		String tip = "";

		switch (ThreadLocalRandom.current().nextInt(9)) {
			case 0:
				tip = "&e&lTIP: &7We are open source! You can always help us by contributing! Check https://github.com/Despical/KOTL";
				break;
			case 1:
				tip = "&e&lTIP: &7Need help? Check our wiki: https://github.com/Despical/KOTL/wiki";
				break;
			case 2:
				tip = "&e&lTIP: &7Don't know where to start? Check out our tutorial video: " + TUTORIAL_VIDEO;
				break;
			case 3:
				tip = "&e&lTIP: &7Help us translating plugin to your language here: https://github.com/Despical/LocaleStorage/";
				break;
			default:
				break;
		}

		if (!tip.isEmpty()) {
			player.sendMessage(chatManager.coloredRawMessage(tip));
		}
	}

	public void openInventory() {
		sendProTip(player);
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

	public SetupUtilities getSetupUtilities() {
		return setupUtilities;
	}
}