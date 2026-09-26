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
			String recipeimagepath,
			String recipedescription,
			String instructions,
			Integer totalminutes,
			String whentoeat,
			String course,
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
		
		int recipeid = createRecipe(recipename, recipeimagepath, recipedescription, instructions, totalminutes, whentoeat, course, authorid);
		
		addRecipeRelationships(this.starterselector.selectApplianceIds(appliances), "APPLIANCE_ID", recipeid, "RECIPES_APPLIANCES");
		addRecipeRelationships(this.starterselector.selectDishTypeIds(dishtypes), "DISH_TYPE_ID", recipeid, "RECIPES_DISH_TYPES");
		addRecipeRelationships(this.starterselector.selectIngredientIds(ingredients), "INGREDIENT_ID", recipeid, "RECIPES_INGREDIENTS");
	}
	
	public int createRecipe(String recipename, String recipeimagepath, String recipedescription, String instructions, Integer totalminutes, String whentoeat, String course, Integer authorid) throws SQLException {
		String recipeimagepathcleaned = this.starterpopulator.clean(recipeimagepath);
		String recipedescriptioncleaned = this.starterpopulator.clean(recipedescription);
		String whentoeatcleaned = this.starterpopulator.clean(whentoeat);
		String coursecleaned = this.starterpopulator.clean(course);
		
		String addRecipe = MessageFormat.format(this.starterpopulator.doubleSingleQuote("""
        INSERT INTO RECIPES (RECIPE_NAME, RECIPE_IMAGE_PATH, RECIPE_DESCRIPTION, INSTRUCTIONS, TOTAL_MINUTES, WHEN_TO_EAT, COURSE, AUTHOR_ID)
    	VALUES ('{0}', {1}, {2}, '{3}', {4}, {5}, {6}, {7})"""), recipename, recipeimagepathcleaned, recipedescriptioncleaned, instructions, totalminutes, whentoeatcleaned, coursecleaned, authorid);
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
		return MessageFormat.format(starterpopulator.doubleSingleQuote("""
		        INSERT INTO {0} ({1}, RECIPE_ID)
	        	VALUES ({2}, {3})"""), tablename, manyidcolumnname, manyid, recipeid);
	}
}
