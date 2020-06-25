package me.despical.kotl.commands.exception;

/**
 * @author Despical
 * <p>
 * Created at 22.06.2020
 */
public class CommandException extends Exception {

	private static final long serialVersionUID = 1L;

	public CommandException(String message) {
		super (message);
	}
}