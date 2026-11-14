package servlet.search;

import util.SqlBuilder;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;

public class RecipeListDAO {
	private final RecursionDAO recursionDAO = new RecursionDAO();

    public ArrayList<RecipeListItem> search(RecipeListRequest requestInfo, Connection dbConnection, int sessionUserId) throws JsonProcessingException {
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

        return executeRecipeQuery(buildRecipeQuery(requestInfo, sessionUserId), dbConnection);
    }
    
    private SqlBuilder buildRecipeQuery(RecipeListRequest requestInfo, int sessionUserId) {
        Map<String, Integer[]> filterIdMap = requestInfo.getFilterIdMap();
        Map<String, Map<String, Double>> ranges = requestInfo.getRanges();
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
            	double minRating = ranges.get("Ratings").get("min");
            	double maxRating = ranges.get("Ratings").get("max");
                sb.addString("AND (");
                if (minRating == Integer.MIN_VALUE && maxRating == Integer.MAX_VALUE) sb.addString("AVG(rating) IS NULL OR ");
                sb.addString("(AVG(rating) >= ? AND AVG(rating) <= ?)");
                sb.addString(")");

                sb.addParam(ranges.get("Reviews").get("min").intValue());
                sb.addParam(ranges.get("Reviews").get("max").intValue());
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
                sb.addParam(ranges.get("Bookmarks").get("min").intValue());
                sb.addParam(ranges.get("Bookmarks").get("max").intValue());
        sb.addString(") recipes_users ");
            
        Integer[] authors = filterIdMap.get("Author");
        if (authors.length > 0) {
            sb.addString("JOIN users ON recipes.author_id = users.user_id AND users.user_id IN (");
                sb.addManyInts(authors);
            sb.addString(")");
        } 
        else sb.addString("LEFT JOIN users ON recipes.author_id = users.user_id ");
        
        sb.addString("WHERE recipes.recipe_status = 'Active' ");
        
        Map<Integer, Integer[]> ingredientMatches = requestInfo.getIngredientMatches();
        int requiredIngredients = ingredientMatches.size() - tolerances.get("Ingredient");
        if (requiredIngredients > 0) {
            sb.addString("AND ("); 
            for (Integer[] matchingIds : ingredientMatches.values()) {
                sb.addString("EXISTS (");
                    sb.addString("SELECT 1 "); 
                    sb.addString("FROM recipes_cook_ingredients rci ");
                    sb.addString("WHERE rci.recipe_id = recipes.recipe_id AND rci.ingredient_id IN (");
                        sb.addManyInts(matchingIds);
                    sb.addString(")");
                sb.addString(") + ");
            }
            sb.truncate(2);
            sb.addString(") >= ? ");
            sb.addParam(requiredIngredients);
        }
        
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
        
        int minMinutes = ranges.get("Cooking Time").get("min").intValue();
        int maxMinutes = ranges.get("Cooking Time").get("max").intValue();
        if (!(minMinutes == Integer.MIN_VALUE && maxMinutes == Integer.MAX_VALUE)) {
            sb.addString("AND recipes.total_minutes >= ? AND recipes.total_minutes <= ? ");
            sb.addParam(minMinutes);
            sb.addParam(maxMinutes);
        }
        
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
        
        Integer[] courseIds = filterIdMap.get("Course");
        if (courseIds.length > 0) {
            sb.addString("AND recipes.course IN (");
                sb.addManyInts(courseIds);
            sb.addString(") ");
        }
        
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
        if(!options.get("Ai Generated Description"))     sb.addString("AND recipes.ai_gen_description = FALSE ");
        if(!options.get("Ai Generated Appliances"))      sb.addString("AND recipes.ai_gen_appliances = FALSE ");
        if(!options.get("Ai Generated Cuisines"))        sb.addString("AND recipes.ai_gen_cuisines = FALSE ");
        if(!options.get("Ai Generated Dish Types"))      sb.addString("AND recipes.ai_gen_dish_types = FALSE ");
        if(!options.get("Ai Generated Cooking Minutes")) sb.addString("AND recipes.ai_gen_total_minutes = FALSE ");
        if(!options.get("Ai Generated Courses"))         sb.addString("AND recipes.ai_gen_course = FALSE ");
        
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
			        rs.getInt("user_id"),
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