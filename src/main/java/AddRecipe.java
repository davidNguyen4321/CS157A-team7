import java.sql.Connection;
import java.sql.Date;
import java.time.LocalDate;

public class AddRecipe {
	Connection connection;
	
	public AddRecipe(Connection connection) {
		this.connection = connection;
	}
	
	public void addRecipe() {
		Date today = Date.valueOf(LocalDate.now());
	}
	
	/*
	 * String recipeName, 
	 * int cookingTime, 
	 * String recipeImagePath, 
	 * String Description, 
	 * String Instructions, 
	 * String[] Ingredients, 
	 * String[] cookingAppliances, 
	 * String author, 
	 * String[] nationality, 
	 * String[] dishTypes, 
	 * String imagePath
	 */
	public void createRecipe() {
		// To be implemented
	}
	
	/*
	 * ID_RECIPE INT NOT NULL AUTO_INCREMENT,
	 * RECIPE_NAME VARCHAR(45) NOT NULL,
	 * COOKING_TIME TIME,
	 * PUBLICATION_DATE DATE NOT NULL,
	 * RECIPE_IMAGE_PATH TEXT(100),
	 * DESCRIPTION TEXT(3000),
	 * INSTRUCTIONS TEXT(12000) NOT NULL
	 */
}
