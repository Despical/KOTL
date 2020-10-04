package me.despical.kotl.api.events.player;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

import me.despical.kotl.api.StatsStorage;
import me.despical.kotl.api.events.KOTLEvent;
import me.despical.kotl.arena.Arena;

/**
 * @author Despical
 * @see StatsStorage.StatisticType
 * @since 1.0.0
 * <p>
 * Called when player receive new statistic.
 */
public class KOTLPlayerStatisticChangeEvent extends KOTLEvent {

	private final HandlerList HANDLERS = new HandlerList();
	private final Player player;
	private final StatsStorage.StatisticType statisticType;
	private final int number;

	public KOTLPlayerStatisticChangeEvent(Arena eventArena, Player player, StatsStorage.StatisticType statisticType, int number) {
		super(eventArena);

		this.player = player;
		this.statisticType = statisticType;
		this.number = number;
	}

	@Override
	public HandlerList getHandlers() {
		return HANDLERS;
	}

	public Player getPlayer() {
		return player;
	}

	public StatsStorage.StatisticType getStatisticType() {
		return statisticType;
	}

	public int getNumber() {
		return number;
	}
}