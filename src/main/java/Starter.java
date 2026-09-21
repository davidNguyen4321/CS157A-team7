import java.sql.Connection;
import java.sql.SQLException;
import java.sql.DriverManager;
import java.sql.Statement;
import java.sql.ResultSet;
import java.util.Scanner;

public class Starter {
	Connection connection;
	Scanner scanner;
	
	public static void main(String[] args) throws SQLException {
		Scanner scanner = new Scanner(System.in);
		
		System.out.print("Input database password: ");
		String password = scanner.next();
		
		Connection connection = DriverManager.getConnection(
				"jdbc:mysql://localhost:3306/recipegenie?serverTimezone=UTC", 
				"root", 
				password
		);
		
		Starter starter = new Starter(connection, scanner); 
		
		if(starter.promptAction("reset")) {
		    starter.actOnAllTables("drop");
		}
		
		starter.createTables();

		if(starter.promptAction("repopulate")) {
		    starter.actOnAllTables("truncate");
		    
		    StarterPopulator populator = new StarterPopulator(starter);
		    populator.populateTables();
		}
		
		starter.close();
	}
	
	public Starter(Connection connection, Scanner scanner) {
		this.connection = connection;
		this.scanner = scanner;
	}
	
	public void actOnAllTables(String action) throws SQLException {
		Statement grabStatement = connection.createStatement(); // Grab all tables
		ResultSet rs = grabStatement.executeQuery("SHOW TABLES");
		
		Statement execStatement = connection.createStatement(); // Open another statement for executing actions

		while(rs.next()){ // Action all tables
			execStatement.execute(action + " TABLE " + rs.getString(1));
		}
		
		grabStatement.close();
		execStatement.close();
	}
	
	public boolean promptAction(String action) {
		System.out.println("Do you want to " + action + " all tables? Enter yes or no.");
		if(scanner.next().toLowerCase().equals("yes")) {
			return true;
		}
		else {
			return false;
		}
	}

	public void createTables() throws SQLException {
		createEntitySets();
		createRelationshipSets();
	}
	
	// 3000 bytes ~ 500 words
	public void createEntitySets() throws SQLException {
		String[] codeblocks = {
				"""
		        CREATE TABLE IF NOT EXISTS INGREDIENTS (
		            ID_INGREDIENT INT NOT NULL AUTO_INCREMENT,
		            INGREDIENT_NAME VARCHAR(45) NOT NULL UNIQUE,
		            PRIMARY KEY (ID_INGREDIENT)
		        )
		        """,
		        """
		        CREATE TABLE IF NOT EXISTS COOKING_APPLIANCES (
		        	ID_COOKING_APPLIANCE INT NOT NULL AUTO_INCREMENT,
		        	COOKING_APPLIANCE_NAME VARCHAR(45) NOT NULL UNIQUE,
		        	PRIMARY KEY (ID_COOKING_APPLIANCE)
		        )
		        """,
		        """
		        CREATE TABLE IF NOT EXISTS USERS (
		        	ID_USER INT NOT NULL AUTO_INCREMENT,
		        	USER_NAME VARCHAR(45) NOT NULL UNIQUE,
		        	EMAIL_ADDRESS VARCHAR(45) NOT NULL UNIQUE,
		        	ACCOUNT_CREATION_DATE DATE NOT NULL,
		        	PRIMARY KEY (ID_USER)
		        )
		        """,
		        """
		        CREATE TABLE IF NOT EXISTS RECIPES (
		            ID_RECIPE INT NOT NULL AUTO_INCREMENT,
		            RECIPE_NAME VARCHAR(45) NOT NULL,
		            COOKING_TIME TIME,
		            PUBLICATION_DATE DATE NOT NULL,
		            RECIPE_IMAGE_PATH TEXT(100),
		            DESCRIPTION TEXT(3000),
		            INSTRUCTIONS TEXT(12000) NOT NULL,
		            PRIMARY KEY (ID_RECIPE)
		        )
		        """, 
		        """
		        CREATE TABLE IF NOT EXISTS REVIEWS (
		            ID_REVIEW INT NOT NULL AUTO_INCREMENT,
		            TEXT TEXT(3000) NOT NULL,
		            RATING INT NOT NULL,
		            PRIMARY KEY (ID_REVIEW)
		        )
		        """,
		        """
		        CREATE TABLE IF NOT EXISTS NATIONALITY (
		            ID_NATIONALITY INT NOT NULL AUTO_INCREMENT,
		            NATIONALITY_NAME VARCHAR(45) NOT NULL UNIQUE,
		            PRIMARY KEY (ID_NATIONALITY)
		        )
		        """,
		        """
		        CREATE TABLE IF NOT EXISTS DISH_TYPE (
		            ID_DISH_TYPE INT NOT NULL AUTO_INCREMENT,
		            DISH_TYPE_NAME VARCHAR(45) NOT NULL UNIQUE,
		            PRIMARY KEY (ID_DISH_TYPE)
		        )
		        """,
		        """
		        CREATE TABLE IF NOT EXISTS DIETARY_RESTRICTIONS (
		            ID_DIETARY_RESTRICTION INT NOT NULL AUTO_INCREMENT,
		            DIETARY_RESTRICTION_NAME VARCHAR(45) NOT NULL UNIQUE,
		            PRIMARY KEY (ID_DIETARY_RESTRICTION)
		        )
		        """,
		        """
		        CREATE TABLE IF NOT EXISTS IMAGES (
		            ID_IMAGE INT NOT NULL AUTO_INCREMENT,
		            IMAGE_PATH VARCHAR(45) NOT NULL UNIQUE,
		            PRIMARY KEY (ID_IMAGE)
		        )
		        """
		};
		
		executeAll(codeblocks);
	}
	
