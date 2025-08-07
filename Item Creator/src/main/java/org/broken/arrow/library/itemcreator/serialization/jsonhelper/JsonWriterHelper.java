package org.broken.arrow.library.itemcreator.serialization.jsonhelper;

import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.util.List;

public class JsonWriterHelper {
	private final JsonWriter writer;
	private boolean startedObject;

	public JsonWriterHelper(JsonWriter writer) throws IOException {
		this.writer = writer;
		this.writer.beginObject();
		this.startedObject = true;
	}

	public JsonWriterHelper beginObject() throws IOException {
		writer.beginObject();
		return this;
	}

	public JsonWriterHelper endObject() throws IOException {
		writer.endObject();
		return this;
	}

	public void finish() throws IOException {
		if (startedObject) {
			writer.endObject();
			startedObject = false;
		}
	}

	// Write a value field
	public JsonWriterHelper value(String name, String value) throws IOException {
		writer.name(name).value(value);
		return this;
	}

	public JsonWriterHelper value(String name, int value) throws IOException {
		writer.name(name).value(value);
		return this;
	}

	public JsonWriterHelper value(String name, boolean value) throws IOException {
		writer.name(name).value(value);
		return this;
	}

	// ForEach array of primitives or raw values
	public <T> JsonWriterHelper forEach(String name, List<T> list, ThrowingConsumer<? super T> action) throws IOException {
		writer.name(name).beginArray();
		for (T item : list) {
			action.accept(item);
		}
		writer.endArray();
		return this;
	}

	// ForEach array of objects
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

	@FunctionalInterface
	public interface ThrowingConsumer<T> {
		void accept(T t) throws IOException;
	}
}