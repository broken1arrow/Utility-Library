package org.broken.arrow.command.library.command.builders;

import org.broken.arrow.command.library.command.CommandHolder;
import org.broken.arrow.command.library.command.CommandProperty;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * A wrapper class for building a command from a class that extends {@link CommandHolder}.
 * Here you set the properties for your command, such as subLabel, permission, description, permission message,
 * usage message, and whether to hide the label. Some settings can be done in CommandHolder instead, for example,
 * subLabel. If you don't set the label in the builder {@link Builder}, it will use the one set inside the CommandHolder class.
 *
 * @deprecated use only {@link CommandHolder}, this class only create unnecessary extra step.
 */
@Deprecated
public class CommandBuilder {

	private final String subLabel;
	private final String description;
	private final String permission;
	private final String permissionMessage;
	private final List<String> usageMessages;
	private final boolean hideLabel;
	private final CommandHolder executor;
	private final Builder builder;

	/**
	 * Constructs a new CommandBuilder with the provided builder instance.
	 *
	 * @param builder The builder instance used to set the command properties.
	 */
	private CommandBuilder(final Builder builder) {
		this.subLabel = builder.subLabel;
		this.description = builder.description;
		this.permission = builder.permission;
		this.permissionMessage = builder.permissionMessage;

		this.usageMessages = builder.usageMessages;
		this.hideLabel = builder.hideLabel;
		this.executor = builder.executor;
		this.builder = builder;
	}

	/**
	 * Returns the sub-label of the command.
	 *
	 * @return The sub-label.
	 */
	public String getSubLabel() {
		return subLabel;
	}

	/**
	 * Returns the description of the command. The description should provide information about what the command does.
	 * Players add a "?" or "help" at the end of the command to request the information.
	 *
	 * @return The description.
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Returns the list of usage messages for the command. When use method {@link org.broken.arrow.command.library.command.CommandHolder#onCommand(org.bukkit.command.CommandSender, String, String[])}
	 * and it return false to indicate that the specified usage message should be displayed.
	 *
	 * @return The list of usage messages.
	 */
	public List<String> getUsageMessages() {
		return usageMessages;
	}

	/**
	 * Returns the required permission for the command.
	 *
	 * @return The permission.
	 */
	public String getPermission() {
		return permission;
	}

	/**
	 * Returns the message to display when the command is executed without the required permission.
	 *
	 * @return The permission message.
	 */
	public String getPermissionMessage() {
		return permissionMessage;
	}

	/**
	 * Checks if the command label should be hidden from tab completion without permission.
	 *
	 * @return True if the label should be hidden, false otherwise.
	 */
	public boolean isHideLabel() {
		return hideLabel;
	}

	/**
	 * Returns the executor that handles the execution of the command.
	 *
	 * @return The command executor.
	 */
	@Nonnull
	public CommandHolder getExecutor() {
		return executor;
	}

	/**
	 * Returns the builder used to create this CommandBuilder instance.
	 *
	 * @return The builder.
	 */
	public Builder getBuilder() {
		return builder;
	}

	/**
	 * The builder class for creating CommandBuilder instances.
	 * @deprecated use only {@link CommandHolder}, this class only create unnecessary extra step.
	 */
	@Deprecated
	public static class Builder {
		private String subLabel;
		private String description;
		private String permission;
		private String permissionMessage;
		private List<String> usageMessages;
		private boolean hideLabel;
		private final CommandHolder executor;

		public Builder(@Nonnull final CommandHolder executor) {
			this.executor = executor;
			this.subLabel = executor.getCommandLabels().stream().findFirst().orElse("");
		}

		/**
		 * Sets the prefix for the subcommand. Use "|" to separate multiple options for the same command.
		 *
		 * @param subLabel The prefix to set as the sub-label.
		 * @return The Builder instance.
		 */
		public Builder setSubLabel(final String subLabel) {
			this.subLabel = subLabel;
			return this;
		}

		/**
		 * Sets a list of messages to suggest to the player how to use the command. These usage messages provide guidance on how to properly
		 * use the command and its arguments.
		 * Note: You can use the {@link org.broken.arrow.command.library.command.CommandHolder#onCommand(org.bukkit.command.CommandSender, String, String[])}
		 * method and set it to false to indicate that the specified usage message should be displayed.
		 *
		 * @param usageMessages The array of usage messages.
		 * @return The Builder instance.
		 */
		public Builder setUsageMessages(final String... usageMessages) {
			this.usageMessages = Arrays.asList(usageMessages);
			return this;
		}

		/**
		 * Sets a list of messages to suggest to the player how to use the command. These usage messages provide guidance on how to properly
		 * use the command and its arguments.
		 * Note: when you use {@link org.broken.arrow.command.library.command.CommandHolder#onCommand(org.bukkit.command.CommandSender, String, String[])}
		 * method and set it to false to indicate that the specified usage message should be displayed.
		 *
		 * @param usageMessages The list of usage messages.
		 * @return The Builder instance.
		 */
		public Builder setUsageMessages(final List<String> usageMessages) {
			this.usageMessages = usageMessages;
			return this;
		}

		/**
		 * Sets the description of the command. The description should provide information about what the command does.
		 * Player then add a "?" or "help" at the end of the command to request additional information about the command.
		 *
		 * @param description The description message that explains what the command does.
		 * @return The Builder instance.
		 */
		public Builder setDescription(final String description) {
			this.description = description;
			return this;
		}

		/**
		 * Sets the required permission for the command.
		 *
		 * @param permission The permission to set.
		 * @return The Builder instance.
		 */
		public Builder setPermission(final String permission) {
			this.permission = permission;
			return this;
		}

		/**
		 * Sets the message to display when the player can't run the command.
		 * Use "{perm}" to replace it with the missing permission automatically.
		 *
		 * @param permissionMessage The permission failure message.
		 * @return The Builder instance.
		 */
		public Builder setPermissionMessage(final String permissionMessage) {
			this.permissionMessage = permissionMessage;
			return this;
		}

		/**
		 * Sets whether to hide the subcommand from tab completion without permission.
		 *
		 * @param hideLabel Set to true to hide the label from tab completion.
		 * @return The Builder instance.
		 */
		public Builder setHideLabel(final boolean hideLabel) {
			this.hideLabel = hideLabel;
			return this;
		}

		/**
		 * Builds and returns a new CommandBuilder instance.
		 *
		 * @return The newly created CommandBuilder instance.
		 * @deprecated use only {@link CommandHolder}, this class only create unnecessary extra step.
		 */
		@Deprecated
		public CommandProperty build() {
			CommandProperty commandProperty = new CommandProperty(this.subLabel);
			commandProperty.setDescription(this.description);
			commandProperty.setPermission(this.permission);
			commandProperty.setPermissionMessage(this.permissionMessage);
			commandProperty.setHideLabel(this.hideLabel);
			commandProperty.setUsageMessages(this.usageMessages);

			return commandProperty;
		}
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		final CommandBuilder that = (CommandBuilder) o;
		return hideLabel == that.hideLabel && Objects.equals(subLabel, that.subLabel) && Objects.equals(description, that.description) && Objects.equals(permission, that.permission) && Objects.equals(permissionMessage, that.permissionMessage) && Objects.equals(usageMessages, that.usageMessages) && Objects.equals(executor, that.executor) && Objects.equals(builder, that.builder);
	}

	@Override
	public int hashCode() {
		return Objects.hash(subLabel, description, permission, permissionMessage, usageMessages, hideLabel, executor, builder);
	}

}
