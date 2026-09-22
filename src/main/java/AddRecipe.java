import java.sql.Connection;
import java.sql.Date;
import java.sql.Time;
import java.time.LocalDate;
import java.sql.SQLException;

public class AddRecipe {
	Connection connection;
	StarterPopulator starterpopulator;
	
	public AddRecipe(Connection connection, StarterPopulator starterpopulator) {
		this.connection = connection;
		this.starterpopulator = starterpopulator;
	}
	
	public void addRecipe(
			String recipename,
			int cookingtime,
			String recipeimagepath,
			String description,
			String instructions,
			String[] ingredients,
			String[] cookingappliances,
			String author,
			String[] nationality,
			String[] dishtypes
	) throws SQLException {
		int recipeId = createRecipe(recipename, cookingtime, recipeimagepath, description, instructions);
		// TODO: add relationships
	}
	
	/*
	 * String recipename, 
	 * Time cookingtime, 
	 * String recipeimagepath, 
	 * String description, 
	 * String instructions, 
	 * String[] ingredients, 
	 * String[] cookingappliances, 
	 * String author, 
	 * String[] nationality, 
	 * String[] dishtypes, 
	 */
	public int createRecipe(String recipename, int cookingtime, String recipeimagepath, String description, String instructions) throws SQLException { //TODO: standardize
		Date today = Date.valueOf(LocalDate.now());
		String addRecipe = """
        INSERT INTO RECIPES (RECIPE_NAME, COOKING_TIME, PUBLICATION_DATE, RECIPE_IMAGE_PATH, DESCRIPTION, INSTRUCTIONS)
    	VALUES ('""" + recipename.toLowerCase() + """
    	', '""" + cookingtime + """
    	', '""" + today + """
    	', '""" + recipeimagepath + """
    	', '""" + description + """
    	', '""" + instructions + """
    	')
    	""";
		String[] key = {"ID_RECIPE"};
		return this.starterpopulator.createAndQueryId(addRecipe, key);
	}
}
