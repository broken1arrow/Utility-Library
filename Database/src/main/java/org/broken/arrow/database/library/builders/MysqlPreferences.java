package org.broken.arrow.database.library.builders;


public class MysqlPreferences {

	private final String databaseName;
	private final String hostAddress;
	private final String port;
	private final String user;
	private final String password;

	public MysqlPreferences(final String path) {
		this.databaseName = "";
		this.hostAddress = path;
		this.port = "";
		this.user = "";
		this.password = "";
	}

	public MysqlPreferences(final String databaseName, final String hostAddress, final String port, final String user, final String password) {
		this.databaseName = databaseName;
		this.hostAddress = hostAddress;
		this.port = port;
		this.user = user;
		this.password = password;
	}

	public String getDatabaseName() {
		return databaseName;
	}

	public String getHostAddress() {
		return hostAddress;
	}

	public String getPort() {
		return port;
	}

	public String getUser() {
		return user;
	}

	public String getPassword() {
		return password;
	}

}