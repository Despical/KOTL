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
import me.despical.commons.item.ItemBuilder;
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
		this.gui = new Gui(plugin, 3, "Arena Setup Menu");
		this.gui.setOnGlobalClick(e -> e.setCancelled(true));

		final StaticPane pane = new StaticPane(9, 3);
		final ItemBuilder registeredItem = new ItemBuilder(XMaterial.GREEN_STAINED_GLASS_PANE).name("&aArena Validation Successful"), notRegisteredItem = new ItemBuilder(XMaterial.BLACK_STAINED_GLASS_PANE).name("&cArena Validation Not Finished Yet");
		pane.fillProgressBorder(GuiItem.of(registeredItem.build()), GuiItem.of(notRegisteredItem.build()), arena.isReady() ? 100 : 0);

		this.gui.addPane(pane);

		prepareComponents(pane);
	}

	private void prepareComponents(StaticPane pane) {
		final SpawnComponents spawnComponents = new SpawnComponents();
		spawnComponents.injectComponents(this, pane);
		
		final MiscComponents miscComponents = new MiscComponents();
		miscComponents.injectComponents(this, pane);
		
		final ArenaRegisterComponents arenaRegistryComponents = new ArenaRegisterComponents();
		arenaRegistryComponents.injectComponents(this, pane);
	}

	private void sendProTip(Player player) {
		final ChatManager chatManager = plugin.getChatManager();
		String tip = "";

		switch (ThreadLocalRandom.current().nextInt(20)) {
			case 0:
				tip = "We are open source! You can always help us by contributing! Check https://github.com/Despical/KOTL";
				break;
			case 1:
				tip = "Need help? Check our wiki: https://github.com/Despical/KOTL/wiki";
				break;
			case 2:
				tip = "Don't know where to start? Check out our tutorial video: " + TUTORIAL_VIDEO;
				break;
			case 3:
				tip = "Help us translating plugin to your language here: https://github.com/Despical/LocaleStorage/";
				break;
			case 4:
				tip = "You can support us with becoming Patron on https://www.patreon.com/despical to make updates better and sooner.";
				break;
			case 5:
				tip = "Need help? You can join our Discord community. Check out https://discord.gg/rVkaGmyszE";
				break;
			case 6:
				tip = "You have suggestions to improve the plugin? Use our issue tracker or join our Discord server.";
				break;
			case 7:
				tip = "If you like the plugin you can try the premium version with more features and better performance. Check out https://spigotmc.org/resources/king-of-the-ladder-premium-1-8-1-19.102644/";
				break;
			case 8:
				tip = "Check out our other plugins: https://spigotmc.org/resources/authors/despical.615094/";
				break;
			case 9:
				tip = "You liked KOTL? Check out my other plugin that you can like it too: https://spigotmc.org/resources/whack-me-1-9-1-19.103482/";
				break;
			default:
				break;
		}

		if (!tip.isEmpty()) {
			player.sendMessage(chatManager.coloredRawMessage("&e&lTIP: &7" + tip));
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