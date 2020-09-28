package me.despical.kotl.utils;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Despical
 * <p>
 * Created at 20.06.2020
 */
public class Debugger {

	private static boolean enabled = false;
	private static final Logger logger = Logger.getLogger("");

	private Debugger() {}

	public static void setEnabled(boolean enabled) {
		Debugger.enabled = enabled;
	}

	/**
	 * Prints debug message with selected log level. Messages of level INFO or TASK
	 * won't be posted if debugger is enabled, warnings and errors will be.
	 *
	 * @param level level of debugged message
	 * @param msg debugged message
	 */
	public static void debug(Level level, String msg) {
		if (!enabled && (level != Level.WARNING || level != Level.SEVERE)) {
			return;
		}

		logger.log(level, "[KOTLDBG] " + msg);
	}

	/**
	 * Prints debug message with selected log level and replaces parameters.
	 * Messages of level INFO or TASK won't be posted if debugger is enabled,
	 * warnings and errors will be.
	 *
	 * @param level level of debugged message
	 * @param params any params to debug
	 * @param msg debugged message
	 */
	public static void debug(Level level, String msg, Object... params) {
		if (!enabled && (level != Level.WARNING || level != Level.FINE)) {
			return;
		}

		logger.log(level, "[KOTLDBG] " + msg, params);
	}
}