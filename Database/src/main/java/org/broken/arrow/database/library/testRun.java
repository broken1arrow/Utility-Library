package org.broken.arrow.database.library;

import org.broken.arrow.database.library.builders.Databasewrapper;
import org.broken.arrow.database.library.builders.Databasewrapper.TableWrapper;
import org.broken.arrow.database.library.builders.tables.TableRow.Builder;
import org.broken.arrow.database.library.utility.serialize.ConfigurationSerializeUtility;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

public class testRun {


	public static void main(String[] args) {
		SQLite db = new SQLite("database.db");
		TableWrapper databasewrapper = Databasewrapper.of("Test", "fun_key", true);
		db.addTable(databasewrapper.add("fun_key1", "VARCHAR(120)")
				.add("fun_key", "VARCHAR(120)")
				.addDefult("fun_key2", "VARCHAR(120)", "HI")
				.addNotNull("fun_key3", "VARCHAR(120)")
				.addCustom("fun_key4", new Builder("fun_key4", "VARCHAR(120)")
						.setNotNull(true))
				.addNotNull("fun_key5", "VARCHAR(120)")
		);
		db.createTables();
		db.save("Test", "fun_key", "something", new testData());
	}


	public static class testData implements ConfigurationSerializeUtility {
		@Nonnull
		@Override
		public Map<String, Object> serialize() {
			Map<String, Object> map = new HashMap<>();
			map.put("fun_key3", "testinggggg");
			map.put("fun_key4", "testing555yyyyyy");
			return map;
		}
	}
}
