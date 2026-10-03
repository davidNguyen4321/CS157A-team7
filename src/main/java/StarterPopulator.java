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
	
	public void populateEntitySets() throws SQLException{
		String[] setpaths = {
				"src/main/database/Appliances.sql",
				"src/main/database/Cuisines.sql",
				"src/main/database/DietaryRestrictions.sql",
				"src/main/database/DishTypes.sql",
				"src/main/database/Users.sql",
				"src/main/database/Ingredients.sql",
				"src/main/database/Recipes.sql",
				"src/main/database/Reviews.sql",
				"src/main/database/Reports.sql",
				"src/main/database/RecipeReports.sql",
				"src/main/database/ReviewReports.sql",
				"src/main/database/Violations.sql",
				"src/main/database/Appeals.sql",
				};
		for (String setpath : setpaths) {
			SqlReader.rExecute(connection, setpath);
		}
	}
	
	public void populateRelationships() throws SQLException {
		String[] setpaths = {
				"src/main/database/IngredientsFitDietaryRestrictions.sql",
				"src/main/database/RecipesCookIngredients.sql",
				"src/main/database/RecipesHasDishTypes.sql",
				"src/main/database/RecipesFeatureCuisines.sql",
				"src/main/database/RecipesUseAppliances.sql",
				"src/main/database/Follow.sql",
				"src/main/database/Bookmark.sql"
				};
		for (String setpath : setpaths) {
			SqlReader.rExecute(connection, setpath);
		}
	}
	// TODO: Clean up file methods
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
	
	public String clean(Integer integer) {
		if (integer != null) {
			return integer.toString().replace(",", "");
		}
		return null;
	}
	
	public String clean(Double number) {
		if (number != null) {
			return number.toString().replace(",", "");
		}
		return null;
	}
	
	public String doubleSingleQuote(String string) {
		return string.replace("'", "''");
	}
}