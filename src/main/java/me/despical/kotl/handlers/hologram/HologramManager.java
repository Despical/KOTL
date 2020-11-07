/*
 *  KOTL - Don't let others to climb top of the ladders!
 *  Copyright (C) 2020 Despical and contributors
 *  
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *  
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License
 *  along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package me.despical.kotl.handlers.hologram;

import org.bukkit.entity.ArmorStand;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Despical
 * <p>
 * Created at 04.10.2020
 */
public class HologramManager {

	private final List<ArmorStand> holograms;

	public HologramManager() {
		this.holograms = new ArrayList<>();
	}

	public List<ArmorStand> getHolograms() {
		return holograms;
	}

	public void add(ArmorStand hologram) {
		holograms.add(hologram);
	}

	public void remove(ArmorStand hologram) {
		holograms.remove(hologram);
	}
}