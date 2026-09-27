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
			boolean minutesaigenerated,
			String course,
			String author,
			String cuisine,
			String[] appliances,
			String[] dishtypes,
			ArrayList<RecipeIngredient> ingredients
	) throws SQLException {
		
		int authorid = this.starterselector.selectUserIds(new String[]{author}).get(0);
		Integer cuisineid = null;
		ArrayList<Integer> cuisineids = this.starterselector.selectCuisineIds(new String[]{cuisine});
		if (cuisineids.size()>0) {
			cuisineid = cuisineids.get(0);
		}
		
		int recipeid = createRecipe(recipename, recipeimagepath, recipedescription, instructions, totalminutes, minutesaigenerated, course, cuisineid, authorid);
		
		addRecipeRelationshipsBasic(this.starterselector.selectApplianceIds(appliances), "APPLIANCE_ID", recipeid, "RECIPES_APPLIANCES");
		addRecipeRelationshipsBasic(this.starterselector.selectDishTypeIds(dishtypes), "DISH_TYPE_ID", recipeid, "RECIPES_DISH_TYPES");
//		addRecipeRelationshipsBasic(this.starterselector.selectIngredientIds(ingredients), "INGREDIENT_ID", recipeid, "RECIPES_INGREDIENTS");
	}
	
	public int createRecipe(String recipename, String recipeimagepath, String recipedescription, String instructions, Integer totalminutes, boolean minutesaigenerated, String course, Integer cuisineid, Integer authorid) throws SQLException {
		String recipeimagepathcleaned = this.starterpopulator.clean(recipeimagepath);
		String recipedescriptioncleaned = this.starterpopulator.clean(recipedescription);
		String coursecleaned = this.starterpopulator.clean(course);
		
		String addrecipe = MessageFormat.format(this.starterpopulator.doubleSingleQuote("""
        INSERT INTO RECIPES (RECIPE_NAME, RECIPE_IMAGE_PATH, RECIPE_DESCRIPTION, INSTRUCTIONS, TOTAL_MINUTES, MINUTES_AI_GENERATED, COURSE, CUISINE_ID, AUTHOR_ID)
    	VALUES ('{0}', {1}, {2}, '{3}', {4}, {5}, {6}, {7}, {8})"""), recipename, recipeimagepathcleaned, recipedescriptioncleaned, instructions, totalminutes, minutesaigenerated, coursecleaned, cuisineid, authorid);
		String[] key = {"ID_RECIPE"};
		return this.starterpopulator.createAndQueryId(addrecipe, key);
	}
	
	public void addRecipeRelationshipsBasic(ArrayList<Integer> manyids, String manyidcolumnname, int recipeid, String tablename) throws SQLException {
		String[] codeblocks = new String[manyids.size()];
		for (int i = 0; i<manyids.size(); i++) {
			codeblocks[i] = formatRecipeRelationshipBasic(manyids.get(i), manyidcolumnname, recipeid, tablename);
		}
		starter.executeAll(codeblocks);

	}
	
	public String formatRecipeRelationshipBasic(int manyid, String manyidcolumnname, int recipeid, String tablename) {
		return MessageFormat.format(starterpopulator.doubleSingleQuote("""
		        INSERT INTO {0} ({1}, RECIPE_ID)
	        	VALUES ({2}, {3})"""), tablename, manyidcolumnname, manyid, recipeid);
	}
}
