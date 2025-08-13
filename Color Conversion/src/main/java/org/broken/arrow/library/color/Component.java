package org.broken.arrow.library.color;

import com.google.gson.JsonObject;

/**
 * Create one immutable instance.
 */
public final class Component {

	private final String colorCode;
	private final String message;
	private final boolean bold;
	private final boolean italic;
	private final boolean underline;
	private final boolean strikethrough;
	private final boolean obfuscated;
	private final boolean reset;

	private Component(Builder builder) {
		this.message = builder.message;
		this.colorCode = builder.colorCode;
		this.bold = builder.bold;
		this.italic = builder.italic;
		this.underline = builder.underline;
		this.strikethrough = builder.strikethrough;
		this.obfuscated = builder.obfuscated;
		this.reset = builder.reset;
	}

	/**
	 * Get the message.
	 *
	 * @return the message or null.
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * Get the color.
	 *
	 * @return the color or null.
	 */
	public String getColorCode() {
		return colorCode;
	}

	/**
	 * Get if message are bold or not.
	 *
	 * @return true if message are bold.
	 */
	public boolean isBold() {
		return bold;
	}

	/**
	 * Get if message are italic or not.
	 *
	 * @return true if message are italic.
	 */
	public boolean isItalic() {
		return italic;
	}

	/**
	 * Get if message have underline or not.
	 *
	 * @return true if message have underline.
	 */
	public boolean isUnderline() {
		return underline;
	}

	/**
	 * Get if message have strikethrough or not.
	 *
	 * @return true if message have strikethrough.
	 */
	public boolean isStrikethrough() {
		return strikethrough;
	}

	/**
	 * Get if message is obfuscated or not.
	 *
	 * @return true if message is obfuscated.
	 */
	public boolean isObfuscated() {
		return obfuscated;
	}

	/**
	 * Get if color are reset or not.
	 *
	 * @return true if color are reset.
	 */
	public boolean isReset() {
		return reset;
	}

	@Override
	public String toString() {
		return toJson() + "";

	}

	/**
	 * Convert the set data to json.
	 *
	 * @return It returns an {@link JsonObject}.
	 */
	public JsonObject toJson() {
		JsonObject json = new JsonObject();
		if (colorCode != null)
			json.addProperty("color", colorCode);
		if (!reset) {
			if (bold)
				json.addProperty("bold", true);
			if (strikethrough)
				json.addProperty("strikethrough", true);
			if (underline)
				json.addProperty("underline", true);
			if (italic)
				json.addProperty("italic", true);
			if (obfuscated)
				json.addProperty("obfuscated", true);
		}
		if (message != null)
			json.addProperty("text", message);
		return json;

	}

	/**
	 * Create json, you have several options to chose from.
	 */
	public static class Builder {

		private String message;
		private String colorCode;
		private boolean bold;
		private boolean italic;
		private boolean underline;
		private boolean strikethrough;
		private boolean obfuscated;
		private boolean reset;

		/**
		 * Text/message you want to add.
		 *
		 * @param message you want to add.
		 * @return this class.
		 */
		public Builder message(String message) {
			this.message = message;
			return this;
		}

		/**
		 * color code you want to add. Mojang support §5 or purple
		 * and in 1.16+ you can parse #55F758. if you not set this
		 * it will be ether purple or white. I have not set any default
		 * color.
		 *
		 * @param color you whant to add.
		 * @return this class.
		 */
		public Builder colorCode(String color) {
			this.colorCode = color;
			return this;
		}

		/**
		 * If you want thick letters.
		 * <p>
		 * You don't need add false. If you
		 * don´t set this value, it will
		 * be false.
		 *
		 * @param b use true or false
		 * @return this class.
		 */
		public Builder bold(boolean b) {
			this.bold = b;
			return this;
		}

		/**
		 * If you want italic letters.
		 * <p>
		 * You don't need add false. If you
		 * don´t set this value, it will
		 * be false.
		 *
		 * @param b use true or false
		 * @return this class.
		 */
		public Builder italic(boolean b) {
			this.italic = b;
			return this;
		}

		/**
		 * If you want underline letters.
		 * <p>
		 * You don't need add false. If you
		 * don´t set this value, it will
		 * be false.
		 *
		 * @param b use true or false
		 * @return this class.
		 */
		public Builder underline(boolean b) {
			this.underline = b;
			return this;
		}

		/**
		 * If you want strikethrough letters.
		 * <p>
		 * You don't need add false. If you
		 * don´t set this value, it will
		 * be false.
		 *
		 * @param b use true or false
		 * @return this class.
		 */
		public Builder strikethrough(boolean b) {
			this.strikethrough = b;
			return this;
		}

		/**
		 * If you want hide/obfuscated letters.
		 * <p>
		 * You don't need add false. If you
		 * don´t set this value, it will
		 * be false.
		 *
		 * @param b use true or false
		 * @return this class.
		 */
		public Builder obfuscated(boolean b) {
			this.obfuscated = b;
			return this;
		}

		/**
		 * If you want reset colors.
		 * <p>
		 * You don't need add false. If you
		 * don´t set this value, it will
		 * be false.
		 *
		 * @param b use true or false
		 * @return this class.
		 */
		public Builder reset(boolean b) {
			this.reset = b;
			return this;
		}

		/**
		 * build your values. You need add this in the
		 * end, or it will not return finish json.
		 *
		 * @return json with one row, if you want an array you have to add
		 * it inside an array.
		 */
		public Component build() {
			return new Component(this);
		}
	}

}
