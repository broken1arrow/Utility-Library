package org.broken.arrow.yaml.library.config.updater.utility;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class KeyCache {

	private final Map<String, CommentBuilder> configKeys = new HashMap<>();

	public Map<String, CommentBuilder> getConfigKeys() {
		return Collections.unmodifiableMap(configKeys);
	}

	public CommentBuilder getConfigKey(final String key) {
		return configKeys.get(key);
	}

	public void putConfigKey(final String key, final CommentBuilder keyBuilder) {
		configKeys.put(key, keyBuilder);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder(configKeys.size());
		for (Entry<String, CommentBuilder> entry : configKeys.entrySet()) {
			builder.append("{configKeys='")
					.append(entry.getKey())
					.append("' coment='")
					.append(entry.getValue().getComment())
					.append("'}")
					.append(",");
		}
		builder.setLength(builder.length() - 1);
		String string = builder.toString().replace("\n", "");
		return "KeyCache{" + string + "}";
	}
}
