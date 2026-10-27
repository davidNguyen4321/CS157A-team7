package Search;
import java.sql.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.Arrays;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

@WebServlet("/Recipes/Search")
public class RecipeSearch extends HttpServlet {
	private static final long serialVersionUID = 1L; // The compiler warned that I should do this but I don't know why.
	

	@Override //Mandatory get method for fetch requests
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		//verification checks
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		
		HttpSession session = request.getSession(false);
		Integer sessionUserId;
		//assumes that if the userId exists, that it is a valid id
		if(!request.isRequestedSessionIdValid() || session == null || (sessionUserId = (Integer) session.getAttribute("userId")) == null) {
			log("Invalid session.");
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			response.getWriter().write("{}");
			return;
		}
		
		Connection dbConnection = DatabaseAccess.getConnection();
		if(dbConnection==null) {
			log("No dbConnection available.");
			response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
			response.getWriter().write("{}");
			return;
		}
		RecipeSearchRequest requestInfo = readRequest(request, response);
		try{
			requestInfo.filterIdsNotCourses.put("Dietary Restriction",
					recurseBaseElement(
							requestInfo.filterIdsNotCourses.get("Dietary Restriction"),
							"DIETARY_RESTRICTION_ID",
							"BASE_DIETARY_RESTRICTION_ID",
							"DIETARY_RESTRICTIONS",
							dbConnection
							)
					);
			requestInfo.filterIdsNotCourses.put("Cuisine",
					recurseBaseElement(
							requestInfo.filterIdsNotCourses.get("Cuisine"),
							"BASE_CUISINE_ID", //special case where more specific cuisines are permissible but more general cuisines are not necessarily
							"CUISINE_ID",
							"CUISINES",
							dbConnection
							)
					);
			requestInfo.filterIdsNotCourses.put("Dish Type",
					recurseBaseElement(
							requestInfo.filterIdsNotCourses.get("Dish Type"),
							"BASE_DISH_TYPE_ID", //special case where more specific dish types are permissible but more general dish types are not necessarily
							"DISH_TYPE_ID",
							"DISH_TYPES",
							dbConnection
							)
					);
			requestInfo.filterIdsNotCourses.put("Ingredient", 
					recurseBaseIngredient(
							requestInfo.filterIdsNotCourses.get("Ingredient"), 
							requestInfo.filterIdsNotCourses.get("Dietary Restriction"), 
							dbConnection
							)
					);
			searchRecipe(requestInfo, dbConnection, response, sessionUserId);
		}
		catch(Exception ex) {//error checking
			log("Recipe search failed: " + ex.getMessage());
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().write("{}");
			return;
		}
	}
	
	private RecipeSearchRequest readRequest(HttpServletRequest request, HttpServletResponse response) throws IOException{
		//get parameters
		HashMap<String, Object> parameterMap = new HashMap<String, Object>();
		String[] parameters = {"searchTerm", "sortOrder", "sortOptions", "filterIds", "tolerances", "ranges", "options"};
		for (String parameter : parameters) {
			String value = request.getParameter(parameter);
			if(value==null){
				log("No " + parameter + " provided.");
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				response.getWriter().write("{}");
				return null;
			}
			parameterMap.put(parameter, value);
		}
		
		// map
		ObjectMapper mapper = new ObjectMapper();
		String searchTerm = parameterMap.get("searchTerm").toString().trim();
		String[] sortOrder = mapper.readValue(parameterMap.get("sortOrder").toString(), String[].class);
		
		HashMap<String, HashMap<String, String>> sortOptions = mapper.readValue(
				parameterMap.get("sortOptions").toString(),
				new TypeReference<HashMap<String, HashMap<String, String>>>() {});
		
		// filterIds
		HashMap<String, String[]> filterIds = mapper.readValue(
				parameterMap.get("filterIds").toString(),
				new TypeReference<HashMap<String, String[]>>() {});
		HashMap<String, Integer[]> filterIdsNotCourses = new HashMap<String, Integer[]>();
		String[] courses = {}; // placeholder array
		for (HashMap.Entry<String, String[]> set : filterIds.entrySet()) {
			String[] stringArray = set.getValue();
			if(set.getKey().equals("Course")) {//courses is the only non-integer id (because it is not an id, and is a string)
				courses = stringArray;
			}
			else {
				Integer[] idArray = new Integer[stringArray.length];
				for(int i = 0; i<stringArray.length; i++) {
					idArray[i]=Integer.parseInt(stringArray[i]);
				}
				filterIdsNotCourses.put(set.getKey(), idArray);
			}
		}
		
		HashMap<String, Integer> tolerances = mapper.readValue(
				parameterMap.get("tolerances").toString(),
				new TypeReference<HashMap<String, Integer>>() {}
				);
		
		HashMap<String, HashMap<String, Integer>> ranges = mapper.readValue(
				parameterMap.get("ranges").toString(),
				new TypeReference<HashMap<String, HashMap<String, Integer>>>() {}
				);
		
		HashMap<String, Boolean> options = mapper.readValue(
				parameterMap.get("options").toString(),
				new TypeReference<HashMap<String, Boolean>>() {}
				);
		
		return new RecipeSearchRequest(searchTerm,
				sortOrder,
				sortOptions,
				filterIdsNotCourses,
				courses,
				tolerances,
				ranges,
				options);
	}
	
	private class RecipeSearchRequest{
		private String searchTerm;
		private String[] sortOrder;
		private HashMap<String, HashMap<String, String>> sortOptions;
		private HashMap<String, Integer[]> filterIdsNotCourses; //for the sake of not having to parse int every time
		private String[] courses;
		private HashMap<String, Integer> tolerances;
		private HashMap<String, HashMap<String, Integer>> ranges;
		private HashMap<String, Boolean> options;
		
		private RecipeSearchRequest(
				String searchTerm,
				String[] sortOrder,
				HashMap<String, HashMap<String, String>> sortOptions,
				HashMap<String, Integer[]> filterIdsNotCourses,
				String[] courses,
				HashMap<String, Integer> tolerances,
				HashMap<String, HashMap<String, Integer>> ranges,
				HashMap<String, Boolean> options
				){
			this.searchTerm=searchTerm;
			this.sortOrder=sortOrder;
			this.sortOptions=sortOptions;
			this.filterIdsNotCourses=filterIdsNotCourses;
			this.courses=courses;
			this.tolerances=tolerances;
			this.ranges=ranges;
			this.options=options;
		}
	}
	// Made into own query due to recursion
	private Integer[] recurseBaseIngredient(Integer[] providedIngredientIds, Integer[] dietaryRestrictions, Connection dbConnection) throws SQLException{
		if(providedIngredientIds.length==0) { // unfortunately, an empty tuple is not allowed. This exists to guard against that edge case.
			return providedIngredientIds; //vacuously compliant
		}
		
		HashSet<Integer> permissibleProvidedIngredientIds = new HashSet<Integer>();
		ArrayList<Integer> uniqueNewIngredientIds = new ArrayList<Integer>();
		
		//build query
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT DISTINCT i.INGREDIENT_ID, i.BASE_INGREDIENT_ID FROM INGREDIENTS i ");
		sb.append("WHERE i.INGREDIENT_ID IN (");
			for(int i = 0; i < providedIngredientIds.length; i++) {
				sb.append("?, ");
			}
			sb.setLength(sb.length()-2);//remove the last comma and space
		sb.append(") ");
		if(dietaryRestrictions.length>0) {
			sb.append("AND NOT EXISTS ( SELECT 1 FROM INGREDIENTS_VIOLATE_DIETARY_RESTRICTIONS ivds "); //Only have to do one check for compliancy because if the child is compliant, then can substitute parent for child.
				sb.append("WHERE i.INGREDIENT_ID = ivds.INGREDIENT_ID ");
				sb.append("AND ivds.DIETARY_RESTRICTION_ID IN (");
					for(int i = 0; i < dietaryRestrictions.length; i++) {
						sb.append("?, ");
					}
					sb.setLength(sb.length()-2);//remove the last comma and space
			sb.append(") ) ");
		}
		
		//sql connection
		try (PreparedStatement statement = dbConnection.prepareStatement(sb.toString());){
			int counter = 1;
			for(Integer id : providedIngredientIds) {
				statement.setInt(counter++, id);// prevents sql injection
			}
			for(Integer id : dietaryRestrictions) {
				statement.setInt(counter++, id);// prevents sql injection
			}
			//read and store results
    		try(ResultSet ingredients = statement.executeQuery();){
    			HashSet<Integer> newIngredientIds = new HashSet<Integer>(); // specifically to root out non-distinct base_ingredient_ids in the first query
    			while(ingredients.next()) {
    				permissibleProvidedIngredientIds.add(ingredients.getInt("INGREDIENT_ID")); //add results
    				Integer baseIngredientId = ingredients.getInt("BASE_INGREDIENT_ID");
    				if (baseIngredientId!=0) { // 0 is null for integers
    					newIngredientIds.add(baseIngredientId);
    				}
    			}
    			for (Integer baseIngredientId : newIngredientIds) {//checking if the base ingredient is unique
    				if(permissibleProvidedIngredientIds.add(baseIngredientId)) {
    					uniqueNewIngredientIds.add(baseIngredientId);
    				}
    			}
    		}
		}
		
		// if there is an exception, it is thrown, so only states that have not yet encountered an issue will arrive here
		while(!uniqueNewIngredientIds.isEmpty()) { //recursion means that multiple queries are needed
			sb = new StringBuilder();
			sb.append("SELECT DISTINCT BASE_INGREDIENT_ID FROM INGREDIENTS "); // sadly, distinct only applies to the INGREDIENT_ID, BASE_INGREDIENT_ID tuple
			sb.append("WHERE INGREDIENT_ID IN (");
				for(int i = 0; i < uniqueNewIngredientIds.size(); i++) {
					sb.append("?, ");
				}
			sb.setLength(sb.length()-2);//remove the last comma and space
			sb.append(")"); // does not have to check for compliancy because child ingredient (already verified to be compliant or have a compliant descendant) can be used in place of base ingredient.
			try (PreparedStatement statement = dbConnection.prepareStatement(sb.toString());){
				int counter = 1;
				for(Integer id : uniqueNewIngredientIds) {
					statement.setInt(counter++, id);// prevents sql injection
				}
	    		try(ResultSet ingredients = statement.executeQuery();){
	    			uniqueNewIngredientIds = new ArrayList<Integer>();
	    			while(ingredients.next()) {
	    				Integer baseIngredientId = ingredients.getInt("BASE_INGREDIENT_ID");
	    				//check if base ingredient is null [0 is null for integers] (to prevent adding null to the list) and unique (to reduce repeated searches)
	    				if (baseIngredientId!=0 && permissibleProvidedIngredientIds.add(baseIngredientId)) {
	    					uniqueNewIngredientIds.add(baseIngredientId);
	    				}
	    			}
	    		}
			}
		}
		return permissibleProvidedIngredientIds.toArray(new Integer[permissibleProvidedIngredientIds.size()]);
	}
	
	// Made into own query due to recursion
	private Integer[] recurseBaseElement(Integer[] providedElementIds, String idName, String baseIdName, String tableName, Connection dbConnection) throws SQLException{
		if(providedElementIds.length==0) { // unfortunately, an empty tuple is not allowed. This exists to guard against that edge case.
			return providedElementIds; // vacuously compliant
		}
		
		HashSet<Integer> permissibleProvidedIds = new HashSet<Integer>(Arrays.asList(providedElementIds));
		ArrayList<Integer> uniqueNewIds = new ArrayList<Integer>(Arrays.asList(providedElementIds));
		StringBuilder sb;
		// if there is an exception, it is thrown, so only states that have not yet encountered an issue will arrive here
		while(!uniqueNewIds.isEmpty()) { // recursion means that multiple queries are needed
			sb = new StringBuilder();
			sb.append("SELECT DISTINCT ");
			sb.append(baseIdName);
			sb.append(" FROM ");
			sb.append(tableName);
			sb.append(" WHERE ");
			sb.append(idName);
			sb.append(" IN (");
				for(int i = 0; i < uniqueNewIds.size(); i++) {
					sb.append("?, ");
				}
			sb.setLength(sb.length()-2);//remove the last comma and space
			sb.append(")");
			try (PreparedStatement statement = dbConnection.prepareStatement(sb.toString());){
				int counter = 1;
				for(Integer id : uniqueNewIds) {
					statement.setInt(counter++, id);// prevents sql injection
				}
	    		try(ResultSet elements = statement.executeQuery();){
	    			uniqueNewIds = new ArrayList<Integer>();
	    			while(elements.next()) {
	    				Integer baseElementId = elements.getInt(baseIdName);
	    				//check if base element is null [0 is null for integers] (to prevent adding null to the list) and unique (to reduce repeated searches)
	    				if (baseElementId!=0 && permissibleProvidedIds.add(baseElementId)) {
	    					uniqueNewIds.add(baseElementId);
	    				}
	    			}
	    		}
			}
		}
		return permissibleProvidedIds.toArray(new Integer[permissibleProvidedIds.size()]);
	}
	
	private void searchRecipe(RecipeSearchRequest requestInfo, Connection dbConnection, HttpServletResponse response, int sessionUserId) throws SQLException, IOException{
		try (PreparedStatement statement = dbConnection.prepareStatement(buildRecipeQuery(requestInfo, sessionUserId));){
			int counter = 1;
			
			// ingredient filters
			for(Integer id : requestInfo.filterIdsNotCourses.get("Ingredient")) {
	    			statement.setInt(counter++, id);
	    		}
    		for(Integer id : requestInfo.filterIdsNotCourses.get("Dietary Restriction")) {
    			statement.setInt(counter++, id);
    		}
    		if((requestInfo.filterIdsNotCourses.get("Ingredient").length>0) || (requestInfo.tolerances.get("Ingredient")>0)) {
    	    	statement.setInt(counter++, requestInfo.tolerances.get("Ingredient"));
    		}
    		
    		//review count and average rating
			statement.setInt(counter++, requestInfo.ranges.get("Reviews").get("min"));
			statement.setInt(counter++, requestInfo.ranges.get("Reviews").get("max"));
			statement.setInt(counter++, requestInfo.ranges.get("Ratings").get("min"));
			statement.setInt(counter++, requestInfo.ranges.get("Ratings").get("max"));    		
			
			//bookmarks
			statement.setInt(counter++, sessionUserId);
			statement.setInt(counter++, requestInfo.ranges.get("Bookmarks").get("min"));
			statement.setInt(counter++, requestInfo.ranges.get("Bookmarks").get("max"));
			
			// author
    		for(Integer id : requestInfo.filterIdsNotCourses.get("Author")) {
    			statement.setInt(counter++, id);
    		}
			
    		//search term
			if(!requestInfo.searchTerm.isEmpty()) {
    			statement.setString(counter++, requestInfo.searchTerm + "%");
        		statement.setString(counter++, "% "+ requestInfo.searchTerm + "%");
    		}
			
			//appliance tolerance
			if (requestInfo.filterIdsNotCourses.get("Appliance").length>0 || requestInfo.tolerances.get("Appliance")>0) {
	    		for(Integer id : requestInfo.filterIdsNotCourses.get("Appliance")) {
	    			statement.setInt(counter++, id);
	    		}
	    		statement.setInt(counter++, requestInfo.tolerances.get("Appliance"));
			}
    		
			//dish type
    		for(Integer id : requestInfo.filterIdsNotCourses.get("Dish Type")) {
    			statement.setInt(counter++, id);
    		}    	
    		
			//cuisine
    		for(Integer id : requestInfo.filterIdsNotCourses.get("Cuisine")) {
    			statement.setInt(counter++, id);
    		}    
    		
			//cuisine
    		for(String name : requestInfo.courses) {
    			statement.setString(counter++, name);
    		}
    		
	    	try(
	    		ResultSet recipes = statement.executeQuery();
	    		PrintWriter pw = response.getWriter();
	    		){
	    		writeRecipeResults(recipes, pw);
    		}
		}
	}
	private String buildRecipeQuery(RecipeSearchRequest requestInfo, int sessionUserId) {
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT rec.RECIPE_ID, rec.RECIPE_NAME, rec.RECIPE_IMAGE_PATH, rec.RECIPE_DESCRIPTION, ");
		sb.append("rec.TOTAL_MINUTES, rec.RECIPE_ACTIVE_TIMESTAMP, recRevSubquery.reviewCount, ");
		sb.append("recRevSubquery.averageRating, recBookSubquery.bookmarkCount, recBookSubquery.userbookmarked, ");
		sb.append("recAuthSubquery.USER_ID, recAuthSubquery.USER_NAME, recAuthSubquery.USER_IMAGE_PATH FROM RECIPES rec ");
		
		//ingredient filter
		int ingredientsLength = requestInfo.filterIdsNotCourses.get("Ingredient").length;
		int dietaryRestrictionsLength = requestInfo.filterIdsNotCourses.get("Dietary Restriction").length;
		boolean ingredientToleranceAboveZero = requestInfo.tolerances.get("Ingredient")>0;
		if((ingredientsLength>0)
				|| (dietaryRestrictionsLength>0)
				|| (ingredientToleranceAboveZero)
				) {
			sb.append("INNER JOIN (");
				buildRecipeIngredientSubquery(sb, ingredientsLength, dietaryRestrictionsLength, ingredientToleranceAboveZero);
			sb.append(") recIngSubquery ON rec.RECIPE_ID = recIngSubquery.RECIPE_ID ");
		}
		
		// review count and average rating
		sb.append("INNER JOIN (");
			buildRecipeReviewSubquery(sb);
		sb.append(") recRevSubquery ON rec.RECIPE_ID = recRevSubquery.RECIPE_ID ");
		
		// bookmarks
		sb.append("INNER JOIN (");
			buildRecipeBookmarkSubquery(sb, sessionUserId);
		sb.append(") recBookSubquery ON rec.RECIPE_ID = recBookSubquery.RECIPE_ID ");
		
		// authors
		buildRecipeAuthorSubquery(sb, requestInfo.filterIdsNotCourses.get("Author").length, "recAuthSubquery");
		
		// moderation status
		sb.append("WHERE rec.RECIPE_STATUS = 'Active' ");
		
		//search term
		if(!requestInfo.searchTerm.isEmpty()) {
			sb.append("AND (rec.RECIPE_NAME LIKE ? OR rec.RECIPE_NAME LIKE ?) ");
		}
		
		// appliance tolerance
		buildApplianceCondition(sb, requestInfo.filterIdsNotCourses.get("Appliance").length, requestInfo.tolerances.get("Appliance")>0);
		
		// dish type
		buildDishTypeCondition(sb, requestInfo.filterIdsNotCourses.get("Dish Type").length);
		
		// cuisine
		buildCuisineCondition(sb, requestInfo.filterIdsNotCourses.get("Cuisine").length);
		
		// courses
		buildCourseCondition(sb, requestInfo.courses.length);
		
		// options
		buildCheckboxConditions(sb, requestInfo.options);
		
		// sort order
		buildSortOrder(sb, requestInfo.sortOrder, requestInfo.sortOptions);
		
		//limit
		sb.append(" LIMIT 10");
		return sb.toString();
	}
	
	private void buildRecipeIngredientSubquery(StringBuilder sb, int ingredientsLength, int dietaryRestrictionsLength, boolean ingredientToleranceAboveZero) {
		sb.append("SELECT recIngSubquery.RECIPE_ID FROM RECIPES recIngSubquery ");
		sb.append("LEFT JOIN (SELECT * FROM RECIPES_COOK_INGREDIENTS as rci ");
		if(ingredientsLength>0) {
				sb.append("WHERE rci.INGREDIENT_ID NOT IN (");
				for (int i = 0; i<ingredientsLength; i++) {
					sb.append("?, ");
				}
			sb.setLength(sb.length()-2);//remove the last comma and space
			sb.append(") ");
		}
		sb.append(") rci ON recIngSubquery.RECIPE_ID = rci.RECIPE_ID ");
		if (dietaryRestrictionsLength>0) {
			sb.append("LEFT JOIN (SELECT * FROM INGREDIENTS_VIOLATE_DIETARY_RESTRICTIONS as ivds ");
					sb.append("WHERE ivds.DIETARY_RESTRICTION_ID IN (");
					for (int i = 0; i<dietaryRestrictionsLength; i++) {
						sb.append("?, ");
					}
				sb.setLength(sb.length()-2);//remove the last comma and space
				sb.append(") ");
			sb.append(") ivds ON rci.INGREDIENT_ID = ivds.INGREDIENT_ID ");
		}
		sb.append("GROUP BY recIngSubquery.RECIPE_ID HAVING "); //HAVING is safe because at least one of the following conditions will pass
		if ((ingredientsLength>0) || (ingredientToleranceAboveZero)) {
			sb.append("COUNT(DISTINCT rci.INGREDIENT_ID) <= ? AND ");
		}
		if (dietaryRestrictionsLength>0) {
			sb.append("COUNT(ivds.DIETARY_RESTRICTION_ID) = 0 ");
		}
		else {
			sb.setLength(sb.length()-4); // remove "AND " (guaranteed because at least one condition must be true, so by default, the first condition must have passed)
		}
	}
	
	private void buildRecipeReviewSubquery(StringBuilder sb) {
		sb.append("SELECT recRevSubquery.RECIPE_ID, COUNT(rev.REVIEW_ID) AS reviewCount, AVG(rev.RATING*1.0) AS averageRating FROM RECIPES recRevSubquery ");
		sb.append("LEFT JOIN REVIEWS rev ON recRevSubquery.RECIPE_ID = rev.RECIPE_ID ");
		sb.append("GROUP BY recRevSubquery.RECIPE_ID ");
		sb.append("HAVING COUNT(rev.REVIEW_ID)>=? ");
		sb.append("AND COUNT(rev.REVIEW_ID)<=? ");
		sb.append("AND ((AVG(rev.RATING*1.0) IS NULL) ");//edge case: no reviews
			sb.append("OR (AVG(rev.RATING*1.0)>=? ");
				sb.append("AND AVG(rev.RATING*1.0)<=? ");
		sb.append("))");
	}
	
	private void buildRecipeBookmarkSubquery(StringBuilder sb, int sessionUserId) {
		sb.append("SELECT recBookSubquery.RECIPE_ID, COUNT(b.USER_ID) AS bookmarkCount, ");
		sb.append("SUM(b.USER_ID = ?) AS userbookmarked FROM RECIPES recBookSubquery ");
		sb.append("LEFT JOIN BOOKMARK b ON recBookSubquery.RECIPE_ID = b.RECIPE_ID ");
		sb.append("GROUP BY recBookSubquery.RECIPE_ID ");
		sb.append("HAVING COUNT(b.USER_ID)>=? ");
		sb.append("AND COUNT(b.USER_ID)<=? ");
	}
	
	private void buildRecipeAuthorSubquery(StringBuilder sb, int authorsLength, String joinTableName) {
		if(authorsLength>0) { // case 1: there is a list of provided authors
			sb.append("INNER JOIN (");
				buildRecipeAuthorSubqueryCaseWhereTheVariableAuthorsLengthIsAValidIntegerThatIsAboveZero(sb, authorsLength);
			sb.append(") ");
		}
		else { // case 2: no authors provided, any author will do
			sb.append("LEFT JOIN USERS ");
		}
		sb.append(joinTableName); 
		sb.append(" ON rec.AUTHOR_ID = ");
		
		sb.append(joinTableName);
		sb.append(".USER_ID ");
	}
	
	private void buildRecipeAuthorSubqueryCaseWhereTheVariableAuthorsLengthIsAValidIntegerThatIsAboveZero(StringBuilder sb, int authorsLength) {
		sb.append("SELECT recAuthSubquery.USER_ID, recAuthSubquery.USER_NAME, recAuthSubquery.USER_IMAGE_PATH FROM USERS recAuthSubquery ");
			sb.append("WHERE recAuthSubquery.USER_ID IN (");
			for (int i = 0; i<authorsLength; i++) {
				sb.append("?, ");
			}
			sb.setLength(sb.length()-2);//remove the last comma and space
			sb.append(")");
	}
	
	private void buildApplianceCondition(StringBuilder sb, int appliancesLength, boolean applianceToleranceBoolean) {
		if (appliancesLength>0 || applianceToleranceBoolean) {
			sb.append("AND (SELECT COUNT(rua.APPLIANCE_ID) FROM RECIPES_USE_APPLIANCES as rua ");
				sb.append("WHERE rec.RECIPE_ID = rua.RECIPE_ID ");
				if (appliancesLength>0) {
					sb.append("AND rua.APPLIANCE_ID NOT IN (");
					for (int i = 0; i<appliancesLength; i++) {
						sb.append("?, ");
					}
					sb.setLength(sb.length()-2);//remove the last comma and space
					sb.append(")");
				}
			sb.append(") <=? ");
		}
	}
	
	private void buildDishTypeCondition(StringBuilder sb, int dishTypesLength) {
		if(dishTypesLength>0) {
			sb.append("AND NOT EXISTS (SELECT 1 FROM RECIPES_HAS_DISH_TYPES as rhdt ");
			sb.append("WHERE rec.RECIPE_ID = rhdt.RECIPE_ID ");
			sb.append("AND rhdt.DISH_TYPE_ID NOT IN (");
			for (int i = 0; i<dishTypesLength; i++) {
				sb.append("?, ");
			}
			sb.setLength(sb.length()-2);//remove the last comma and space
			sb.append(")");
			sb.append(") ");
		}
	}
	
	private void buildCuisineCondition(StringBuilder sb, int cuisinesLength) {
		if(cuisinesLength>0) {
			sb.append("AND NOT EXISTS (SELECT 1 FROM RECIPES_FEATURE_CUISINES as rfc ");
			sb.append("WHERE rec.RECIPE_ID = rfc.RECIPE_ID ");
			sb.append("AND rfc.CUISINE_ID NOT IN (");
			for (int i = 0; i<cuisinesLength; i++) {
				sb.append("?, ");
			}
			sb.setLength(sb.length()-2);//remove the last comma and space
			sb.append(")");
			sb.append(") ");
		}
	}
	
	private void buildCourseCondition(StringBuilder sb, int coursesLength) {
		if(coursesLength>0) {
			sb.append("AND rec.COURSE IN (");
			for (int i = 0; i<coursesLength; i++) {
				sb.append("?, ");
			}
			sb.setLength(sb.length()-2);//remove the last comma and space
			sb.append(") ");
		}
	}
	
	private void buildCheckboxConditions(StringBuilder sb, HashMap<String, Boolean> options) {
		if(!options.get("Ai Generated Description")) {
			sb.append("AND rec.AI_GEN_DESCRIPTION = FALSE ");
		}
		if(!options.get("Ai Generated Appliances")) {
			sb.append("AND rec.AI_GEN_APPLIANCES = FALSE ");
		}
		if(!options.get("Ai Generated Cuisines")) {
			sb.append("AND rec.AI_GEN_CUISINES = FALSE ");
		}
		if(!options.get("Ai Generated Dish Types")) {
			sb.append("AND rec.AI_GEN_DISH_TYPES = FALSE ");
		}
		if(!options.get("Ai Generated Cooking Minutes")) {
			sb.append("AND rec.AI_GEN_TOTAL_MINUTES = FALSE ");
		}
		if(!options.get("Ai Generated Courses")) {
			sb.append("AND rec.AI_GEN_COURSE = FALSE ");
		}
	}
	
	private void buildSortOrder(StringBuilder sb, String[] sortOrder, HashMap<String, HashMap<String, String>> sortOptions) {
		if(sortOrder.length>0) {
			sb.append(" ORDER BY ");
			for(String sortKey : sortOrder) {
				switch (sortKey){
					case "Publication Date":
						sb.append("rec.RECIPE_ACTIVE_TIMESTAMP ");
						break;
					case "Bookmarks":
						sb.append("recBookSubquery.bookmarkCount ");
						break;
					case "Reviews":
						sb.append("recRevSubquery.reviewCount ");
						break;
					default:// assumption: the value is a valid key
						sb.append("recRevSubquery.averageRating ");
				}
				String order;
				if(sortOptions.get(sortKey).get("sort").equals("ascending")) {
					order = "ASC";
				}
				else {
					order = "DESC";
				}
				sb.append(order);
				sb.append(", ");
			}
			sb.setLength(sb.length()-2); // Remove last comma and space
		}
	}
	
	private void writeRecipeResults(ResultSet recipes, PrintWriter pw) throws SQLException, IOException{
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode outerNode = mapper.createObjectNode();
		Integer counter = 0;
		// formatting data into json
		while(recipes.next()) {
			ObjectNode innerNode = mapper.createObjectNode();
			innerNode.put("recipeId", recipes.getInt("RECIPE_ID"));
			innerNode.put("recipeName", recipes.getString("RECIPE_NAME"));
			innerNode.put("recipeImagePath", recipes.getString("RECIPE_IMAGE_PATH"));
			innerNode.put("recipeDescription", recipes.getString("RECIPE_DESCRIPTION"));
			innerNode.put("recipeTotalMinutes", recipes.getObject("TOTAL_MINUTES") == null ? "N/A" : recipes.getInt("TOTAL_MINUTES") + "m");
			innerNode.put("recipeActiveTimestamp", recipes.getTimestamp("RECIPE_ACTIVE_TIMESTAMP").toString());
			innerNode.put("reviewCount", recipes.getInt("reviewCount"));
			innerNode.put("averageRating", String.format("%.1f", recipes.getFloat("averageRating")));
			innerNode.put("bookmarkCount", recipes.getInt("bookmarkCount"));
			innerNode.put("userBookmarked", recipes.getInt("userbookmarked")>0);
			innerNode.put("userId", recipes.getInt("USER_ID"));
			innerNode.put("userName", recipes.getString("USER_NAME"));
			innerNode.put("userImagePath", recipes.getString("USER_IMAGE_PATH"));
			outerNode.put((counter++).toString(), mapper.writeValueAsString(innerNode));
		}
		
		//writing response
		pw.write(mapper.writeValueAsString(outerNode));
	}
}