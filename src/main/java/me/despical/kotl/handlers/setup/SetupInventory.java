package me.despical.kotl.handlers.setup;

import com.github.despical.inventoryframework.Gui;
import com.github.despical.inventoryframework.pane.StaticPane;
import me.despical.commonsbox.configuration.ConfigUtils;
import me.despical.kotl.Main;
import me.despical.kotl.arena.Arena;
import me.despical.kotl.handlers.setup.components.ArenaRegisterComponents;
import me.despical.kotl.handlers.setup.components.MiscComponents;
import me.despical.kotl.handlers.setup.components.SpawnComponents;
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

	public static final String TUTORIAL_VIDEO = "https://www.youtube.com/watch?v=O_vkf_J4OgY";

	private final Random random = new Random();
	private final Main plugin = JavaPlugin.getPlugin(Main.class);
	private final FileConfiguration config = ConfigUtils.getConfig(plugin, "arenas");
	private final Arena arena;
	private final Player player;
	private Gui gui;
	private final SetupUtilities setupUtilities;

	public SetupInventory(Arena arena, Player player) {
		this.arena = arena;
		this.player = player;
		this.setupUtilities = new SetupUtilities(config);

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
		int rand = random.nextInt(8 + 1);

		switch (rand) {
			case 0:
				p.sendMessage(plugin.getChatManager().colorRawMessage("&e&lTIP: &7We are open source! You can always help us by contributing! Check https://github.com/Despical/KOTL"));
				break;
			case 1:
				p.sendMessage(plugin.getChatManager().colorRawMessage("&e&lTIP: &7Need help? Join our discord server: https://discordapp.com/invite/Vhyy4HA"));
				break;
			case 2:
				p.sendMessage(plugin.getChatManager().colorRawMessage("&e&lTIP: &7Need help? Check our wiki: https://github.com/Despical/KOTL/wiki"));
				break;
			case 3:
				p.sendMessage(plugin.getChatManager().colorRawMessage("&e&lTIP: &7Don't know where to start? Check out our tutorial video: " + TUTORIAL_VIDEO));
				break;
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

	public SetupUtilities getSetupUtilities() {
		return setupUtilities;
	}
}