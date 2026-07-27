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

package dev.despical.kotl.bossbar;

import dev.despical.kotl.KOTL;
import dev.despical.kotl.arena.options.ArenaKeys;
import dev.despical.kotl.game.Game;
import dev.despical.kotl.option.BooleanOption;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

/**
 * Manages the boss bar display for a King of the Ladder game.
 * Handles visibility, appearance, and updates based on configuration.
 *
 * @author Berke Akçen
 * <p>
 * Created at 06.06.2026
 */
public class BossBarManager {

    private final Game game;
    private final BossBar bossBar;
    private final BossBarConfig configProvider;

    public BossBarManager(Game game) {
        this.game = game;
        this.bossBar = BossBar.bossBar(Component.empty(), 1F, BossBar.Color.PURPLE, BossBar.Overlay.PROGRESS);
        this.configProvider = KOTL.getInstance().getGameManager().getBossBarConfig();
        this.update();
    }

    /**
     * Updates the boss bar appearance from config.
     */
    public void update() {
        if (!isEnabled()) {
            removeAll();
            return;
        }

        BossBarConfig.BossBarData barData = configProvider.getData();
        if (barData == null) {
            removeAll();
            return;
        }

        bossBar.name(barData.title());
        bossBar.color(barData.color());
        bossBar.overlay(barData.overlay());

        if (barData.visible()) {
            showToAll();
        } else {
            removeAll();
        }
    }

    /**
     * Updates the boss bar progress.
     *
     * @param progress the progress value (0.0 to 1.0)
     */
    public void setProgress(float progress) {
        bossBar.progress(Math.max(0f, Math.min(progress, 1f)));
    }

    /**
     * Shows the boss bar to all players in the game.
     */
    public void showToAll() {
        game.getPlayers().forEach(bossBar::addViewer);
    }

    /**
     * Removes the boss bar from all players in the game.
     */
    public void removeAll() {
        game.getPlayers().forEach(bossBar::removeViewer);
    }

    /**
     * Adds the boss bar to a specific player.
     *
     * @param player the player to add
     */
    public void addPlayer(Player player) {
        if (isEnabled() && shouldShow()) {
            bossBar.addViewer(player);
        }
    }

    /**
     * Removes the boss bar from a specific player.
     *
     * @param player the player to remove
     */
    public void removePlayer(Player player) {
        bossBar.removeViewer(player);
    }

    private boolean shouldShow() {
        BossBarConfig.BossBarData data = configProvider.getData();
        return data != null && data.visible();
    }

    private boolean isEnabled() {
        return BooleanOption.BOSS_BAR_ENABLED.value()
            && configProvider.isEnabled()
            && game.getArena().getOption(ArenaKeys.ARENA_BOSSBAR_ENABLED);
    }
}
