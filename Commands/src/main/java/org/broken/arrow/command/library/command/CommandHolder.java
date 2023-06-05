package org.broken.arrow.command.library.command;

import org.bukkit.command.CommandException;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

public abstract class CommandHolder implements CommandHandler {

	private final String commandLable;
	private String[] args;
	private CommandSender sender;
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

	/**
	 * Called when the command is executed by the specified sender. It can be either a player or another sender.
	 * Therefore, check if the sender is a player before casting it to a Player instance.
	 *
	 * @param sender       The command sender, could be player or console.
	 * @param commandLabel The command prefix for example this will be /command converted to commandName.
	 * @param cmdArg       The arguments for the command. The `cmdArg` array contains the additional arguments provided
	 *                     after the command prefix. For example, if the command used is "/commandName menu 1," the
	 *                     `cmdArg` array will contain ["menu", "1"]. You can access and process these arguments as needed.
	 */
	public abstract void onCommand(@Nonnull final CommandSender sender, @Nonnull final String commandLabel, @Nonnull final String[] cmdArg);

	/**
	 * Called when the sender is trying to tab-complete/type the command. This method is used to suggest the next part
	 * of the command after the initial part.
	 *
	 * @param sender       The command sender, could be player or console.
	 * @param commandLabel The command prefix for example this will be /commandName converted to command.
	 * @param cmdArg       The arguments for the command. The `cmdArg` array contains the additional arguments provided
	 *                     after the initial part of the command. For example, if the command typed so far is
	 *                     "/commandName menu 1," and the user is currently trying to type the next argument, the
	 *                     `cmdArg` array will contain ["menu", "1"]. You can use these arguments to suggest the next
	 *                     part of the command or provide auto-completion options.
	 * @return A list of command suggestions.
	 */
	@Nullable
	public List<String> onTabComplete(@Nonnull final CommandSender sender, @Nonnull final String commandLabel, @Nonnull final String[] cmdArg) {
		return Collections.emptyList();
	}

	@Override
	public final void excuteCommand(@Nonnull final CommandSender sender, @Nonnull final String commandLabel, @Nonnull final String[] cmdArg) {
		this.args = cmdArg;
		this.sender = sender;
		onCommand(sender, commandLabel, cmdArg);
	}

	@Nullable
	@Override
	public final List<String> excuteTabComplete(@Nonnull final CommandSender sender, @Nonnull final String commandLabel, @Nonnull final String[] cmdArg) {
		args = cmdArg;
		this.sender = sender;
		return Collections.emptyList();
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
	 * @throws org.bukkit.command.CommandException when an unhandled exception occurs during the execution of a Command.
	 */
	public final void checkConsole() throws CommandException {
		if (!isPlayer(this.sender))
			throw new CommandException("&c" + "You can´t run this command from console");
	}

	/**
	 * Copies and returns the arguments {@link #args} from the given range
	 * to their end joined by spaces
	 *
	 * @param from
	 * @return
	 */
	@Nonnull
	protected final String joinArgs(final int from) {
		return joinArgs(from, args.length);
	}

	/**
	 * Joins an array together using spaces from the given start index
	 *
	 * @param startIndex were it shall start join the array.
	 * @return a string with your join word.
	 */
	@Nonnull
	public String joinRange(final int startIndex) {
		return joinRange(startIndex, args.length);
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
	 * Join an array together using the given deliminer
	 *
	 * @param start     were it shall start join the array.
	 * @param stop      were it shall stop join the array.
	 * @param delimiter add delimiter between words.
	 * @return a string with your join word.
	 */
	@Nonnull
	public String joinRange(final int start, final int stop, final String delimiter) {
		final StringBuilder joined = new StringBuilder();
		String[] args = this.args;
		if (args != null)
			for (int i = start; i < range(stop, 0, args.length); i++)
				joined.append((joined.length() == 0) ? "" : delimiter).append(args[i]);

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
	@Nonnull
	protected final String joinArgs(final int from, final int to) {
		final StringBuilder message = new StringBuilder();
		String[] args = this.args;
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
	 * @return
	 */
	@Nonnull
	protected final String getLastArg() {
		String[] args = this.args;
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
		String[] args = this.args;
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
		String[] args = this.args;
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
}
