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

		execStatement.execute("SET FOREIGN_KEY_CHECKS = 0");
		while(rs.next()){ // Action all tables
			execStatement.execute(action + " TABLE " + rs.getString(1));
		}
	    execStatement.execute("SET FOREIGN_KEY_CHECKS = 1");
		
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
	
	public void createEntitySets() throws SQLException {
		String[] codeblocks = {
				"""
		        CREATE TABLE IF NOT EXISTS CUISINES (
		            CUISINE_ID INT NOT NULL AUTO_INCREMENT,
		            CUISINE_NAME VARCHAR(50) NOT NULL UNIQUE,
		            BASE_CUISINE_ID INT NULL,
		            PRIMARY KEY (CUISINE_ID),
		        	FOREIGN KEY (BASE_CUISINE_ID) REFERENCES CUISINES (CUISINE_ID)
		        )
		        """,
		        """
		        CREATE TABLE IF NOT EXISTS DISH_TYPES (
				    DISH_TYPE_ID INT NOT NULL AUTO_INCREMENT,
				    DISH_TYPE_NAME VARCHAR(50) NOT NULL UNIQUE,
				    PRIMARY KEY (DISH_TYPE_ID)
				)
		        """,
		        """
		        CREATE TABLE IF NOT EXISTS DIETARY_RESTRICTIONS (
				    DIETARY_RESTRICTION_ID INT NOT NULL AUTO_INCREMENT,
				    DIETARY_RESTRICTION_NAME VARCHAR(50) NOT NULL UNIQUE,
				    PRIMARY KEY (DIETARY_RESTRICTION_ID)
				)
		        """,
				"""
		        CREATE TABLE IF NOT EXISTS INGREDIENTS (
		            INGREDIENT_ID INT NOT NULL AUTO_INCREMENT,
		            INGREDIENT_NAME VARCHAR(150) NOT NULL UNIQUE,
		          	BASE_INGREDIENT_ID INT NULL,
		            PRIMARY KEY (INGREDIENT_ID),
		            FOREIGN KEY (BASE_INGREDIENT_ID) REFERENCES INGREDIENTS (INGREDIENT_ID)
		        )
		        """,
		        """
		        CREATE TABLE IF NOT EXISTS APPLIANCES (
		        	APPLIANCE_ID INT NOT NULL AUTO_INCREMENT,
		        	APPLIANCE_NAME VARCHAR(50) NOT NULL UNIQUE,
		        	PRIMARY KEY (APPLIANCE_ID)
		        )
		        """,
		        """
		        CREATE TABLE IF NOT EXISTS USERS (
		        	USER_ID INT NOT NULL AUTO_INCREMENT,
		        	USER_NAME VARCHAR(50) NOT NULL UNIQUE,
		        	EMAIL_ADDRESS VARCHAR(254) NOT NULL UNIQUE,
		        	PASSWORD_HASH VARCHAR(150) NOT NULL,
		        	AVATAR_PATH VARCHAR(150) NULL,
		        	JOIN_DATE DATE NOT NULL DEFAULT (CURRENT_DATE),
		        	PRIMARY KEY (USER_ID)
		        )
		        """,
		        """
		        CREATE TABLE IF NOT EXISTS RECIPES (
		            RECIPE_ID INT NOT NULL AUTO_INCREMENT,
		            RECIPE_NAME VARCHAR(150) NOT NULL,
		            COOK_MINUTES INT NULL,
		            DESCRIPTION TEXT NULL,
		            INSTRUCTIONS TEXT NOT NULL,
		            COURSE ENUM('Appetizer', 'Main Course', 'Side Dish', 'Dessert') NULL,
		            IMAGE_PATH VARCHAR(150) NULL,
		            PUBLISH_DATE DATE NOT NULL DEFAULT (CURRENT_DATE),
		            AUTHOR_ID INT NOT NULL,
		            CUISINE_ID INT NULL,
		            PRIMARY KEY (RECIPE_ID),
		            FOREIGN KEY (AUTHOR_ID) REFERENCES USERS (USER_ID),
		            FOREIGN KEY (CUISINE_ID) REFERENCES CUISINES (CUISINE_ID)
		        )
		        """,
		        """
		        CREATE TABLE IF NOT EXISTS REVIEWS (
		            REVIEW_ID INT NOT NULL AUTO_INCREMENT,
		            RATING INT NOT NULL,
		            CHECK (RATING BETWEEN 1 AND 5),
		            CONTENT TEXT NULL,
		            PHOTO_PATH VARCHAR(150) NULL,
		            POST_DATE DATE NOT NULL DEFAULT (CURRENT_DATE),
		            RECIPE_ID INT NOT NULL,
		        	USER_ID INT NOT NULL,
		            PRIMARY KEY (REVIEW_ID),
		            FOREIGN KEY (RECIPE_ID) REFERENCES RECIPES (RECIPE_ID),
		        	FOREIGN KEY (USER_ID) REFERENCES USERS (USER_ID),
		        	UNIQUE (RECIPE_ID, USER_ID)
		        )
		        """,
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
		            PRIMARY KEY (ID_REVIEW)
		        )
		        """,// many to one
		        """
		        CREATE TABLE IF NOT EXISTS RECIPES_USER_AUTHORS (
		            ID_RECIPE INT NOT NULL,
		            ID_USER INT NOT NULL,
		            PRIMARY KEY (ID_RECIPE)
		        )
		        """,//many to one
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
		        	PRIMARY KEY (ID_USER)
		        )
		        """, // many to one
		        """
		        CREATE TABLE IF NOT EXISTS RECIPES_IMAGES (
		        	ID_RECIPE INT NOT NULL,
		        	ID_IMAGE INT NOT NULL,
		        	PRIMARY KEY(ID_RECIPE)
		        )
		        """, // many to one for now, only cover images are supported. including images in the instructions is beyond the current scope
		        """
		        CREATE TABLE IF NOT EXISTS REVIEWS_IMAGES (
		        	ID_REVIEW INT NOT NULL,
		        	ID_IMAGE INT NOT NULL,
		        	PRIMARY KEY(ID_REVIEW)
		        )
		        """, // many to one for now
		        """
		        CREATE TABLE IF NOT EXISTS DIETARY_RESTRICTIONS_IMAGES (
		        	ID_DIETARY_RESTRICTION INT NOT NULL,
		        	ID_IMAGE INT NOT NULL,
		        	PRIMARY KEY(ID_DIETARY_RESTRICTION)
		        )
		        """ // many to one
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


