package me.despical.kotl.utils;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import me.despical.commonsbox.item.ItemBuilder;
import me.despical.commonsbox.item.ItemUtils;
import me.despical.kotl.Main;

/**
 * @author Despical
 * <p>
 * Created at 24.06.2020
 */
public class CuboidSelector implements Listener {

	private Main plugin;
	private Map<Player, Selection> selections = new HashMap<>();

	public CuboidSelector(Main plugin) {
		this.plugin = plugin;
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}

	public void giveSelectorWand(Player p) {
		ItemStack stack = new ItemBuilder(Material.BLAZE_ROD).name(plugin.getChatManager().colorRawMessage("&6&lArea selector")).build();
		p.getInventory().addItem(stack);
		
		p.sendMessage(plugin.getChatManager().colorRawMessage(plugin.getChatManager().getPrefix() + "&eYou received area selector wand!"));
		p.sendMessage(plugin.getChatManager().colorRawMessage(plugin.getChatManager().getPrefix() + "&eSelect bottom corner using left click!"));
	}

	public Selection getSelection(Player p) {
		return selections.getOrDefault(p, null);
	}

	public void removeSelection(Player p) {
		selections.remove(p);
	}

	@EventHandler
	public void onWandUse(PlayerInteractEvent e) {
		if (!ItemUtils.isItemStackNamed(e.getItem()) || !e.getItem().getItemMeta().getDisplayName().equals(plugin.getChatManager().colorRawMessage("&6&lArea selector"))) {
			return;
		}
		e.setCancelled(true);
		switch (e.getAction()) {
		case LEFT_CLICK_BLOCK:
			selections.put(e.getPlayer(), new Selection(e.getClickedBlock().getLocation(), null));
			e.getPlayer().sendMessage(plugin.getChatManager().colorRawMessage("&e✔ Completed | &aNow select top corner using right click!"));
			break;
		case RIGHT_CLICK_BLOCK:
			if (!selections.containsKey(e.getPlayer())) {
				e.getPlayer().sendMessage(plugin.getChatManager().colorRawMessage("&c&l✖ &cWarning | Please select bottom corner using left click first!"));
				break;
			}
			selections.put(e.getPlayer(), new Selection(selections.get(e.getPlayer()).getFirstPos(), e.getClickedBlock().getLocation()));
			e.getPlayer().sendMessage(plugin.getChatManager().colorRawMessage("&e✔ Completed | &aNow you can set the area via setup menu!"));
			break;
		case LEFT_CLICK_AIR:
		case RIGHT_CLICK_AIR:
			e.getPlayer().sendMessage(plugin.getChatManager().colorRawMessage("&c&l✖ &cWarning | Please select solid block (not air)!"));
			break;
		default:
			break;
		}
	}

	public class Selection {

		private Location firstPos;
		private Location secondPos;

		public Selection(Location firstPos, Location secondPos) {
			this.firstPos = firstPos;
			this.secondPos = secondPos;
		}

		public Location getFirstPos() {
			return firstPos;
		}

		public Location getSecondPos() {
			return secondPos;
		}
	}
}