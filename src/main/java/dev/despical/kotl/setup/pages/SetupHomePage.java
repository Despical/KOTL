package dev.despical.kotl.setup.pages;

import dev.despical.fileitems.SpecialItem;
import dev.despical.inventoryframework.Gui;
import dev.despical.inventoryframework.GuiItem;
import dev.despical.inventoryframework.pane.PaginatedPane;
import dev.despical.inventoryframework.pane.StaticPane;
import dev.despical.kotl.arena.Arena;
import dev.despical.kotl.arena.options.ArenaKeys;
import dev.despical.kotl.setup.SetupMenu;
import dev.despical.kotl.setup.SetupPage;
import dev.despical.kotl.util.ItemUtils;
import dev.despical.kotl.util.Utils;
import dev.despical.kotl.util.Var;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;
import java.util.Optional;

public class SetupHomePage extends SetupPage {

    public static final NamespacedKey KING_PLATE_TOOL_KEY = new NamespacedKey(plugin, "setup_king_plate");

    public SetupHomePage(SetupMenu menu) {
        super(menu);
    }

    @Override
    public void beforeOpening(Gui gui) {
        int rows = arena.getOption(ArenaKeys.READY) ? 3 : 4;
        gui.setRows(rows);
    }

    @Override
    public void injectItems(PaginatedPane paginatedPane) {
        StaticPane pane = new StaticPane(9, 4);

        pane.addItem(createEndLocationItem(), 1, 1);
        pane.addItem(createKingPlateItem(), 3, 1);
        pane.addItem(createAreaSelectorItem(), 5, 1);
        pane.addItem(createPlayerSettingsItem(), 7, 1);

        GuiItem resetArenaRecordsItem = createArenaRecordResetItem();
        if (resetArenaRecordsItem != null) {
            pane.addItem(resetArenaRecordsItem, 3, 3);
        }

        if (!arena.getOption(ArenaKeys.READY)) {
            pane.addItem(createRegisterItem(), 8, 3);
        }

        paginatedPane.addPane(0, pane);
    }

    private GuiItem createEndLocationItem() {
        var specialItem = itemManager.getItem("end");
        ItemStack item = specialItem.getItemStack();

        return GuiItem.of(item, event -> {
            Player player = (Player) event.getWhoClicked();
            Location playerLoc = player.getLocation();
            Location targetLocation = event.isShiftClick()
                ? playerLoc.getBlock().getLocation().add(0.5, 0, 0.5)
                : playerLoc.clone();

            targetLocation.setYaw(playerLoc.getYaw());
            targetLocation.setPitch(event.isShiftClick() ? 0 : playerLoc.getPitch());

            arena.setOption(ArenaKeys.END_LOCATION, targetLocation);

            player.playSound(player.getLocation(), Sound.BLOCK_LODESTONE_PLACE, 1f, 1f);
            chatManager.sendRawMessage(player, specialItem.getCustomKey("message"));

            menu.close();
        });
    }

