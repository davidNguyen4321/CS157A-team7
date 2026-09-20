import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.sql.ResultSet;
import java.util.Scanner;

public class Starter {
	Connection connection;
	Scanner scanner;
	Statement statement;
	
	public static void main(String[] args) throws Exception{
		String password = [password here];
		Starter starter = new Starter(DriverManager.getConnection("jdbc:mysql://localhost:3306/recipegenie?serverTimezone=UTC", "root", password));
		
		// Options for restarting table
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
		createEntitySets();
		createRelationshipSets();
	}
	
	public void createEntitySets() throws Exception{
		this.statement = this.connection.createStatement();
		this.statement.execute("CREATE TABLE IF NOT EXISTS INGREDIENTS (ID_INGREDIENT INT NOT NULL AUTO_INCREMENT, " 
				+ "INGREDIENT_NAME VARCHAR(45) NOT NULL UNIQUE, PRIMARY KEY (ID_INGREDIENT))");
		this.statement.execute("CREATE TABLE IF NOT EXISTS COOKING_APPLIANCES (ID_COOKING_APPLIANCE INT NOT "
				+ "NULL AUTO_INCREMENT, COOKING_APPLIANCE_NAME VARCHAR(45) NOT NULL UNIQUE, PRIMARY KEY "
				+ "(ID_COOKING_APPLIANCE))");
		this.statement.execute("CREATE TABLE IF NOT EXISTS USERS (ID_USER INT NOT NULL AUTO_INCREMENT, " 
				+ "USER_NAME VARCHAR(45) NOT NULL UNIQUE, EMAIL_ADDRESS VARCHAR(45) NOT NULL UNIQUE, "
				+ "PROFILE_PICTURE BLOB, ACCOUNT_CREATION_DATE DATE NOT NULL, PRIMARY KEY (ID_USER))");
		this.statement.execute("CREATE TABLE IF NOT EXISTS RECIPES (ID_RECIPE INT NOT NULL AUTO_INCREMENT, " 
				+ "RECIPE_NAME VARCHAR(45) NOT NULL, COOKING_TIME TIME, PUBLICATION_DATE DATE NOT NULL, PRIMARY "
				+ "KEY (ID_RECIPE))");
		this.statement.execute("CREATE TABLE IF NOT EXISTS REVIEWS (ID_REVIEW INT NOT NULL AUTO_INCREMENT, " 
				+ "TEXT VARCHAR(45) NOT NULL, IMAGE BLOB, RATING SMALLINT NOT NULL, PRIMARY KEY (ID_REVIEW))");
		this.statement.execute("CREATE TABLE IF NOT EXISTS NATIONALITY (ID_NATIONALITY INT NOT NULL AUTO_INCREMENT, " 
				+ "NATIONALITY_NAME VARCHAR(45) NOT NULL UNIQUE, PRIMARY KEY (ID_NATIONALITY))");
		this.statement.execute("CREATE TABLE IF NOT EXISTS DISH_TYPE (ID_DISH_TYPE INT NOT NULL AUTO_INCREMENT, " 
				+ "DISH_TYPE_NAME VARCHAR(45) NOT NULL UNIQUE, PRIMARY KEY (ID_DISH_TYPE))");
		this.statement.execute("CREATE TABLE IF NOT EXISTS DIETARY_RESTRICTIONS (ID_DIETARY_RESTRICTION INT NOT NULL AUTO_INCREMENT, " 
				+ "DIETARY_RESTRICTION_NAME VARCHAR(45) NOT NULL UNIQUE, SYMBOL BLOB, PRIMARY KEY (ID_DIETARY_RESTRICTION))");
		this.statement.close();
	}
	
	public void createRelationshipSets() throws Exception{
		this.statement = this.connection.createStatement();
		this.statement.execute("CREATE TABLE IF NOT EXISTS RECIPES_INGREDIENTS (ID_RECIPE INT NOT NULL, ID_INGREDIENT INT NOT NULL, "
				+ "PRIMARY KEY (ID_RECIPE, ID_INGREDIENT))");
		this.statement.execute("CREATE TABLE IF NOT EXISTS RECIPES_COOKING_APPLIANCES (ID_RECIPE INT NOT NULL, ID_COOKING_APPLIANCE "
				+ "INT NOT NULL, PRIMARY KEY (ID_RECIPE, ID_COOKING_APPLIANCE))");
		this.statement.execute("CREATE TABLE IF NOT EXISTS RECIPES_REVIEWS (ID_RECIPE INT NOT NULL, ID_REVIEW INT NOT"
				+ " NULL, PRIMARY KEY (ID_RECIPE, ID_REVIEW))");
		this.statement.execute("CREATE TABLE IF NOT EXISTS RECIPES_USER_AUTHOR (ID_RECIPE INT NOT NULL, ID_USER INT NOT"
				+ " NULL, PRIMARY KEY (ID_RECIPE, ID_USER))");
		this.statement.execute("CREATE TABLE IF NOT EXISTS USER_FOLLOWED__USER_FOLLOWER (ID_USER_FOLLOWED INT NOT NULL, ID_USER_FOLLOWER INT NOT"
				+ " NULL, PRIMARY KEY (ID_USER_FOLLOWED, ID_USER_FOLLOWER))");
		this.statement.execute("CREATE TABLE IF NOT EXISTS RECIPES_USER_BOOKMARKER (ID_RECIPE INT NOT NULL, ID_USER INT NOT"
				+ " NULL, PRIMARY KEY (ID_RECIPE, ID_USER))");
		this.statement.execute("CREATE TABLE IF NOT EXISTS RECIPES_NATIONALITIES (ID_RECIPE INT NOT NULL, ID_NATIONALITY INT NOT"
				+ " NULL, PRIMARY KEY (ID_RECIPE, ID_NATIONALITY))");
		this.statement.execute("CREATE TABLE IF NOT EXISTS RECIPES_DISH_TYPE (ID_RECIPE INT NOT NULL, ID_DISH_TYPE INT NOT"
				+ " NULL, PRIMARY KEY (ID_RECIPE, ID_DISH_TYPE))");
		this.statement.execute("CREATE TABLE IF NOT EXISTS INGREDIENTS_DIETARY_RESTRICTIONS (ID_INGREDIENT INT NOT NULL, ID_DIETARY_RESTRICTION INT NOT"
				+ " NULL, PRIMARY KEY (ID_INGREDIENT, ID_DIETARY_RESTRICTION))");
		this.statement.execute("CREATE TABLE IF NOT EXISTS RECIPE_DIETARY_RESTRICTIONS (ID_RECIPE INT NOT NULL, ID_DIETARY_RESTRICTION INT NOT"
				+ " NULL, PRIMARY KEY (ID_RECIPE, ID_DIETARY_RESTRICTION))");
		this.statement.close();
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
		statement = this.connection.createStatement();

		while(rs.next()){ // Action all tables
			String dropSq = action + " TABLE " + rs.getString(1);
			statement.execute(dropSq);
		}
		statement.close();
		return true;
	}
	
	public void close() throws Exception{ // Deallocate memory
		this.connection.close();
		this.scanner.close();
	}
}
