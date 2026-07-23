package dev.despical.kotl.setup.pages;

import dev.despical.commons.configuration.ConfigUtils;
import dev.despical.fileitems.SpecialItem;
import dev.despical.inventoryframework.Gui;
import dev.despical.inventoryframework.GuiItem;
import dev.despical.inventoryframework.pane.PaginatedPane;
import dev.despical.inventoryframework.pane.StaticPane;
import dev.despical.kotl.arena.options.ArenaKeys;
import dev.despical.kotl.option.BooleanOption;
import dev.despical.kotl.setup.SetupMenu;
import dev.despical.kotl.setup.SetupPage;
import dev.despical.kotl.util.Var;
import net.kyori.adventure.text.Component;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class PlayerSettingsPage extends SetupPage {

    public PlayerSettingsPage(SetupMenu menu) {
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

        pane.addItem(createGamemodeSelectorItem(), 1, 1);
        pane.addItem(createClearInventoryToggleItem(), 3, 1);
        pane.addItem(createClearEffectsToggleItem(), 5, 1);
        pane.addItem(createScoreboardToggleItem(), 7, 1);
        pane.addItem(createBossBarToggleItem(), 4, 2);
        pane.addItem(createGoBackItem(), 8, 3);
    }

    private GuiItem createGamemodeSelectorItem() {
        SpecialItem specialItem = itemManager.getItem("gamemode-selector");
        ItemStack item = replaceLore(specialItem.getItemStack().clone(), Var.of("%current_gamemode%", arena.getOption(ArenaKeys.ARENA_GAMEMODE).name()));

        return GuiItem.of(item, event -> {
            Player player = (Player) event.getWhoClicked();
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1.5f);
            menu.openGamemodeSelection();
        });
    }

    private GuiItem createClearInventoryToggleItem() {
        return createGlobalToggle("clear-inventory-toggle", BooleanOption.CLEAR_INVENTORY_ON_JOIN, "%inventory_toggle_status%");
    }

    private GuiItem createClearEffectsToggleItem() {
        return createGlobalToggle("clear-effects-toggle", BooleanOption.CLEAR_EFFECTS_ON_JOIN, "%effects_toggle_status%");
    }

    private GuiItem createScoreboardToggleItem() {
        SpecialItem specialItem = itemManager.getItem("scoreboard-toggle");
        ItemStack item = replaceLore(specialItem.getItemStack().clone(), Var.of("%scoreboard_toggle_status%", status(arena.getOption(ArenaKeys.ARENA_SCOREBOARD_ENABLED))));

        return GuiItem.of(item, event -> {
            boolean newValue = !arena.getOption(ArenaKeys.ARENA_SCOREBOARD_ENABLED);

            arena.setOption(ArenaKeys.ARENA_SCOREBOARD_ENABLED, newValue);
            arena.getGame().getScoreboardManager().refreshAllScoreboards();

            clickAndReopen(event, newValue);
        });
    }

    private GuiItem createBossBarToggleItem() {
        SpecialItem specialItem = itemManager.getItem("bossbar-toggle");
        ItemStack item = replaceLore(specialItem.getItemStack().clone(), Var.of("%bossbar_toggle_status%", status(arena.getOption(ArenaKeys.ARENA_BOSSBAR_ENABLED))));

        return GuiItem.of(item, event -> {
            boolean newValue = !arena.getOption(ArenaKeys.ARENA_BOSSBAR_ENABLED);
            arena.setOption(ArenaKeys.ARENA_BOSSBAR_ENABLED, newValue);

            clickAndReopen(event, newValue);
        });
    }

    private GuiItem createGlobalToggle(String itemId, BooleanOption option, String placeholder) {
        SpecialItem specialItem = itemManager.getItem(itemId);
        ItemStack item = replaceLore(specialItem.getItemStack().clone(), Var.of(placeholder, status(option.value())));

        return GuiItem.of(item, event -> {
            boolean newValue = !option.value();
            plugin.getConfig().set(option.getPath(), newValue);
            ConfigUtils.saveConfig(plugin, plugin.getConfig(), "config");
            plugin.getOptions().reloadOptions();
            clickAndReopen(event, newValue);
        });
    }

    private void clickAndReopen(org.bukkit.event.inventory.InventoryClickEvent event, boolean enabled) {
        Player player = (Player) event.getWhoClicked();

        if (enabled) {
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 1.8f);
        } else {
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 0.8f);
        }

        menu.openPlayerSettings();
    }

    private ItemStack replaceLore(ItemStack item, Var var) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasLore()) return item;

        List<Component> lore = meta.lore();
        if (lore != null) {
            meta.lore(lore.stream().map(line -> chatManager.replaceVarsInComponent(line, var)).toList());
        }

        item.setItemMeta(meta);
        return item;
    }

    private String status(boolean enabled) {
        return enabled ? "<#00E676>ENABLED" : "<#FF5252>DISABLED";
    }
}
