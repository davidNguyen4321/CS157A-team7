package Search;
import java.sql.Connection;
public class DatabaseAccess {
	private static Connection dbConnection = null;
	public static void setConnection(Connection connection) {
		dbConnection = connection;
	}
	public static Connection getConnection() {
		return dbConnection;
	}
}