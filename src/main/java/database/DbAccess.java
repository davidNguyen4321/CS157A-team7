package database;

import java.sql.Connection;

public class DbAccess {
	
	private static Connection dbConnection = null;
	
	public static void setConnection(Connection connection) {
		dbConnection = connection;
	}
	
	public static Connection getConnection() {
		if (dbConnection == null) {
			throw new IllegalStateException("Database connection is not available");
        }
		
		return dbConnection;
	}
}