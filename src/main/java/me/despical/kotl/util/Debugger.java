/*
 * KOTL - Don't let others to climb top of the ladders!
 * Copyright (C) 2021 Despical and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package me.despical.kotl.util;

import me.despical.commons.util.Strings;
import me.despical.kotl.Main;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Despical
 * <p>
 * Created at 20.06.2020
 */
public class Debugger {

	private static Main plugin;
	private static boolean enabled;
	private static final Logger logger = Logger.getLogger("KOTL");

	private Debugger() {
	}

	public static void setEnabled(Main plugin) {
		Debugger.plugin = plugin;
		Debugger.enabled = plugin.getDescription().getVersion().contains("debug") || plugin.getConfig().getBoolean("Debug-Messages");
	}

	public static boolean isEnabled() {
		return enabled;
	}

	public static void sendConsoleMessage(String message) {
		plugin.getServer().getConsoleSender().sendMessage(Strings.format(message));
	}

	public static void debug(Level level, String msg) {
		if (!enabled && (level != Level.WARNING || level != Level.SEVERE)) {
			return;
		}

		logger.log(level, "[KOTLDBG] " + msg);
	}

	public static void debug(String msg) {
		debug(Level.INFO, msg);
	}

	public static void debug(String msg, Object... params) {
		debug(Level.INFO, msg, params);
	}

	public static void debug(Level level, String msg, Object... params) {
		if (!enabled && (level != Level.WARNING || level != Level.FINE)) {
			return;
		}

		logger.log(level, "[KOTLDBG] " + msg, params);
	}
}