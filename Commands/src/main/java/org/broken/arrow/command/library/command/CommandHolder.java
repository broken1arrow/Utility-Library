package org.broken.arrow.command.library.command;

import org.bukkit.command.CommandException;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

public abstract class CommandHolder implements CommandHandler {

	private final String commandLabel;
	private String[] arguments;
	private CommandSender sender;

	/**
	 * Set the prefix for subcommand. Use | like this
	 * first|second command to add two options for same
	 * command.
	 *
	 * @param commandLabel the prefix you want as sublable.
	 */
	protected CommandHolder(final String commandLabel) {
		this.commandLabel = commandLabel;

	}

	/**
	 * Called when the command is executed by the specified sender. The sender can be a player or another command sender,
	 * such as the console. Make sure to check the type of the sender before casting it to a specific sender type.
	 *
	 * @param sender       The command sender, which can be a player or console.
	 * @param commandLabel The command label. For example, if the command executed is "/commandName menu 1", the command
	 *                     label will be "commandName".
	 * @param cmdArgs      The arguments for the command. The `cmdArgs` array contains any additional arguments provided
	 *                     after the command label. For example, in the command "/MaincommandName menu 1", the `cmdArgs` array
	 *                     will contain ["1"].
	 * @return True if the command execution is successful, false otherwise.
	 */
	public abstract boolean onCommand(@Nonnull final CommandSender sender, @Nonnull final String commandLabel, @Nonnull final String[] cmdArgs);

	/**
	 * Called when the sender is trying to tab-complete/type the command. This method is used to suggest the next part
	 * of the command after the initial part.
	 *
	 * @param sender       The command sender, could be player or console.
	 * @param commandLabel The command prefix for example this will be /commandName converted to command.
	 * @param cmdArgs      The arguments for the command. The `cmdArg` array contains the additional arguments provided
	 *                     after the initial part of the command. For example, if the command typed so far is
	 *                     "/MaincommandName menu 1," and the user is currently trying to type the next argument, the
	 *                     `cmdArg` array will contain ["1"]. You can use these arguments to suggest the next
	 *                     part of the command or provide auto-completion options.
	 * @return A list of command suggestions.
	 */
	@Nullable
	public List<String> onTabComplete(@Nonnull final CommandSender sender, @Nonnull final String commandLabel, @Nonnull final String[] cmdArgs) {
		return Collections.emptyList();
	}

	@Override
	public final boolean executeCommand(@Nonnull final CommandSender sender, @Nonnull final String commandLabel, @Nonnull final String[] cmdArgs) {
		this.arguments = cmdArgs;
		this.sender = sender;
		return onCommand(sender, commandLabel, cmdArgs);
	}

	@Nullable
	@Override
	public final List<String> executeTabComplete(@Nonnull final CommandSender sender, @Nonnull final String commandLabel, @Nonnull final String[] cmdArgs) {
		arguments = cmdArgs;
		this.sender = sender;
		return this.onTabComplete(sender, commandLabel, cmdArgs);
	}

	/**
	 * Set the command label.
	 *
	 * @return the command label.
	 */
	public String getCommandLabel() {
		return commandLabel;
	}

	/**
	 * Attempts to get the sender as player, only works if the sender is actually a player,
	 * otherwise we return null
	 *
	 * @return player or null.
	 */
	@Nullable
	protected final Player getPlayer() {
		return isPlayer(this.sender) ? (Player) this.sender : null;
	}

	/**
	 * check if it is a player.
	 *
	 * @param sender ho some send the command.
	 * @return true if it is a player.
	 */
	public final boolean isPlayer(final CommandSender sender) {
		return sender instanceof Player;
	}

	/**
	 * This method will stop command from get executed if is from the console.
	 *
	 * @throws org.bukkit.command.CommandException when the command is run from console.
	 */
	public final void checkConsole() {
		if (!isPlayer(this.sender))
			throw new CommandException("&c" + "You can´t run this command from console");
	}

	/**
	 * Copies and returns the arguments {@link #arguments} from the given range
	 * to their end joined by spaces.
	 *
	 * @param from The starting index of the args array from which the joining operation will begin.
	 * @return The joined string from the specified start index to the end of the args array.
	 */
	@Nonnull
	protected final String joinArgs(final int from) {
		return joinArgs(from, arguments.length);
	}

	/**
	 * Joins an array together using spaces from the given start index
	 *
	 * @param startIndex from The starting index (inclusive) of the args array from which the joining operation will begin.
	 * @return The joined string from the specified start index to the end of the args array.
	 */
	@Nonnull
	public String joinRange(final int startIndex) {
		return joinRange(startIndex, arguments.length);
	}

	/**
	 * Join an array together using spaces using the given range
	 *
	 * @param startIndex were it shall start join the array.
	 * @param stopIndex  were it shall stop join the array.
	 * @return a string with your join word.
	 */
	@Nonnull
	public String joinRange(final int startIndex, final int stopIndex) {
		return joinRange(startIndex, stopIndex, " ");
	}

	/**
	 * Joins the elements of an array together using the specified delimiter.
	 *
	 * @param start     The index at which to start joining the array (inclusive).
	 * @param stop      The index at which to stop joining the array (exclusive).
	 * @param delimiter The delimiter to add between the elements.
	 * @return A string containing the joined elements with the specified delimiter.
	 */
	@Nonnull
	public String joinRange(final int start, final int stop, final String delimiter) {
		final StringBuilder joined = new StringBuilder();
		String[] args = this.arguments;
		if (args != null)
			for (int i = start; i < range(stop, 0, args.length); i++)
				joined.append((joined.length() == 0) ? "" : delimiter).append(args[i]);

		return joined.toString();
	}

