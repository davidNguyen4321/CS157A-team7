package seed;
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
		        CREATE TABLE IF NOT EXISTS appliances (
		        	appliance_id INT PRIMARY KEY AUTO_INCREMENT,
		        	appliance_name VARCHAR(100) NOT NULL UNIQUE,
		        	appliance_image_path VARCHAR(500) NOT NULL,
		        	appliance_description TEXT NOT NULL
		        )
		        """, // Not required in recipes, admin managed
		        """
		        CREATE TABLE IF NOT EXISTS ingredients (
		            ingredient_id INT PRIMARY KEY AUTO_INCREMENT,
		            ingredient_name VARCHAR(100) NOT NULL UNIQUE,
		            ingredient_image_path VARCHAR(500) NOT NULL,
		            ingredient_description TEXT NOT NULL,
		         	base_ingredient_id INT NULL,
		            FOREIGN KEY (base_ingredient_id) REFERENCES ingredients (ingredient_id) ON DELETE SET NULL
		        )
		        """, // Required in recipes, admin managed
		        """
		        CREATE TABLE IF NOT EXISTS dietary_restrictions (
				    dietary_restriction_id INT PRIMARY KEY AUTO_INCREMENT,
				    dietary_restriction_name VARCHAR(100) NOT NULL UNIQUE,
				    dietary_restriction_description TEXT NOT NULL,
				    base_dietary_restriction_id INT NULL,
				    FOREIGN KEY (base_dietary_restriction_id) REFERENCES dietary_restrictions (dietary_restriction_id) ON DELETE SET NULL
				)
		        """, // Calculated using ingredients
		        """
		        CREATE TABLE IF NOT EXISTS cuisines (
		            cuisine_id INT PRIMARY KEY AUTO_INCREMENT,
		            cuisine_name VARCHAR(100) NOT NULL UNIQUE,
		            cuisine_description TEXT NOT NULL,
		            base_cuisine_id INT NULL,
		        	FOREIGN KEY (base_cuisine_id) REFERENCES cuisines (cuisine_id) ON DELETE SET NULL
		        )
		        """, // User input, AI predict based on ingredients / instructions, or blank
		        """
		        CREATE TABLE IF NOT EXISTS dish_types (
				    dish_type_id INT PRIMARY KEY AUTO_INCREMENT,
				    dish_type_name VARCHAR(100) NOT NULL UNIQUE,
				    dish_type_description TEXT NOT NULL,
				    base_dish_type_id INT NULL,
				    FOREIGN KEY (base_dish_type_id) REFERENCES dish_types (dish_type_id) ON DELETE SET NULL
				)
		        """, // User, AI predict based on ingredients / instructions
		        """
		        CREATE TABLE IF NOT EXISTS users (
		        	user_id INT PRIMARY KEY AUTO_INCREMENT,
		        	user_name VARCHAR(100) NOT NULL UNIQUE,
		        	user_image_path VARCHAR(500) NULL,
		        	user_description TEXT NULL,
		        	user_active_timestamp TIMESTAMP NOT NULL DEFAULT (UTC_TIMESTAMP),
		        	user_remove_timestamp TIMESTAMP NULL,
		        	user_status ENUM('Active', 'Deleted', 'Banned') NOT NULL DEFAULT 'Active',
		        	role ENUM('User', 'Moderator', 'Admin') NOT NULL,
		        	timezone VARCHAR(30) NOT NULL,
		            password_hash VARCHAR(254) NOT NULL,
		        	email VARCHAR(254) NOT NULL UNIQUE
		        )
		        """, // Soft delete only
		        """
		        CREATE TABLE IF NOT EXISTS recipes (
		            recipe_id INT PRIMARY KEY AUTO_INCREMENT,
		            recipe_name VARCHAR(100) NOT NULL,
		            recipe_image_path VARCHAR(500) NULL,
		            recipe_description TEXT NULL,
		            recipe_active_timestamp TIMESTAMP NOT NULL DEFAULT (UTC_TIMESTAMP),
		            recipe_remove_timestamp TIMESTAMP NULL,
		            recipe_status ENUM('Active', 'Deleted', 'Banned') NOT NULL DEFAULT 'Active',
		            instructions TEXT NOT NULL,
		            total_minutes INT NULL,
		            course ENUM('Main Course', 'Appetizer / Side', 'Dessert') NULL,
		            ai_gen_description BOOLEAN NOT NULL,
				    ai_gen_appliances BOOLEAN NOT NULL,
				    ai_gen_cuisines BOOLEAN NOT NULL,
				    ai_gen_dish_types BOOLEAN NOT NULL,
				    ai_gen_total_minutes BOOLEAN NOT NULL,
		            ai_gen_course BOOLEAN NOT NULL,
		            author_id INT NULL,
		            FOREIGN KEY (author_id) REFERENCES users (user_id)
		        )
		        """, // Hard delete after 30 days or appeal rejection, anonymize after 30 days if user delete and choose this option 
		        """
		        CREATE TABLE IF NOT EXISTS reviews (
		            review_id INT PRIMARY KEY AUTO_INCREMENT,
		            review_name VARCHAR(100) NOT NULL,
		            review_image_path VARCHAR(500) NULL,
		            review_description TEXT NOT NULL,
		            review_active_timestamp TIMESTAMP NOT NULL DEFAULT (UTC_TIMESTAMP),
		            review_remove_timestamp TIMESTAMP NULL,
		            review_status ENUM('Active', 'Deleted', 'Banned') NOT NULL DEFAULT 'Active',
		            rating INT NOT NULL,
		            recipe_id INT NOT NULL,
		            reviewer_id INT NULL,
		            FOREIGN KEY (recipe_id) REFERENCES recipes (recipe_id) ON DELETE CASCADE,
		        	FOREIGN KEY (reviewer_id) REFERENCES users (user_id),
		        	UNIQUE (recipe_id, reviewer_id)
		        )
		        """, // Hard delete after 30 days or appeal rejection, anonymize after 30 days if user delete and choose this option
		        """
		        CREATE TABLE IF NOT EXISTS reports (
				    report_id INT PRIMARY KEY AUTO_INCREMENT,
				    report_description TEXT NULL,
				    report_active_timestamp TIMESTAMP NOT NULL DEFAULT (UTC_TIMESTAMP),
				    report_status ENUM('Active', 'Approved', 'Dismissed') NOT NULL DEFAULT 'Active',
				    reason ENUM('Spam', 'Inappropriate Content', 'Malicious Links', 'Copyright', 'Misinformation', 'Harassment', 'Scam', 'Health Concerns') NOT NULL,
				    rejection_description TEXT NULL,
				    snapshot_name VARCHAR(100) NOT NULL,
				    snapshot_image_path VARCHAR(500) NULL,
				    snapshot_description TEXT NULL,
				    snapshot_active_timestamp TIMESTAMP NOT NULL,
				    accused_id INT NOT NULL,
				    reporter_id INT NOT NULL,
				    moderator_id INT NULL,
				    FOREIGN KEY (accused_id) REFERENCES users (user_id),
				    FOREIGN KEY (reporter_id) REFERENCES users (user_id),
				    FOREIGN KEY (moderator_id) REFERENCES users (user_id)
				)		
		        """, // Never deleted unless spam
		        """
		        CREATE TABLE IF NOT EXISTS recipe_reports (
				    snapshot_recipe_id INT NOT NULL,
				    snapshot_appliances TEXT NULL,
				    snapshot_ingredients TEXT NOT NULL,
				    snapshot_cuisines TEXT NULL,
				    snapshot_dish_types TEXT NOT NULL,
				    snapshot_instructions TEXT NOT NULL,
				    snapshot_total_minutes INT NULL,
				    snapshot_course ENUM('Main Course', 'Appetizer / Side', 'Dessert') NULL,
				    snapshot_ai_gen_description BOOLEAN NOT NULL,
				    snapshot_ai_gen_appliances BOOLEAN NOT NULL,
				    snapshot_ai_gen_cuisines BOOLEAN NOT NULL,
				    snapshot_ai_gen_dish_types BOOLEAN NOT NULL,
				    snapshot_ai_gen_total_minutes BOOLEAN NOT NULL,
		            snapshot_ai_gen_course BOOLEAN NOT NULL,
		            report_id INT PRIMARY KEY,
				    FOREIGN KEY (report_id) REFERENCES reports (report_id)
				)
				""", // Subclass of reports
				"""
				CREATE TABLE IF NOT EXISTS review_reports (
				    snapshot_review_id INT NOT NULL,
		            snapshot_rating INT NOT NULL,
		            snapshot_recipe_id INT NOT NULL,
		            report_id INT PRIMARY KEY,
				    FOREIGN KEY (report_id) REFERENCES reports (report_id)
				)	
				""", // Subclass of reports
		        """
			    CREATE TABLE IF NOT EXISTS violations (
			        violation_id INT PRIMARY KEY AUTO_INCREMENT,
			        violation_description TEXT NOT NULL,
			        violation_active_timestamp TIMESTAMP NOT NULL DEFAULT (UTC_TIMESTAMP),
			        violation_status ENUM('Active', 'Expired', 'Reversed', 'Permanent') NOT NULL,
				    severity ENUM('Minor', 'Moderate', 'Severe') NOT NULL,
				    suspension_status ENUM('Review', 'Recipe', 'All', 'N/A'),
					expire_timestamp TIMESTAMP NULL,
				    violator_id INT NOT NULL,
				    report_id INT NULL UNIQUE,
				    FOREIGN KEY (violator_id) REFERENCES users (user_id),
				    FOREIGN KEY (report_id) REFERENCES reports (report_id)
				)
		        """, // Never deleted
		        """
		        CREATE TABLE IF NOT EXISTS appeals (
		        	appeal_id INT PRIMARY KEY AUTO_INCREMENT,
		        	appeal_description TEXT NOT NULL,
		        	appeal_active_timestamp TIMESTAMP NOT NULL DEFAULT (UTC_TIMESTAMP),
		        	appeal_status ENUM ('Active', 'Approved', 'Dismissed') NOT NULL DEFAULT 'Active',
		        	decision_description TEXT NULL,
		        	resolved_timestamp TIMESTAMP NULL,
		        	auditor_id INT NULL,
				    violation_id INT NOT NULL UNIQUE,
				    FOREIGN KEY (auditor_id) REFERENCES users (user_id),
				    FOREIGN KEY (violation_id) REFERENCES violations (violation_id)
		        )
		        """ // Never deleted
		};
		
		executeAll(codeblocks);
	}
	
	public void createRelationshipSets() throws SQLException {
		String[] codeblocks = {
		        """
		        CREATE TABLE IF NOT EXISTS appliances (
		        	appliance_id INT PRIMARY KEY AUTO_INCREMENT,
		        	appliance_name VARCHAR(100) NOT NULL UNIQUE,
		        	appliance_image_path VARCHAR(500) NOT NULL,
		        	appliance_description TEXT NOT NULL
		        )
		        """, // Not required in recipes, admin managed
		        """
		        CREATE TABLE IF NOT EXISTS ingredients (
		            ingredient_id INT PRIMARY KEY AUTO_INCREMENT,
		            ingredient_name VARCHAR(100) NOT NULL UNIQUE,
		            ingredient_image_path VARCHAR(500) NOT NULL,
		            ingredient_description TEXT NOT NULL,
		         	base_ingredient_id INT NULL,
		            FOREIGN KEY (base_ingredient_id) REFERENCES ingredients (ingredient_id) ON DELETE SET NULL
		        )
		        """, // Required in recipes, admin managed
		        """
		        CREATE TABLE IF NOT EXISTS dietary_restrictions (
				    dietary_restriction_id INT PRIMARY KEY AUTO_INCREMENT,
				    dietary_restriction_name VARCHAR(100) NOT NULL UNIQUE,
				    dietary_restriction_description TEXT NOT NULL,
				    base_dietary_restriction_id INT NULL,
				    FOREIGN KEY (base_dietary_restriction_id) REFERENCES dietary_restrictions (dietary_restriction_id) ON DELETE SET NULL
				)
		        """, // Calculated using ingredients
		        """
		        CREATE TABLE IF NOT EXISTS cuisines (
		            cuisine_id INT PRIMARY KEY AUTO_INCREMENT,
		            cuisine_name VARCHAR(100) NOT NULL UNIQUE,
		            cuisine_description TEXT NOT NULL,
		            base_cuisine_id INT NULL,
		        	FOREIGN KEY (base_cuisine_id) REFERENCES cuisines (cuisine_id) ON DELETE SET NULL
		        )
		        """, // User input, AI predict based on ingredients / instructions, or blank
		        """
		        CREATE TABLE IF NOT EXISTS dish_types (
				    dish_type_id INT PRIMARY KEY AUTO_INCREMENT,
				    dish_type_name VARCHAR(100) NOT NULL UNIQUE,
				    dish_type_description TEXT NOT NULL,
				    base_dish_type_id INT NULL,
				    FOREIGN KEY (base_dish_type_id) REFERENCES dish_types (dish_type_id) ON DELETE SET NULL
				)
		        """, // User, AI predict based on ingredients / instructions
		        """
		        CREATE TABLE IF NOT EXISTS users (
		        	user_id INT PRIMARY KEY AUTO_INCREMENT,
		        	user_name VARCHAR(100) NOT NULL UNIQUE,
		        	user_image_path VARCHAR(500) NULL,
		        	user_description TEXT NULL,
		        	user_active_timestamp TIMESTAMP NOT NULL DEFAULT (UTC_TIMESTAMP),
		        	user_remove_timestamp TIMESTAMP NULL,
		        	user_status ENUM('Active', 'Deleted', 'Banned') NOT NULL DEFAULT 'Active',
		        	role ENUM('User', 'Moderator', 'Admin') NOT NULL,
		        	timezone VARCHAR(30) NOT NULL,
		            password_hash VARCHAR(254) NOT NULL,
		        	email VARCHAR(254) NOT NULL UNIQUE
		        )
		        """, // Soft delete only
		        """
		        CREATE TABLE IF NOT EXISTS recipes (
		            recipe_id INT PRIMARY KEY AUTO_INCREMENT,
		            recipe_name VARCHAR(100) NOT NULL,
		            recipe_image_path VARCHAR(500) NULL,
		            recipe_description TEXT NULL,
		            recipe_active_timestamp TIMESTAMP NOT NULL DEFAULT (UTC_TIMESTAMP),
		            recipe_remove_timestamp TIMESTAMP NULL,
		            recipe_status ENUM('Active', 'Deleted', 'Banned') NOT NULL DEFAULT 'Active',
		            instructions TEXT NOT NULL,
		            total_minutes INT NULL,
		            course ENUM('Main Course', 'Appetizer / Side', 'Dessert') NULL,
		            ai_gen_description BOOLEAN NOT NULL,
				    ai_gen_appliances BOOLEAN NOT NULL,
				    ai_gen_cuisines BOOLEAN NOT NULL,
				    ai_gen_dish_types BOOLEAN NOT NULL,
				    ai_gen_total_minutes BOOLEAN NOT NULL,
		            ai_gen_course BOOLEAN NOT NULL,
		            author_id INT NULL,
		            FOREIGN KEY (author_id) REFERENCES users (user_id)
		        )
		        """, // Hard delete after 30 days or appeal rejection, anonymize after 30 days if user delete and choose this option 
		        """
		        CREATE TABLE IF NOT EXISTS reviews (
		            review_id INT PRIMARY KEY AUTO_INCREMENT,
		            review_name VARCHAR(100) NOT NULL,
		            review_image_path VARCHAR(500) NULL,
		            review_description TEXT NOT NULL,
		            review_active_timestamp TIMESTAMP NOT NULL DEFAULT (UTC_TIMESTAMP),
		            review_remove_timestamp TIMESTAMP NULL,
		            review_status ENUM('Active', 'Deleted', 'Banned') NOT NULL DEFAULT 'Active',
		            rating INT NOT NULL,
		            recipe_id INT NOT NULL,
		            reviewer_id INT NULL,
		            FOREIGN KEY (recipe_id) REFERENCES recipes (recipe_id) ON DELETE CASCADE,
		        	FOREIGN KEY (reviewer_id) REFERENCES users (user_id),
		        	UNIQUE (recipe_id, reviewer_id)
		        )
		        """, // Hard delete after 30 days or appeal rejection, anonymize after 30 days if user delete and choose this option
		        """
		        CREATE TABLE IF NOT EXISTS reports (
				    report_id INT PRIMARY KEY AUTO_INCREMENT,
				    report_description TEXT NULL,
				    report_active_timestamp TIMESTAMP NOT NULL DEFAULT (UTC_TIMESTAMP),
				    report_status ENUM('Active', 'Approved', 'Dismissed') NOT NULL DEFAULT 'Active',
				    reason ENUM('Spam', 'Inappropriate Content', 'Malicious Links', 'Copyright', 'Misinformation', 'Harassment', 'Scam', 'Health Concerns') NOT NULL,
				    rejection_description TEXT NULL,
				    snapshot_name VARCHAR(100) NOT NULL,
				    snapshot_image_path VARCHAR(500) NULL,
				    snapshot_description TEXT NULL,
				    snapshot_active_timestamp TIMESTAMP NOT NULL,
				    accused_id INT NOT NULL,
				    reporter_id INT NOT NULL,
				    moderator_id INT NULL,
				    FOREIGN KEY (accused_id) REFERENCES users (user_id),
				    FOREIGN KEY (reporter_id) REFERENCES users (user_id),
				    FOREIGN KEY (moderator_id) REFERENCES users (user_id)
				)		
		        """, // Never deleted unless spam
		        """
		        CREATE TABLE IF NOT EXISTS recipe_reports (
				    snapshot_recipe_id INT NOT NULL,
				    snapshot_appliances TEXT NULL,
				    snapshot_ingredients TEXT NOT NULL,
				    snapshot_cuisines TEXT NULL,
				    snapshot_dish_types TEXT NOT NULL,
				    snapshot_instructions TEXT NOT NULL,
				    snapshot_total_minutes INT NULL,
				    snapshot_course ENUM('Main Course', 'Appetizer / Side', 'Dessert') NULL,
				    snapshot_ai_gen_description BOOLEAN NOT NULL,
				    snapshot_ai_gen_appliances BOOLEAN NOT NULL,
				    snapshot_ai_gen_cuisines BOOLEAN NOT NULL,
				    snapshot_ai_gen_dish_types BOOLEAN NOT NULL,
				    snapshot_ai_gen_total_minutes BOOLEAN NOT NULL,
		            snapshot_ai_gen_course BOOLEAN NOT NULL,
		            report_id INT PRIMARY KEY,
				    FOREIGN KEY (report_id) REFERENCES reports (report_id)
				)
				""", // Subclass of reports
				"""
				CREATE TABLE IF NOT EXISTS review_reports (
				    snapshot_review_id INT NOT NULL,
		            snapshot_rating INT NOT NULL,
		            snapshot_recipe_id INT NOT NULL,
		            report_id INT PRIMARY KEY,
				    FOREIGN KEY (report_id) REFERENCES reports (report_id)
				)	
				""", // Subclass of reports
		        """
			    CREATE TABLE IF NOT EXISTS violations (
			        violation_id INT PRIMARY KEY AUTO_INCREMENT,
			        violation_description TEXT NOT NULL,
			        violation_active_timestamp TIMESTAMP NOT NULL DEFAULT (UTC_TIMESTAMP),
			        violation_status ENUM('Active', 'Expired', 'Reversed', 'Permanent') NOT NULL,
				    severity ENUM('Minor', 'Moderate', 'Severe') NOT NULL,
				    suspension_status ENUM('Review', 'Recipe', 'All', 'N/A'),
					expire_timestamp TIMESTAMP NULL,
				    violator_id INT NOT NULL,
				    report_id INT NULL UNIQUE,
				    FOREIGN KEY (violator_id) REFERENCES users (user_id),
				    FOREIGN KEY (report_id) REFERENCES reports (report_id)
				)
		        """, // Never deleted
		        """
		        CREATE TABLE IF NOT EXISTS appeals (
		        	appeal_id INT PRIMARY KEY AUTO_INCREMENT,
		        	appeal_description TEXT NOT NULL,
		        	appeal_active_timestamp TIMESTAMP NOT NULL DEFAULT (UTC_TIMESTAMP),
		        	appeal_status ENUM ('Active', 'Approved', 'Dismissed') NOT NULL DEFAULT 'Active',
		        	decision_description TEXT NULL,
		        	resolved_timestamp TIMESTAMP NULL,
		        	auditor_id INT NULL,
				    violation_id INT NOT NULL UNIQUE,
				    FOREIGN KEY (auditor_id) REFERENCES users (user_id),
				    FOREIGN KEY (violation_id) REFERENCES violations (violation_id)
		        )
		        """ // Never deleted
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