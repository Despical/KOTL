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