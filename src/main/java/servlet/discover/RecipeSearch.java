package servlet.discover;

import auth.ServletSecurity;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.io.IOException;
import java.io.PrintWriter;

import java.util.Map;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.Arrays;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

@WebServlet("/Recipes/Search")
public class RecipeSearch extends HttpServlet {
	
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		
		Integer sessionUserId = ServletSecurity.getAuthenticatedUserId(request, response);
		
		Connection dbConnection = ServletSecurity.getValidatedDbConnection(request, response);
		if(dbConnection == null) return;
		
		String[] parameters = {"searchTerm", "sortOrder", "sortOptions", "filterIds", "tolerances", "ranges", "options"};
	    Map<String, String> parameterMap = ServletSecurity.getValidatedParameters(request, response, parameters, true);
	    if (parameterMap == null) return;
		
		RecipeSearchRequest requestInfo = new RecipeSearchRequest(parameterMap);
		Map<String, Integer[]> filterIdMap = requestInfo.getFilterIdMap();
		
		try{
			filterIdMap.put("Dietary Restriction",
					recurseBaseElement(
							filterIdMap.get("Dietary Restriction"),
							"DIETARY_RESTRICTION_ID",
							"BASE_DIETARY_RESTRICTION_ID",
							"DIETARY_RESTRICTIONS",
							dbConnection
							)
					);
			filterIdMap.put("Cuisine",
					recurseBaseElement(
							filterIdMap.get("Cuisine"),
							"BASE_CUISINE_ID", //special case where more specific cuisines are permissible but more general cuisines are not necessarily
							"CUISINE_ID",
							"CUISINES",
							dbConnection
							)
					);
			filterIdMap.put("Dish Type",
					recurseBaseElement(
							filterIdMap.get("Dish Type"),
							"BASE_DISH_TYPE_ID", //special case where more specific dish types are permissible but more general dish types are not necessarily
							"DISH_TYPE_ID",
							"DISH_TYPES",
							dbConnection
							)
					);
			requestInfo.getFilterIdMap().put("Ingredient", 
					recurseBaseIngredient(
							requestInfo.getFilterIdMap().get("Ingredient"), 
							requestInfo.getFilterIdMap().get("Dietary Restriction"), 
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
		try (PreparedStatement statement = dbConnection.prepareStatement(buildRecipeQuery(requestInfo));){
			int counter = 1;
			String searchTerm = requestInfo.getSearchTerm();
			Map<String, Integer[]> filterIdMap = requestInfo.getFilterIdMap();
			Map<String, Map<String, Integer>> ranges = requestInfo.getRanges();
		    Map<String, Integer> tolerances = requestInfo.getTolerances();

			// ingredient filters
			for(Integer id : filterIdMap.get("Ingredient")) {
	    			statement.setInt(counter++, id);
	    		}
    		for(Integer id : filterIdMap.get("Dietary Restriction")) {
    			statement.setInt(counter++, id);
    		}
    		if((filterIdMap.get("Ingredient").length>0) || (tolerances.get("Ingredient")>0)) {
    	    	statement.setInt(counter++, tolerances.get("Ingredient"));
    		}
    		
    		//review count and average rating
			statement.setInt(counter++, ranges.get("Reviews").get("min"));
			statement.setInt(counter++, ranges.get("Reviews").get("max"));
			statement.setInt(counter++, ranges.get("Ratings").get("min"));
			statement.setInt(counter++, ranges.get("Ratings").get("max"));    		
			
			//bookmarks
			statement.setInt(counter++, sessionUserId);
			statement.setInt(counter++, ranges.get("Bookmarks").get("min"));
			statement.setInt(counter++, ranges.get("Bookmarks").get("max"));
			
			// author
    		for(Integer id : filterIdMap.get("Author")) {
    			statement.setInt(counter++, id);
    		}
			
    		//search term
			if(!searchTerm.isEmpty()) {
    			statement.setString(counter++, searchTerm + "%");
        		statement.setString(counter++, "% "+ searchTerm + "%");
    		}
			
			//appliance tolerance
			if (filterIdMap.get("Appliance").length>0 || tolerances.get("Appliance")>0) {
	    		for(Integer id : filterIdMap.get("Appliance")) {
	    			statement.setInt(counter++, id);
	    		}
	    		statement.setInt(counter++, tolerances.get("Appliance"));
			}
    		
			//cooking time
			if(ranges.get("Cooking Time").get("min")!=0 || ranges.get("Cooking Time").get("max")!=Integer.MAX_VALUE) {
				statement.setInt(counter++, ranges.get("Cooking Time").get("min"));
				statement.setInt(counter++, ranges.get("Cooking Time").get("max"));
			}
			
			//dish type
    		for(Integer id : filterIdMap.get("Dish Type")) {
    			statement.setInt(counter++, id);
    		}    	
    		
			//cuisine
    		for(Integer id : filterIdMap.get("Cuisine")) {
    			statement.setInt(counter++, id);
    		}    
    		
    		for(Integer id : filterIdMap.get("Course")) {
    		    statement.setInt(counter++, id);
    		}
    		
	    	try(
	    		ResultSet recipes = statement.executeQuery();
	    		PrintWriter pw = response.getWriter();
	    		){
	    		writeRecipeResults(recipes, pw);
    		}
		}
	}
	private String buildRecipeQuery(RecipeSearchRequest requestInfo) {
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT rec.RECIPE_ID, rec.RECIPE_NAME, rec.RECIPE_IMAGE_PATH, rec.RECIPE_DESCRIPTION, ");
		sb.append("rec.TOTAL_MINUTES, rec.RECIPE_ACTIVE_TIMESTAMP, recRevSubquery.reviewCount, ");
		sb.append("recRevSubquery.averageRating, recBookSubquery.bookmarkCount, recBookSubquery.userbookmarked, ");
		sb.append("recAuthSubquery.USER_ID, recAuthSubquery.USER_NAME, recAuthSubquery.USER_IMAGE_PATH FROM RECIPES rec ");
		
		String searchTerm = requestInfo.getSearchTerm();
		Map<String, Integer[]> filterIdMap = requestInfo.getFilterIdMap();
		Map<String, Map<String, Integer>> ranges = requestInfo.getRanges();
	    Map<String, Integer> tolerances = requestInfo.getTolerances();
		
		//ingredient filter
		int ingredientsLength = filterIdMap.get("Ingredient").length;
		int dietaryRestrictionsLength = filterIdMap.get("Dietary Restriction").length;
		boolean ingredientToleranceAboveZero = tolerances.get("Ingredient")>0;
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
			buildRecipeBookmarkSubquery(sb);
		sb.append(") recBookSubquery ON rec.RECIPE_ID = recBookSubquery.RECIPE_ID ");
		
		// authors
		buildRecipeAuthorSubquery(sb, filterIdMap.get("Author").length, "recAuthSubquery");
		
		// moderation status
		sb.append("WHERE rec.RECIPE_STATUS = 'Active' ");
		
		//search term
		if(!searchTerm.isEmpty()) {
			sb.append("AND (rec.RECIPE_NAME LIKE ? OR rec.RECIPE_NAME LIKE ?) ");
		}
		
		// appliance tolerance
		buildApplianceCondition(sb, filterIdMap.get("Appliance").length, tolerances.get("Appliance")>0);
		
		// cooking time. if the requested range encompasses the entire possible range, then any null cooking times are permissible 
		if(ranges.get("Cooking Time").get("min")!=0 || ranges.get("Cooking Time").get("max")!=Integer.MAX_VALUE) {
			buildCookingTimeCondition(sb);
		}
		
		// dish type
		buildDishTypeCondition(sb, filterIdMap.get("Dish Type").length);
		
		// cuisine
		buildCuisineCondition(sb, filterIdMap.get("Cuisine").length);
		
		// courses
		buildCourseCondition(sb, filterIdMap.get("Course").length);
		
		// options
		buildCheckboxConditions(sb, requestInfo.getOptions());
		
		// sort order
		buildSortOrder(sb, requestInfo.getSortOptions());
		
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
		sb.append("LEFT JOIN REVIEWS rev ON recRevSubquery.RECIPE_ID = rev.RECIPE_ID AND rev.REVIEW_STATUS = 'ACTIVE' ");
		sb.append("GROUP BY recRevSubquery.RECIPE_ID ");
		sb.append("HAVING COUNT(rev.REVIEW_ID)>=? ");
		sb.append("AND COUNT(rev.REVIEW_ID)<=? ");
		sb.append("AND ((AVG(rev.RATING*1.0) IS NULL) ");//edge case: no reviews
			sb.append("OR (AVG(rev.RATING*1.0)>=? ");
				sb.append("AND AVG(rev.RATING*1.0)<=? ");
		sb.append("))");
	}
	
	private void buildRecipeBookmarkSubquery(StringBuilder sb) {
		sb.append("SELECT recBookSubquery.RECIPE_ID, COUNT(b.USER_ID) AS bookmarkCount, ");
		sb.append("SUM(b.USER_ID = ?) AS userbookmarked FROM RECIPES recBookSubquery ");
		sb.append("LEFT JOIN (SELECT b.USER_ID, b.RECIPE_ID FROM BOOKMARK b ");
		sb.append("INNER JOIN USERS u ON u.USER_ID = b.USER_ID AND u.USER_STATUS = 'ACTIVE' ");
		sb.append(") b ON recBookSubquery.RECIPE_ID = b.RECIPE_ID ");
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
	
	private void buildCookingTimeCondition(StringBuilder sb) {
		sb.append("AND rec.TOTAL_MINUTES>=? ");
		sb.append("AND rec.TOTAL_MINUTES<=? ");
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
	
	private void buildCheckboxConditions(StringBuilder sb, Map<String, Boolean> options) {
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
	
	private void buildSortOrder(StringBuilder sb, Map<String, Map<String, String>> sortOptions) {
	    if (sortOptions != null && !sortOptions.isEmpty()) {
	        sb.append(" ORDER BY ");
	        for (String sortKey : sortOptions.keySet()) {
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
	            Map<String, String> innerMap = sortOptions.get(sortKey);
	            if (innerMap != null && "ascending".equals(innerMap.get("sort"))) {
	                order = "ASC";
	            } else {
	                order = "DESC";
	            }
	            sb.append(order);
	            sb.append(", ");
	        }
	        sb.setLength(sb.length() - 2); // Remove last comma and space
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