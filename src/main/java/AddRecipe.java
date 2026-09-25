import java.sql.Connection;
import java.sql.Date;
import java.sql.Time;
import java.time.LocalDate;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;

public class AddRecipe {
	Connection connection;
	StarterPopulator starterpopulator;
	StarterSelector starterselector;
	
	public AddRecipe(Connection connection, StarterPopulator starterpopulator) {
		this.connection = connection;
		this.starterpopulator = starterpopulator;
		this.starterselector = starterpopulator.starterselector;
	}
	
	public void addRecipe(
			String recipename,
			int cookminutes,
			String description,
			String instructions,
			String course,
			String imagepath,
			String author,
			String cuisine,
			String[] appliances,
			String[] dishtypes,
			String[] ingredients
	) throws SQLException {
		
		int authorid = this.starterselector.selectUserIds(new String[]{author}).get(0);
		int cuisineid = this.starterselector.selectCuisineIds(new String[]{cuisine}).get(0);
		ArrayList<Integer> applianceids = this.starterselector.selectApplianceIds(appliances);
		ArrayList<Integer> dishtypeids = this.starterselector.selectDishTypeIds(dishtypes);
		ArrayList<Integer> ingredientids = this.starterselector.selectIngredientIds(ingredients);
		
		int recipeId = createRecipe(recipename, cookminutes, description, instructions, course, imagepath, authorid, cuisineid);
		// TODO: add relationships
	}
	
	public int createRecipe(String recipename, int cookminutes, String description, String instructions, String course, String imagepath, int authorid, int cuisineid) throws SQLException { //TODO: standardize
		Date today = Date.valueOf(LocalDate.now());
		String addRecipe = """
        INSERT INTO RECIPES (RECIPE_NAME, COOK_MINUTES, DESCRIPTION, INSTRUCTIONS, COURSE, IMAGE_PATH, AUTHOR_ID, CUISINE_ID)
    	VALUES ('""" + recipename.toLowerCase() + """
    	', '""" + cookminutes + """
    	', '""" + description + """
    	', '""" + instructions + """
    	', '""" + course + """
    	', '""" + imagepath + """			
    	', '""" + authorid + """
    	', '""" + cuisineid + """
    	')
    	""";
		String[] key = {"ID_RECIPE"};
		return this.starterpopulator.createAndQueryId(addRecipe, key);
	}
}
