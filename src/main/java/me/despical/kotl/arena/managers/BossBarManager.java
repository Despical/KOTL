package me.despical.kotl.arena.managers;

import me.despical.commons.number.NumberUtils;
import me.despical.kotl.Main;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

/**
 * @author Despical
 * <p>
 * Created at 30.09.2022
 */
public class BossBarManager extends BukkitRunnable {

	private final Main plugin;
	private final BossBar bossBar;
	private final List<String> messages;

	private int queue = 0;

	public BossBarManager(Main plugin) {
		this.plugin = plugin;
		this.bossBar = plugin.getServer().createBossBar("", BarColor.valueOf(plugin.getChatManager().message("boss_bar.color")), BarStyle.valueOf(plugin.getChatManager().message("boss_bar.style")));
		this.messages = plugin.getChatManager().getStringList("boss_bar.messages");

		this.runTaskTimer(plugin, 20, NumberUtils.getInt(plugin.getChatManager().message("boss_bar.interval"), 300));
	}

	public void addPlayer(Player player) {
		this.bossBar.addPlayer(player);
	}

	public void removePlayer(Player player) {
		this.bossBar.removePlayer(player);
	}

	@Override
	public void run() {
		if (queue + 1 > messages.size()) queue = 0;

		this.bossBar.setTitle(plugin.getChatManager().coloredRawMessage(messages.get(queue++)));
	}
}