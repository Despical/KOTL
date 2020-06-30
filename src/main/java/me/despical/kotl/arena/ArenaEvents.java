package me.despical.kotl.arena;

import org.apache.commons.lang.math.IntRange;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import me.despical.commonsbox.compat.XMaterial;
import me.despical.commonsbox.configuration.ConfigUtils;
import me.despical.commonsbox.serializer.LocationSerializer;
import me.despical.kotl.ConfigPreferences;
import me.despical.kotl.Main;
import me.despical.kotl.api.StatsStorage;
import me.despical.kotl.handler.ChatManager.ActionType;
import me.despical.kotl.handler.rewards.Reward;

/**
 * @author Despical
 * <p>
 * Created at 22.06.2020
 */
public class ArenaEvents implements Listener {
	
	private Main plugin;
	
	public ArenaEvents(Main plugin) {
		this.plugin = plugin;
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}

//	to do new arena manager, api impl. and broadcasting join msg
	@EventHandler
	public void onEnterAndLeaveGameArea(PlayerMoveEvent event) {
		Player player = event.getPlayer();
		Arena arena = isInArea(player.getLocation());
		if (!ArenaRegistry.isInArena(player) && arena != null) {
			arena.addPlayer(player);
			player.setGameMode(GameMode.SURVIVAL);
			player.setFoodLevel(20);
			player.setHealth(20d);
			arena.doBarAction(Arena.BarAction.ADD, player);
			if (plugin.getConfigPreferences().getOption(ConfigPreferences.Option.JOIN_NOTIFY)) {
				plugin.getChatManager().broadcastAction(arena, player, ActionType.JOIN);
			}
		} 
		if (ArenaRegistry.isInArena(player) && arena == null) {
			ArenaRegistry.getArena(player).doBarAction(Arena.BarAction.REMOVE, player);
			if (plugin.getConfigPreferences().getOption(ConfigPreferences.Option.LEAVE_NOTIFY)) {
				plugin.getChatManager().broadcastAction(ArenaRegistry.getArena(player), player, ActionType.LEAVE);
			}
			ArenaRegistry.getArena(player).removePlayer(player);
		}
	}
	
	@EventHandler
	public void onInteractWithPlate(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		if (!ArenaRegistry.isInArena(player)) {
			return;
		}
		Arena arena = ArenaRegistry.getArena(player);
		if (event.getAction() == Action.PHYSICAL) {
			if (event.getClickedBlock().getType() == XMaterial.OAK_PRESSURE_PLATE.parseMaterial()) {
				if (arena.getPlayers().size() == 1 && arena.getKing() == player) return;
				arena.setKing(player);
				plugin.getChatManager().broadcastAction(arena, player, ActionType.NEW_KING);
				plugin.getUserManager().getUser(player).addStat(StatsStorage.StatisticType.SCORE, 1);
				plugin.getRewardsFactory().performReward(player, Reward.RewardType.WIN);
				for (Player p : arena.getPlayers()) {
					plugin.getUserManager().getUser(p).addStat(StatsStorage.StatisticType.TOURS_PLAYED, 1);
					plugin.getRewardsFactory().performReward(p, Reward.RewardType.LOSE);
					for (StatsStorage.StatisticType stat : StatsStorage.StatisticType.values()) {
						plugin.getUserManager().getDatabase().saveStatistic(plugin.getUserManager().getUser(p), stat);
					}
				}
			}
		}
	}
	
	@EventHandler
	public void onInteractWithDeathBlocks(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		if (!plugin.getConfigPreferences().getOption(ConfigPreferences.Option.DEATHBLOCKS_ENABLED)) {
			return;
		}
		if (!ArenaRegistry.isInArena(player)) {
			return;
		}
		if (event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.LEFT_CLICK_BLOCK) {
			for (String material : plugin.getConfig().getStringList("Death-Blocks.Blacklisted-Blocks")) {
				if (event.getClickedBlock().getType() == Material.valueOf(material.toUpperCase())) {
					ArenaRegistry.getArena(player).doBarAction(Arena.BarAction.REMOVE, player);
					plugin.getChatManager().broadcastMessage(ArenaRegistry.getArena(player), plugin.getChatManager().colorMessage("In-Game.Clicked-Death-Block").replace("%player%", player.getName()));
					plugin.getRewardsFactory().performReward(player, Reward.RewardType.LOSE);
					player.teleport(ArenaRegistry.getArena(player).getEndLocation());
					ArenaRegistry.getArena(player).removePlayer(player);
					return;
				}
			}
		}
	}
	
	@EventHandler
	public void onDamage(EntityDamageByEntityEvent event) {
		if (!(event.getEntity() instanceof Player && event.getDamager() instanceof Player)) {
			return;
		}
		Player entity = (Player) event.getEntity();
		Player damager = (Player) event.getDamager();
		if (ArenaRegistry.isInArena(entity) && ArenaRegistry.isInArena(damager)) {
			event.setCancelled(false);
			event.setDamage(0d);
		}
	}
	
	private Arena isInArea(Location origin) {
		FileConfiguration config = ConfigUtils.getConfig(plugin, "arenas");
		Location first, second;
		for (Arena arena : ArenaRegistry.getArenas()) {
			if (!config.getBoolean("instances." + arena.getId() + ".isdone", false)) {
				continue;
			}
			first = LocationSerializer.locationFromString(config.getString("instances." + arena.getId() + ".areaMin"));
			second = LocationSerializer.locationFromString(config.getString("instances." + arena.getId() + ".areaMax"));
		
			if (new IntRange(first.getX(), second.getX()).containsDouble(origin.getX())
				&& new IntRange(first.getY(), second.getY()).containsDouble(origin.getY())
				&& new IntRange(first.getZ(), second.getZ()).containsDouble(origin.getZ())) {
				return arena;
			}
		}
		return null;
	}
}