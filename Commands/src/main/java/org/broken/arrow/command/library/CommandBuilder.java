package org.broken.arrow.command.library;

import java.util.Arrays;
import java.util.List;

public class CommandBuilder {

	private final String subLable;
	private final String description;
	private final String permission;
	private final String permissionMessage;
	private final List<String> usageMessages;
	private final boolean hideLable;
	private final CommandHolder executor;
	private final Builder builder;

	public CommandBuilder(final Builder builder) {
		this.subLable = builder.subLable;
		this.description = builder.description;
		this.permission = builder.permission;
		this.permissionMessage = builder.permissionMessage;

		this.usageMessages = builder.usageMessages;
		this.hideLable = builder.hideLable;
		this.executor = builder.executor;
		this.builder = builder;
	}

	public String getSubLable() {
		return subLable;
	}

	public String getDescription() {
		return description;
	}

	public List<String> getUsageMessages() {
		return usageMessages;
	}

	public String getPermission() {
		return permission;
	}

	public String getPermissionMessage() {
		return permissionMessage;
	}

	public boolean isHideLable() {
		return hideLable;
	}

	public CommandHolder getExecutor() {
		return executor;
	}

	public Builder getBuilder() {
		return builder;
	}

	public static class Builder {
		private String subLable;
		private String description;
		private String permission;
		private String permissionMessage;
		private List<String> usageMessages;
		private boolean hideLable;
		private final CommandHolder executor;

		public Builder(final CommandHolder executor) {
			this.executor = executor;
			this.subLable = executor.getCommandLable();
		}

		/**
		 * Set the prefix for subcommand. Use | like this
		 * first|second command to add two options for same
		 * command.
		 *
		 * @param subLable the prefix you want as sublable.
		 * @return this class.
		 */
		public Builder setSubLable(final String subLable) {
			this.subLable = subLable;
			return this;
		}

		/**
		 * Set one or several messages to suggest to player how to use the command.
		 *
		 * @param usageMessages the message list.
		 * @return this class.
		 */
		public Builder setUsageMessages(final String... usageMessages) {
			this.usageMessages = Arrays.asList(usageMessages);
			return this;
		}

		/**
		 * Sett list of messages to suggest to player how to use the command.
		 *
		 * @param usageMessages the message list.
		 * @return this class.
		 */
		public Builder setUsageMessages(final List<String> usageMessages) {
			this.usageMessages = usageMessages;
			return this;
		}

		/**
		 * Description of the command.
		 *
		 * @param description type in a description message.
		 * @return this class.
		 */
		public Builder setDescription(final String description) {
			this.description = description;
			return this;
		}

		/**
		 * Set the command, if you not set this the command will always be accepted.
		 *
		 * @param permission permission you want to have.
		 * @return this class.
		 */
		public Builder setPermission(final String permission) {
			this.permission = permission;
			return this;
		}

		/**
		 * Set the message when player can't run the command. Use {perm} to replace
		 * it with the missing permission auto.
		 *
		 * @param permissionMessage the message when fail to run the command
		 * @return this class.
		 */
		public Builder setPermissionMessage(final String permissionMessage) {
			this.permissionMessage = permissionMessage;
			return this;
		}

		/**
		 * Set if it shall hide tab complete the subcommand without permission.
		 *
		 * @param hideLable set to true if you want to hide tab complete.
		 * @return this class.
		 */
		public Builder setHideLable(final boolean hideLable) {
			this.hideLable = hideLable;
			return this;
		}

		public CommandBuilder build() {
			return new CommandBuilder(this);
		}
	}
}