	/**
	 * Copies and returns the arguments  from the given range
	 * to the given end joined by spaces
	 *
	 * @param from The starting index from where you want to join the arguments.
	 * @param to   The ending index (exclusive) until which you want to join the arguments.
	 * @return A joined string of the arguments from the specified range, separated by spaces.
	 */
	@Nonnull
	protected final String joinArgs(final int from, final int to) {
		final StringBuilder message = new StringBuilder();
		String[] args = this.arguments;
		if (args != null)
			for (int i = from; i < args.length && i < to; i++)
				message.append(args[i]).append(i + 1 == args.length ? "" : " ");

		return message.toString();
	}

	protected int range(final int value, final int min, final int max) {
		return Math.min(Math.max(value, min), max);
	}

	/**
	 * Convenience method for returning the last word in arguments
	 *
	 * @return the last in the {@link #arguments} array.
	 */
	@Nonnull
	protected final String getLastArg() {
		String[] args = this.arguments;
		if (args != null)
			return args.length > 0 ? args[args.length - 1] : "";
		return "";
	}

	/**
	 * Convenience method for automatically completing the last word
	 * with the given suggestions. We sort them and only select ones
	 * that the last word starts with.
	 *
	 * @param <T>         class this list are from.
	 * @param suggestions list of suggestions you want to tabcomplete.
	 * @return list of words mathing one or several suggestions.
	 */
	@Nonnull
	@SafeVarargs
	protected final <T> List<String> completeLastWord(final T... suggestions) {
		String lastArg = null;
		String[] args = this.arguments;
		if (args != null)
			lastArg = args.length > 0 ? args[args.length - 1] : "";
		return complete(lastArg, suggestions);
	}

	/**
	 * Convenience method for automatically completing the last word
	 * with the given suggestions. We sort them and only select ones
	 * that the last word starts with.
	 *
	 * @param toString    the function you want to exicute.
	 * @param <T>         class this list are from.
	 * @param suggestions list of suggestions you want to tabcomplete.
	 * @return list of words mathing one or several suggestions.
	 */
	@Nonnull
	protected final <T> List<String> completeLastWord(final Iterable<T> suggestions, final Function<T, String> toString) {
		final List<String> list = new ArrayList<>();
		String lastArg = null;
		String[] args = this.arguments;
		if (args != null)
			lastArg = args.length > 0 ? args[args.length - 1] : "";
		for (final T suggestion : suggestions)
			list.add(toString.apply(suggestion));

		return complete(lastArg, list.toArray());
	}


	/**
	 * Return a list of tab completions for the given array,
	 * we attempt to resolve what type of the array it is,
	 * supports for chat colors, command senders, enumerations etc.
	 *
	 * @param <T>         class for this array you put in.
	 * @param partialName parts of the name.
	 * @param all         the list to check for the name.
	 * @return list of mathing names to partialName.
	 */
	@Nonnull
	@SafeVarargs
	public final <T> List<String> complete(@Nullable final String partialName, final T... all) {
		final List<String> clone = new ArrayList<>();

		if (all == null) return complete(partialName, clone);

		for (final T s : all) {
			if (s == null) continue;

			if (s instanceof Iterable)
				iterableObject(clone,  s);
				// Trick: Automatically parse enum constants
			else if (s instanceof Enum[])
				addEnums(clone,  s);
			else {
				addString(clone,  s);
			}
		}
		return complete(partialName, clone);
	}

	/**
	 * Returns valid tab completions for the given collection
	 *
	 * @param partialName parts of the name.
	 * @param all         the list to check for the name.
	 * @return list of mathing names to partialName.
	 */
	@Nonnull
	public List<String> complete(String partialName, final Iterable<String> all) {
		final ArrayList<String> tab = new ArrayList<>();
		if (partialName == null)
			partialName = "";
		for (final String s : all)
			tab.add(s);

		partialName = partialName.toLowerCase();

		for (final Iterator<String> iterator = tab.iterator(); iterator.hasNext(); ) {
			final String val = iterator.next();

			if (!val.toLowerCase().startsWith(partialName))
				iterator.remove();
		}

		Collections.sort(tab);

		return tab;
	}
	private <T> void iterableObject(List<String> clone, T s){
		for (final Object iterable : (Iterable<?>) s) {
			if (iterable instanceof Player)
				clone.add(((Player) iterable).getName());
			else
				clone.add(iterable instanceof Enum ? iterable.toString().toLowerCase() : iterable.toString());
		}
	}
	private <T> void addEnums(List<String> clone, T s){
		for (final Object iterable : ((Enum[]) s)[0].getClass().getEnumConstants())
			clone.add(iterable.toString().toLowerCase());
	}
	private <T> void addString(List<String> clone, T s){
		if (s instanceof Player)
			clone.add(((Player) s).getName());
		else {
			final boolean lowercase = s instanceof Enum;
			final String parsed = s.toString();

			if (!"".equals(parsed))
				clone.add(lowercase ? parsed.toLowerCase() : parsed);
		}
	}
}
