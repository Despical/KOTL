package me.despical.kotl.handlers.setup.components;

import me.despical.commons.compat.XMaterial;
import me.despical.commons.item.ItemBuilder;
import me.despical.inventoryframework.GuiItem;
import me.despical.inventoryframework.pane.StaticPane;
import me.despical.kotl.handlers.setup.SetupInventory;

/**
 * @author Despical
 * <p>
 * Created at 30.09.2023
 */
public class ArenaOptionComponents implements SetupInventory.SetupComponent {

	private static boolean supportsParticle;

	@Override
	public void injectComponents(SetupInventory setup, StaticPane pane) {
		final var arena = setup.getArena();
		final var path = "instances.%s.".formatted(arena.getId());
		final var arenaOptions = new StaticPane(9, 3);

		final var outlineItem = supportsParticle ? new ItemBuilder(arena.isShowOutlines() ? XMaterial.ENDER_PEARL : XMaterial.ENDER_EYE).name("           " + (arena.isShowOutlines() ? "&c&lDisable" : "&e&lEnable") + " Outline Particles           ").lore("&7You can create particles around the game arena.") : new ItemBuilder(XMaterial.BARREL).name("&c&lYour server does not support Particles!");

		arenaOptions.fillWith(new ItemBuilder(XMaterial.BLACK_STAINED_GLASS_PANE).name("&7Current plate: &a" + arena.getArenaPlate().toString()).build());

		setup.getPaginatedPane().addPane(3, arenaOptions);

		arenaOptions.addItem(GuiItem.of(outlineItem.build(), e -> {
			arena.setShowOutlines(!arena.isShowOutlines());

			config.set(path + "showOutlines", arena.isShowOutlines());
			saveConfig();

			setup.getPlayer().closeInventory();
		}), 4, 1);

		arenaOptions.addItem(GuiItem.of(mainMenuItem, event -> setup.restorePage()), 8, 2);
	}

	static {
		try {
			Class.forName("org.bukkit.Particle");

			supportsParticle = true;
		} catch (ClassNotFoundException exception) {
			supportsParticle = false;
		}
	}
}