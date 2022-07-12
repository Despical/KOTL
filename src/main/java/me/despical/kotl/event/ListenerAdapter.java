package me.despical.kotl.event;

import me.despical.kotl.Main;
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
}