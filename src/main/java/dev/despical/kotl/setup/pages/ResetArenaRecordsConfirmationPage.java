/*
 * KOTL - Don't let others climb to top of the ladders!
 * Copyright (C) 2026  Berke Akçen
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package dev.despical.kotl.setup.pages;

import dev.despical.fileitems.SpecialItem;
import dev.despical.inventoryframework.Gui;
import dev.despical.inventoryframework.GuiItem;
import dev.despical.inventoryframework.pane.PaginatedPane;
import dev.despical.inventoryframework.pane.StaticPane;
import dev.despical.kotl.arena.options.ArenaKeys;
import dev.despical.kotl.setup.SetupMenu;
import dev.despical.kotl.setup.SetupPage;
import dev.despical.kotl.util.ItemUtils;
import dev.despical.kotl.util.Var;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * @author Despical
 * <p>
 * Created at 25.07.2026
 */
public class ResetArenaRecordsConfirmationPage extends SetupPage {

    public ResetArenaRecordsConfirmationPage(SetupMenu menu) {
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

        pane.addItem(createRecordPreviewItem(), 4, 1);
        pane.addItem(createConfirmItem(), 2, 2);
        pane.addItem(createCancelItem(), 6, 2);
    }

    private GuiItem createRecordPreviewItem() {
        String recordHolder = arena.getOption(ArenaKeys.TOP_KING);
        SpecialItem specialItem = itemManager.getItem("arena-record-reset");
        ItemStack item = ItemUtils.formatItemStack(specialItem,
            Var.of("%record_holder%", recordHolder),
            Var.of("%record_score%", arena.getOption(ArenaKeys.TOP_KING_SCORE))
        );

        ItemUtils.applyArenaRecordResetHead(item, recordHolder);
        return GuiItem.of(item, event -> event.setCancelled(true));
    }

    private GuiItem createConfirmItem() {
        ItemStack item = itemManager.getItem("confirm-arena-record-reset").getItemStack();

        return GuiItem.of(item, event -> {
            plugin.getUserManager().getUsers().forEach(user -> user.resetArenaStats(arena.getId()));

            arena.setOption(ArenaKeys.KING, null);
            arena.setOption(ArenaKeys.LAST_KING, "None");
            arena.setOption(ArenaKeys.TOP_KING, "None");
            arena.setOption(ArenaKeys.TOP_KING_SCORE, 0);

            plugin.getDatabase().saveAllData();
            plugin.getArenaDataSaver().saveAllArenas();
            arena.getGame().getScoreboardManager().updateAllScoreboards();

            Player player = (Player) event.getWhoClicked();
            player.playSound(player.getLocation(), Sound.BLOCK_BEACON_DEACTIVATE, 1f, 0.7f);
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.8f, 1.3f);
            chatManager.sendMessage(player, "setup.arena-record-reset", Var.of("%arena_id%", arena.getId()));

            menu.close();
        });
    }

    private GuiItem createCancelItem() {
        ItemStack item = itemManager.getItem("cancel-arena-record-reset").getItemStack();

        return GuiItem.of(item, event -> {
            Player player = (Player) event.getWhoClicked();
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 0.8f);

            menu.setPage(0);
        });
    }
}
