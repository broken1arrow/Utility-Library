package org.broken.arrow.library.utility;

public abstract class CommandHolder extends org.broken.arrow.command.library.command.CommandHolder {

	/**
	 * Set the prefix for subcommand. Use | like this
	 * first|second command to add two options for same
	 * command.
	 *
	 * @param commandLable the prefix you want as sublable.
	 */
	public CommandHolder(final String commandLable) {
		super(commandLable);
	}

}
