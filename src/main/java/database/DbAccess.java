package database;

import java.sql.Connection;

public class DbAccess {
	
	private static Connection dbConnection = null;
	
	public static void setConnection(Connection connection) {
		dbConnection = connection;
	}
	
	public static Connection getConnection() {
		return dbConnection;
	}
}