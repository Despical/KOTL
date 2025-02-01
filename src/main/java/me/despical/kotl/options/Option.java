/*
 * KOTL - Don't let others climb to top of the ladders!
 * Copyright (C) 2025  Berke Akçen
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

package me.despical.kotl.options;

import me.despical.commons.serializer.InventorySerializer;
import me.despical.kotl.KOTL;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.function.Function;

/**
 * @author Despical
 * <p>
 * Created at 1.02.2025
 */
public enum Option {

    APPLY_KING_DELAY_BAR("King-Settings.Cooldown-Bar", true),
    BECOME_KING_IN_A_ROW("King-Settings.Become-King-In-A-Row", true),
    BLOCK_COMMANDS("Block-Commands", false),
    BOSS_BAR_ENABLED("Boss-Bar-Enabled", true),
    CHAT_FORMAT_ENABLED("Chat-Format-Enabled", true),
    CLEAR_EFFECTS("Clear-Effects", true),
    CLEAR_INVENTORY("Clear-Inventory", false),
    COOLDOWN("King-Settings.Cooldown", 5),
    COOLDOWN_WHEN_ALONE("King-Settings.Cooldown-When-Alone", false),
    COUNT_COOLDOWN_OUTSIDE("King-Settings.Count-Cooldown-Bar-Outside", false),
    DAMAGE_ENABLED("Damage-Enabled", true),
    DATABASE_ENABLED("Database-Enabled", false),
    DEATH_BLOCKS_ENABLED("Death-Blocks-Enabled", false),
    DISABLE_FALL_DAMAGE("Disable-Fall-Damage", true),
    DISABLE_SEPARATE_CHAT("Disable-Separate-Chat", false),
    FIREWORKS_ON_NEW_KING("Fireworks-On-New-King", true),
    HEAL_PLAYER((config) -> {
        List<String> list = config.getStringList("Inventory-Manager.Do-Not-Restore");
        list.forEach(InventorySerializer::addNonSerializableElements);
        return !list.contains("health");
    }),
    INVENTORY_MANAGER_ENABLED("Inventory-Manager.Enabled", true),
    JOIN_NOTIFY("Join-Notify", true),
    LEAVE_NOTIFY("Leave-Notify", true),
    PICK_UP_ITEMS("Pick-Up-Items", false),
    REMOVE_COOLDOWN_ON_JOIN("King-Settings.Remove-Cooldown-On.Join", false),
    REMOVE_COOLDOWN_ON_LEAVE("King-Settings.Remove-Cooldown-On.Leave", false),
    RESET_COOLDOWNS_ON_NEW_KING("King-Settings.Reset-Cooldowns-On-New-King", true),
    SCOREBOARD_ENABLED("Scoreboard-Enabled", true),
    SEPARATE_COOLDOWNS("King-Settings.Separate-Cooldowns", true),
    SHOW_COOLDOWN_ON_REJOIN("King-Settings.Show-Cooldown-If-Rejoin", true),
    UPDATE_GAME_MODE(config -> !config.getStringList("Inventory-Manager.Do-Not-Restore").contains("game-mode")),
    UPDATE_HUNGER(config -> !config.getStringList("Inventory-Manager.Do-Not-Restore").contains("hunger")),
    UPDATE_NOTIFIER_ENABLED("Update-Notifier-Enabled", false);

    final String path;
    final Object defaultValue;

    Option(String path, Object def) {
        this.path = path;
        this.defaultValue = def;
    }

    Option(Function<FileConfiguration, Object> supplier) {
        this.path = "";
        this.defaultValue = supplier.apply(JavaPlugin.getPlugin(KOTL.class).getConfig());
    }
}
