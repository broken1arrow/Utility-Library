package org.broken.arrow.command.library;

import org.bukkit.command.CommandException;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

public abstract class CommandHolder implements CommandHandler {

	private final String commandLable;
	private Plugin plugin;

	/**
	 * Set the prefix for subcommand. Use | like this
	 * first|second command to add two options for same
	 * command.
	 *
	 * @param commandLable the prefix you want as sublable.
	 */
	public CommandHolder(final String commandLable) {
		this.commandLable = commandLable;
	}

	public String getCommandLable() {
		return commandLable;
	}

	/**
	 * Attempts to get the sender as player, only works if the sender is actually a player,
	 * otherwise we return null
	 *
	 * @return player or null.
	 */
	@Nullable
	protected final Player getPlayer(final CommandSender sender) {
		return isPlayer(sender) ? (Player) sender : null;
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
	 * @param sender the sender of the command.
	 * @throws org.bukkit.command.CommandException when an unhandled exception occurs during the execution of a Command.
	 */
	public final void checkConsole(final CommandSender sender) throws CommandException {
		if (!isPlayer(sender))
			throw new CommandException("&c" + "You can´t run this command from console");
	}

	/**
	 * Copies and returns the arguments {@link #args} from the given range
	 * to their end joined by spaces
	 *
	 * @param from
	 * @return
	 */
	protected final String joinArgs(final String[] array, final int from) {
		return joinArgs(array, from, array.length);
	}

	/**
	 * Joins an array together using spaces from the given start index
	 *
	 * @param startIndex were it shall start join the array.
	 * @param array      array you want to join
	 * @return a string with your join word.
	 */
	public String joinRange(final int startIndex, final String[] array) {
		return joinRange(startIndex, array.length, array);
	}

	/**
	 * Join an array together using spaces using the given range
	 *
	 * @param startIndex were it shall start join the array.
	 * @param stopIndex  were it shall stop join the array.
	 * @param array      array you want to join
	 * @return a string with your join word.
	 */
	public String joinRange(final int startIndex, final int stopIndex, final String[] array) {
		return joinRange(startIndex, stopIndex, array, " ");
	}

	/**
	 * Join an array together using the given deliminer
	 *
	 * @param start     were it shall start join the array.
	 * @param stop      were it shall stop join the array.
	 * @param array     array you want to join
	 * @param delimiter add delimiter between words.
	 * @return a string with your join word.
	 */
	public String joinRange(final int start, final int stop, final String[] array, final String delimiter) {
		final StringBuilder joined = new StringBuilder();
		for (int i = start; i < range(stop, 0, array.length); i++)
			joined.append((joined.length() == 0) ? "" : delimiter).append(array[i]);

		return joined.toString();
	}

	/**
	 * Copies and returns the arguments  from the given range
	 * to the given end joined by spaces
	 *
	 * @param from
	 * @param to
	 * @return
	 */
	protected final String joinArgs(final String[] array, final int from, final int to) {
		final StringBuilder message = new StringBuilder();

		for (int i = from; i < array.length && i < to; i++)
			message.append(array[i]).append(i + 1 == array.length ? "" : " ");

		return message.toString();
	}

	protected int range(final int value, final int min, final int max) {
		return Math.min(Math.max(value, min), max);
	}

	/**
	 * Convenience method for automatically completing the last word
	 * with the given suggestions. We sort them and only select ones
	 * that the last word starts with.
	 *
	 * @param <T>         class this list are from.
	 * @param suggestions list of suggestions you want to tabcomplete.
	 * @param args        list of words player type or parts of it.
	 * @return list of words mathing one or several suggestions.
	 */
	@SafeVarargs
	protected final <T> List<String> completeLastWord(final String[] args, final T... suggestions) {
		final String lastArg = args != null && args.length > 0 ? args[args.length - 1] : "";
		return complete(lastArg, suggestions);
	}

	/**
	 * Convenience method for automatically completing the last word
	 * with the given suggestions. We sort them and only select ones
	 * that the last word starts with.
	 *
	 * @param <T>         class this list are from.
	 * @param suggestions list of suggestions you want to tabcomplete.
	 * @param args        list of words player type or parts of it.
	 * @return list of words mathing one or several suggestions.
	 */
	protected final <T> List<String> completeLastWord(final String[] args, final Iterable<T> suggestions, final Function<T, String> toString) {
		final List<String> list = new ArrayList<>();
		final String lastArg = args != null && args.length > 0 ? args[args.length - 1] : "";
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
	@SafeVarargs
	public final <T> List<String> complete(final String partialName, final T... all) {
		final List<String> clone = new ArrayList<>();

		if (all == null) return complete(partialName, clone);

		for (final T s : all) {
			if (s == null) continue;

			if (s instanceof Iterable)
				for (final Object iterable : (Iterable<?>) s) {
					if (iterable instanceof Player)
						clone.add(((Player) iterable).getName());
					else
						clone.add(iterable instanceof Enum ? iterable.toString().toLowerCase() : iterable.toString());
				}
				// Trick: Automatically parse enum constants
			else if (s instanceof Enum[])
				for (final Object iterable : ((Enum[]) s)[0].getClass().getEnumConstants())
					clone.add(iterable.toString().toLowerCase());

			else {
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
		return complete(partialName, clone);
	}

	/**
	 * Returns valid tab completions for the given collection
	 *
	 * @param partialName parts of the name.
	 * @param all         the list to check for the name.
	 * @return list of mathing names to partialName.
	 */
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
}
