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

    APPLY_KING_DELAY_BAR("king-settings.cooldown-bar", true),
    BLOCK_OUTSIDE_CHAT("chat-settings.block-outside-chat", true),
    BOSS_BAR_ENABLED("boss-bar-enabled", true),
    BECOME_KING_IN_A_ROW("king-settings.become-king-in-a-row", true),
    CLEAR_EFFECTS_ON_JOIN("player-settings.clear-effects-on-join", true),
    CLEAR_INVENTORY_ON_JOIN("player-settings.clear-inventory-on-join", true),
    COOLDOWN_WHEN_ALONE("king-settings.cooldown-when-alone", false),
    DEBUG("debug", false),
    DISABLE_CHAT_IN_GAME("chat-settings.disable-chat-in-game", false),
    DISABLE_COMMANDS_WHILE_PLAYING("command-settings.disable-commands-while-playing", true),
    DISABLE_FALL_DAMAGE("disable-fall-damage", true),
    ENABLE_CHAT_FORMATTING("chat-settings.enable-formatting", true),
    EVENT_PROFILING_ENABLED("event-profiling.enabled", false),
    EVENT_PROFILING_VERBOSE("event-profiling.verbose", false),
    FIREWORKS_ON_NEW_KING("fireworks-on-new-king", true),
    JOIN_NOTIFY("notify.join", true),
    KING_PLATE_KNOCKBACK_ENABLED("king-settings.plate-knockback.enabled", true),
    LEAVE_NOTIFY("notify.leave", true),
    PICK_UP_ITEMS("pick-up-items", false),
    SCOREBOARD_ENABLED("scoreboard-enabled", true),
    SEPARATE_CHAT("chat-settings.separate-chat", true),
    SHOW_COOLDOWN_ON_REJOIN("king-settings.show-cooldown-on-rejoin", true),
    UPDATE_NOTIFIER("update-notifier", true);

    private final String path;
    private final Boolean defaultValue;
    private final Class<Boolean> type = Boolean.class;
}
