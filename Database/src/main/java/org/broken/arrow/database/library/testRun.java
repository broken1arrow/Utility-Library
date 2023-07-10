package org.broken.arrow.database.library;

import org.broken.arrow.database.library.builders.MysqlPreferences;
import org.broken.arrow.database.library.builders.TableWrapper;
import org.broken.arrow.database.library.builders.tables.TableRow;
import org.broken.arrow.database.library.builders.tables.TableRow.Builder;
import org.broken.arrow.serialize.library.utility.serialize.ConfigurationSerializable;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;


public class testRun {
	static {
/*		Logger.getLogger("com.zaxxer.hikari.pool.PoolBase").setLevel(Level.OFF);
		Logger.getLogger("com.zaxxer.hikari.pool.HikariPool").setLevel(Level.OFF);
		Logger.getLogger("com.zaxxer.hikari.HikariDataSource").setLevel(Level.OFF);
		Logger.getLogger("com.zaxxer.hikari.HikariConfig").setLevel(Level.OFF);
		Logger.getLogger("com.zaxxer.hikari.util.DriverDataSource").setLevel(Level.OFF);
		Logger.getLogger("com.zaxxer.hikari.*").setLevel(Level.OFF);*/
	}

	public static void main(String[] args) {
		TableWrapper databasewrapper = TableWrapper.of("Test", new TableRow.Builder("fun_key", "TEXT(120)").build(), false);
		MySQL mysql = new MySQL(new MysqlPreferences("sql7621744", "sql7.freesqldatabase.com", "3306", "sql7621744", "bZS6iTeRpm"));
		databasewrapper
				.add("fun_key1", "VARCHAR(120)")
				.addDefult("fun_key2", "VARCHAR(120)", "HI")
				.addNotNull("fun_key3", "VARCHAR(120)")
				.addCustom("fun_key4", new Builder("fun_key4", "VARCHAR(120)").setNotNull(true))
				.addNotNull("fun_key5", "VARCHAR(120)");
		mysql.addTable(databasewrapper);
		mysql.createTables();
		databasewrapper.addRecord("something");
		//mysql.save("Test", "fun_key", "something", new testData());

		SQLite db = new SQLite("database.db");
		databasewrapper = TableWrapper.of("Test", new TableRow.Builder("fun_key", "VARCHAR(120)").build(), true);
		databasewrapper.add("fun_key1", "VARCHAR(120)")
				.addDefult("fun_key2", "VARCHAR(120)", "HI")
				.addNotNull("fun_key3", "VARCHAR(120)")
				.addCustom("fun_key4", new Builder("fun_key4", "VARCHAR(120)").setNotNull(true))
				.addNotNull("fun_key5", "VARCHAR(120)");
		db.addTable(databasewrapper);
		db.createTables();
		//db.save("Test", "fun_key", "something", new testData());

		//LoadDataWrapper<testData> load = db.load("Test", testData.class);
		//testData testData = load.getDeSerializedData();
	}


	public static class testData implements ConfigurationSerializable {
		@Nonnull
		@Override
		public Map<String, Object> serialize() {
			Map<String, Object> map = new HashMap<>();
			map.put("fun_key3", "testing");
			map.put("fun_key4", "testing555");
			return map;
		}
	}

}
