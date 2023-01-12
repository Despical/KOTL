/*
 * KOTL - Don't let others to climb top of the ladders!
 * Copyright (C) 2023 Despical
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

package me.despical.kotl.handler.hologram;

import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Despical
 * <p>
 * Created at 04.10.2020
 */
public class Hologram {

	private final Location location;
	private final Set<String> lines;
	private final Set<ArmorStand> armorStands;

	public Hologram(Location location, String... lines) {
		this.location = location;
		this.lines = me.despical.commons.util.Collections.setOf(lines);
		this.armorStands = new HashSet<>();

		append();
	}

	public Location getLocation() {
		return location;
	}

	public void delete() {
		for (ArmorStand armor : armorStands) {
			armor.remove();
		}

		armorStands.clear();
	}

	public boolean isDeleted() {
		return this.armorStands.isEmpty();
	}

	public void append() {
		delete();

		double y = location.getY();

		for (String line : lines) {
			ArmorStand holo = getEntityArmorStand(location, y);
			holo.setCustomName(line);
			armorStands.add(holo);
		}
	}

	public void appendLine(String line) {
		this.lines.clear();
		this.lines.add(line);

		append();
	}

	private ArmorStand getEntityArmorStand(Location loc, double y) {
		loc.setY(y);

		ArmorStand stand = (ArmorStand) loc.getWorld().spawnEntity(loc, EntityType.ARMOR_STAND);
		stand.setVisible(false);
		stand.setGravity(false);
		stand.setCustomNameVisible(true);
		return stand;
	}
}