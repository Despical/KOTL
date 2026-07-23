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

package dev.despical.kotl.setup;

import dev.despical.commons.configuration.ConfigUtils;
import dev.despical.inventoryframework.Gui;
import dev.despical.inventoryframework.pane.PaginatedPane;
import dev.despical.kotl.KOTL;
import dev.despical.kotl.arena.Arena;
import dev.despical.kotl.menu.Menu;
import dev.despical.kotl.setup.pages.GamemodeSelectionPage;
import dev.despical.kotl.setup.pages.PlateMaterialSelectionPage;
import dev.despical.kotl.setup.pages.PlayerSettingsPage;
import dev.despical.kotl.setup.pages.SetupHomePage;
import dev.despical.kotl.user.User;
import dev.despical.kotl.util.Var;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * @author Despical
 * <p>
 * Created at 05.06.2026
 */
@Getter
public class SetupMenu implements Menu {

    private Gui gui;
    protected PaginatedPane basePane;

    private final User user;
    private final Arena arena;
    private final Map<Integer, Supplier<SetupPage>> pages;

    public SetupMenu(User user, Arena arena) {
        this.user = user;
        this.arena = arena;
        this.pages = new HashMap<>();
        this.initializeGui();
    }

    private void initializeGui() {
        KOTL plugin = KOTL.getInstance();
        FileConfiguration config = ConfigUtils.getConfig(plugin, "menu/setup-menu");

        String rawTitle = config.getString("title", "Arena Editor");
        Component title = plugin.getChatManager().parseMessage(rawTitle, Var.of("%arena_id%", arena.getId()));

        gui = new Gui(plugin, 5, title);
        gui.setOnGlobalClick(event -> event.setCancelled(true));

        pages.put(0, () -> new SetupHomePage(this));
        setPage(0);
    }

    public void setPage(int page) {
        gui.removePanes();

        basePane = new PaginatedPane(9, 6);
        gui.addPane(basePane);

        SetupPage setupPage = pages.get(page).get();
        setupPage.beforeOpening(gui);
        setupPage.injectItems(basePane);

        basePane.setPage(0);

        gui.update();
    }

    public void openPlayerSettings() {
        gui.removePanes();

        basePane = new PaginatedPane(9, 6);
        gui.addPane(basePane);

        SetupPage playerSettingsPage = new PlayerSettingsPage(this);
        playerSettingsPage.beforeOpening(gui);
        playerSettingsPage.injectItems(basePane);

        basePane.setPage(0);

        gui.update();
    }

    public void openGamemodeSelection() {
        gui.removePanes();

        basePane = new PaginatedPane(9, 6);
        gui.addPane(basePane);

        SetupPage gamemodePage = new GamemodeSelectionPage(this);
        gamemodePage.beforeOpening(gui);
        gamemodePage.injectItems(basePane);

        basePane.setPage(0);

        gui.update();
    }

    public void openPlateMaterialSelection() {
        gui.removePanes();

        basePane = new PaginatedPane(9, 6);
        gui.addPane(basePane);

        SetupPage platePage = new PlateMaterialSelectionPage(this);
        platePage.beforeOpening(gui);
        platePage.injectItems(basePane);

        basePane.setPage(0);

        gui.update();
    }

    @Override
    public void open() {
        Player player = user.getPlayer();
        player.setGameMode(GameMode.CREATIVE);
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1.5f);

        gui.show(player);
    }
}
