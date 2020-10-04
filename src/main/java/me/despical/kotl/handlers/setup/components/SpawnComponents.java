package me.despical.kotl.handlers.setup.components;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import com.github.despical.inventoryframework.GuiItem;
import com.github.despical.inventoryframework.pane.StaticPane;

import me.despical.commonsbox.compat.XMaterial;
import me.despical.commonsbox.configuration.ConfigUtils;
import me.despical.commonsbox.item.ItemBuilder;
import me.despical.commonsbox.serializer.LocationSerializer;
import me.despical.kotl.Main;
import me.despical.kotl.arena.Arena;
import me.despical.kotl.handlers.setup.SetupInventory;

/**
 * @author Despical
 * <p>
 * Created at 22.06.2020
 */
public class SpawnComponents implements SetupComponent {

	private SetupInventory setupInventory;

	@Override
	public void prepare(SetupInventory setupInventory) {
		this.setupInventory = setupInventory;
	}

	@Override
	public void injectComponents(StaticPane pane) {
		Player player = setupInventory.getPlayer();
		FileConfiguration config = setupInventory.getConfig();
		Arena arena = setupInventory.getArena();
		Main plugin = setupInventory.getPlugin();

		pane.addItem(new GuiItem(new ItemBuilder(Material.REDSTONE_BLOCK)
			.name(plugin.getChatManager().colorRawMessage("&e&lSet Ending Location"))
			.lore(ChatColor.GRAY + "Click to set ending location on")
			.lore(ChatColor.GRAY + "the place where you are standing.")
			.lore(ChatColor.DARK_GRAY + "(location where players will be")
			.lore(ChatColor.DARK_GRAY + "teleported after the reloading)")
			.lore("", setupInventory.getSetupUtilities()
			.isOptionDoneBool("instances." + arena.getId() + ".endLocation"))
			.build(), e -> {
				e.getWhoClicked().closeInventory();
				config.set("instances." + arena.getId() + ".endLocation", LocationSerializer.locationToString(player.getLocation()));
				arena.setEndLocation(player.getLocation());
				player.sendMessage(plugin.getChatManager().colorRawMessage("&e✔ Completed | &aEnding location for arena " + arena.getId() + " set at your location!"));

				ConfigUtils.saveConfig(plugin, config, "arenas");
		}), 0, 0);
		
		pane.addItem(new GuiItem(new ItemBuilder(XMaterial.OAK_PRESSURE_PLATE.parseMaterial())
			.name(plugin.getChatManager().colorRawMessage("&e&lSet Plate Location"))
			.lore(ChatColor.GRAY + "Click to set plate location on")
			.lore(ChatColor.GRAY + "the place where you are standing.")
			.lore(ChatColor.DARK_GRAY + "(location where players will try to")
			.lore(ChatColor.DARK_GRAY + "reach)")
			.lore("", setupInventory.getSetupUtilities().isOptionDoneBool("instances." + arena.getId() + ".plateLocation"))
			.build(), e -> {
				e.getWhoClicked().closeInventory();
				player.getLocation().getBlock().getRelative(BlockFace.DOWN).setType(XMaterial.OAK_PRESSURE_PLATE.parseMaterial());
				config.set("instances." + arena.getId() + ".plateLocation", LocationSerializer.locationToString(player.getLocation().getBlock().getRelative(BlockFace.DOWN).getLocation()));
				arena.setPlateLocation(player.getLocation());
				player.sendMessage(plugin.getChatManager().colorRawMessage("&e✔ Completed | &aPlate location for arena " + arena.getId() + " set at your location!"));

				ConfigUtils.saveConfig(plugin, config, "arenas");
		}), 1, 0);

		pane.addItem(new GuiItem(new ItemBuilder(XMaterial.BLAZE_ROD.parseItem())
			.name(plugin.getChatManager().colorRawMessage("&e&lSet Arena Region"))
			.lore(ChatColor.GRAY + "Click to set arena's region")
			.lore(ChatColor.GRAY + "with the cuboid selector.")
			.lore(ChatColor.DARK_GRAY + "(area where game will be playing)")
			.lore("", setupInventory.getSetupUtilities()
			.isOptionDoneBool("instances." + arena.getId() + ".areaMax"))
			.build(), e -> {
				e.getWhoClicked().closeInventory();

				if (plugin.getCuboidSelector().getSelection(player) == null) {
					plugin.getCuboidSelector().giveSelectorWand(player);
					return;
				}

				if (plugin.getCuboidSelector().getSelection(player).getSecondPos() == null) {
					player.sendMessage(plugin.getChatManager().colorRawMessage("&c&l✖ &cWarning | Please select top corner using right click!"));
					return;
				}

				config.set("instances." + arena.getId() + ".areaMin", LocationSerializer.locationToString(plugin.getCuboidSelector().getSelection(player).getFirstPos()));
				config.set("instances." + arena.getId() + ".areaMax", LocationSerializer.locationToString(plugin.getCuboidSelector().getSelection(player).getSecondPos()));
				player.sendMessage(plugin.getChatManager().colorRawMessage("&e✔ Completed | &aGame area of arena " + arena.getId() + " set as you selection!"));
				plugin.getCuboidSelector().removeSelection(player);

				ConfigUtils.saveConfig(plugin, config, "arenas");
		}), 2, 0);
	}
}