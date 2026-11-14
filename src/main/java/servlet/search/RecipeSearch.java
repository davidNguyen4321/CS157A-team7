package servlet.search;

import util.SqlBuilder;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.io.IOException;
import java.io.PrintWriter;

import java.util.Map;
import java.util.LinkedHashMap;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.Arrays;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import servlet.error.ServletSecurity;

import com.fasterxml.jackson.databind.ObjectMapper;

import database.DbAccess;

@WebServlet("/Recipes/Search")
public class RecipeSearch extends HttpServlet {
	
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
				
		Connection dbConnection = DbAccess.getConnection();
		
		int sessionUserId = (Integer) request.getAttribute("userId");
		String[] parameters = {"searchTerm", "sortOptions", "filterIds", "tolerances", "ranges", "options"};
	    Map<String, String> parameterMap = ServletSecurity.getValidatedParameters(request, parameters, true);
		
		RecipeSearchRequest requestInfo = new RecipeSearchRequest(parameterMap);
		Map<String, Integer[]> filterIdMap = requestInfo.getFilterIdMap();
		Map<String, Map<String, Integer>> ranges = requestInfo.getRanges();
		
		try {
			filterIdMap.put("Dietary Restriction",
				recurseUniDir( // Recurse up
					filterIdMap.get("Dietary Restriction"),
					/* curLevelCol: */ "DIETARY_RESTRICTION_ID",
					/* nextLevelCol: */ "BASE_DIETARY_RESTRICTION_ID",
					"DIETARY_RESTRICTIONS",
					dbConnection
				).toArray(Integer[]::new)
			);
			filterIdMap.put("Cuisine",
				recurseUniDir( // Recurse down
					filterIdMap.get("Cuisine"),
					/* nextLevelCol: */ "BASE_CUISINE_ID",
					/* nextLevelCol: */ "CUISINE_ID",
					"CUISINES",
					dbConnection
				).toArray(Integer[]::new)
			);
			filterIdMap.put("Dish Type",
				recurseUniDir( // Recurse down
					filterIdMap.get("Dish Type"),
					/* curLevelCol: */ "BASE_DISH_TYPE_ID",
					/* nextLevelCol: */ "DISH_TYPE_ID",
					"DISH_TYPES",
					dbConnection
				).toArray(Integer[]::new)
			);
			
			for (Integer wantedId : filterIdMap.get("Ingredient")) {
			    requestInfo.getIngredientMatches().put(wantedId,
			        recurseBiDir(new Integer[] { wantedId },
			            /* colName: */ "INGREDIENT_ID",
			            /* baseColName: */ "BASE_INGREDIENT_ID",
			            "INGREDIENTS",
			            dbConnection
			        ).toArray(Integer[]::new)
			    );
			}
			
			sanitizeRange(ranges.get("Ratings"), 1, 5);
			sanitizeRange(ranges.get("Reviews"), 0, Integer.MAX_VALUE);
			sanitizeRange(ranges.get("Bookmarks"), 0, Integer.MAX_VALUE);
			sanitizeRange(ranges.get("Cooking Time"), 0, Integer.MAX_VALUE);
			
			searchRecipe(requestInfo, dbConnection, response, sessionUserId);
		}
		catch(Exception ex) { //error checking
			log("Recipe search failed: " + ex.getMessage());
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().write("{}");
			return;
		}
	}

	private HashSet<Integer> recurseUniDir(Integer[] providedElementIds, String curLevelCol, String nextLevelCol, String tableName, Connection dbConnection) throws SQLException {
		if(providedElementIds.length == 0) return new HashSet<Integer>(); 
		
		HashSet<Integer> closureSet = new HashSet<Integer>(Arrays.asList(providedElementIds));
		ArrayList<Integer> newIdsToRecurse = new ArrayList<Integer>(Arrays.asList(providedElementIds));
		
		while(!newIdsToRecurse.isEmpty()) {
			SqlBuilder sb = new SqlBuilder();
			
			sb.addString("SELECT DISTINCT ");
			sb.addString(nextLevelCol);
			sb.addString(" FROM ");
			sb.addString(tableName);
			sb.addString(" WHERE ");
			sb.addString(curLevelCol);
			sb.addString(" IN (");	
			sb.addManyInts(newIdsToRecurse);
			sb.addString(")");
			
			try (PreparedStatement statement = dbConnection.prepareStatement(sb.toString());) {
				int counter = 1;
				for(Integer id : newIdsToRecurse) statement.setInt(counter++, id); 
	    		
				try(ResultSet nextLevelIds = statement.executeQuery();) {
	    			newIdsToRecurse = new ArrayList<Integer>();
	    			while(nextLevelIds.next()) {
	    				Integer nextLevelId = nextLevelIds.getInt(nextLevelCol);
	    				if (!nextLevelIds.wasNull() && closureSet.add(nextLevelId)) { // ignore null values and duplicates
	    					newIdsToRecurse.add(nextLevelId);
	    				}
	    			}
	    		}
			}
		}
		
		return closureSet;
	}
	
	private HashSet<Integer> recurseBiDir(Integer[] providedElementIds, String colName, String baseColName, String tableName, Connection dbConnection) throws SQLException {
		HashSet<Integer> recurseUp = recurseUniDir(providedElementIds, colName, baseColName, tableName, dbConnection);
		HashSet<Integer> recurseDown = recurseUniDir(providedElementIds, baseColName, colName, tableName, dbConnection);
		
		recurseUp.addAll(recurseDown);
	    return recurseUp;
	}
	
	private void sanitizeRange(Map<String, Integer> range, int absMin, int absMax) {
	    Integer min = range.get("min");
	    Integer max = range.get("max");
	    
	    min = (min == null || min < absMin || min > absMax) ? 0 : min;
	    max = (max == null || max < absMin || max > absMax) ? absMax : max;
	    
	    if(min > max) {
	    	int temp = min;
	        min = max;
	        max = temp;
	    }
	    
	    range.put("min", min);
	    range.put("max", max);
	}
	
	private void searchRecipe(RecipeSearchRequest requestInfo, Connection dbConnection, HttpServletResponse response, int sessionUserId) throws SQLException, IOException {

		try (PreparedStatement ps = buildRecipeQuery(requestInfo, sessionUserId).build(dbConnection); ) {    		
	    	try (
	    		ResultSet rs = ps.executeQuery();
	    		PrintWriter pw = response.getWriter();
	    	) { writeRecipeResults(rs, pw); }
		}
	}
	
	private SqlBuilder buildRecipeQuery(RecipeSearchRequest requestInfo, int sessionUserId) {
		Map<String, Integer[]> filterIdMap = requestInfo.getFilterIdMap();
		Map<String, Map<String, Integer>> ranges = requestInfo.getRanges();
	    Map<String, Integer> tolerances = requestInfo.getTolerances();
	    
	    SqlBuilder sb = new SqlBuilder();
	    	    
		sb.addString("SELECT recipes.recipe_id, "
				+ "recipes.recipe_name, "
				+ "recipes.recipe_image_path, "
				+ "recipes.recipe_description, "
				+ "recipes.total_minutes, "
				+ "recipes.recipe_active_timestamp, ");
		sb.addString("recipes_reviews.review_count, "
				+ "recipes_reviews.average_rating,");
		sb.addString("recipes_users.bookmark_count, "
				+ "recipes_users.user_bookmarked, ");
		sb.addString("users.user_id, "
				+ "users.user_name, "
				+ "users.user_image_path ");
		
		sb.addString("FROM recipes NATURAL JOIN (");		
			sb.addString("SELECT recipes.recipe_id, COUNT(review_id) AS review_count, AVG(rating) AS average_rating ");
			sb.addString("FROM recipes LEFT JOIN reviews ON recipes.recipe_id = reviews.recipe_id AND review_status = 'Active' ");
			sb.addString("GROUP BY recipes.recipe_id ");
			sb.addString("HAVING COUNT(review_id) >= ? AND COUNT(review_id) <= ? ");
				int minRating = ranges.get("Ratings").get("min");
				int maxRating = ranges.get("Ratings").get("max");
				sb.addString("AND (");
				if (minRating == 0) sb.addString("AVG(rating) IS NULL OR "); // Unrated recipes are included only when there's no minimum
				sb.addString("(AVG(rating) >= ? AND AVG(rating) <= ?)");
				sb.addString(")");

				sb.addParam(ranges.get("Reviews").get("min"));
				sb.addParam(ranges.get("Reviews").get("max"));
				sb.addParam(minRating);
				sb.addParam(maxRating);
		sb.addString(") recipes_reviews ");
		
		sb.addString("NATURAL JOIN (");
			sb.addString("SELECT recipes.recipe_id, "
					+ "COUNT(bookmark_users.user_id) AS bookmark_count, "
					+ "SUM(bookmark_users.user_id = ?) AS user_bookmarked ");
				sb.addParam(sessionUserId);
			sb.addString("FROM recipes LEFT JOIN (");
				sb.addString("SELECT bookmark.user_id, bookmark.recipe_id "
						+ "FROM bookmark JOIN users ON users.user_id = bookmark.user_id AND user_status = 'Active'");
			sb.addString(") bookmark_users ON recipes.recipe_id = bookmark_users.recipe_id ");
			sb.addString("GROUP BY recipes.recipe_id ");
			sb.addString("HAVING COUNT(bookmark_users.user_id) >= ? AND COUNT(bookmark_users.user_id) <= ? ");
				sb.addParam(ranges.get("Bookmarks").get("min"));
				sb.addParam(ranges.get("Bookmarks").get("max"));
			sb.addString(") recipes_users ");
			
		Integer[] authors = filterIdMap.get("Author");
		if (authors.length > 0) {
			sb.addString("JOIN users ON recipes.author_id = users.user_id AND users.user_id IN (");
				sb.addManyInts(authors);
			sb.addString(")");
		} 
		else sb.addString("LEFT JOIN users ON recipes.author_id = users.user_id ");
		
		sb.addString("WHERE recipes.recipe_status = 'Active' ");
		
		// Ingredient filter: Recipe must contain at least (wanted - tolerance) of the wanted ingredients.
		// A wanted ingredient counts as present if the recipe has it or any related one in the hierarchy.
	    Map<Integer, Integer[]> ingredientMatches = requestInfo.getIngredientMatches();
		int requiredIngredients = ingredientMatches.size() - tolerances.get("Ingredient");
		if (requiredIngredients > 0) {
		    sb.addString("AND ("); 
			 // The whole (EXISTS + EXISTS + ...) expression is evaluated once per recipe row.
			 // So recipes.recipe_id is the same recipe in every EXISTS below.
			 // Each EXISTS looks at that recipe's rows in rci to see if it has any ingredient that satisfies the wanted ingredient from the loop.
		    for (Integer[] matchingIds : ingredientMatches.values()) {
		        sb.addString("EXISTS (");
	        		sb.addString("SELECT 1 "); 
	        		sb.addString("FROM recipes_cook_ingredients rci ");
	        		sb.addString("WHERE rci.recipe_id = recipes.recipe_id AND rci.ingredient_id IN (");
	        			sb.addManyInts(matchingIds);
				    sb.addString(")");
		        sb.addString(") + ");
		    }
		    sb.truncate(2); // remove the last plus and space
		    sb.addString(") >= ? ");
		    sb.addParam(requiredIngredients);
		}
		
		// Dietary restriction filter: the recipe must not contain any ingredient that violates a selected restriction.
		// As mentioned before, recipes.recipe_id is the recipe currently being checked.
		Integer[] dietaryRestrictionIds = filterIdMap.get("Dietary Restriction");
		if (dietaryRestrictionIds.length > 0) {
		    sb.addString("AND NOT EXISTS (");
		        sb.addString("SELECT 1 ");
		        sb.addString("FROM recipes_cook_ingredients rci JOIN ingredients_violate_dietary_restrictions ivdr ON rci.ingredient_id = ivdr.ingredient_id ");
		        sb.addString("WHERE rci.recipe_id = recipes.recipe_id AND ivdr.dietary_restriction_id IN (");
		        	sb.addManyInts(dietaryRestrictionIds);
		        sb.addString(")");
		    sb.addString(") ");
		}
		
		String searchTerm = requestInfo.getSearchTerm();
		if(!searchTerm.isEmpty()) {
			sb.addString("AND (recipes.recipe_name LIKE ? OR recipes.recipe_name LIKE ?) ");
				sb.addParam(searchTerm + "%");
				sb.addParam("% "+ searchTerm + "%");
		}
		
		int minMinutes = ranges.get("Cooking Time").get("min");
		int maxMinutes = ranges.get("Cooking Time").get("max");
		if (!(minMinutes == 0 && maxMinutes == Integer.MAX_VALUE)) {
		    sb.addString("AND recipes.total_minutes >= ? AND recipes.total_minutes <= ? ");
		    sb.addParam(minMinutes);
		    sb.addParam(maxMinutes);
		}
		
		// Appliance filter: The recipe may need at most (tolerance) appliances that are NOT in the user's list.
		// As mentioned before, recipes.recipe_id is the recipe currently being checked.
		Integer[] applianceIds = filterIdMap.get("Appliance");
		int applianceTolerance = Math.max(0, tolerances.get("Appliance"));
		if (applianceIds.length > 0) {
		    sb.addString("AND (");
		    	sb.addString("SELECT COUNT(*) ");
		    	sb.addString("FROM recipes_use_appliances rua ");
		    	sb.addString("WHERE rua.recipe_id = recipes.recipe_id AND rua.appliance_id NOT IN (");
		    		sb.addManyInts(applianceIds);
				sb.addString(")");
		    sb.addString(") <= ? ");
		    sb.addParam(applianceTolerance);
		}
		
		// Course filter: Recipe's course must be one of the selected courses
		Integer[] courseIds = filterIdMap.get("Course");
		if (courseIds.length > 0) {
		    sb.addString("AND recipes.course IN (");
		    	sb.addManyInts(courseIds);
		    sb.addString(") ");
		}
		
		// Dish type filter: Recipe must have at least one of the selected dish types.
		Integer[] dishTypeIds = filterIdMap.get("Dish Type");
		if (dishTypeIds.length > 0) {
		    sb.addString("AND EXISTS (");
		    	sb.addString("SELECT 1 ");
		    	sb.addString("FROM recipes_has_dish_types rhdt ");
		    	sb.addString("WHERE rhdt.recipe_id = recipes.recipe_id AND rhdt.dish_type_id IN (");
		    		sb.addManyInts(dishTypeIds);
		    	sb.addString(")");
		    sb.addString(") ");
		}
		
		// Dish type filter: Recipe must have at least one of the selected dish types.
		Integer[] cuisineIds = filterIdMap.get("Cuisine");
		if (cuisineIds.length > 0) {
		    sb.addString("AND EXISTS (");
		    	sb.addString("SELECT 1 ");
		    	sb.addString("FROM recipes_feature_cuisines rfc ");
		    	sb.addString("WHERE rfc.recipe_id = recipes.recipe_id AND rfc.cuisine_id IN (");
		    		sb.addManyInts(cuisineIds);
		    	sb.addString(")");
		    sb.addString(") ");
		}
		
		Map<String, Boolean> options = requestInfo.getOptions();
		if(!options.get("Ai Generated Description")) 	 sb.addString("AND recipes.ai_gen_description = FALSE ");
		if(!options.get("Ai Generated Appliances")) 	 sb.addString("AND recipes.ai_gen_appliances = FALSE ");
		if(!options.get("Ai Generated Cuisines")) 		 sb.addString("AND recipes.ai_gen_cuisines = FALSE ");
		if(!options.get("Ai Generated Dish Types")) 	 sb.addString("AND recipes.ai_gen_dish_types = FALSE ");
		if(!options.get("Ai Generated Cooking Minutes")) sb.addString("AND recipes.ai_gen_total_minutes = FALSE ");
		if(!options.get("Ai Generated Courses")) 		 sb.addString("AND recipes.ai_gen_course = FALSE ");
		
		
		sb.addString("ORDER BY ");
		boolean hasSort = false; 
	    for (Map.Entry<String, String> entry : requestInfo.getSortOptions().entrySet()) {
	        String column;
	        switch (entry.getKey()) {
	            case "Publication Date": column = "recipes.recipe_active_timestamp"; break;
	            case "Bookmarks":        column = "recipes_users.bookmark_count";    break;
	            case "Reviews":          column = "recipes_reviews.review_count";    break;
	            case "Ratings":          column = "recipes_reviews.average_rating";  break;
	            default: continue; 
	        }
	        if (!hasSort) hasSort = true;
	        else sb.addString(", ");
	        sb.addString(column)
	          .addString("ascending".equals(entry.getValue()) ? " ASC" : " DESC");
	    }
	    if (!hasSort) sb.addString("recipes.recipe_active_timestamp DESC");
		
		return sb;
	}
	
	private void writeRecipeResults(ResultSet recipes, PrintWriter pw) throws SQLException, IOException{
		ObjectMapper mapper = new ObjectMapper();
		ArrayList<Map<String, Object>> recipeList = new ArrayList<>();

		while (recipes.next()) {
	        Map<String, Object> recipeMap = new LinkedHashMap<>();
	        recipeMap.put("recipeId", recipes.getInt("recipe_id"));
	        recipeMap.put("recipeName", recipes.getString("recipe_name"));
	        recipeMap.put("recipeImagePath", recipes.getString("recipe_image_path"));
	        recipeMap.put("recipeDescription", recipes.getString("recipe_description"));
	        recipeMap.put("recipeTotalMinutes", recipes.getObject("total_minutes") == null ? "N/A" : recipes.getInt("total_minutes") + "m");
	        recipeMap.put("recipeActiveTimestamp", recipes.getTimestamp("recipe_active_timestamp").toString());
	        recipeMap.put("reviewCount", recipes.getInt("review_count"));
	        recipeMap.put("averageRating", String.format("%.1f", recipes.getFloat("average_rating")));
	        recipeMap.put("bookmarkCount", recipes.getInt("bookmark_count"));
	        recipeMap.put("userBookmarked", recipes.getInt("user_bookmarked") > 0);
	        recipeMap.put("userId", recipes.getInt("user_id"));
	        recipeMap.put("userName", recipes.getString("user_name"));
	        recipeMap.put("userImagePath", recipes.getString("user_image_path"));
	        
	        recipeList.add(recipeMap);
	    }
		
		pw.write(mapper.writeValueAsString(recipeList));
	}
}