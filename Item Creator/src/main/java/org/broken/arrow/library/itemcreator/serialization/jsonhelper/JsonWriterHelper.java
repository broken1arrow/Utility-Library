package org.broken.arrow.library.itemcreator.serialization.jsonhelper;

import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;

/**
 * A helper class for writing JSON using Gson's {@link JsonWriter}.
 * <p>
 * This utility provides a fluent API for writing JSON objects, values, and arrays,
 * reducing boilerplate code when serializing data. It supports both primitive
 * and object arrays and handles object boundaries automatically.
 * </p>
 *
 * <strong>Example Usage:</strong>
 * <pre>{@code
 * JsonWriter writer = new JsonWriter(new OutputStreamWriter(System.out));
 * JsonWriterHelper helper = new JsonWriterHelper(writer);
 * helper
 *     .value("name", "John Doe")
 *     .value("age", 30)
 *     .forEach("tags", List.of("dev", "java"), t -> writer.value(t))
 *     .finish();
 * writer.flush();
 * }</pre>
 *
 * <p><b>Note:</b> Always call {@link #finish()} before closing the {@link JsonWriter}.</p>
 */
public class JsonWriterHelper {
	private final JsonWriter writer;
	private boolean startedObject;

	/**
	 * Creates a new {@code JsonWriterHelper} and automatically begins a root JSON object.
	 *
	 * @param writer the underlying {@link JsonWriter} to use
	 * @throws IOException if an I/O error occurs
	 */
	public JsonWriterHelper(JsonWriter writer) throws IOException {
		this.writer = writer;
		this.writer.beginObject();
		this.startedObject = true;
	}

	/**
	 * Begins a new JSON object.
	 *
	 * @return this helper instance for chaining
	 * @throws IOException if an I/O error occurs
	 */
	public JsonWriterHelper beginObject() throws IOException {
		writer.beginObject();
		return this;
	}

	/**
	 * Finishes the root object if it was started automatically.
	 *
	 * @throws IOException if an I/O error occurs
	 */
	public void finish() throws IOException {
		if (startedObject) {
			writer.endObject();
			startedObject = false;
		}
	}

	/**
	 * Writes a string field to the current JSON object.
	 *
	 * @param name  the field name
	 * @param value the string value
	 * @return this helper instance for chaining
	 * @throws IOException if an I/O error occurs
	 */
	public JsonWriterHelper value(String name, String value) throws IOException {
		writer.name(name).value(value);
		return this;
	}

	/**
	 * Writes an integer field to the current JSON object.
	 *
	 * @param name  the field name
	 * @param value the integer value
	 * @return this helper instance for chaining
	 * @throws IOException if an I/O error occurs
	 */
	public JsonWriterHelper value(String name, int value) throws IOException {
		writer.name(name).value(value);
		return this;
	}

	/**
	 * Writes a boolean field to the current JSON object.
	 *
	 * @param name  the field name
	 * @param value the boolean value
	 * @return this helper instance for chaining
	 * @throws IOException if an I/O error occurs
	 */
	public JsonWriterHelper value(String name, boolean value) throws IOException {
		writer.name(name).value(value);
		return this;
	}

	/**
	 * Writes an array field to the JSON, applying the given action to each element.
	 * <p>This method is suitable for arrays of primitives or simple values.</p>
	 *
	 * @param name   the array field name
	 * @param list   the list of elements
	 * @param action a consumer that writes each element
	 * @param <T>    the element type
	 * @return this helper instance for chaining
	 * @throws IOException if an I/O error occurs
	 */
	public <T> JsonWriterHelper forEach(String name, List<T> list, ThrowingConsumer<? super T> action) throws IOException {
		writer.name(name).beginArray();
		for (T item : list) {
			action.accept(item);
		}
		writer.endArray();
		return this;
	}

	/**
	 * Writes an array field of JSON objects, applying the given action to each element.
	 *
	 * @param name   the array field name
	 * @param list   the list of elements
	 * @param action a consumer that writes each object
	 * @param <T>    the element type
	 * @return this helper instance for chaining
	 * @throws IOException if an I/O error occurs
	 */
	public <T> JsonWriterHelper forEachObject(String name, List<T> list, ThrowingConsumer<? super T> action) throws IOException {
		writer.name(name).beginArray();
		for (T item : list) {
			writer.beginObject();
			action.accept(item);
			writer.endObject();
		}
		writer.endArray();
		return this;
	}

	/**
	 * A functional interface for consuming elements while allowing I/O exceptions.
	 *
	 * @param <T> the input type
	 */
	@FunctionalInterface
	public interface ThrowingConsumer<T> {
		 /**
		 * Performs this operation on the given argument.
		 *
		 * @param t the input argument
		 * @throws IOException if an I/O error occurs
		 */
		void accept(T t) throws IOException;
	}
}