    private GuiItem createKingPlateItem() {
        SpecialItem specialItem = itemManager.getItem("king-plate");
        String materialName = ItemUtils.formatMaterialName(arena.getOption(ArenaKeys.ARENA_PLATE));

        ItemStack item = ItemUtils.formatItemStack(specialItem, Var.of("%selected_material%", materialName));
        item.setType(arena.getOption(ArenaKeys.ARENA_PLATE));

        return GuiItem.of(item, event -> {
            Player player = (Player) event.getWhoClicked();

            if (event.isShiftClick()) {
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1.25f);
                menu.openPlateMaterialSelection();
                return;
            }

            if (event.isRightClick()) {
                Location location = player.getLocation().getBlock().getLocation();
                setKingPlate(player, location);
                menu.setPage(0);
                return;
            }

            menu.close();

            ItemStack tool = specialItem.getItemStack().clone();
            Material material = arena.getOption(ArenaKeys.ARENA_PLATE);
            tool.setType(material);

            ItemMeta meta = tool.getItemMeta();
            if (meta != null) {
                meta.displayName(chatManager.parseMessage("<!i><#76FF03>" + ItemUtils.formatMaterialName(material)));
                meta.lore(null);
                meta.getPersistentDataContainer().set(KING_PLATE_TOOL_KEY, PersistentDataType.STRING, arena.getId());
                tool.setItemMeta(meta);
            }

            player.getInventory().setItemInMainHand(tool);
            player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1f, 1f);

            chatManager.sendMessage(player, "setup.king-plate-tool-received");
        });
    }

    public static void setKingPlate(Player player, Arena arena, Location location) {
        Material material = arena.getOption(ArenaKeys.ARENA_PLATE);

        Optional.ofNullable(arena.getOption(ArenaKeys.PLATE_LOCATION))
            .map(Location::getBlock)
            .ifPresent(block -> block.setType(Material.AIR));

        location.getBlock().setType(material);
        arena.setOption(ArenaKeys.PLATE_LOCATION, location);
        arena.setOption(ArenaKeys.ARENA_PLATE, material);

        plugin.getChatManager().sendMessage(player, "setup.king-plate-set");
        player.playSound(player.getLocation(), Sound.BLOCK_LODESTONE_PLACE, 1f, 1f);
    }

    private void setKingPlate(Player player, Location location) {
        setKingPlate(player, arena, location);
    }

    private GuiItem createAreaSelectorItem() {
        ItemStack item = ItemUtils.formatItemStack(itemManager.getItem("platform-selector"),
            Var.of("%outline_status%", arena.getOption(ArenaKeys.SHOW_OUTLINES) ? "<#00E676>ENABLED" : "<#FF5252>DISABLED")
        );

        return GuiItem.of(item, event -> {
            Player player = (Player) event.getWhoClicked();

            if (event.isRightClick()) {
                boolean newState = !arena.getOption(ArenaKeys.SHOW_OUTLINES);
                arena.setOption(ArenaKeys.SHOW_OUTLINES, newState);

                plugin.getOutlineManager().handleOutlines(arena);

                if (newState) {
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 1.8f);
                    chatManager.sendMessage(player, "setup.area-outline-enabled");
                } else {
                    player.playSound(player.getLocation(), Sound.BLOCK_BEACON_DEACTIVATE, 1f, 1f);
                    chatManager.sendMessage(player, "setup.area-outline-disabled");
                }

                menu.setPage(0);
                return;
            }

            menu.close();

            plugin.getCuboidSelector().giveSelectorWand(player, arena);

            player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1f, 1f);
            chatManager.sendMessage(player, "setup.area-selector-received");
        });
    }

    private GuiItem createPlayerSettingsItem() {
        ItemStack item = itemManager.getItem("player-settings").getItemStack();

        return GuiItem.of(item, event -> {
            Player player = (Player) event.getWhoClicked();
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);

            menu.openPlayerSettings();
        });
    }

    private GuiItem createArenaRecordResetItem() {
        int topScore = arena.getOption(ArenaKeys.TOP_KING_SCORE);
        String topName = arena.getOption(ArenaKeys.TOP_KING);

        if (topScore <= 0 || topName.equalsIgnoreCase("None")) {
            return null;
        }

        SpecialItem specialItem = itemManager.getItem("arena-record-reset");
        ItemStack item = ItemUtils.formatItemStack(specialItem,
            Var.of("%record_holder%", topName),
            Var.of("%record_score%", topScore)
        );

        return GuiItem.of(item, event -> {
            plugin.getUserManager().getUsers().forEach(user -> user.resetArenaStats(arena.getId()));

            arena.setOption(ArenaKeys.TOP_KING, "None");
            arena.setOption(ArenaKeys.TOP_KING_SCORE, 0);

            Player player = (Player) event.getWhoClicked();
            player.playSound(player.getLocation(), Sound.BLOCK_BEACON_DEACTIVATE, 1f, 0.8f);
            menu.setPage(0);
        });
    }

    private GuiItem createRegisterItem() {
        SpecialItem specialItem = itemManager.getItem("register-arena");

        return GuiItem.of(specialItem.getItemStack(), event -> {
            menu.close();

            Player player = (Player) event.getWhoClicked();
            String missingInfo = null;

            if (arena.getOption(ArenaKeys.END_LOCATION) == null) {
                missingInfo = "End Location";
            } else if (arena.getOption(ArenaKeys.PLATE_LOCATION) == null) {
                missingInfo = "King Plate";
            } else if (arena.getOption(ArenaKeys.MIN_CORNER) == null || arena.getOption(ArenaKeys.MAX_CORNER) == null) {
                missingInfo = "Arena Area";
            }

            if (missingInfo != null) {
                List<String> errorMessages = specialItem.getCustomKey("missing-option");

                Var errorVar = Var.of("%option%", missingInfo);
                chatManager.sendCenteredMessage(player, errorMessages, errorVar);

                player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_LAND, 0.5f, 0.5f);
                return;
            }

            arena.setOption(ArenaKeys.READY, true);

            Var[] vars = {
                Var.of("%arena_id%", arena.getId())
            };

            List<String> messages = specialItem.getCustomKey("registered-successfully");
            messages = messages.stream().map(line -> Utils.format(line, vars)).toList();

            chatManager.sendCenteredMessage(player, messages);
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
        });
    }
}
