import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
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
			String[] cuisines,
			boolean cuisineaigenerated,
			String[] appliances,
			String[] dishtypes,
			boolean dishtypeaigenerated,
			ArrayList<RecipeIngredient> ingredients
	) throws SQLException {
		int authorid = this.starterselector.selectUserIds(new String[]{author}).get(0);
		int recipeid = createRecipe(recipename, recipeimagepath, recipedescription, instructions, totalminutes, minutesaigenerated, course, authorid);
		
		addRecipeApplianceRelationship(this.starterselector.selectApplianceIds(appliances), recipeid);
		addRecipeDishTypeRelationship(this.starterselector.selectDishTypeIds(dishtypes), recipeid, dishtypeaigenerated);
		addRecipeCuisineRelationship(this.starterselector.selectCuisineIds(cuisines), recipeid, cuisineaigenerated);
		addRecipeIngredientRelationship(ingredients, recipeid);
	}
	
	public int createRecipe(String recipename, String recipeimagepath, String recipedescription, String instructions, Integer totalminutes, boolean minutesaigenerated, String course, Integer authorid) throws SQLException {
		String recipeimagepathcleaned = this.starterpopulator.clean(recipeimagepath);
		String recipedescriptioncleaned = this.starterpopulator.clean(recipedescription);
		String coursecleaned = this.starterpopulator.clean(course);
		
		String addrecipe = MessageFormat.format(this.starterpopulator.doubleSingleQuote("""
        INSERT INTO RECIPES (RECIPE_NAME, RECIPE_IMAGE_PATH, RECIPE_DESCRIPTION, INSTRUCTIONS, TOTAL_MINUTES, MINUTES_AI_GENERATED, COURSE, AUTHOR_ID)
    	VALUES ('{0}', {1}, {2}, '{3}', {4}, {5}, {6}, {7})"""), recipename, recipeimagepathcleaned, recipedescriptioncleaned, instructions, totalminutes, minutesaigenerated, coursecleaned, authorid);
		String[] key = {"ID_RECIPE"};
		return this.starterpopulator.createAndQueryId(addrecipe, key);
	}
	
	public void addRecipeApplianceRelationship(ArrayList<Integer> applianceids, int recipeid) throws SQLException {
		String[] codeblocks = new String[applianceids.size()];
		for (int i = 0; i<applianceids.size(); i++) {
			codeblocks[i] = formatRecipeApplianceRelationship(applianceids.get(i), recipeid);
		}
		starter.executeAll(codeblocks);

	}
	
	public String formatRecipeApplianceRelationship(int applianceid, int recipeid) {
		return MessageFormat.format(starterpopulator.doubleSingleQuote("""
		        INSERT INTO RECIPES_USE_APPLIANCES (APPLIANCE_ID, RECIPE_ID)
	        	VALUES ({0}, {1})"""), applianceid, recipeid);
	}
	
	public void addRecipeDishTypeRelationship(ArrayList<Integer> dishtypeids, int recipeid, boolean dishtypeaigenerated) throws SQLException {
		String[] codeblocks = new String[dishtypeids.size()];
		for (int i = 0; i<dishtypeids.size(); i++) {
			codeblocks[i] = formatRecipeDishTypeRelationship(dishtypeids.get(i), recipeid, dishtypeaigenerated);
		}
		starter.executeAll(codeblocks);

	}
	
	public String formatRecipeDishTypeRelationship(int dishtypeid, int recipeid, boolean dishtypeaigenerated) {
		return MessageFormat.format(starterpopulator.doubleSingleQuote("""
		        INSERT INTO RECIPES_DISH_TYPES (DISH_TYPE_ID, RECIPE_ID, DISH_TYPE_AI_GENERATED)
	        	VALUES ({0}, {1}, {2})"""), dishtypeid, recipeid, dishtypeaigenerated);
	}
	
	public void addRecipeCuisineRelationship(ArrayList<Integer> cuisineids, int recipeid, boolean cuisineaigenerated) throws SQLException {
		String[] codeblocks = new String[cuisineids.size()];
		for (int i = 0; i<cuisineids.size(); i++) {
			codeblocks[i] = formatRecipeDishTypeRelationship(cuisineids.get(i), recipeid, cuisineaigenerated);
		}
		starter.executeAll(codeblocks);

	}
	
	public String formatRecipeCuisineRelationship(int cuisineid, int recipeid, boolean cuisineaigenerated) {
		return MessageFormat.format(starterpopulator.doubleSingleQuote("""
		        INSERT INTO RECIPES_FEATURE_CUISINES (CUISINE_ID, RECIPE_ID, CUISINE_AI_GENERATED)
	        	VALUES ({0}, {1}, {2})"""), cuisineid, recipeid, cuisineaigenerated);
	}
	
	public void addRecipeIngredientRelationship(ArrayList<RecipeIngredient> ingredients, int recipeid) throws SQLException {
		ArrayList<String> codelists = new ArrayList<String>();
		for (RecipeIngredient ingredient : ingredients) {
			ArrayList<Integer> ingredientids = this.starterselector.selectIngredientIds(new String[]{ingredient.name});
			if (ingredientids.size()>0) { // if ingredient id was found
				ingredient.id = ingredientids.get(0);
				codelists.add(formatRecipeIngredientRelationship(ingredient, recipeid));
			}
		}
		
		String[] codeblocks = new String[codelists.size()];
		codeblocks = codelists.toArray(codeblocks);
		
		starter.executeAll(codeblocks);
	}
	
	public String formatRecipeIngredientRelationship(RecipeIngredient ingredient, int recipeid) {
		String amountunitcleaned = this.starterpopulator.clean(ingredient.amountunit);
		return MessageFormat.format(starterpopulator.doubleSingleQuote("""
		        INSERT INTO RECIPES_COOK_INGREDIENTS (INGREDIENT_ID, RECIPE_ID, AMOUNT, AMOUNT_UNIT)
	        	VALUES ({0}, {1}, {2}, {3})"""), ingredient.id, recipeid, ingredient.amount, amountunitcleaned);
	}
}
