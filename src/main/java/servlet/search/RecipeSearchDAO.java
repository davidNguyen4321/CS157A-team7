package servlet.search;

import util.SqlBuilder;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;

public class RecipeSearchDAO {
    private static final ObjectMapper mapper = new ObjectMapper();

    public String search(RecipeSearchRequest requestInfo, Connection dbConnection, int sessionUserId) throws SQLException, JsonProcessingException {
        Map<String, Integer[]> filterIdMap = requestInfo.getFilterIdMap();

        filterIdMap.put("Dietary Restriction",
            recurseUniDir(
                filterIdMap.get("Dietary Restriction"),
                "DIETARY_RESTRICTION_ID",
                "BASE_DIETARY_RESTRICTION_ID",
                "DIETARY_RESTRICTIONS",
                dbConnection
            ).toArray(Integer[]::new)
        );
        filterIdMap.put("Cuisine",
            recurseUniDir(
                filterIdMap.get("Cuisine"),
                "CUISINE_ID",
                "BASE_CUISINE_ID",
                "CUISINES",
                dbConnection
            ).toArray(Integer[]::new)
        );
        filterIdMap.put("Dish Type",
            recurseUniDir(
                filterIdMap.get("Dish Type"),
                "DISH_TYPE_ID",
                "BASE_DISH_TYPE_ID",
                "DISH_TYPES",
                dbConnection
            ).toArray(Integer[]::new)
        );
        
        for (Integer wantedId : filterIdMap.get("Ingredient")) {
            requestInfo.getIngredientMatches().put(wantedId,
                recurseBiDir(new Integer[] { wantedId },
                    "INGREDIENT_ID",
                    "BASE_INGREDIENT_ID",
                    "INGREDIENTS",
                    dbConnection
                ).toArray(Integer[]::new)
            );
        }

        try (PreparedStatement ps = buildRecipeQuery(requestInfo, sessionUserId).build(dbConnection);
             ResultSet rs = ps.executeQuery()) {
            return writeRecipeResults(rs);
        }
    }

    private HashSet<Integer> recurseUniDir(Integer[] providedElementIds, String curLevelCol, String nextLevelCol, String tableName, Connection dbConnection) throws SQLException {
        if(providedElementIds.length == 0) return new HashSet<Integer>(); 
        
        HashSet<Integer> closureSet = new HashSet<Integer>(Arrays.asList(providedElementIds));
        ArrayList<Integer> newIdsToRecurse = new ArrayList<Integer>(Arrays.asList(providedElementIds));
        
        while(!newIdsToRecurse.isEmpty()) {
            SqlBuilder sb = new SqlBuilder();
            sb.addString("SELECT DISTINCT ").addString(nextLevelCol)
              .addString(" FROM ").addString(tableName)
              .addString(" WHERE ").addString(curLevelCol).addString(" IN (");
            sb.addManyInts(newIdsToRecurse);
            sb.addString(")");
            
            try (PreparedStatement statement = dbConnection.prepareStatement(sb.toString())) {
                int counter = 1;
                for(Integer id : newIdsToRecurse) statement.setInt(counter++, id); 
                
                try (ResultSet nextLevelIds = statement.executeQuery()) {
                    newIdsToRecurse = new ArrayList<Integer>();
                    while(nextLevelIds.next()) {
                        Integer nextLevelId = nextLevelIds.getInt(nextLevelCol);
                        if (!nextLevelIds.wasNull() && closureSet.add(nextLevelId)) {
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
    
    private SqlBuilder buildRecipeQuery(RecipeSearchRequest requestInfo, int sessionUserId) {
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
        if (!(minMinutes == 0 && maxMinutes == Integer.MAX_VALUE)) {
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
        if(!options.get("Ai Generated Description"))      sb.addString("AND recipes.ai_gen_description = FALSE ");
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
    
    private String writeRecipeResults(ResultSet recipes) throws SQLException, JsonProcessingException {
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
        
        return mapper.writeValueAsString(recipeList);
    }
}