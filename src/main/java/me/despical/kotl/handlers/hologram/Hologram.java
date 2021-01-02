/*
 * KOTL - Don't let others to climb top of the ladders!
 * Copyright (C) 2021 Despical and contributors
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

package me.despical.kotl.handlers.hologram;

import me.despical.kotl.Main;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Despical
 * <p>
 * Created at 04.10.2020
 */
public class Hologram {

	private List<String> lines = new ArrayList<>();
	private Location location;

	private final List<ArmorStand> armorStands = new LinkedList<>();
	private final Main plugin = JavaPlugin.getPlugin(Main.class);

	public Hologram(Location location) {
		this.location = location;
	}

	public Hologram(Location location, @NotNull String... lines) {
		this.location = location;
		this.lines = new ArrayList<>(Arrays.asList(lines));

		append();
	}

	public Location getLocation() {
		return location;
	}

	public void setLocation(Location location) {
		this.location = location;
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
		this.lines.clear();
		this.lines = lines;

		append();
		return this;
	}

	public Hologram appendLine(@NotNull String line) {
		this.lines.clear();
		this.lines.add(line);

		append();
		return this;
	}

	public void delete() {
		for (ArmorStand armor : armorStands) {
			armor.setCustomNameVisible(false);
			armor.remove();

			plugin.getHologramManager().remove(armor);
		}

		armorStands.clear();
	}

	public boolean isDeleted() {
		return armorStands.isEmpty();
	}

	public void append() {
		delete();

		double y = location.getY();

		for (int i = 0; i <= lines.size() - 1; i++) {
			ArmorStand holo = getEntityArmorStand(location, y);
			holo.setCustomName(lines.get(i));
			armorStands.add(holo);

			plugin.getHologramManager().add(holo);
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