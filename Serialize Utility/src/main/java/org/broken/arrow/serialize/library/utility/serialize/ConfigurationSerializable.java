package org.broken.arrow.serialize.library.utility.serialize;

import javax.annotation.Nonnull;
import java.util.Map;

/**
 * Represents an object that may be serialized.
 * <p>
 * These objects MUST implement one of the following, in addition to the
 * methods as defined by this interface:
 * <ul>
 * <li>A static method "deserialize" that accepts a single {@link java.util.Map}&lt;
 * {@link String}, {@link Object}&gt; and returns the class.</li>
 * <li>A static method "valueOf" that accepts a single {@link java.util.Map}&lt;{@link
 * String}, {@link Object}&gt; and returns the class.</li>
 * </ul>
 */
public interface ConfigurationSerializable {

	/**
	 * Serializes the PluginMessages object to a map of key-value pairs.
	 *
	 * @return a map containing the serialized data
	 */
	@Nonnull
	Map<String, Object> serialize();

}
