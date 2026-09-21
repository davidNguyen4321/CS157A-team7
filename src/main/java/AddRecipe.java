import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;

public class AddRecipe {
	Connection connection;
	public AddRecipe(Connection connection) {
		this.connection = connection;
	}
	
	public void addRecipe() {
		Date now = new Date();
		java.sql.Date sqldate = new java.sql.Date(now.getTime());
		
	}
	
//	String recipename, int cookingtime, String recipeimagepath, String description, String instructions, String[] ingredients, String[] cookingappliances, String author, String[] nationality, String[] dishtypes, String imagepath
	
	public void createRecipe() {
		
	}
//    ID_RECIPE INT NOT NULL AUTO_INCREMENT,
//    RECIPE_NAME VARCHAR(45) NOT NULL,
//    COOKING_TIME TIME,
//    PUBLICATION_DATE DATE NOT NULL,
//    RECIPE_IMAGE_PATH TEXT(100),
//    DESCRIPTION TEXT(3000),
//    INSTRUCTIONS TEXT(12000) NOT NULL,
}
