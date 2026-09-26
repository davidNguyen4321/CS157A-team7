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
		        CREATE TABLE IF NOT EXISTS APPLIANCES (
		        	APPLIANCE_ID INT PRIMARY KEY AUTO_INCREMENT,
		        	APPLIANCE_NAME VARCHAR(100) NOT NULL UNIQUE,
		        	APPLIANCE_IMAGE_PATH VARCHAR(500) NOT NULL,
		        	APPLIANCE_DESCRIPTION TEXT NOT NULL
		        )
		        """,
		        """
		        CREATE TABLE IF NOT EXISTS INGREDIENTS (
		            INGREDIENT_ID INT PRIMARY KEY AUTO_INCREMENT,
		            INGREDIENT_NAME VARCHAR(100) NOT NULL UNIQUE,
		            INGREDIENT_IMAGE_PATH VARCHAR(500) NOT NULL,
		            INGREDIENT_DESCRIPTION TEXT NOT NULL,
		          	BASE_INGREDIENT_ID INT NULL,
		            FOREIGN KEY (BASE_INGREDIENT_ID) REFERENCES INGREDIENTS (INGREDIENT_ID) ON DELETE SET NULL
		        )
		        """,
		        """
		        CREATE TABLE IF NOT EXISTS DIETARY_RESTRICTIONS (
				    DIETARY_RESTRICTION_ID INT PRIMARY KEY AUTO_INCREMENT,
				    DIETARY_RESTRICTION_NAME VARCHAR(100) NOT NULL UNIQUE,
				    DIETARY_RESTRICTION_DESCRIPTION TEXT NOT NULL,
				    BASE_DIETARY_RESTRICTION_ID INT NULL,
				    FOREIGN KEY (BASE_DIETARY_RESTRICTION_ID) REFERENCES DIETARY_RESTRICTIONS (DIETARY_RESTRICTION_ID) ON DELETE SET NULL
				)
		        """, // Business Logic
		        """
		        CREATE TABLE IF NOT EXISTS CUISINES (
		            CUISINE_ID INT PRIMARY KEY AUTO_INCREMENT,
		            CUISINE_NAME VARCHAR(100) NOT NULL UNIQUE,
		            CUISINE_DESCRIPTION TEXT NOT NULL,
		            BASE_CUISINE_ID INT NULL,
		        	FOREIGN KEY (BASE_CUISINE_ID) REFERENCES CUISINES (CUISINE_ID) ON DELETE SET NULL
		        )
		        """, // User, AI, or blank
		        """
		        CREATE TABLE IF NOT EXISTS DISH_TYPES (
				    DISH_TYPE_ID INT PRIMARY KEY AUTO_INCREMENT,
				    DISH_TYPE_NAME VARCHAR(100) NOT NULL UNIQUE,
				    DISH_TYPE_DESCRIPTION TEXT NOT NULL,
				    BASE_DISH_TYPE_ID INT NULL,
				    FOREIGN KEY (BASE_DISH_TYPE_ID) REFERENCES DISH_TYPES (DISH_TYPE_ID) ON DELETE SET NULL
				)
		        """, // User, AI
		        """
		        CREATE TABLE IF NOT EXISTS USERS (
		        	USER_ID INT PRIMARY KEY AUTO_INCREMENT,
		        	USER_NAME VARCHAR(100) NOT NULL UNIQUE,
		        	USER_IMAGE_PATH VARCHAR(500) NULL,
		        	USER_DESCRIPTION TEXT NULL,
		        	USER_ACTIVE_TIMESTAMP TIMESTAMP NOT NULL DEFAULT (UTC_TIMESTAMP),
		        	USER_REMOVE_TIMESTAMP TIMESTAMP NULL,
		        	USER_STATUS ENUM('Active', 'Deleted', 'Warned', 'Suspended', 'Banned') NOT NULL DEFAULT 'Active',
		        	ROLE ENUM('User', 'Moderator', 'Admin') NOT NULL,
		        	TIMEZONE VARCHAR(30) NOT NULL,
		            PASSWORD_HASH VARCHAR(254) NOT NULL,
		        	EMAIL_ADDRESS VARCHAR(254) NULL UNIQUE,
		        	PHONE_NUMBER VARCHAR(15) NULL UNIQUE,
		        	CHECK (COALESCE (EMAIL_ADDRESS, PHONE_NUMBER) IS NOT NULL)
		        )
		        """, // Soft delete only
		        """
		        
		        """,
		        """
		        CREATE TABLE IF NOT EXISTS RECIPES (
		            RECIPE_ID INT PRIMARY KEY AUTO_INCREMENT,
		            RECIPE_NAME VARCHAR(100) NOT NULL,
		            RECIPE_IMAGE_PATH VARCHAR(500) NULL,
		            RECIPE_DESCRIPTION TEXT NULL,
		            RECIPE_ACTIVE_TIMESTAMP TIMESTAMP NOT NULL DEFAULT (UTC_TIMESTAMP),
		            RECIPE_REMOVE_TIMESTAMP TIMESTAMP NULL,
		            RECIPE_STATUS ENUM('Active', 'Deleted', 'Moderated') NOT NULL DEFAULT 'Active',
		            INSTRUCTIONS TEXT NOT NULL,
		            TOTAL_MINUTES INT NULL,
		            MINUTES_AI_GENERATED BOOLEAN NOT NULL,
		            COURSE ENUM('Entrée', 'Appetizer / Side', 'Dessert', 'Other') NOT NULL,
		            AUTHOR_ID INT NULL,
		            FOREIGN KEY (AUTHOR_ID) REFERENCES USERS (USER_ID)
		        )
		        """, // Hard delete for 'Deleted' after 30 days, for 'Moderated' after violation expires
		        """
		        CREATE TABLE IF NOT EXISTS REVIEWS (
		            REVIEW_ID INT PRIMARY KEY AUTO_INCREMENT,
		            REVIEW_IMAGE_PATH VARCHAR(500) NULL,
		            REVIEW_DESCRIPTION TEXT NOT NULL,
		            REVIEW_ACTIVE_TIMESTAMP TIMESTAMP NOT NULL DEFAULT (UTC_TIMESTAMP),
		            REVIEW_REMOVE_TIMESTAMP TIMESTAMP NULL,
		            REVIEW_STATUS ENUM('Active', 'Deleted', 'Moderated') NOT NULL DEFAULT 'Active',
		            RATING INT NOT NULL,
		            CHECK (RATING BETWEEN 1 AND 5),
		            REF_RECIPE_ID INT NOT NULL,
		            FOREIGN KEY (REF_RECIPE_ID) REFERENCES RECIPES (RECIPE_ID) ON DELETE CASCADE,
		        	REVIEWER_ID INT NOT NULL,
		        	FOREIGN KEY (REVIEWER_ID) REFERENCES USERS (USER_ID),
		        	UNIQUE (REF_RECIPE_ID, REVIEWER_ID)
		        )
		        """, // Hard delete for 'Deleted' after 30 days, for 'Moderated' after violation expires
		        """
		        CREATE TABLE IF NOT EXISTS REPORTS (
				    REPORT_ID INT PRIMARY KEY AUTO_INCREMENT,
				    REPORT_DESCRIPTION TEXT NULL,
				    REPORT_ACTIVE_TIMESTAMP TIMESTAMP NOT NULL DEFAULT (UTC_TIMESTAMP),
				    REPORT_STATUS ENUM('Active', 'Resolved', 'Dismissed') NOT NULL DEFAULT 'Active',
				    CONTENT_TYPE ENUM('Recipe', 'Review', 'User') NOT NULL,
				    REASON ENUM('Spam', 'Vulgar Content', 'Malicious Links', 'Copyright', 'Misinformation', 'Harassment', 'Scam', 'Other') NOT NULL,
				    EVIDENCE JSON NOT NULL,
				    REPORTED_ID INT NOT NULL,
				    FOREIGN KEY (REPORTED_ID) REFERENCES USERS (USER_ID),
				    REPORTER_ID INT NOT NULL,
				    FOREIGN KEY (REPORTER_ID) REFERENCES USERS (USER_ID)
				)		
		        """, // Never deleted
		        """
			    CREATE TABLE IF NOT EXISTS VIOLATIONS (
			        VIOLATION_ID INT PRIMARY KEY AUTO_INCREMENT,
			        VIOLATION_DESCRIPTION TEXT NOT NULL,
			        VIOLATION_ACTIVE_TIMESTAMP TIMESTAMP NOT NULL DEFAULT (UTC_TIMESTAMP),
				    SEVERITY ENUM('Minor', 'Moderate', 'Severe') NOT NULL,
				    IS_PERMANENT BOOLEAN NOT NULL,
				    VIOLATOR_ID INT NOT NULL,
				    FOREIGN KEY (VIOLATOR_ID) REFERENCES USERS (USER_ID),
				    REPORT_ID INT NULL,
				    FOREIGN KEY (REPORT_ID) REFERENCES REPORTS (REPORT_ID)
				)
		        """ // 'Minor' and 'Moderate' expire unless violator banned
		};
		
		executeAll(codeblocks);
	}
	
	public void createRelationshipSets() throws SQLException {
		String[] codeblocks = {
				"""
		        CREATE TABLE IF NOT EXISTS RECIPES_USE_APPLIANCES (
		            RECIPE_ID INT, APPLIANCE_ID INT,
		            PRIMARY KEY (RECIPE_ID, APPLIANCE_ID),
		            FOREIGN KEY (RECIPE_ID) REFERENCES RECIPES (RECIPE_ID) ON DELETE CASCADE,
		            FOREIGN KEY (APPLIANCE_ID) REFERENCES APPLIANCES (APPLIANCE_ID)
		        )
		        """,
		        """
		        CREATE TABLE IF NOT EXISTS RECIPES_COOK_INGREDIENTS (
		            RECIPE_ID INT, INGREDIENT_ID INT,
		            PRIMARY KEY (RECIPE_ID, INGREDIENT_ID),
					FOREIGN KEY (RECIPE_ID) REFERENCES RECIPES (RECIPE_ID) ON DELETE CASCADE,
					FOREIGN KEY (INGREDIENT_ID) REFERENCES INGREDIENTS (INGREDIENT_ID),
					AMOUNT DECIMAL(5,2) NULL,
					AMOUNT_UNIT VARCHAR(25) NULL
		        )
		        """,
		        """
		        CREATE TABLE IF NOT EXISTS INGREDIENTS_FIT_DIETARY_RESTRICTIONS (
		            INGREDIENT_ID INT, DIETARY_RESTRICTION_ID INT,
		            PRIMARY KEY (INGREDIENT_ID, DIETARY_RESTRICTION_ID),
		            FOREIGN KEY (INGREDIENT_ID) REFERENCES INGREDIENTS (INGREDIENT_ID),
		            FOREIGN KEY (DIETARY_RESTRICTION_ID) REFERENCES DIETARY_RESTRICTIONS (DIETARY_RESTRICTION_ID)
		        )
		        """,
		        """
		        CREATE TABLE IF NOT EXISTS RECIPES_FEATURE_CUISINES (
		            RECIPE_ID INT, CUISINE_ID INT,
		            PRIMARY KEY (RECIPE_ID, CUISINE_ID),
		            FOREIGN KEY (RECIPE_ID) REFERENCES RECIPES (RECIPE_ID) ON DELETE CASCADE,
		            FOREIGN KEY (CUISINE_ID) REFERENCES CUISINES (CUISINE_ID),
		            CUISINE_AI_GENERATED BOOLEAN NOT NULL
		        )
		        """,
		        """
		        CREATE TABLE IF NOT EXISTS RECIPES_DISH_TYPES (
		            RECIPE_ID INT, DISH_TYPE_ID INT,
		            PRIMARY KEY (RECIPE_ID, DISH_TYPE_ID),
		            FOREIGN KEY (RECIPE_ID) REFERENCES RECIPES (RECIPE_ID) ON DELETE CASCADE,
		            FOREIGN KEY (DISH_TYPE_ID) REFERENCES DISH_TYPES (DISH_TYPE_ID),
		            DISH_TYPE_AI_GENERATED BOOLEAN NOT NULL
		        )
		        """,
		        """
		        CREATE TABLE IF NOT EXISTS BOOKMARKED (
		            USER_ID INT, RECIPE_ID INT, 
		            PRIMARY KEY (USER_ID, RECIPE_ID),
		            FOREIGN KEY (USER_ID) REFERENCES USERS (USER_ID),
		            FOREIGN KEY (RECIPE_ID) REFERENCES RECIPES (RECIPE_ID) ON DELETE CASCADE,
		        )
		        """,
		        """
		        CREATE TABLE IF NOT EXISTS FOLLOWED (
		            FOLLOWER_ID INT, FOLLOWING_ID INT,
		            PRIMARY KEY (FOLLOWER_ID, FOLLOWING_ID),
		            FOREIGN KEY (FOLLOWER_ID) REFERENCES USERS (USER_ID),
		            FOREIGN KEY (FOLLOWING_ID) REFERENCES USERS (USER_ID)
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