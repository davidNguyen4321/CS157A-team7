import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.sql.ResultSet;
import java.util.Scanner;

public class Starter {
	Connection connection;
	Scanner scanner;
	
	public static void main(String[] args) throws Exception{
		String password = "SixNine-42020";
		Starter starter = new Starter(DriverManager.getConnection("jdbc:mysql://localhost:3306/recipegenie?serverTimezone=UTC", "root", password));
		
		// Drop tables for a new start
		if (!starter.actOnAllTables("drop")) {
			starter.actOnAllTables("truncate");
		}
		starter.createTables();
		starter.close();
	}
	
	public Starter(Connection connection) {
		this.connection = connection;
		this.scanner = new Scanner(System.in);
	}

	public void createTables() throws Exception {
		Statement statement = this.connection.createStatement();
		String createSql = "CREATE TABLE IF NOT EXISTS INGREDIENTS (ID_INGREDIENT INT NOT NULL AUTO_INCREMENT, " 
				+ "INGREDIENT_NAME VARCHAR(45) NOT NULL, PRIMARY KEY (ID_INGREDIENT))";
		statement.execute(createSql);
		
		statement.close();
	}
	
	public boolean actOnAllTables(String action) throws Exception{ // False if action not done. True if action done
		System.out.println("Are you sure you want to " + action.toLowerCase() + " all tables?"); // Verification to ensure that action is intentional
		String response = this.scanner.next();
		if(!response.toLowerCase().equals("yes")) {
			return false;
		}
		
		Statement statement = this.connection.createStatement(); // Grab all tables
		String showSq = "SHOW TABLES";
		ResultSet rs = statement.executeQuery(showSq);
		
		while(rs.next()){ // Action all tables
			String dropSq = action + " TABLE " + rs.getString(1);
			statement = this.connection.createStatement();
			statement.execute(dropSq);
		}
		statement.close();
		return true;
	}
	
	public void close() throws Exception{
		this.connection.close();
		this.scanner.close();
	}
}
