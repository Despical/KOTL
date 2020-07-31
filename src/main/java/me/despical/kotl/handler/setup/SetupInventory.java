package me.despical.kotl.handler.setup;

import com.github.stefvanschie.inventoryframework.Gui;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import me.despical.commonsbox.configuration.ConfigUtils;
import me.despical.kotl.Main;
import me.despical.kotl.arena.Arena;
import me.despical.kotl.handler.setup.components.ArenaRegisterComponents;
import me.despical.kotl.handler.setup.components.MiscComponents;
import me.despical.kotl.handler.setup.components.SpawnComponents;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Random;

/**
 * @author Despical
 * <p>
 * Created at 22.06.2020
 */
public class SetupInventory {

	private static Random random = new Random();
	private static Main plugin = JavaPlugin.getPlugin(Main.class);
	private FileConfiguration config = ConfigUtils.getConfig(plugin, "arenas");
	private Arena arena;
	private Player player;
	private Gui gui;
	private SetupUtilities setupUtilities;

	public SetupInventory(Arena arena, Player player) {
		this.arena = arena;
		this.player = player;
		this.setupUtilities = new SetupUtilities(config, arena);
		prepareGui();
	}

	private void prepareGui() {
		this.gui = new Gui(plugin, 1, "King of the Ladder Arena Setup");
		this.gui.setOnGlobalClick(e -> e.setCancelled(true));
		StaticPane pane = new StaticPane(9, 1);
		this.gui.addPane(pane);
		prepareComponents(pane);
	}

	private void prepareComponents(StaticPane pane) {
		SpawnComponents spawnComponents = new SpawnComponents();
		spawnComponents.prepare(this);
		spawnComponents.injectComponents(pane);

		MiscComponents miscComponents = new MiscComponents();
		miscComponents.prepare(this);
		miscComponents.injectComponents(pane);

		ArenaRegisterComponents arenaRegistryComponents = new ArenaRegisterComponents();
		arenaRegistryComponents.prepare(this);
		arenaRegistryComponents.injectComponents(pane);
	}

	private void sendProTip(Player p) {
		int rand = random.nextInt(7 + 1);
		switch (rand) {
			case 0:
				p.sendMessage(plugin.getChatManager().colorRawMessage("&e&lTIP: &7We are open source! You can always help us by contributing! Check https://github.com/Despical/KOTL"));
				break;
			case 1:
				p.sendMessage(plugin.getChatManager().colorRawMessage("&e&lTIP: &7Need help? Join our discord server: https://discordapp.com/invite/Vhyy4HA"));
				break;
			case 2:
				p.sendMessage(plugin.getChatManager().colorRawMessage("&e&lTIP: &7Need help? Check our wiki: https://github.com/Despical/KOTL/wiki"));
			default:
				break;
		}
	}

	public void openInventory() {
		sendProTip(player);
		gui.show(player);
	}

	public Main getPlugin() {
		return plugin;
	}

	public FileConfiguration getConfig() {
		return config;
	}

	public Arena getArena() {
		return arena;
	}

	public Player getPlayer() {
		return player;
	}

	public Gui getGui() {
		return gui;
	}

	public SetupUtilities getSetupUtilities() {
		return setupUtilities;
	}
}