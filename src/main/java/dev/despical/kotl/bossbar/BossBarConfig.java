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
import dev.despical.kotl.chat.ChatManager;
import dev.despical.commons.configuration.ConfigUtils;
import lombok.AccessLevel;
import lombok.Getter;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import org.bukkit.configuration.file.FileConfiguration;

/**
 * @author Despical
 * <p>
 * Created at 06.06.2026
 */
@Getter
public class BossBarConfig {

    private boolean enabled;
    private BossBarData data;

    @Getter(AccessLevel.NONE)
    private final KOTL plugin;

    public BossBarConfig(KOTL plugin) {
        this.plugin = plugin;
        this.load();
    }

    public void load() {
        FileConfiguration config = ConfigUtils.getConfig(plugin, "bossbar");
        ChatManager chatManager = plugin.getChatManager();

        this.enabled = config.getBoolean("enabled", true);

        String text = config.getString("text", "");
        this.data = new BossBarData(
            chatManager.parseMessage(text),
            BossBar.Color.valueOf(config.getString("color", "PINK")),
            BossBar.Overlay.valueOf(config.getString("overlay", "PROGRESS")),
            !text.isEmpty()
        );
    }

    public record BossBarData(Component title, BossBar.Color color, BossBar.Overlay overlay, boolean visible) {
    }
}
