package dev.despical.kotl.setup.pages;

import dev.despical.kotl.arena.options.ArenaKeys;
import dev.despical.kotl.setup.SetupMenu;
import dev.despical.kotl.setup.SetupPage;
import dev.despical.inventoryframework.Gui;
import dev.despical.inventoryframework.GuiItem;
import dev.despical.inventoryframework.pane.PaginatedPane;
import dev.despical.inventoryframework.pane.StaticPane;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Locale;

public class GamemodeSelectionPage extends SetupPage {

    private static final GameMode[] GAMEMODES = {GameMode.SURVIVAL, GameMode.CREATIVE, GameMode.ADVENTURE};

    public GamemodeSelectionPage(SetupMenu menu) {
        super(menu);
    }

    @Override
    public void beforeOpening(Gui gui) {
        gui.setRows(4);
    }

    @Override
    public void injectItems(PaginatedPane paginatedPane) {
        StaticPane pane = new StaticPane(9, 4);
        paginatedPane.addPane(0, pane);

        for (GameMode gamemode : GAMEMODES) {
            var itemConfig = itemManager.getItem("gamemode-" + gamemode.name().toLowerCase(Locale.ENGLISH));
            int slot = itemConfig.getCustomKey("slot");
            pane.addItem(createGamemodeItem(gamemode), slot);
        }

        var clearConfig = itemManager.getItem("clear-gamemode");
        int clearSlot = clearConfig.getCustomKey("slot");
        pane.addItem(createClearGamemodeItem(), clearSlot);
        pane.addItem(createPlayerSettingsBackItem(), 8, 3);
    }

    private GuiItem createGamemodeItem(GameMode gamemode) {
        ItemStack item = itemManager.getItem("gamemode-" + gamemode.name().toLowerCase(Locale.ENGLISH)).getItemStack();

        return GuiItem.of(item, event -> {
            Player player = (Player) event.getWhoClicked();
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1.5f);

            arena.setOption(ArenaKeys.ARENA_GAMEMODE, gamemode);
            menu.openPlayerSettings();
        });
    }

    private GuiItem createClearGamemodeItem() {
        ItemStack item = itemManager.getItem("clear-gamemode").getItemStack();

        return GuiItem.of(item, event -> {
            Player player = (Player) event.getWhoClicked();
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 1f);

            arena.setOption(ArenaKeys.ARENA_GAMEMODE, null);
            menu.openPlayerSettings();
        });
    }
}
