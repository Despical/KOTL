package me.despical.kotl.api.events;

import me.despical.kotl.arena.Arena;
import org.bukkit.event.Event;

/**
 * @author Despical
 * <p>
 * Created at 20.06.2020
 * <p>
 * Represents King of the Ladder game related events.
 */
public abstract class KOTLEvent extends Event {

	protected Arena arena;

	public KOTLEvent(Arena eventArena) {
		arena = eventArena;
	}

	/**
	 * Returns event arena
	 *
	 * @return event arena
	 */
	public Arena getArena() {
		return arena;
	}
}