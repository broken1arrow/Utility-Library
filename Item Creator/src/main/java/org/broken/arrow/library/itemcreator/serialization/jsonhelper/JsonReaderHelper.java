package org.broken.arrow.library.itemcreator.serialization.jsonhelper;

import com.google.gson.stream.JsonReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class JsonReaderHelper {
	private final JsonReader reader;

	public JsonReaderHelper(JsonReader reader) throws IOException {
		this.reader = reader;
		this.reader.beginObject();
	}

	public String nextName() throws IOException {
		return reader.nextName();
	}

	public int nextInt() throws IOException {
		return reader.nextInt();
	}

	public boolean nextBoolean() throws IOException {
		return reader.nextBoolean();
	}

	public String nextString() throws IOException {
		return reader.nextString();
	}

	public void skipValue() throws IOException {
		reader.skipValue();
	}

	public void endObject() throws IOException {
		reader.endObject();
	}

	public <T> List<T> forEachInArray(ThrowingFunction<JsonReaderHelper, T> mapper) throws IOException {
		List<T> list = new ArrayList<>();
		reader.beginArray();
		while (reader.hasNext()) {
			list.add(mapper.apply(this));
		}
		reader.endArray();
		return list;
	}

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

	public void forEachObjectField(ThrowingFieldHandler handler) throws IOException {
		while (reader.hasNext()) {
			String name = nextName();
			handler.handle(name, this);
		}
	}

	@FunctionalInterface
	public interface ThrowingFunction<T, R> {
		R apply(T t) throws IOException;
	}

	@FunctionalInterface
	public interface ThrowingFieldHandler {
		void handle(String name, JsonReaderHelper reader) throws IOException;
	}
}