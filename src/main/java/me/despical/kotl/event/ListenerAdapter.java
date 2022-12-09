package me.despical.kotl.event;

import me.despical.commons.util.LogUtils;
import me.despical.kotl.Main;
import me.despical.kotl.arena.ArenaEvents;
import me.despical.kotl.handler.ChatManager;
import org.bukkit.event.Listener;

/**
 * @author Despical
 * <p>
 * Created at 12.07.2022
 */
public abstract class ListenerAdapter implements Listener {

	protected final Main plugin;
	protected final ChatManager chatManager;

	public ListenerAdapter(Main plugin) {
		this.plugin = plugin;
		this.chatManager = plugin.getChatManager();
		this.plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}

	public static void registerEvents(Main plugin) {
		final Class<?>[] listenerAdapters = {Events.class, ArenaEvents.class, ChatEvents.class};

		try {
			for (Class<?> listenerAdapter : listenerAdapters) {
				listenerAdapter.getConstructor(Main.class).newInstance(plugin);

				LogUtils.log("[Listener Adapter] Registering new listener class: {0}", listenerAdapter.getSimpleName());
			}
		} catch (Exception ignored) {
			LogUtils.sendConsoleMessage("&cAn exception occured on event registering.");
		}
	}
}