package servlet.search;

import util.SqlBuilder;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.Map;
import java.util.ArrayList;

public class RecipeListDAO {
	private final RecursionDAO recursionDAO = new RecursionDAO();

    public ArrayList<RecipeListItem> search(RecipeListRequest requestInfo, Connection dbConnection) {
        Map<String, Integer[]> filterIdMap = requestInfo.getFilterIdMap();

        filterIdMap.put("Dietary Restriction",
    		recursionDAO.recurseUniDir( // recurse up
                filterIdMap.get("Dietary Restriction"),
                "dietary_restriction_id",
                "base_dietary_restriction_id",
                "dietary_restrictions",
                dbConnection
            ).toArray(Integer[]::new)
        );
        filterIdMap.put("Cuisine",
    		recursionDAO.recurseUniDir( // recurse down
                filterIdMap.get("Cuisine"),
                "base_cuisine_id",
                "cuisine_id",
                "cuisines",
                dbConnection
            ).toArray(Integer[]::new)
        );
        filterIdMap.put("Dish Type",
    		recursionDAO.recurseUniDir( // recurse down
                filterIdMap.get("Dish Type"),
                "base_dish_type_id",
                "dish_type_id",
                "dish_types",
                dbConnection
            ).toArray(Integer[]::new)
        );
        
        for (Integer wantedId : filterIdMap.get("Ingredient")) {
            requestInfo.getIngredientMatches().put(wantedId,
        		recursionDAO.recurseBiDir(new Integer[] { wantedId },
                    "ingredient_id",
                    "base_ingredient_id",
                    "ingredients",
                    dbConnection
                ).toArray(Integer[]::new)
            );
        }

        return executeRecipeQuery(buildRecipeQuery(requestInfo), dbConnection);
    }
    
    private SqlBuilder buildRecipeQuery(RecipeListRequest requestInfo) {
    	String searchTerm = requestInfo.getSearchTerm();
    	Map<String, Integer[]> filterIdMap = requestInfo.getFilterIdMap();
        Map<String, Map<String, Double>> ranges = requestInfo.getRanges();
        Map<String, Integer> tolerances = requestInfo.getTolerances();
        
        SqlBuilder sb = new SqlBuilder();
        
        sb.addString("SELECT recipe_id, recipe_name, recipe_image_path, recipe_description, recipe_active_timestamp, total_minutes, ");
        sb.addString("review_count, average_rating, ");
        sb.addString("bookmark_count, user_bookmarked, ");
        sb.addString("author_id, user_name, user_image_path ");
        
        sb.addString("FROM recipes ");
        joinWithReviews(sb, ranges.get("Reviews"), ranges.get("Ratings"));
        joinWithBookmarkingUsers(sb, requestInfo.getUserId(), ranges.get("Bookmarks"));
        joinWithAuthors(sb, filterIdMap.get("Author"));
        
        sb.addString("WHERE recipe_status = 'Active' ")
          .addString("AND (recipe_name LIKE ? OR recipe_name LIKE ?) ")
          .addParam(searchTerm + "%").addParam("% "+ searchTerm + "%");
        addTimeCondition(sb, ranges.get("Cooking Time"));
        addIngredientCondition(sb, requestInfo.getIngredientMatches(), tolerances.get("Ingredient"));
        addDietaryCondition(sb, filterIdMap.get("Dietary Restriction"));
        addApplianceCondition(sb, filterIdMap.get("Appliance"), tolerances.get("Appliance"));
        addCourseCondition(sb, filterIdMap.get("Course"));
        addDishTypeCondition(sb, filterIdMap.get("Dish Type"));
        addCuisineCondition(sb, filterIdMap.get("Cuisine"));
        
        Map<String, Boolean> options = requestInfo.getOptions();
        if(!options.get("Ai Generated Description"))     sb.addString("AND ai_gen_description = FALSE ");
        if(!options.get("Ai Generated Appliances"))      sb.addString("AND ai_gen_appliances = FALSE ");
        if(!options.get("Ai Generated Cuisines"))        sb.addString("AND ai_gen_cuisines = FALSE ");
        if(!options.get("Ai Generated Dish Types"))      sb.addString("AND ai_gen_dish_types = FALSE ");
        if(!options.get("Ai Generated Cooking Minutes")) sb.addString("AND ai_gen_total_minutes = FALSE ");
        if(!options.get("Ai Generated Courses"))         sb.addString("AND ai_gen_course = FALSE ");
        if(Boolean.TRUE.equals(options.get("Bookmarked Only"))) sb.addString("AND user_bookmarked > 0 ");
        
        sb.addString("ORDER BY ");
        boolean hasSort = false; 
        for (Map.Entry<String, String> entry : requestInfo.getSortOptions().entrySet()) {
            switch (entry.getKey()) {
                case "Publication Date": sb.addString("recipe_active_timestamp "); break;
                case "Bookmarks":        sb.addString("bookmark_count ");   	   break;
                case "Reviews":          sb.addString("review_count ");   		   break;
                case "Ratings":          sb.addString("average_rating ");		   break;
                default: continue; 
            }
            sb.addString("ascending".equals(entry.getValue()) ? "ASC, " : "DESC, ");
            hasSort = true;
        }
        if (hasSort) sb.truncate(2);
        else 		 sb.addString("recipe_active_timestamp DESC");
        
        return sb;
    }
    
