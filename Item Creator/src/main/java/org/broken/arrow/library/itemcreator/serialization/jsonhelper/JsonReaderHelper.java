package org.broken.arrow.library.itemcreator.serialization.jsonhelper;

import com.google.gson.stream.JsonReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Utility wrapper for {@link com.google.gson.stream.JsonReader} that simplifies reading JSON objects and arrays
 * with functional-style iteration.
 * <p>
 * This helper begins reading a JSON object immediately upon instantiation (by calling {@link JsonReader#beginObject()}).
 * It provides convenience methods for reading primitive values, skipping fields, iterating over arrays,
 * and processing object fields.
 * </p>
 *
 * <strong>Example usage:</strong>
 * <pre>{@code
 * try (JsonReader reader = new JsonReader(new StringReader(jsonString))) {
 *     JsonReaderHelper helper = new JsonReaderHelper(reader);
 *     helper.forEachObjectField((name, r) -> {
 *         switch (name) {
 *             case "id" -> System.out.println(r.nextInt());
 *             case "name" -> System.out.println(r.nextString());
 *             default -> r.skipValue();
 *         }
 *     });
 *     helper.endObject();
 * }
 * }</pre>
 *
 * <p>Features:</p>
 * <ul>
 *     <li>Automatic {@code beginObject()} call in constructor.</li>
 *     <li>Convenience methods for reading typed values.</li>
 *     <li>Functional interfaces for mapping array elements and handling object fields.</li>
 *     <li>Automatic iteration over arrays of objects or primitives.</li>
 * </ul>
 *
 * @author broken arrow
 */
public class JsonReaderHelper {
	private final JsonReader reader;

	/**
	 * Constructs a new helper for the given {@link JsonReader} and begins reading an object.
	 *
	 * @param reader the JSON reader to wrap
	 * @throws IOException if an I/O error occurs while starting object reading
	 */
	public JsonReaderHelper(JsonReader reader) throws IOException {
		this.reader = reader;
		this.reader.beginObject();
	}

	/**
	 * Reads the next property name in the current JSON object.
	 *
	 * @return the property name
	 * @throws IOException if an I/O error occurs
	 */
	public String nextName() throws IOException {
		return reader.nextName();
	}

	/**
	 * Reads the next integer value in the JSON.
	 *
	 * @return the integer value
	 * @throws IOException if an I/O error occurs
	 */
	public int nextInt() throws IOException {
		return reader.nextInt();
	}

	/**
	 * Reads the next boolean value in the JSON.
	 *
	 * @return the boolean value
	 * @throws IOException if an I/O error occurs
	 */
	public boolean nextBoolean() throws IOException {
		return reader.nextBoolean();
	}

	/**
	 * Reads the next string value in the JSON.
	 *
	 * @return the string value
	 * @throws IOException if an I/O error occurs
	 */
	public String nextString() throws IOException {
		return reader.nextString();
	}

	/**
	 * Skips the next value in the JSON.
	 *
	 * @throws IOException if an I/O error occurs
	 */
	public void skipValue() throws IOException {
		reader.skipValue();
	}

	/**
	 * Ends reading of the current JSON object.
	 *
	 * @throws IOException if an I/O error occurs
	 */
	public void endObject() throws IOException {
		reader.endObject();
	}

	/**
	 * Iterates over an array of JSON values, applying a mapper function to each element.
	 *
	 * @param mapper the function to map each element
	 * @param <T>    the type of the mapped result
	 * @return a list of mapped results
	 * @throws IOException if an I/O error occurs
	 */
	public <T> List<T> forEachInArray(ThrowingFunction<JsonReaderHelper, T> mapper) throws IOException {
		List<T> list = new ArrayList<>();
		reader.beginArray();
		while (reader.hasNext()) {
			list.add(mapper.apply(this));
		}
		reader.endArray();
		return list;
	}

	/**
	 * Iterates over an array of JSON objects, applying a mapper function to each object.
	 * Automatically calls {@link JsonReader#beginObject()} and {@link JsonReader#endObject()} for each element.
	 *
	 * @param mapper the function to map each object
	 * @param <T>    the type of the mapped result
	 * @return a list of mapped results
	 * @throws IOException if an I/O error occurs
	 */
	public <T> List<T> forEachObjectInArray(ThrowingFunction<JsonReaderHelper, T> mapper) throws IOException {
		List<T> list = new ArrayList<>();
		reader.beginArray();
		while (reader.hasNext()) {
			reader.beginObject();
			list.add(mapper.apply(this));
			reader.endObject();
		}
		reader.endArray();
		return list;
	}

	/**
	 * Iterates over all fields in the current object, calling the handler for each field name and value.
	 *
	 * @param handler the handler to process each field
	 * @throws IOException if an I/O error occurs
	 */
	public void forEachObjectField(ThrowingFieldHandler handler) throws IOException {
		while (reader.hasNext()) {
			String name = nextName();
			handler.handle(name, this);
		}

	}

	/**
	 * Represents a function that accepts one argument, produces a result,
	 * and is allowed to throw an {@link IOException}.
	 * <p>
	 * This is similar to {@link java.util.function.Function} but supports checked I/O exceptions.
	 *
	 * @param <T> the type of the input argument
	 * @param <R> the type of the result
	 */
	@FunctionalInterface
	public interface ThrowingFunction<T, R> {

		/**
		 * Applies this function to the given argument.
		 *
		 * @param t the input argument
		 * @return the computed result
		 * @throws IOException if an I/O error occurs during processing
		 */
		R apply(T t) throws IOException;
	}

	/**
	 * Represents an operation to process a JSON object's field during iteration,
	 * allowing checked {@link IOException}s.
	 * <p>
	 * Used to handle field names and their corresponding values when reading JSON objects.
	 */
	@FunctionalInterface
	public interface ThrowingFieldHandler {
		/**
		 * Processes a JSON field with the given name and associated reader.
		 *
		 * @param name   the field name of the JSON object
		 * @param reader the {@link JsonReaderHelper} instance to read the field's value
		 * @throws IOException if an I/O error occurs while reading the field
		 */
		void handle(String name, JsonReaderHelper reader) throws IOException;
	}
}