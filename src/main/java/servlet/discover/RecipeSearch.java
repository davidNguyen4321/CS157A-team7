package servlet.discover;

import auth.ServletSecurity;

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

import com.fasterxml.jackson.databind.ObjectMapper;

@WebServlet("/Recipes/Search")
public class RecipeSearch extends HttpServlet {
	
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		
		Integer sessionUserId = ServletSecurity.getAuthenticatedUserId(request, response);
		
		Connection dbConnection = ServletSecurity.getValidatedDbConnection(request, response);
		if(dbConnection == null) return;
		
		String[] parameters = {"searchTerm", "sortOptions", "filterIds", "tolerances", "ranges", "options"};
	    Map<String, String> parameterMap = ServletSecurity.getValidatedParameters(request, response, parameters, true);
	    if (parameterMap == null) return;
		
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
			StringBuilder sb = new StringBuilder();
			
			sb.append("SELECT DISTINCT ");
			sb.append(nextLevelCol);
			sb.append(" FROM ");
			sb.append(tableName);
			sb.append(" WHERE ");
			sb.append(curLevelCol);
			sb.append(" IN (");	
			for(int i = 0; i < newIdsToRecurse.size(); i++) sb.append("?, ");
			sb.setLength(sb.length() - 2); // remove the last comma and space
			sb.append(")");
			
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
		ArrayList<Object> sqlParams = new ArrayList<>();

