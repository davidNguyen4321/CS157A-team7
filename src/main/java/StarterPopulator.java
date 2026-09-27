import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.sql.Connection;
import java.sql.ResultSet;
import java.text.MessageFormat;

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
		populateReports();
		populateViolations();
	}
	
	public void populateRelationships() throws SQLException {
		// To be implemented
	}
	
	public void populateAppliances() throws SQLException {
		String[] codeblocks = {
				formatAppliance("pizza oven", "images/appliances/pizza_oven.webp", "An oven that is specially suited for making pizzas, especially Neapolitan pizza."),
				formatAppliance("kitchen scale", "images/appliances/kitchenscale.avif", "A device used to measure the mass/weight of ingredients."),
				formatAppliance("stick blender", "images/appliances/stickblender.webp", "Hand-held blade grinder commonly used to purée food.")
		};
		starter.executeAll(codeblocks);

	}
	
	public void populateCuisines() throws SQLException {
		addCuisine("mediterranean", "Cuisine from the Mediterranean basin, commonly using olives, wheat, and grapes.", null);
		addCuisine("italian", "Famous for its simplicity, and even more popular in kitchens worldwide.", "mediterranean");
		addCuisine("american", "Cuisine prepared in the United States, known for its mixing of various other cuisines.", null);
	}
	public void populateDietaryRestrictions() throws SQLException {
		addDietaryRestriction("vegan", "Does not contain meat or animal products.", null);
		addDietaryRestriction("vegetarian", "Does not contain meat.", null);
		addDietaryRestriction("gluten-free", "Does not contain the protein gluten.", null);
		addDietaryRestriction("dairy-free", "Does not contain milk or food made from milk.", null);
		addDietaryRestriction("pescetarian", "Does not contain any meat, except fish", null);
		addDietaryRestriction("shellfish-free", "Does not contain shellfish", null);
		addDietaryRestriction("paleo", "Diet based on foods humans may have eaten during the Paleolithic Era", null);
	}
	
	public void populateDishTypes() throws SQLException {
		addDishType("salad", "A dish containing mixed ingredients", null);
		addDishType("fruit salad", "A dish containing mixed fruit", "salad");
		addDishType("bread", "Baked food product from water, flour, and often yeast.", null);
		addDishType("flatbread", "A type of bread where the dough is rolled flat.", "bread");
		addDishType("pizza", "Italian dish, typically with a flatbread base, covered with tomato sauce, and topped by cheese.", "flatbread");
	}
	
	
	public void populateReviews() throws SQLException {
		String[] codeblocks = {
				 formatReview("images/reviews/Sunshine-Fruit-Salad-review.jpg", "I loved this! I drizzled some honey, and it was the best thing ever!", 5, 1, "Tom")
		};
		starter.executeAll(codeblocks);
	}
	
	public void populateUsers() throws SQLException {
		String[] codeblocks = {
				formatUser("Bob", "images/avatars/Bob.jpg", "Professional chef with 10 years of experience", "UTC-2", "ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad", "Bob@gmail.com", null),
				formatUser("Tom", null, null, "UTC+8", "1c45f6c7abc8cb3d379ed1cad8344a40aa4a5e8a2f615a480580160b33d0fd33", null, "+1 1234567890"),
				formatUser("UpToNoGood", "images/avatars/evilhacker.webp", null, "UTC+8", "54208633d444549e359b09c53e3496b32d57974ab566d16400ef6e32f096bde4", "malicious@gmail.com", null)

		};
		starter.executeAll(codeblocks);
	}
	
	public void populateIngredients() throws SQLException {
		String[] restrictions = {"vegatarian", "vegan", "gluten-free", "dairy-free", "pescetarian", "shellfish-free", "palaeo"};
		addIngredient("pineapple", "images/ingredients/Pineapple.webp", "Tropical fruit with a tough, spiky skin.", null, restrictions);
		addIngredient("orange", "images/ingredients/Ambersweet_oranges.jpg", "Citric berry known for its distinctive color.", null, restrictions);
		addIngredient("mandarin orange", "images/ingredients/mandarinorange.jpg", "Small and oblate species of orange", "orange", restrictions);
		addIngredient("strawberry", "images/ingredients/Strawberries.jpg", "Sweet summer aggregate accessory fruit.", null, restrictions);
		addIngredient("banana", "images/ingredients/bananas.avif", "Elongated, yellow fruit.", null, restrictions);
		addIngredient("blueberry", "images/ingredients/blueberries.avif", "Small, round, dark blue berries.", null, restrictions);
		addIngredient("tomato", "images/ingredients/tomato.webp", "Vibrant fruit popular in many dishes.", null, restrictions);
		addIngredient("tomato puree", "images/ingredients/fresh-tomato-puree.jpg", "Thick liquid made by cooking and straining tomatoes.", null, restrictions);
		addIngredient("basil", "images/ingredients/basil.jpg", "Popular, fragant leafy green used as a culinary herb.", null, restrictions);
		addIngredient("black pepper", "images/ingredients/Black-Pepper.jpg", "Dark brown/black and wrinkled, lighter when ground.", null, restrictions);
		addIngredient("yeast", "images/ingredients/yeast.jpg", "Used to cause fermentation and leavening, such as in baking.", null, restrictions);
		addIngredient("plum tomato", "images/ingredients/plum-tomato.jpg", "Generally oval or cylindrical in shape, bred for sauce and packing.", "tomato", restrictions);
		addIngredient("san marzano tomato", "images/ingredients/San-Marzano-Tomatoes.jpg", "Thicker flesh, fewer seeds, and lower water content than other plum tomato varieties.", "plum tomato", restrictions);
		addIngredient("water", "images/ingredients/tap-water.jpg", "Frequently used for boiling, steaming, and simmering.", null, restrictions);

		
		restrictions = new String[]{"vegatarian", "vegan", "gluten-free", "dairy-free", "pescetarian", "shellfish-free"};
		addIngredient("salt", "images/ingredients/salt.webp", "Fine and refined mineral for seasoning foods.", null, restrictions);
		addIngredient("olive oil", "images/ingredients/olive-oil.jpg", "A cooking oil used for frying foods, as a condiment, and salad dressing.", null, restrictions);
		addIngredient("virgin olive oil", "images/ingredients/virginoliveoil.webp", "While slight sensory defects are permitted, virgin olive oil must have a free acidity less than 2.0%.", "olive oil", restrictions);
		addIngredient("extra virgin olive oil", "images/ingredients/virginoliveoil.webp", "Must be free of sensory defects, exhibit positive fruity characteristics, and have a free acidity less than 0.8%.", "virgin olive oil", restrictions);
		
		restrictions = new String[]{"vegatarian", "vegan", "dairy-free", "pescetarian", "shellfish-free"};
		addIngredient("flour", "images/ingredients/All-Purpose_Flour.jpg", "Fine white powder commonly used for baking.", null, restrictions);

		restrictions = new String[]{"vegatarian", "pescetarian", "shellfish-free"};
		addIngredient("cheese", "images/ingredients/sharp-cheddar-baby-swiss.webp", "Dairy product from curdling milk.", null, restrictions);
		addIngredient("mozzarella", "images/ingredients/mozzarella.jpg", "Semi-soft non-aged cheese prepared using the pasta filata method.", "cheese", restrictions);
		addIngredient("parmesan", "images/ingredients/parmesan.jpg", "The \"King of Cheese,\" Parmigiano-Reggiano, Parmesan is an italian hard, granular cheese.", "cheese", restrictions);
		addIngredient("vanilla pudding", "images/ingredients/vanilla-pudding.webp", "Rich, silky custard with its titular flavor", null, restrictions);
	}
	
	public void populateRecipes() throws SQLException {
		AddRecipe addrecipe = new AddRecipe(connection, this);
		String recipename = "Sunshine Fruit Salad";
		String recipeimagepath = "images/recipes/Sunshine-Fruit-Salad.jpg";
		String recipedescription = "A delicious side dish for brunch!";
		String instructions = "1. Cut pineapples and bananas.\n"
				+ "2. Peel oranges.\n"
				+ "3. Mix pineapples, oranges, strawberries and pudding.\n"
				+ "4. Keep refrigerated until served.\n"
				+ "5. Add bananas and blueberries"; // bananas and blueberries are a little more fragile
		Integer totalminutes = 30; // minutes
		boolean minutesaigenerated = false;
		String course = "Dessert";
		String author = "Bob";
		String[] cuisines = {"American"};
		boolean cuisineaigenerated = false;
		String[] appliances = new String[]{};
		String[] dishtypes = {"fruit salad"};
		boolean dishtypesaigenerated = false;
		ArrayList<RecipeIngredient> ingredients = new ArrayList<RecipeIngredient>();
		ingredients.add(new RecipeIngredient("pineapple", 20, "oz"));
		ingredients.add(new RecipeIngredient("madarin orange", 11, "oz"));
		ingredients.add(new RecipeIngredient("vanilla pudding", 3.4, "oz"));
		ingredients.add(new RecipeIngredient("strawberry", 1, "cup"));
		ingredients.add(new RecipeIngredient("banana", 1, "cup"));
		ingredients.add(new RecipeIngredient("blueberry", 1, "cup"));

		addrecipe.addRecipe(recipename, recipeimagepath, recipedescription, instructions, totalminutes, minutesaigenerated, course, author, cuisines, cuisineaigenerated, appliances, dishtypes, dishtypesaigenerated, ingredients);

		recipename = "Sinister Stew";
		recipeimagepath = "images/recipes/sinisterstew.jpg";
		recipedescription = "You should try it.";
		instructions = "1. Eat it";
		totalminutes = -10;
		minutesaigenerated = false;
		course = "Entrée";
		author = "UpToNoGood";
		cuisines = new String[]{};
		cuisineaigenerated = false;
		appliances = new String[]{};
		ingredients = new ArrayList<RecipeIngredient>();
		dishtypes = new String[]{};
		addrecipe.addRecipe(recipename, recipeimagepath, recipedescription, instructions, totalminutes, minutesaigenerated, course, author, cuisines, cuisineaigenerated, appliances, dishtypes, dishtypesaigenerated, ingredients);
		
		recipename = "Neapolitan Pizza";
		recipeimagepath = "images/recipes/neaopolitan-pizza-authentic.jpg";
		recipedescription = "A more practical, albeit not quite authentic version of the classic Italian dish.";
		instructions = "1. Mix flour, water, salt, and yeast in a large bowl, water first.\n"
				+"2. Cover the bowl with plastic wrap.\n"
				+"3. Leave the dough to rest for an hour.\n"
				+ "4. Remove the dough from the bowl and kneed the dough for five minutes.\n"
				+ "5. Place the dough back in the bowl and leave it for 20 hours.\n"
				+ "6. Divide the dough into 4 equal parts and place each ball into individual balls and cover.\n"
				+ "7. Leave the balls to prove for 5 hours.\n"
				+ "8. Blend";
		totalminutes = 26*60;
		minutesaigenerated = false;
		course = "Entrée";
		author = "Tom";
		cuisines = new String[]{"italian"};
		cuisineaigenerated = false;
		appliances = new String[]{"pizza oven", "kitchen scale", "stick blender"};
		ingredients = new ArrayList<RecipeIngredient>();
		ingredients.add(new RecipeIngredient("flour", 620, "g"));
		ingredients.add(new RecipeIngredient("water", 380, "g"));
		ingredients.add(new RecipeIngredient("salt", 14, "g"));
		ingredients.add(new RecipeIngredient("yeast", 0.3, "g"));
		ingredients.add(new RecipeIngredient("san marzano tomato", 300, "g"));
		ingredients.add(new RecipeIngredient("black pepper", null, null));
		ingredients.add(new RecipeIngredient("mozzarella", 250, "g"));
		ingredients.add(new RecipeIngredient("parmesan", 30, "g"));
		ingredients.add(new RecipeIngredient("extra virgin olive oil", null, null));
		ingredients.add(new RecipeIngredient("basil", null, null));
		dishtypes = new String[]{"pizza"};
		addrecipe.addRecipe(recipename, recipeimagepath, recipedescription, instructions, totalminutes, minutesaigenerated, course, author, cuisines, cuisineaigenerated, appliances, dishtypes, dishtypesaigenerated, ingredients);
	}
	public void populateReports() throws SQLException{
		AddReport addreport = new AddReport(connection, this);
		String reportdescription = "Someone may get seriously hurt if they make this recipe!";
		String contenttype = "Recipe";
		String reason = "Spam";
		int offendingmaterialid = 2; //it is bad practice to hardcode the ids.
		int reportedid = 3;
		int reporterid = 2;
		addreport.addReport(reportdescription, contenttype, reason, offendingmaterialid, offendingmaterialid, reportedid, reporterid);
	}
	
	public void populateViolations() {
		
	}
	
	public String formatAppliance(String appliancename, String applianceimagepath, String appliancedescription) throws SQLException {
		return 	MessageFormat.format(doubleSingleQuote("""
		        INSERT INTO APPLIANCES (APPLIANCE_NAME, APPLIANCE_IMAGE_PATH, APPLIANCE_DESCRIPTION)
	        	VALUES ('{0}', '{1}', '{2}')"""),
	        	appliancename.toLowerCase(), applianceimagepath, appliancedescription);
	}		        
	
	public void addCuisine(String cuisinename, String cuisinedescription, String basecuisinename) throws SQLException {
		Integer basecuisineid = null;
		ArrayList<Integer> basecuisineids = this.starterselector.selectCuisineIds(new String[]{basecuisinename});
		if (basecuisineids.size()>0) { // if cuisine was found
			basecuisineid = basecuisineids.get(0);
		}
		String addcuisine = MessageFormat.format(doubleSingleQuote("""
		        INSERT INTO CUISINES (CUISINE_NAME, CUISINE_DESCRIPTION, BASE_CUISINE_ID)
	        	VALUES ('{0}', '{1}', {2})"""), cuisinename.toLowerCase(), cuisinedescription, basecuisineid);
		execute(addcuisine);
	}
	
	public void addDietaryRestriction(String dietaryrestrictionname, String dietaryrestrictiondescription, String basedietaryrestrictionname) throws SQLException {
		Integer basedietaryrestrictionid = null;
		ArrayList<Integer> basedietaryrestrictionids = this.starterselector.selectDietaryRestrictionIds(new String[]{basedietaryrestrictionname});
		if (basedietaryrestrictionids.size()>0) { // if cuisine was found
			basedietaryrestrictionid = basedietaryrestrictionids.get(0);
		}
		String adddietaryrestriction = MessageFormat.format(doubleSingleQuote("""
		        INSERT INTO DIETARY_RESTRICTIONS (DIETARY_RESTRICTION_NAME, DIETARY_RESTRICTION_DESCRIPTION, BASE_DIETARY_RESTRICTION_ID)
	        	VALUES ('{0}', '{1}', {2})"""), dietaryrestrictionname.toLowerCase(), dietaryrestrictiondescription, basedietaryrestrictionid);
		execute(adddietaryrestriction);
	}
	
	public void addDishType(String dishtypename, String dishtypedescription, String basedishtypename) throws SQLException{
		Integer basedishtypeid = null;
		ArrayList<Integer> basedishtypeids = this.starterselector.selectDietaryRestrictionIds(new String[]{basedishtypename});
		if (basedishtypeids.size()>0) { // if cuisine was found
			basedishtypeid = basedishtypeids.get(0);
		}
		String adddishtype = MessageFormat.format(doubleSingleQuote("""
		        INSERT INTO DISH_TYPES (DISH_TYPE_NAME, DISH_TYPE_DESCRIPTION, BASE_DISH_TYPE_ID)
	        	VALUES ('{0}', '{1}', {2})"""), dishtypename.toLowerCase(), dishtypedescription, basedishtypeid);
		execute(adddishtype);
	}
	
	public String formatReview(String reviewimagepath, String reviewdescription, int rating, int refrecipeid, String reviewer) throws SQLException{ // DO NOT USE SINGLE QUOTES IN THE REVIEW basecuisineids.
		String reviewimagepathcleaned = clean(reviewimagepath);
		int reviewerid = this.starterselector.selectUserIds(new String[]{reviewer}).get(0);
		return MessageFormat.format(doubleSingleQuote("""
		        INSERT INTO REVIEWS (REVIEW_IMAGE_PATH, REVIEW_DESCRIPTION, RATING, REF_RECIPE_ID, REVIEWER_ID)
	        	VALUES ({0}, '{1}', {2}, {3}, {4})"""), reviewimagepathcleaned, reviewdescription, rating, refrecipeid, refrecipeid, reviewerid);
	}
	
	public String formatUser(String username, String userimagepath, String userdescription, String timezone,  String passwordhash, String emailaddress, String phonenumber) throws SQLException {
		String userimagepathcleaned = clean(userimagepath);
		String userdescriptioncleaned = clean(userdescription);
		String emailaddresscleaned = clean(emailaddress);
		String phonenumbercleaned = clean(phonenumber);

		return 	MessageFormat.format(doubleSingleQuote("""
		        INSERT INTO USERS (USER_NAME, USER_IMAGE_PATH, USER_DESCRIPTION, TIMEZONE, PASSWORD_HASH, EMAIL_ADDRESS, PHONE_NUMBER)
	        	VALUES ('{0}', {1}, {2}, '{3}', '{4}', {5}, {6})"""), username, userimagepathcleaned, userdescriptioncleaned, timezone, passwordhash, emailaddresscleaned, phonenumbercleaned);
	}
	
	public void addIngredient(String ingredientname, String ingredientimagepath, String ingredientdescription, String baseingredientname, String[] dietaryrestrictions) throws SQLException {
		int ingredientid = createIngredient(ingredientname, ingredientimagepath, ingredientdescription, baseingredientname);
		ArrayList<Integer> dietaryrestrictionids = this.starterselector.selectDietaryRestrictionIds(dietaryrestrictions);
		ArrayList<String> dietaryrestrictionsrelationships = new ArrayList<String>();
		
		for(int dietaryrestrictionid : dietaryrestrictionids) {
			dietaryrestrictionsrelationships.add(
			MessageFormat.format(doubleSingleQuote("""
			INSERT INTO INGREDIENTS_FIT_DIETARY_RESTRICTIONS (INGREDIENT_ID, DIETARY_RESTRICTION_ID) 
			VALUES ({0}, {1})"""), ingredientid, dietaryrestrictionid));
		}
		
		String[] codeblocks = new String[dietaryrestrictionsrelationships.size()];
		codeblocks = dietaryrestrictionsrelationships.toArray(codeblocks);
		
		starter.executeAll(codeblocks);
	}
	
	public int createIngredient(String ingredientname, String ingredientimagepath, String ingredientdescription, String baseingredientname) throws SQLException {
		Integer baseingredientid = null;
		ArrayList<Integer> baseingredientids = this.starterselector.selectIngredientIds(new String[]{baseingredientname});
		if (baseingredientids.size()>0) {
			baseingredientid = baseingredientids.get(0);
		}
		String addingredient = MessageFormat.format(doubleSingleQuote("""
        INSERT INTO INGREDIENTS (INGREDIENT_NAME, INGREDIENT_IMAGE_PATH, INGREDIENT_DESCRIPTION, BASE_INGREDIENT_ID)
    	VALUES ('{0}', '{1}', '{2}', {3})"""),
    	ingredientname.toLowerCase(), ingredientimagepath, ingredientdescription, baseingredientid);
		String[] key = {"INGREDIENT_ID"};
		return createAndQueryId(addingredient, key);
	}
	
	public void addViolation(String violationdescription, String severity, boolean ispermanent, int violatorid, int reportid) throws SQLException{
		String[] codeblocks = {
				MessageFormat.format(doubleSingleQuote("""
		        INSERT INTO VIOLATIONS (VIOLATION_DESCRIPTION, SEVERITY, IS_PERMANENT, VIOLATOR_ID, REPORT_ID)
		    	VALUES ('{0}', '{1}', {2}, {3}, {4})"""), violationdescription, severity, ispermanent, violatorid, reportid),
				MessageFormat.format(doubleSingleQuote("""
		        UPDATE REPORTS SET REPORT_STATUS='Resolved' WHERE REPORT_ID={0})"""), reportid)
				}; //TODO: Update the offending material and user
		starter.executeAll(codeblocks);
		
	}
	
	public void execute(String query) throws SQLException{//intended for singular executions
		Statement statement = connection.createStatement();
		statement.execute(query);
		statement.close();
	}
	
	public int createAndQueryId(String query, String[] key) throws SQLException{
		int generatedkey = 0; // Default value: 0. ID should never be 0 unless there is no key that matches
		Statement statement = connection.prepareStatement(query, key); // add ingredient
		statement.executeUpdate(query, Statement.RETURN_GENERATED_KEYS);
		ResultSet resultset = statement.getGeneratedKeys();
		
		if(resultset.next()) {
			generatedkey= resultset.getInt(1);
		}
		statement.close();
		resultset.close();
		
		return generatedkey;
	}
	
	public String clean(String string) {
		if (string != null && !string.isEmpty()) {
			return "'"+string+"'";
		}
		return null;
	}
	
	public String doubleSingleQuote(String string) {
		return string.replace("'", "''");
	}
}