    private void joinWithReviews(SqlBuilder sb, Map<String, Double> reviewRange, Map<String, Double> ratingRange) {
    	sb.addString("NATURAL JOIN (");
	        sb.addString("SELECT recipe_id, COUNT(review_id) AS review_count, AVG(rating) AS average_rating ")
	          .addString("FROM recipes NATURAL LEFT JOIN (SELECT recipe_id, review_id, rating FROM reviews WHERE review_status = 'Active') active_reviews ")
	          .addString("GROUP BY recipe_id ")
	          .addString("HAVING COUNT(review_id) >= ? AND COUNT(review_id) <= ? ")
	          .addParam(reviewRange.get("min")).addParam(reviewRange.get("max"));
	        sb.addString("AND (");
	        	double minRating = ratingRange.get("min");
	        	double maxRating = ratingRange.get("max");
	        	if (minRating == Integer.MIN_VALUE && maxRating == Integer.MAX_VALUE) {
	        		sb.addString("AVG(rating) IS NULL OR ");
	        	}
	            sb.addString("(AVG(rating) >= ? AND AVG(rating) <= ?)")
	              .addParam(minRating).addParam(maxRating);
	        sb.addString(")");
	    sb.addString(") recipes_reviews ");
    }
    
    private void joinWithBookmarkingUsers(SqlBuilder sb, int sessionUserId, Map<String, Double> bookmarkRange) {
    	sb.addString("NATURAL JOIN (");
	        sb.addString("SELECT recipe_id, COUNT(user_id) AS bookmark_count, SUM(user_id = ?) AS user_bookmarked ").addParam(sessionUserId)
	          .addString("FROM recipes NATURAL LEFT JOIN (SELECT recipe_id, user_id FROM bookmark NATURAL JOIN users WHERE user_status = 'Active') bookmark_users ")
	          .addString("GROUP BY recipe_id ")
	          .addString("HAVING COUNT(user_id) >= ? AND COUNT(user_id) <= ? ")
	          .addParam(bookmarkRange.get("min")).addParam(bookmarkRange.get("max"));
	    sb.addString(") recipes_users ");
    }
    
    private void joinWithAuthors(SqlBuilder sb, Integer[] authors) {
    	if (authors.length > 0) sb.addString("JOIN users ON author_id = user_id AND author_id IN (").addManyInts(authors).addString(") ");
    	else 					sb.addString("LEFT JOIN users ON author_id = user_id ");
    }
    
    private void addTimeCondition(SqlBuilder sb, Map<String, Double> timeRange) {
    	Integer min = timeRange.get("min").intValue();
        Integer max = timeRange.get("max").intValue();
        if (!(min.equals(Integer.MIN_VALUE) && max.equals(Integer.MAX_VALUE))) {
            sb.addString("AND total_minutes >= ? AND total_minutes <= ? ")
              .addParam(min).addParam(max);
        }
    }
    
