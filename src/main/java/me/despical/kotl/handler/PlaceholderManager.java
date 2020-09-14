package me.despical.kotl.handler;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.despical.kotl.Main;
import me.despical.kotl.api.StatsStorage;
import me.despical.kotl.arena.Arena;
import me.despical.kotl.arena.ArenaRegistry;

/**
 * @author Despical
 * <p>
 * Created at 22.06.2020
 */
public class PlaceholderManager extends PlaceholderExpansion {
	
	private final Main plugin = JavaPlugin.getPlugin(Main.class);
	
	@Override
	public boolean persist() {
		return true;
	}

	public String getIdentifier() {
		return "kotl";
	}

	public String getPlugin() {
		return null;
	}

	public String getAuthor() {
		return "Despical";
	}

	public String getVersion() {
		return "1.1.3b";
	}

	public String onPlaceholderRequest(Player player, String id) {
		if (player == null) {
			return null;
		}
		switch (id.toLowerCase()) {
		case "score":
			return String.valueOf(StatsStorage.getUserStats(player, StatsStorage.StatisticType.SCORE));
		case "tours_played":
			return String.valueOf(StatsStorage.getUserStats(player, StatsStorage.StatisticType.TOURS_PLAYED));
		default:
			return handleArenaPlaceholderRequest(id);
		}
	}

	private String handleArenaPlaceholderRequest(String id) {
		if (!id.contains(":")) {
			return null;
		}
		String[] data = id.split(":");
		Arena arena = ArenaRegistry.getArena(data[0]);
		if (arena == null) {
			return null;
		}
		switch (data[1].toLowerCase()) {
		case "players":
			return String.valueOf(arena.getPlayers().size());
		case "king":
			return arena.getKing() == null ? plugin.getChatManager().colorMessage("In-Game.There-Is-No-King") : "Nobody";
		default:
			return null;
		}
	}
}