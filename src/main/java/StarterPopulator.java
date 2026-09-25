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
		populateReviews();
		populateUsers();

		populateIngredients();
		populateRecipes();
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
		String[] codeblocks = {
				formatCuisine("mediterranean", null),
				formatCuisine("italian", "mediterranean")
		};
		starter.executeAll(codeblocks);
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
				 formatReview("Delicious", 5),
				 formatReview("Feels like its missing something", 3)
		};
		starter.executeAll(codeblocks);
	}
	
	public void populateUsers() throws SQLException {
		String[] codeblocks = {
				formatUser("Bob", "Bob@gmail.com"),
				formatUser("Tom", "Tom@gmail.com")
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
		String cuisine = null;
		String[] appliances = null;
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
	
	public String formatCuisine(String cuisinename, String basecuisinename) throws SQLException {
		int basecuisineid = this.starterselector.selectCuisineIds(new String[]{basecuisinename}).get(0);
		return 	"""
		        INSERT INTO CUISINES (CUISIEN_NAME, BASE_CUISINE_ID)
	        	VALUES ('""" + cuisinename.toLowerCase() + """
	        	', '""" + basecuisineid + """
	        	')
	        	""";
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
	
	public String formatReview(String dishtype, int rating) { // DO NOT USE SINGLE QUOTES IN THE REVIEW TEXT.
		return """
		        INSERT INTO REVIEWS (TEXT, RATING)
	        	VALUES ('""" + dishtype.toLowerCase() + """
	        	', """ + rating + """
	        	)
	        	""";
	}
	
	public String formatUser(String username, String emailaddress) throws SQLException {
		Date now = new Date();
		java.sql.Date sqldate = new java.sql.Date(now.getTime());
		return 	"""
		        INSERT INTO USERS (USER_NAME, EMAIL_ADDRESS, ACCOUNT_CREATION_DATE)
	        	VALUES ('""" + username + """
	        	', '""" + emailaddress + """
	        	', '""" + sqldate + """
	        	')
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
	
	public int createIngredient(String ingredientname, String baseingredientname) throws SQLException { //TODO: standardize
		int baseingredientid = this.starterselector.selectIngredientIds(new String[]{baseingredientname}).get(0);
		String addIngredient = """
        INSERT INTO INGREDIENTS (INGREDIENT_NAME, BASE_INGREDIENT_ID)
    	VALUES ('""" + ingredientname.toLowerCase() + """
    	'""" + baseingredientid + """
    	')
    	""";
		String[] key = {"INGREDIENT_ID"};
		return createAndQueryId(addIngredient, key);
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