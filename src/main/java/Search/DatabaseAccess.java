package Search;
import java.sql.Connection;
public class DatabaseAccess {
	private static Connection dbConnection;
	public static void setConnection(Connection connection) {
		dbConnection = connection;
	}
	public static Connection getConnection() {
		return dbConnection;
	}
}