package me.despical.kotl.utils;

import org.bukkit.ChatColor;

import static org.bukkit.Bukkit.getConsoleSender;

/**
 * @author Despical
 * <p>
 * Created at 20.06.2020
 */
public class MessageUtils {

	private MessageUtils() {}
	
	public static void thisVersionIsNotSupported() {
		getConsoleSender().sendMessage(ChatColor.RED + "  _   _           _                                                    _                _ ");
		getConsoleSender().sendMessage(ChatColor.RED + " | \\ | |   ___   | |_     ___   _   _   _ __    _ __     ___    _ __  | |_    ___    __| |");
		getConsoleSender().sendMessage(ChatColor.RED + " |  \\| |  / _ \\  | __|   / __| | | | | | '_ \\  | '_ \\   / _ \\  | '__| | __|  / _ \\  / _` |");
		getConsoleSender().sendMessage(ChatColor.RED + " | |\\  | | (_) | | |_    \\__ \\ | |_| | | |_) | | |_) | | (_) | | |    | |_  |  __/ | (_| |");
		getConsoleSender().sendMessage(ChatColor.RED + " |_| \\_|  \\___/   \\__|   |___/  \\__,_| | .__/  | .__/   \\___/  |_|     \\__|  \\___|  \\__,_|");
		getConsoleSender().sendMessage(ChatColor.RED + "                                       |_|     |_|                                        ");
	}

	public static void errorOccurred() {
		getConsoleSender().sendMessage(ChatColor.RED + "  _____                                                                                  _   _ ");
		getConsoleSender().sendMessage(ChatColor.RED + " | ____|  _ __   _ __    ___    _ __      ___     ___    ___   _   _   _ __    ___    __| | | |");
		getConsoleSender().sendMessage(ChatColor.RED + " |  _|   | '__| | '__|  / _ \\  | '__|    / _ \\   / __|  / __| | | | | | '__|  / _ \\  / _` | | |");
		getConsoleSender().sendMessage(ChatColor.RED + " | |___  | |    | |    | (_) | | |      | (_) | | (__  | (__  | |_| | | |    |  __/ | (_| | |_|");
		getConsoleSender().sendMessage(ChatColor.RED + " |_____| |_|    |_|     \\___/  |_|       \\___/   \\___|  \\___|  \\__,_| |_|     \\___|  \\__,_| (_)");
		getConsoleSender().sendMessage(ChatColor.RED + "                                                                                               ");
	}

	public static void updateIsHere() {
		getConsoleSender().sendMessage(ChatColor.GREEN + "  _   _               _           _          ");
		getConsoleSender().sendMessage(ChatColor.GREEN + " | | | |  _ __     __| |   __ _  | |_    ___ ");
		getConsoleSender().sendMessage(ChatColor.GREEN + " | | | | | '_ \\   / _` |  / _` | | __|  / _ \\");
		getConsoleSender().sendMessage(ChatColor.GREEN + " | |_| | | |_) | | (_| | | (_| | | |_  |  __/");
		getConsoleSender().sendMessage(ChatColor.GREEN + "  \\___/  | .__/   \\__,_|  \\__,_|  \\__|  \\___|");
		getConsoleSender().sendMessage(ChatColor.GREEN + "         |_|                                 ");
	}
}