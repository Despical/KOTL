package me.despical.kotl.handlers.hologram;

import me.despical.kotl.Main;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Despical
 * <p>
 * Created at 04.10.2020
 */
public class Hologram {

	private Item entityItem;
	private ItemStack item;
	private List<String> lines = new ArrayList<>();
	private Location location;

	private final List<ArmorStand> armorStands = new ArrayList<>();
	private final Main plugin = JavaPlugin.getPlugin(Main.class);

	public Hologram(Location location) {
		this.location = location;
	}

	public Hologram(Location location, @NotNull String... lines) {
		this.location = location;
		this.lines = Arrays.asList(lines);

		append();
	}

	public Hologram(Location location, @NotNull List<String> lines) {
		this.location = location;
		this.lines = lines;

		append();
	}

	public Location getLocation() {
		return location;
	}

	public void setLocation(Location location) {
		this.location = location;
	}

	public ItemStack getItem() {
		return item;
	}

	public Item getEntityItem() {
		return entityItem;
	}

	@NotNull
	public List<String> getLines() {
		return lines;
	}

	@NotNull
	public List<ArmorStand> getArmorStands() {
		return armorStands;
	}

	public Hologram appendLines(@NotNull String... lines) {
		this.lines = Arrays.asList(lines);

		append();
		return this;
	}

	public Hologram appendLines(@NotNull List<String> lines) {
		this.lines = lines;

		append();
		return this;
	}

	public Hologram appendLine(@NotNull String line) {
		this.lines.add(line);

		append();
		return this;
	}

	public Hologram appendItem(@NotNull ItemStack item) {
		this.item = item;

		append();
		return this;
	}

	public void deleteLines() {
		double distanceAbove = -0.27, y = location.getY();

		for (int i = 0; i <= lines.size() - 1; i++) {
			y += distanceAbove;

			ArmorStand holo = getEntityArmorStand(location, y);
			holo.setCustomNameVisible(false);
			lines.clear();
		}
	}

	public void delete() {
		for (ArmorStand armor : armorStands) {
			armor.setCustomNameVisible(false);
			armor.remove();

			plugin.getHologramManager().remove(armor);
		}

		if (entityItem != null)
			entityItem.remove();

		armorStands.clear();
	}

	public boolean isDeleted() {
		return armorStands.isEmpty();
	}

	private void append() {
		deleteLines();

		double distanceAbove = -0.27, y = location.getY(), lastY = y;

		for (int i = 0; i <= lines.size() - 1; i++) {
			y += distanceAbove;

			ArmorStand holo = getEntityArmorStand(location, y);
			holo.setCustomName(lines.get(i));
			armorStands.add(holo);

			plugin.getHologramManager().add(holo);

			lastY = y;
		}

		if (item != null && item.getType() != org.bukkit.Material.AIR) {
			Location l = location.clone().add(0, lastY, 0);

			entityItem = location.getWorld().dropItem(l, item);

			if (Bukkit.getServer().getVersion().contains("Paper"))
				entityItem.setCanMobPickup(false);

			entityItem.setCustomNameVisible(false);
			entityItem.setGravity(true);
			entityItem.setInvulnerable(true);
			entityItem.teleport(l);
		}
	}

	/**
	 * @param y the y axis of the hologram
	 * @return {@link ArmorStand}
	 */
	private ArmorStand getEntityArmorStand(Location loc, double y) {
		loc.setY(y);

		ArmorStand stand = (ArmorStand) loc.getWorld().spawnEntity(loc, EntityType.ARMOR_STAND);
		stand.setVisible(false);
		stand.setGravity(false);
		stand.setCustomNameVisible(true);
		return stand;
	}
}