package org.broken.arrow.yaml.library.config.updater.utility;

public class CommentBuilder implements Cloneable {

	private StringBuilder commentBuilder;

	/**
	 * Constructs a new empty CommentBuilder.
	 */
	public CommentBuilder() {
		this.commentBuilder = new StringBuilder();
	}

	/**
	 * Used to clone a new CommentBuilder.
	 */
	private CommentBuilder(final CommentBuilder commentBuilder) {
		this.commentBuilder = commentBuilder.commentBuilder;
	}

	/**
	 * Sets the comment for the KeyBuilder.
	 *
	 * @param comment The comment to set for the KeyBuilder.
	 */
	public void setComment(final String comment) {
		StringBuilder commentBuilder = new StringBuilder();
		this.commentBuilder = commentBuilder.append(comment).append("\n");
	}

	/**
	 * Adds a comment to the existing comment for the KeyBuilder.
	 *
	 * @param comment The comment to add.
	 */
	public void addComment(final String comment) {
		this.commentBuilder.append(comment).append("\n");
	}

	/**
	 * Retrieves the comment associated with the KeyBuilder.
	 *
	 * @return The comment associated with the KeyBuilder.
	 */
	public String getComment() {
		if (this.commentBuilder == null)
			return "";
		return this.commentBuilder + "";
	}

	/**
	 * Checks if the comments is empty.
	 *
	 * @return {@code true} if the KeyBuilder is empty, {@code false} otherwise.
	 */
	public boolean isEmpty() {
		return getComment().length() == 0;
	}

	@Override
	public String toString() {
		return "CommentBuilder{" +
				"comment=" + commentBuilder +
				'}';
	}

	@Override
	public CommentBuilder clone() {
		return new CommentBuilder(this);
	}
}
