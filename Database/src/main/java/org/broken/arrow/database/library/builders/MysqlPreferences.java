package org.broken.arrow.database.library.builders;


public class MysqlPreferences {

	private final String databaseName;
	private final String hostAdress;
	private final String port;
	private final String user;
	private final String password;
	private final Builder builder;

	private MysqlPreferences(Builder builder) {
		this.databaseName = builder.databaseName;
		this.hostAdress = builder.hostAdress;
		this.port = builder.port;
		this.user = builder.user;
		this.password = builder.password;
		this.builder = builder;
	}

	public String getDatabaseName() {
		return databaseName;
	}

	public String getHostAdress() {
		return hostAdress;
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

	public Builder getBuilder() {
		return builder;
	}

	public static class Builder {
		private final String databaseName;
		private final String hostAdress;
		private final String port;
		private final String user;
		private final String password;

		public Builder(final String databaseName, final String hostAdress, final String port, final String user, final String password) {
			this.databaseName = databaseName;
			this.hostAdress = hostAdress;
			this.port = port;
			this.user = user;
			this.password = password;
		}

		public MysqlPreferences build() {
			return new MysqlPreferences(this);
		}
	}
}