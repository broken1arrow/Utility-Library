package org.broken.arrow.database.library.builders;

/**
 * This class represents the connection settings for a database. It is used to encapsulate the
 * information required to connect to different types of databases, including local and remote
 * database servers.
 */
public final class ConnectionSettings {

	private final String databaseName;
	private final String hostAddress;
	private final String port;
	private final String user;
	private final String password;
	private final String query;

	/**
	 * Constructs a ConnectionSettings object for file-based databases. This constructor should be used
	 * when connecting to file-based databases and should not be used for other types of databases that
	 * require specifying "port", "user", and "password".
	 *
	 * @param path the path to the file.
	 */
	public ConnectionSettings(final String path) {
		this("", path, "", "", "", "");
	}

	/**
	 * Constructs a ConnectionSettings object with the specified login credentials for the database.
	 *
	 * @param databaseName the name of the database.
	 * @param hostAddress  the URL or IP address of the database server.
	 * @param port         the port number for the database server.
	 * @param user         the username for the database connection.
	 * @param password     the password for the database connection.
	 */
	public ConnectionSettings(final String databaseName, final String hostAddress, final String port, final String user, final String password) {
		this(databaseName, hostAddress, port, user, password, "");
	}

	/**
	 * Constructs a ConnectionSettings object with the specified login credentials and additional connection information.
	 *
	 * @param databaseName the name of the database.
	 * @param hostAddress  the URL or IP address of the database server.
	 * @param port         the port number for the database server.
	 * @param user         the username for the database connection.
	 * @param password     the password for the database connection.
	 * @param query        additional connection information, such as query parameters.
	 */
	public ConnectionSettings(final String databaseName, final String hostAddress, final String port, final String user, final String password, final String query) {
		this.databaseName = databaseName;
		this.hostAddress = hostAddress;
		this.port = port;
		this.user = user;
		this.password = password;
		this.query = query != null ? query : "";
	}

	/**
	 * Returns the name of the database.
	 *
	 * @return the database name.
	 */
	public String getDatabaseName() {
		return databaseName;
	}

	/**
	 * Returns the URL or IP address of the database server.
	 *
	 * @return the host address.
	 */
	public String getHostAddress() {
		return hostAddress;
	}

	/**
	 * Returns the port number for the database server.
	 *
	 * @return the port number.
	 */
	public String getPort() {
		return port;
	}

	/**
	 * Returns the username for the database connection.
	 *
	 * @return the username.
	 */
	public String getUser() {
		return user;
	}

	/**
	 * Returns the password for the database connection.
	 *
	 * @return the password.
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * Returns the query parameters set.
	 *
	 * @return the query parameters.
	 */
	public String getQuery() {
		return query;
	}

}