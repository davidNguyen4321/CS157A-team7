import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.sql.Connection;
import java.sql.ResultSet;

public class StarterPopulator {
	Starter starter;
	Connection connection;
	
	public StarterPopulator(Starter starter) {
		this.starter = starter;
		this.connection = starter.connection;
	}
	
	public void populateTables() throws SQLException {
		populateEntitySets();
		populateRelationships();
	}
	
	public void populateEntitySets() throws SQLException {
		populateDietaryRestrictions();
		populateIngredients();
		populateCookingAppliances();
		populateImages();
		populateUsers();
		populateNationality();
		populateDishTypes();
		populateRecipes();
		populateReviews();
	}
	
	public void populateRelationships() throws SQLException {
		// To be implemented
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
	
	public void populateIngredients() throws SQLException {
		String[] restrictions = {"vegatarian", "vegan", "gluten-free", "dairy-free", "pescetarian", "shellfish-free", "palaeo"};
		addIngredient("pineapple", restrictions);
		addIngredient("orange", restrictions);
		addIngredient("strawberry", restrictions);
		addIngredient("banana", restrictions);
		addIngredient("blueberry", restrictions);
		addIngredient("tomato", restrictions);
		addIngredient("tomato puree", restrictions);
		addIngredient("basil", restrictions);
		addIngredient("black pepper", restrictions);
		addIngredient("yeast", restrictions);
		
		restrictions = new String[]{"vegatarian", "vegan", "gluten-free", "dairy-free", "pescetarian", "shellfish-free"};
		addIngredient("salt", restrictions);
		
		restrictions = new String[]{"vegatarian", "vegan", "dairy-free", "pescetarian", "shellfish-free"};
		addIngredient("flour", restrictions);

		restrictions = new String[]{"vegatarian", "pescetarian", "shellfish-free"};
		addIngredient("mozzarella", restrictions);
		addIngredient("parmesan", restrictions);
		addIngredient("vanilla pudding", restrictions);
	}
	
	public void populateCookingAppliances() throws SQLException {
		String[] codeblocks = {
				formatCookingAppliance("pizza oven")
		};
		starter.executeAll(codeblocks);

	}
	
	public void populateImages() throws SQLException {
		String[] codeblocks = {
				formatImage("images/Sunshine-Fruit-Salad.jpg"),
				formatImage("images/Bob.jpg"),
				formatImage("images/neapolitan-pizza-authentic.jpg")
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
	
	public void populateNationality() throws SQLException {
		String[] codeblocks = {
				formatNationality("italian")
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
	
	public void populateRecipes() throws SQLException {
		AddRecipe addrecipe = new AddRecipe(connection, this);
		String recipename = "Sunshine Salad";
		int cookingtime = 30; // minutes
		String recipeimagepath = "images/Sunshine-Fruit-Salad.jpg";
		String description = "A delicious side dish for brunch!";
		String instructions = "1. Cut pineapples and bananas.\n"
				+ "2. Peel oranges.\n"
				+ "3. Mix pineapples, oranges, strawberries and pudding.\n"
				+ "4. Keep refrigerated until served.\n"
				+ "5. Add bananas and blueberries"; // bananas and blueberries are a little more fragile
		String[] ingredients = {"pineapple", "orange", "vanilla pudding", "strawberry", "banana", "blueberry"};
		String[] cookingappliances = null;
		String author = "Bob";
		String[] nationality = null;
		String[] dishtypes = {"salad"};
		addrecipe.addRecipe(recipename, cookingtime, recipeimagepath, description, instructions, ingredients, cookingappliances, author, nationality, dishtypes);
	}
	
	
	public void populateReviews() throws SQLException {
		String[] codeblocks = {
				 formatReview("Delicious", 5),
				 formatReview("Feels like its missing something", 3)
		};
		starter.executeAll(codeblocks);
	}
	
	public String formatCookingAppliance(String cookingappliancename) throws SQLException {
		return 	"""
		        INSERT INTO COOKING_APPLIANCES (COOKING_APPLIANCE_NAME)
	        	VALUES ('""" + cookingappliancename.toLowerCase() + """
	        	')
	        	""";
	}
	
	public String formatImage(String imagepath) throws SQLException {
		return 	"""
		        INSERT INTO IMAGES (IMAGE_PATH)
	        	VALUES ('""" + imagepath + """
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
	
	public String formatNationality(String nationalityname) {
		return """
		        INSERT INTO NATIONALITIES (NATIONALITY_NAME)
	        	VALUES ('""" + nationalityname.toLowerCase() + """
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
	
	public void addIngredient(String ingredientname, String[] dietaryrestrictions) throws SQLException {
		int ingredientId = createIngredient(ingredientname);
		ArrayList<Integer> dietaryRestrictionIds = selectDietaryRestrictions(dietaryrestrictions);
		ArrayList<String> dietaryRestrictionsRelationships = new ArrayList<String>();
		
		for(int dietaryRestrictionId : dietaryRestrictionIds) {
			dietaryRestrictionsRelationships.add(
					"""
			        INSERT INTO INGREDIENTS_DIETARY_RESTRICTIONS (ID_INGREDIENT, ID_DIETARY_RESTRICTION)
		        	VALUES ('""" + ingredientId + """
		        	', '""" + dietaryRestrictionId + """
		        	')
		        	""");
		}
		
		String[] codeblocks = new String[dietaryRestrictionsRelationships.size()];
		codeblocks = dietaryRestrictionsRelationships.toArray(codeblocks);
		
		starter.executeAll(codeblocks);
	}
	
	public int createIngredient(String ingredientname) throws SQLException { //TODO: standardize
		String addIngredient = """
        INSERT INTO INGREDIENTS (INGREDIENT_NAME)
    	VALUES ('""" + ingredientname.toLowerCase() + """
    	')
    	""";
		String[] key = {"ID_INGREDIENT"};
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
	
	public ArrayList<Integer> selectDietaryRestrictions(String[] dietaryrestrictions) throws SQLException{
		Statement statement = connection.createStatement(); // grab dietary restrictions ID
		String selectDietaryRestrictionsId = "SELECT ID_DIETARY_RESTRICTION FROM DIETARY_RESTRICTIONS"
												+ " WHERE DIETARY_RESTRICTION_NAME IN ('" + String.join("', '", dietaryrestrictions) +"')";
		ResultSet resultSet = statement.executeQuery(selectDietaryRestrictionsId);
		ArrayList<Integer> dietaryRestrictionsIds = new ArrayList<Integer>();
		
		while (resultSet.next()) {
			dietaryRestrictionsIds.add(resultSet.getInt("ID_DIETARY_RESTRICTION"));
		}
		
		statement.close();
		resultSet.close();
		return dietaryRestrictionsIds;
	}
}