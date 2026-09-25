import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.sql.Connection;
import java.sql.ResultSet;

public class StarterPopulator {
	Starter starter;
	StarterSelector starterselector;
	Connection connection;
	
	public StarterPopulator(Starter starter) {
		this.starter = starter;
		this.connection = starter.connection;
		this.starterselector= new StarterSelector(this.connection);
		
	}
	
	public void populateTables() throws SQLException {
		populateEntitySets();
		populateRelationships();
	}
	
	public void populateEntitySets() throws SQLException {
		populateAppliances();
		populateCuisines();
		populateDietaryRestrictions();
		populateDishTypes();
		populateUsers();
		populateIngredients();
		populateRecipes();
		populateReviews();
	}
	
	public void populateRelationships() throws SQLException {
		// To be implemented
	}
	
	public void populateAppliances() throws SQLException {
		String[] codeblocks = {
				formatAppliance("pizza oven")
		};
		starter.executeAll(codeblocks);

	}
	
	public void populateCuisines() throws SQLException {
		addCuisine("mediterranean", null);
		addCuisine("italian", "mediterranean");
	}
	
	public void populateDietaryRestrictions() throws SQLException {
		String[] codeblocks = {
				formatDietaryRestriction("vegan"),
				formatDietaryRestriction("vegetarian"),
				formatDietaryRestriction("gluten-free"),
				formatDietaryRestriction("dairy-free"),
				formatDietaryRestriction("pescetarian"),
				formatDietaryRestriction("shellfish-free"),
				formatDietaryRestriction("paleo")

		};
		starter.executeAll(codeblocks);
	}
	
	public void populateDishTypes() throws SQLException {
		String[] codeblocks = {
				formatDishType("salad"),
				formatDishType("flatbread")
		};
		starter.executeAll(codeblocks);
	}
	
	public void populateReviews() throws SQLException {
		String[] codeblocks = {
				 
		};
		starter.executeAll(codeblocks);
	}
	
	public void populateUsers() throws SQLException {
		String[] codeblocks = {
				formatUser("Bob", "Bob@gmail.com", "ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad", "images/Bob.jpg"),
				formatUser("Tom", "Tom@gmail.com", "1c45f6c7abc8cb3d379ed1cad8344a40aa4a5e8a2f615a480580160b33d0fd33", null)
		};
		starter.executeAll(codeblocks);
	}
	
	public void populateIngredients() throws SQLException {
		String[] restrictions = {"vegatarian", "vegan", "gluten-free", "dairy-free", "pescetarian", "shellfish-free", "palaeo"};
		String[] ingredients = {"pineapple", "orange", "strawberry", "banana", "blueberry", "tomato", "tomato puree", "basil", "black pepper", "yeast"};
		for (String ingredient : ingredients) {
			addIngredient(ingredient, null, restrictions);
		}
		addIngredient("plum tomato", "tomato", restrictions);
		addIngredient("san marzano", "plum tomato", restrictions);
		
		restrictions = new String[]{"vegatarian", "vegan", "gluten-free", "dairy-free", "pescetarian", "shellfish-free"};
		addIngredient("salt", null, restrictions);
		
		restrictions = new String[]{"vegatarian", "vegan", "dairy-free", "pescetarian", "shellfish-free"};
		addIngredient("flour", null, restrictions);

		restrictions = new String[]{"vegatarian", "pescetarian", "shellfish-free"};
		addIngredient("cheese", null, restrictions);
		addIngredient("mozzarella", "cheese", restrictions);
		addIngredient("parmesan", "cheese",restrictions);
		addIngredient("vanilla pudding", null, restrictions);
	}
	
	public void populateRecipes() throws SQLException {
		AddRecipe addrecipe = new AddRecipe(connection, this);
		String recipename = "Sunshine Salad";
		int cookminutes = 30; // minutes
		String description = "A delicious side dish for brunch!";
		String instructions = "1. Cut pineapples and bananas.\n"
				+ "2. Peel oranges.\n"
				+ "3. Mix pineapples, oranges, strawberries and pudding.\n"
				+ "4. Keep refrigerated until served.\n"
				+ "5. Add bananas and blueberries"; // bananas and blueberries are a little more fragile
		String course = "Dessert";
		String imagepath = "images/Sunshine-Fruit-Salad.jpg";
		String author = "Bob";
		String cuisine = "italian";
		String[] appliances = new String[]{};
		String[] ingredients = {"pineapple", "orange", "vanilla pudding", "strawberry", "banana", "blueberry"};
		String[] dishtypes = {"salad"};
		addrecipe.addRecipe(recipename, cookminutes, description, instructions, course, imagepath, author, cuisine, appliances, dishtypes, ingredients);
	}
	
	public String formatAppliance(String appliancename) throws SQLException {
		return 	"""
		        INSERT INTO APPLIANCES (APPLIANCE_NAME)
	        	VALUES ('""" + appliancename.toLowerCase() + """
	        	')
	        	""";
	}
	
