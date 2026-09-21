import java.sql.SQLException;
import java.util.Date;

public class StarterPopulate {
	Starter starter;
	public StarterPopulate(Starter starter) {
		this.starter=starter;
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
		//TODO: fill table
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
		starter.addIngredient("pineapple", restrictions);
		starter.addIngredient("orange", restrictions);
		starter.addIngredient("strawberry", restrictions);
		starter.addIngredient("banana", restrictions);
		starter.addIngredient("blueberry", restrictions);
		starter.addIngredient("tomato", restrictions);
		starter.addIngredient("tomato puree", restrictions);
		starter.addIngredient("basil", restrictions);
		starter.addIngredient("black pepper", restrictions);
		starter.addIngredient("yeast", restrictions);
		
		restrictions = new String[]{"vegatarian", "vegan", "gluten-free", "dairy-free", "pescetarian", "shellfish-free"};
		starter.addIngredient("salt", restrictions);
		
		restrictions = new String[]{"vegatarian", "vegan", "dairy-free", "pescetarian", "shellfish-free"};
		starter.addIngredient("flour", restrictions);

		restrictions = new String[]{"vegatarian", "pescetarian", "shellfish-free"};
		starter.addIngredient("mozzarella", restrictions);
		starter.addIngredient("parmesan", restrictions);
		starter.addIngredient("vanilla pudding", restrictions);
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
				formatNationality("Italian")
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
		String[] codeblocks = {
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
		        INSERT INTO NATIONALITY (NATIONALITY_NAME)
	        	VALUES ('""" + nationalityname.toLowerCase() + """
	        	')
	        	""";
	}
	
	public String formatDishType(String dishtype) {
		return """
		        INSERT INTO DISH_TYPE (DISH_TYPE_NAME)
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
}