    private void addIngredientCondition(SqlBuilder sb, Map<Integer, Integer[]> ingredientMatches, int ingredientTolerance) {
        int requiredIngredients = ingredientMatches.size() - ingredientTolerance;
    	if (requiredIngredients > 0) {
            sb.addString("AND ("); 
            for (Integer[] matchingIds : ingredientMatches.values()) {
                sb.addString("EXISTS (");
                    sb.addString("SELECT 1 ")
                      .addString("FROM recipes_cook_ingredients rci ")
                      .addString("WHERE rci.recipe_id = recipes.recipe_id AND ingredient_id IN (").addManyInts(matchingIds).addString(")");
                sb.addString(") + ");
            }
            sb.truncate(3).addString(") >= ? ").addParam(requiredIngredients);
        }
    }
    
    private void addDietaryCondition(SqlBuilder sb, Integer[] dietaryRestrictionIds) {
    	if (dietaryRestrictionIds.length > 0) {
            sb.addString("AND NOT EXISTS (");
                sb.addString("SELECT 1 ")
                  .addString("FROM recipes_cook_ingredients rci NATURAL JOIN ingredients_violate_dietary_restrictions ")
                  .addString("WHERE rci.recipe_id = recipes.recipe_id AND dietary_restriction_id IN (").addManyInts(dietaryRestrictionIds).addString(")");
            sb.addString(") ");
        }
    }
    
    private void addApplianceCondition(SqlBuilder sb, Integer[] applianceIds, int applianceTolerance) {
    	if (applianceIds.length > 0) {
    		sb.addString("AND (");
    			sb.addString("SELECT COUNT(*) ")
    			  .addString("FROM recipes_use_appliances rua ")
    			  .addString("WHERE rua.recipe_id = recipes.recipe_id AND appliance_id NOT IN (").addManyInts(applianceIds).addString(")");
    		sb.addString(") <= ? ").addParam(applianceTolerance);
    	}
    }
    
    private void addCourseCondition(SqlBuilder sb, Integer[] courseIds) {
    	if (courseIds.length > 0) sb.addString("AND recipes.course IN (").addManyInts(courseIds).addString(") ");
    }
    
    private void addDishTypeCondition(SqlBuilder sb, Integer[] dishTypeIds) {
    	if (dishTypeIds.length > 0) {
            sb.addString("AND EXISTS (");
                sb.addString("SELECT 1 ")
                  .addString("FROM recipes_has_dish_types rhdt ")
                  .addString("WHERE rhdt.recipe_id = recipes.recipe_id AND dish_type_id IN (").addManyInts(dishTypeIds).addString(")");
            sb.addString(") ");
        }
    }
    
    private void addCuisineCondition(SqlBuilder sb, Integer[] cuisineIds) {
    	if (cuisineIds.length > 0) {
            sb.addString("AND EXISTS (");
                sb.addString("SELECT 1 ")
                  .addString("FROM recipes_feature_cuisines rfc ")
                  .addString("WHERE rfc.recipe_id = recipes.recipe_id AND cuisine_id IN (").addManyInts(cuisineIds).addString(")");
            sb.addString(") ");
        }
    }
        
    private ArrayList<RecipeListItem> executeRecipeQuery(SqlBuilder sb, Connection dbConnection) {
    	try (PreparedStatement ps = sb.build(dbConnection);
           	 ResultSet rs = ps.executeQuery()) {
           	
		   	ArrayList<RecipeListItem> resultsList = new ArrayList<>();
			while (rs.next()) {
				resultsList.add(new RecipeListItem(
			        rs.getInt("recipe_id"),
			        rs.getString("recipe_name"),
			        rs.getString("recipe_image_path"),
			        rs.getString("recipe_description"),
			        rs.getObject("total_minutes", Integer.class),
			        rs.getDate("recipe_active_timestamp").toString(),
					rs.getInt("review_count"),
					String.format("%.1f", rs.getFloat("average_rating")),
					rs.getInt("bookmark_count"),
					rs.getInt("user_bookmarked") > 0,	
			        rs.getInt("author_id"),
			        rs.getString("user_name"),
			        rs.getString("user_image_path")
			    ));
			}
		    
			return resultsList;
		} 
	    catch (SQLException e) {
	        throw new RuntimeException("Recipe search failed: " + e.getMessage(), e);
	    }
    }
}