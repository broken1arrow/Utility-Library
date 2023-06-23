package org.broken.arrow.command.library.command.builders;

import org.broken.arrow.command.library.command.CommandHolder;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * A utility class for building command objects with various properties.
 * CommandBuilder provides a fluent interface for configuring and creating command instances.
 */
public class CommandBuilder {

	private final String subLable;
	private final String description;
	private final String permission;
	private final String permissionMessage;
	private final List<String> usageMessages;
	private final boolean hideLable;
	private final CommandHolder executor;
	private final Builder builder;

	/**
	 * Constructs a new CommandBuilder with the provided builder instance.
	 *
	 * @param builder The builder instance used to set the command properties.
	 */
	private CommandBuilder(final Builder builder) {
		this.subLable = builder.subLabel;
		this.description = builder.description;
		this.permission = builder.permission;
		this.permissionMessage = builder.permissionMessage;

		this.usageMessages = builder.usageMessages;
		this.hideLable = builder.hideLabel;
		this.executor = builder.executor;
		this.builder = builder;
	}

	/**
	 * Returns the sub-label of the command.
	 *
	 * @return The sub-label.
	 */
	public String getSubLable() {
		return subLable;
	}

	/**
	 * Returns the description of the command.
	 *
	 * @return The description.
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Returns the list of usage messages for the command.
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
	public boolean isHideLable() {
		return hideLable;
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
	 */
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
			this.subLabel = executor.getCommandLable();
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
		 * Sets one or several messages to suggest to the player how to use the command.
		 *
		 * @param usageMessages The array of usage messages.
		 * @return The Builder instance.
		 */
		public Builder setUsageMessages(final String... usageMessages) {
			this.usageMessages = Arrays.asList(usageMessages);
			return this;
		}

		/**
		 * Sets a list of messages to suggest to the player how to use the command.
		 *
		 * @param usageMessages The list of usage messages.
		 * @return The Builder instance.
		 */
		public Builder setUsageMessages(final List<String> usageMessages) {
			this.usageMessages = usageMessages;
			return this;
		}

		/**
		 * Sets the description of the command.
		 *
		 * @param description The description message.
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
		 */
		public CommandBuilder build() {
			return new CommandBuilder(this);
		}
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		final CommandBuilder that = (CommandBuilder) o;
		return hideLable == that.hideLable && Objects.equals(subLable, that.subLable) && Objects.equals(description, that.description) && Objects.equals(permission, that.permission) && Objects.equals(permissionMessage, that.permissionMessage) && Objects.equals(usageMessages, that.usageMessages) && Objects.equals(executor, that.executor) && Objects.equals(builder, that.builder);
	}

	@Override
	public int hashCode() {
		return Objects.hash(subLable, description, permission, permissionMessage, usageMessages, hideLable, executor, builder);
	}

}
