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

package dev.despical.kotl.option;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * @author Despical
 * <p>
 * Created at 05.06.2026
 */
@Getter
@RequiredArgsConstructor
public enum BooleanOption implements ConfigOption<Boolean> {

    APPLY_KING_DELAY_BAR("King-Settings.Cooldown-Bar", true),
    BECOME_KING_IN_A_ROW("King-Settings.Become-King-In-A-Row", true),
    DISABLE_COMMANDS_WHILE_PLAYING("command-settings.disable-commands-while-playing", true),
    BLOCK_OUTSIDE_CHAT("chat-settings.block-outside-chat", true),
    CLEAR_EFFECTS_ON_JOIN("player-settings.clear-effects-on-join", true),
    CLEAR_INVENTORY_ON_JOIN("player-settings.clear-inventory-on-join", true),
    COOLDOWN_WHEN_ALONE("King-Settings.Cooldown-When-Alone", false),
    COUNT_COOLDOWN_OUTSIDE("King-Settings.Count-Cooldown-Bar-Outside", false),
    DAMAGE_ENABLED("Damage-Enabled", true),
    DEATH_BLOCKS_ENABLED("Death-Blocks-Enabled", false),
    DEBUG("debug", false),
    DISABLE_CHAT_IN_GAME("chat-settings.disable-chat-in-game", false),
    DISABLE_FALL_DAMAGE("Disable-Fall-Damage", true),
    ENABLE_CHAT_FORMATTING("chat-settings.enable-formatting", true),
    EVENT_PROFILING_ENABLED("event-profiling.enabled", false),
    EVENT_PROFILING_VERBOSE("event-profiling.verbose", false),
    FIREWORKS_ON_NEW_KING("Fireworks-On-New-King", true),
    JOIN_NOTIFY("Join-Notify", true),
    LEAVE_NOTIFY("Leave-Notify", true),
    PICK_UP_ITEMS("Pick-Up-Items", false),
    REMOVE_COOLDOWN_ON_JOIN("King-Settings.Remove-Cooldown-On.Join", false),
    REMOVE_COOLDOWN_ON_LEAVE("King-Settings.Remove-Cooldown-On.Leave", false),
    RESET_COOLDOWNS_ON_NEW_KING("King-Settings.Reset-Cooldowns-On-New-King", true),
    SEPARATE_CHAT("chat-settings.separate-chat", true),
    SEPARATE_COOLDOWNS("King-Settings.Separate-Cooldowns", true),
    SHOW_COOLDOWN_ON_REJOIN("King-Settings.Show-Cooldown-If-Rejoin", true),
    UPDATE_NOTIFIER("update-notifier", true);

    private final String path;
    private final Boolean defaultValue;
    private final Class<Boolean> type = Boolean.class;
}