	public void createRelationshipSets() throws SQLException {
		String[] codeblocks = {
				"""
		        CREATE TABLE IF NOT EXISTS RECIPES_INGREDIENTS (
		            ID_RECIPE INT NOT NULL,
		            ID_INGREDIENT INT NOT NULL,
		            PRIMARY KEY (ID_RECIPE, ID_INGREDIENT)
		        )
		        """,
		        """
		        CREATE TABLE IF NOT EXISTS RECIPES_COOKING_APPLIANCES (
		            ID_RECIPE INT NOT NULL,
		            ID_COOKING_APPLIANCE INT NOT NULL,
		            PRIMARY KEY (ID_RECIPE, ID_COOKING_APPLIANCE)
		        )
		        """,
		        """
		        CREATE TABLE IF NOT EXISTS RECIPES_REVIEWS (
		            ID_RECIPE INT NOT NULL,
		            ID_REVIEW INT NOT NULL,
		            PRIMARY KEY (ID_RECIPE, ID_REVIEW)
		        )
		        """,
		        """
		        CREATE TABLE IF NOT EXISTS RECIPES_USER_AUTHORS (
		            ID_RECIPE INT NOT NULL,
		            ID_USER INT NOT NULL,
		            PRIMARY KEY (ID_RECIPE, ID_USER)
		        )
		        """,
		        """
		        CREATE TABLE IF NOT EXISTS USER_FOLLOWEDS__USER_FOLLOWERS (
		            ID_USER_FOLLOWED INT NOT NULL,
		            ID_USER_FOLLOWER INT NOT NULL,
		            PRIMARY KEY (ID_USER_FOLLOWED, ID_USER_FOLLOWER)
		        )
		        """,
		        """
		        CREATE TABLE IF NOT EXISTS RECIPES_USER_BOOKMARKERS (
		            ID_RECIPE INT NOT NULL,
		            ID_USER INT NOT NULL,
		            PRIMARY KEY (ID_RECIPE, ID_USER)
		        )
		        """,
		        """
		        CREATE TABLE IF NOT EXISTS RECIPES_NATIONALITIES (
		            ID_RECIPE INT NOT NULL,
		            ID_NATIONALITY INT NOT NULL,
		            PRIMARY KEY (ID_RECIPE, ID_NATIONALITY)
		        )
		        """,
		        """
		        CREATE TABLE IF NOT EXISTS RECIPES_DISH_TYPES (
		            ID_RECIPE INT NOT NULL,
		            ID_DISH_TYPE INT NOT NULL,
		            PRIMARY KEY (ID_RECIPE, ID_DISH_TYPE)
		        )
		        """,
		        """
		        CREATE TABLE IF NOT EXISTS INGREDIENTS_DIETARY_RESTRICTIONS (
		            ID_INGREDIENT INT NOT NULL,
		            ID_DIETARY_RESTRICTION INT NOT NULL,
		            PRIMARY KEY (ID_INGREDIENT, ID_DIETARY_RESTRICTION)
		        )
		        """,
		        """
		        CREATE TABLE IF NOT EXISTS RECIPE_DIETARY_RESTRICTIONS (
		            ID_RECIPE INT NOT NULL,
		            ID_DIETARY_RESTRICTION INT NOT NULL,
		            PRIMARY KEY (ID_RECIPE, ID_DIETARY_RESTRICTION)
		        )
		        """,
		        """
		        CREATE TABLE IF NOT EXISTS USERS_IMAGES (
		        	ID_USER INT NOT NULL,
		        	ID_IMAGE INT NOT NULL,
		        	PRIMARY KEY (ID_USER, ID_IMAGE)
		        )
		        """,
		        """
		        CREATE TABLE IF NOT EXISTS RECIPES_IMAGES (
		        	ID_RECIPE INT NOT NULL,
		        	ID_IMAGE INT NOT NULL,
		        	PRIMARY KEY(ID_RECIPE, ID_IMAGE)
		        )
		        """,
		        """
		        CREATE TABLE IF NOT EXISTS REVIEWS_IMAGES (
		        	ID_REVIEW INT NOT NULL,
		        	ID_IMAGE INT NOT NULL,
		        	PRIMARY KEY(ID_REVIEW, ID_IMAGE)
		        )
		        """,
		        """
		        CREATE TABLE IF NOT EXISTS DIETARY_RESTRICTIONS_IMAGES (
		        	ID_DIETARY_RESTRICTION INT NOT NULL,
		        	ID_IMAGE INT NOT NULL,
		        	PRIMARY KEY(ID_DIETARY_RESTRICTION, ID_IMAGE)
		        )
		        """
		};

		executeAll(codeblocks);
	}	
	
	public void executeAll(String[] codeblocks) throws SQLException {
		Statement statement = connection.createStatement();
		
		for (String block : codeblocks) {
            statement.execute(block);
        }
		
		statement.close();
	}
	
	public void close() throws SQLException{ // Clean up
		this.connection.close();
		this.scanner.close();
	}
}