		try (PreparedStatement ps = dbConnection.prepareStatement(buildRecipeQuery(requestInfo, sessionUserId, sqlParams)); ) {
			for (int i = 0; i < sqlParams.size(); i++) ps.setObject(i + 1, sqlParams.get(i));
    		
	    	try (
	    		ResultSet rs = ps.executeQuery();
	    		PrintWriter pw = response.getWriter();
	    	) { writeRecipeResults(rs, pw); }
		}
	}
	
	private String buildRecipeQuery(RecipeSearchRequest requestInfo, int sessionUserId, ArrayList<Object> sqlParams) {
		Map<String, Integer[]> filterIdMap = requestInfo.getFilterIdMap();
		Map<String, Map<String, Integer>> ranges = requestInfo.getRanges();
	    Map<String, Integer> tolerances = requestInfo.getTolerances();
	    
	    StringBuilder sb = new StringBuilder();
	    
		sb.append("SELECT recipes.recipe_id, "
				+ "recipes.recipe_name, "
				+ "recipes.recipe_image_path, "
				+ "recipes.recipe_description, "
				+ "recipes.total_minutes, "
				+ "recipes.recipe_active_timestamp, ");
		sb.append("recipes_reviews.review_count, "
				+ "recipes_reviews.average_rating,");
		sb.append("recipes_users.bookmark_count, "
				+ "recipes_users.user_bookmarked, ");
		sb.append("users.user_id, "
				+ "users.user_name, "
				+ "users.user_image_path ");
		
		sb.append("FROM recipes NATURAL JOIN (");		
			sb.append("SELECT recipes.recipe_id, COUNT(review_id) AS review_count, AVG(rating) AS average_rating ");
			sb.append("FROM recipes LEFT JOIN reviews ON recipes.recipe_id = reviews.recipe_id AND review_status = 'Active' ");
			sb.append("GROUP BY recipes.recipe_id ");
			sb.append("HAVING COUNT(review_id) >= ? AND COUNT(review_id) <= ? ");
				int minRating = ranges.get("Ratings").get("min");
				int maxRating = ranges.get("Ratings").get("max");
				sb.append("AND (");
				if (minRating == 0) sb.append("AVG(rating) IS NULL OR "); // Unrated recipes are included only when there's no minimum
				sb.append("(AVG(rating) >= ? AND AVG(rating) <= ?)");
				sb.append(")");

				sqlParams.add(ranges.get("Reviews").get("min"));
				sqlParams.add(ranges.get("Reviews").get("max"));
				sqlParams.add(minRating);
				sqlParams.add(maxRating);
		sb.append(") recipes_reviews ");
		
		sb.append("NATURAL JOIN (");
			sb.append("SELECT recipes.recipe_id, "
					+ "COUNT(bookmark_users.user_id) AS bookmark_count, "
					+ "SUM(bookmark_users.user_id = ?) AS user_bookmarked ");
				sqlParams.add(sessionUserId);
			sb.append("FROM recipes LEFT JOIN (");
				sb.append("SELECT bookmark.user_id, bookmark.recipe_id "
						+ "FROM bookmark JOIN users ON users.user_id = bookmark.user_id AND user_status = 'Active'");
			sb.append(") bookmark_users ON recipes.recipe_id = bookmark_users.recipe_id ");
			sb.append("GROUP BY recipes.recipe_id ");
			sb.append("HAVING COUNT(bookmark_users.user_id) >= ? AND COUNT(bookmark_users.user_id) <= ? ");
				sqlParams.add(ranges.get("Bookmarks").get("min"));
				sqlParams.add(ranges.get("Bookmarks").get("max"));
			sb.append(") recipes_users ");
			
		int authorsLength = filterIdMap.get("Author").length;
		if (authorsLength > 0) {
			sb.append("JOIN users ON recipes.author_id = users.user_id AND users.user_id IN (");
			for (int i = 0; i < authorsLength; i++) {
				sb.append("?, ");
				sqlParams.add(filterIdMap.get("Author")[i]);
			}
			sb.setLength(sb.length() - 2);
			sb.append(")");
		} 
		else sb.append("LEFT JOIN users ON recipes.author_id = users.user_id ");
		
		sb.append("WHERE recipes.recipe_status = 'Active' ");
		
		// Ingredient filter: Recipe must contain at least (wanted - tolerance) of the wanted ingredients.
		// A wanted ingredient counts as present if the recipe has it or any related one in the hierarchy.
	    Map<Integer, Integer[]> ingredientMatches = requestInfo.getIngredientMatches();
		int requiredIngredients = ingredientMatches.size() - tolerances.get("Ingredient");
		if (requiredIngredients > 0) {
		    sb.append("AND ("); 
			 // The whole (EXISTS + EXISTS + ...) expression is evaluated once per recipe row.
			 // So recipes.recipe_id is the same recipe in every EXISTS below.
			 // Each EXISTS looks at that recipe's rows in rci to see if it has any ingredient that satisfies the wanted ingredient from the loop.
		    for (Integer[] matchingIds : ingredientMatches.values()) {
		        sb.append("EXISTS (");
	        		sb.append("SELECT 1 "); 
	        		sb.append("FROM recipes_cook_ingredients rci ");
	        		sb.append("WHERE rci.recipe_id = recipes.recipe_id AND rci.ingredient_id IN (");
	        			addPlaceholderArray(sb, sqlParams, matchingIds);
				    sb.append(")");
		        sb.append(") + ");
		    }
		    sb.setLength(sb.length() - 2); // remove the last plus and space
		    sb.append(") >= ? ");
		    sqlParams.add(requiredIngredients);
		}
		
		// Dietary restriction filter: the recipe must not contain any ingredient that violates a selected restriction.
		// As mentioned before, recipes.recipe_id is the recipe currently being checked.
		Integer[] dietaryRestrictionIds = filterIdMap.get("Dietary Restriction");
		if (dietaryRestrictionIds.length > 0) {
		    sb.append("AND NOT EXISTS (");
		        sb.append("SELECT 1 ");
		        sb.append("FROM recipes_cook_ingredients rci JOIN ingredients_violate_dietary_restrictions ivdr ON rci.ingredient_id = ivdr.ingredient_id ");
		        sb.append("WHERE rci.recipe_id = recipes.recipe_id AND ivdr.dietary_restriction_id IN (");
		        	addPlaceholderArray(sb, sqlParams, dietaryRestrictionIds);
		        sb.append(")");
		    sb.append(") ");
		}
		
		String searchTerm = requestInfo.getSearchTerm();
		if(!searchTerm.isEmpty()) {
			sb.append("AND (recipes.recipe_name LIKE ? OR recipes.recipe_name LIKE ?) ");
				sqlParams.add(searchTerm + "%");
				sqlParams.add("% "+ searchTerm + "%");
		}
		
		int minMinutes = ranges.get("Cooking Time").get("min");
		int maxMinutes = ranges.get("Cooking Time").get("max");
		if (!(minMinutes == 0 || maxMinutes == Integer.MAX_VALUE)) {
		    sb.append("AND recipes.total_minutes >= ? AND recipes.total_minutes <= ? ");
		    sqlParams.add(minMinutes);
		    sqlParams.add(maxMinutes);
		}
		
		// Appliance filter: The recipe may need at most (tolerance) appliances that are NOT in the user's list.
		// As mentioned before, recipes.recipe_id is the recipe currently being checked.
		Integer[] applianceIds = filterIdMap.get("Appliance");
		int applianceTolerance = Math.max(0, tolerances.get("Appliance"));
		if (applianceIds.length > 0) {
		    sb.append("AND (");
		    	sb.append("SELECT COUNT(*) ");
		    	sb.append("FROM recipes_use_appliances rua");
		    	sb.append("WHERE rua.recipe_id = recipes.recipe_id AND rua.appliance_id NOT IN (");
		    		addPlaceholderArray(sb, sqlParams, applianceIds);
				sb.append(")");
		    sb.append(") <= ? ");
		    sqlParams.add(applianceTolerance);
		}
		
		// Course filter: Recipe's course must be one of the selected courses
		Integer[] courseIds = filterIdMap.get("Course");
		if (courseIds.length > 0) {
		    sb.append("AND recipes.course IN (");
		    	addPlaceholderArray(sb, sqlParams, courseIds);
		    sb.append(") ");
		}
		
		// Dish type filter: Recipe must have at least one of the selected dish types.
		Integer[] dishTypeIds = filterIdMap.get("Dish Type");
		if (dishTypeIds.length > 0) {
		    sb.append("AND EXISTS (");
		    	sb.append("SELECT 1");
		    	sb.append("FROM recipes_has_dish_types rhdt ");
		    	sb.append("WHERE rhdt.recipe_id = recipes.recipe_id AND rhdt.dish_type_id IN (");
		    		addPlaceholderArray(sb, sqlParams, dishTypeIds);
		    	sb.append(")");
		    sb.append(") ");
		}
		
		// Dish type filter: Recipe must have at least one of the selected dish types.
		Integer[] cuisineIds = filterIdMap.get("Cuisine");
		if (dishTypeIds.length > 0) {
		    sb.append("AND EXISTS (");
		    	sb.append("SELECT 1");
		    	sb.append("FROM recipes_feature_cuisines rfc ");
		    	sb.append("WHERE rfc.recipe_id = recipes.recipe_id AND rfc.cuisine_id IN (");
		    		addPlaceholderArray(sb, sqlParams, cuisineIds);
		    	sb.append(")");
		    sb.append(") ");
		}
		
		Map<String, Boolean> options = requestInfo.getOptions();
		if(!options.get("Ai Generated Description")) 	 sb.append("AND recipes.ai_gen_description = FALSE ");
		if(!options.get("Ai Generated Appliances")) 	 sb.append("AND recipes.ai_gen_appliances = FALSE ");
		if(!options.get("Ai Generated Cuisines")) 		 sb.append("AND recipes.ai_gen_cuisines = FALSE ");
		if(!options.get("Ai Generated Dish Types")) 	 sb.append("AND recipes.ai_gen_dish_types = FALSE ");
		if(!options.get("Ai Generated Cooking Minutes")) sb.append("AND recipes.ai_gen_total_minutes = FALSE ");
		if(!options.get("Ai Generated Courses")) 		 sb.append("AND recipes.ai_gen_course = FALSE ");
		
		
		sb.append("ORDER BY ");
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
	        else sb.append(", ");
	        sb.append(column)
	          .append("ascending".equals(entry.getValue()) ? " ASC" : " DESC");
	    }
	    if (!hasSort) sb.append("recipes.recipe_active_timestamp DESC");
		
		return sb.toString();
	}
	
	private void addPlaceholderArray(StringBuilder sb, ArrayList<Object> sqlParams, Integer[] ids) {
	    for (Integer id : ids) {
	        sb.append("?, ");
	        sqlParams.add(id);
	    }
	    sb.setLength(sb.length() - 2); // remove the last comma and space
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