	public void addCuisine(String cuisinename, String basecuisinename) throws SQLException {
		Integer basecuisineid = null;
		ArrayList<Integer> basecuisineids = this.starterselector.selectCuisineIds(new String[]{basecuisinename});
		if (basecuisineids.size()>0) { // if cuisine was found
			basecuisineid = basecuisineids.get(0);
		}
		String addCuisine = """
		        INSERT INTO CUISINES (CUISINE_NAME, BASE_CUISINE_ID)
	        	VALUES ('""" + cuisinename.toLowerCase() + """
	        	', """ + basecuisineid + """
	        	)
	        	""";
		execute(addCuisine);
		
	}
	
	public String formatDietaryRestriction(String dietaryrestrictionname) {
		return """
		        INSERT INTO DIETARY_RESTRICTIONS (DIETARY_RESTRICTION_NAME)
	        	VALUES ('""" + dietaryrestrictionname.toLowerCase() + """
	        	')
	        	""";
	}
	
	public String formatDishType(String dishtype) {
		return """
		        INSERT INTO DISH_TYPES (DISH_TYPE_NAME)
	        	VALUES ('""" + dishtype.toLowerCase() + """
	        	')
	        	""";
	}
	
	public String formatReview(int rating, String content, String photopath, int recipeid, String username) throws SQLException{ // DO NOT USE SINGLE QUOTES IN THE REVIEW basecuisineids.
		int userid = this.starterselector.selectUserIds(new String[]{username}).get(0);
		return """
		        INSERT INTO REVIEWS (RATING, CONTENT, PHOTO_PATH, RECIPE_ID, USER_ID)
	        	VALUES (""" + rating + """
	        	, '""" + content.toLowerCase() + """
	        	', '""" + photopath + """
	        	', """ + recipeid + """
	        	, '""" + userid + """
	        	')
	        	""";
	}
	
	public String formatUser(String username, String emailaddress, String passwordhash, String avatarpath) throws SQLException {
		String avatarpathcleaned = null;
		if (avatarpath != null && !avatarpath.isEmpty()) {
			avatarpathcleaned = "'"+avatarpath+"'";
		} 
		return 	"""
		        INSERT INTO USERS (USER_NAME, EMAIL_ADDRESS, PASSWORD_HASH, AVATAR_PATH)
	        	VALUES ('""" + username + """
	        	', '""" + emailaddress + """
	        	', '""" + passwordhash + """
	        	', """ + avatarpathcleaned + """
	        	)
	        	""";
	}
	
	public void addIngredient(String ingredientname, String baseingredientname, String[] dietaryrestrictions) throws SQLException {
		int ingredientId = createIngredient(ingredientname, baseingredientname);
		ArrayList<Integer> dietaryRestrictionIds = this.starterselector.selectDietaryRestrictionIds(dietaryrestrictions);
		ArrayList<String> dietaryRestrictionsRelationships = new ArrayList<String>();
		
		for(int dietaryRestrictionId : dietaryRestrictionIds) {
			dietaryRestrictionsRelationships.add(
					"""
			        INSERT INTO INGREDIENTS_DIETARY_RESTRICTIONS (INGREDIENT_ID, DIETARY_RESTRICTION_ID)
		        	VALUES ('""" + ingredientId + """
		        	', '""" + dietaryRestrictionId + """
		        	')
		        	""");
		}
		
		String[] codeblocks = new String[dietaryRestrictionsRelationships.size()];
		codeblocks = dietaryRestrictionsRelationships.toArray(codeblocks);
		
		starter.executeAll(codeblocks);
	}
	
	public int createIngredient(String ingredientname, String baseingredientname) throws SQLException {
		Integer baseingredientid = null;
		ArrayList<Integer> baseingredientids = this.starterselector.selectIngredientIds(new String[]{baseingredientname});
		if (baseingredientids.size()>0) {
			baseingredientid = baseingredientids.get(0);
		}
		String addIngredient = """
        INSERT INTO INGREDIENTS (INGREDIENT_NAME, BASE_INGREDIENT_ID)
    	VALUES ('""" + ingredientname.toLowerCase() + """
    	', """ + baseingredientid + """
    	)
    	""";
		String[] key = {"INGREDIENT_ID"};
		return createAndQueryId(addIngredient, key);
	}
	
	public void execute(String query) throws SQLException{//intended for singular executions
		Statement statement = connection.createStatement();
		statement.execute(query);
		statement.close();
	}
	
	public int createAndQueryId(String query, String[] key) throws SQLException{
		int generatedKey = 0; // Default value: 0. ID should never be 0 unless there is no key that matches
		Statement statement = connection.prepareStatement(query, key); // add ingredient
		statement.executeUpdate(query, Statement.RETURN_GENERATED_KEYS);
		ResultSet resultSet = statement.getGeneratedKeys();
		
		if(resultSet.next()) {
			generatedKey= resultSet.getInt(1);
		}
		statement.close();
		resultSet.close();
		
		return generatedKey;
	}
}