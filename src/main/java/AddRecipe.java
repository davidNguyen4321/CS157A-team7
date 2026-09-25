import java.sql.Connection;
import java.sql.Date;
import java.sql.Time;
import java.time.LocalDate;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.text.MessageFormat;

public class AddRecipe {
	Connection connection;
	Starter starter;
	StarterPopulator starterpopulator;
	StarterSelector starterselector;
	
	public AddRecipe(Connection connection, StarterPopulator starterpopulator) {
		this.connection = connection;
		this.starter = starterpopulator.starter;
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
		Integer cuisineid = null;
		ArrayList<Integer> cuisineids = this.starterselector.selectCuisineIds(new String[]{cuisine});
		if (cuisineids.size()>0) {
			cuisineid = cuisineids.get(0);
		}
		
		int recipeid = createRecipe(recipename, cookminutes, description, instructions, course, imagepath, authorid, cuisineid);
		
		addRecipeRelationships(this.starterselector.selectApplianceIds(appliances), "APPLIANCE_ID", recipeid, "RECIPES_APPLIANCES");
		addRecipeRelationships(this.starterselector.selectDishTypeIds(dishtypes), "DISH_TYPE_ID", recipeid, "RECIPES_DISH_TYPES");
		addRecipeRelationships(this.starterselector.selectIngredientIds(ingredients), "INGREDIENT_ID", recipeid, "RECIPES_INGREDIENTS");
	}
	
	public int createRecipe(String recipename, Integer cookminutes, String description, String instructions, String course, String imagepath, Integer authorid, Integer cuisineid) throws SQLException { //TODO: standardize
		String addRecipe = """
        INSERT INTO RECIPES (RECIPE_NAME, COOK_MINUTES, DESCRIPTION, INSTRUCTIONS, COURSE, IMAGE_PATH, AUTHOR_ID, CUISINE_ID)
    	VALUES ('""" + recipename.toLowerCase() + """
    	', """ + cookminutes + """
    	, '""" + description + """
    	', '""" + instructions + """
    	', '""" + course + """
    	', '""" + imagepath + """			
    	', '""" + authorid + """
    	', """ + cuisineid + """
    	)
    	""";
		String[] key = {"ID_RECIPE"};
		return this.starterpopulator.createAndQueryId(addRecipe, key);
	}
	
	public void addRecipeRelationships(ArrayList<Integer> manyids, String manyidcolumnname, int recipeid, String tablename) throws SQLException {
		String[] codeblocks = new String[manyids.size()];
		for (int i = 0; i<manyids.size(); i++) {
			codeblocks[i] = formatRecipeRelationship(manyids.get(i), manyidcolumnname, recipeid, tablename);
		}
		starter.executeAll(codeblocks);

	}
	
	public String formatRecipeRelationship(int manyid, String manyidcolumnname, int recipeid, String tablename) {
		return MessageFormat.format("""
		        INSERT INTO {0} ({1}, RECIPE_ID)
	        	VALUES ({2}, {3})""", tablename, manyidcolumnname, manyid, recipeid);
	}
}
