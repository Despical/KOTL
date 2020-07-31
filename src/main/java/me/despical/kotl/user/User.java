package me.despical.kotl.user;

import me.despical.kotl.Main;
import me.despical.kotl.api.StatsStorage;
import me.despical.kotl.api.events.player.KOTLPlayerStatisticChangeEvent;
import me.despical.kotl.arena.Arena;
import me.despical.kotl.arena.ArenaRegistry;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.EnumMap;
import java.util.Map;

/**
 * @author Despical
 * <p>
 * Created at 20.06.2020
 */
public class User {

	private static Main plugin = JavaPlugin.getPlugin(Main.class);
	private Player player;
	private Map<StatsStorage.StatisticType, Integer> stats = new EnumMap<>(StatsStorage.StatisticType.class);

	public User(Player player) {
		this.player = player;
	}

	public Arena getArena() {
		return ArenaRegistry.getArena(player);
	}

	public Player getPlayer() {
		return player;
	}

	public int getStat(StatsStorage.StatisticType stat) {
		if (!stats.containsKey(stat)) {
			stats.put(stat, 0);
			return 0;
		} else if (stats.get(stat) == null) {
			return 0;
		}
		return stats.get(stat);
	}

	public void setStat(StatsStorage.StatisticType stat, int i) {
		stats.put(stat, i);
		Bukkit.getScheduler().runTask(plugin, () -> {
			KOTLPlayerStatisticChangeEvent playerStatisticChangeEvent = new KOTLPlayerStatisticChangeEvent(getArena(), player, stat, i);
			Bukkit.getPluginManager().callEvent(playerStatisticChangeEvent);
		});
	}

	public void addStat(StatsStorage.StatisticType stat, int i) {
		stats.put(stat, getStat(stat) + i);
		Bukkit.getScheduler().runTask(plugin, () -> {
			KOTLPlayerStatisticChangeEvent playerStatisticChangeEvent = new KOTLPlayerStatisticChangeEvent(getArena(), player, stat, getStat(stat));
			Bukkit.getPluginManager().callEvent(playerStatisticChangeEvent);
		});
	}
}