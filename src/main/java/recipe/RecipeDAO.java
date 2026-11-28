package recipe;

import util.SqlBuilder;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.List;

public class RecipeDAO {

    public static Recipe findById(int recipeId, Connection dbConnection, int sessionUserId) {
    	SqlBuilder sb = new SqlBuilder();
    	sb.addString("SELECT recipe_id, recipe_name, recipe_image_path, recipe_description, instructions, total_minutes, course, author_id, user_name, user_image_path, ")
    	  .addString("review_count, average_rating, ")
          .addString("bookmark_count, user_bookmarked, ");
    	
    	sb.addString("FROM recipes LEFT JOIN users ON recipes.author_id = users.user_id ");
    	
    	sb.addString("NATURAL JOIN (");
	          sb.addString("SELECT recipe_id, COUNT(user_id) AS bookmark_count, SUM(user_id = ?) AS user_bookmarked ").addParam(sessionUserId)
	            .addString("FROM recipes NATURAL LEFT JOIN (SELECT recipe_id, user_id FROM bookmark NATURAL JOIN users WHERE user_status = 'Active') bookmark_users ")
	            .addString("GROUP BY recipe_id ");
        sb.addString(") recipes_users ");
        
        sb.addString("NATURAL JOIN (");
	        sb.addString("SELECT recipe_id, COUNT(review_id) AS review_count, AVG(rating) AS average_rating ")
	          .addString("FROM recipes NATURAL LEFT JOIN (SELECT recipe_id, review_id, rating FROM reviews WHERE review_status = 'Active') active_reviews ")
	          .addString("GROUP BY recipe_id ");
	    sb.addString(") recipes_reviews ");
	    
    	sb.addString("WHERE recipe_id = ? AND recipe_status = 'Active'").addParam(recipeId);
        
	    try (PreparedStatement ps = sb.build(dbConnection);
        	 ResultSet rs = ps.executeQuery()) {
            if (!rs.next()) return null;
            
            Recipe recipe = new Recipe();
            recipe.id = rs.getInt("recipe_id");
            recipe.name = rs.getString("recipe_name");
            recipe.imagePath = rs.getString("recipe_image_path");
            recipe.description = rs.getString("recipe_description");
            recipe.instructions = rs.getString("instructions");
            recipe.totalMinutes = (Integer) rs.getObject("total_minutes");
            recipe.course = rs.getString("course");
            
            recipe.reviewCount = rs.getInt("review_count");
            recipe.averageRating = String.format("%.1f", rs.getFloat("average_rating"));
            
            recipe.bookmarkCount = rs.getInt("bookmark_count");
            recipe.userBookmarked = rs.getInt("user_bookmarked") > 0;
            
            recipe.authorId = rs.getInt("author_id");
            recipe.authorName = rs.getString("user_name");
            recipe.authorImagePath = rs.getString("user_image_path");
            
            loadIngredients(dbConnection, recipe);
            loadAppliances(dbConnection, recipe);
            loadCuisines(dbConnection, recipe);
            loadDishTypes(dbConnection, recipe);
            
            return recipe;
        }
        catch (SQLException e) {
	        throw new RuntimeException("Recipe data load failed: " + e.getMessage(), e);
        }
    }
    
    private static void loadIngredients(Connection dbConnection, Recipe recipe) throws SQLException {
    	SqlBuilder sb = new SqlBuilder();
    	
    	sb.addString("SELECT ingredient_name, amount, amount_unit ")
    	  .addString("FROM recipes_cook_ingredients NATURAL JOIN ingredients ")
    	  .addString("WHERE recipe_id = ? ").addParam(recipe.id);
    	sb.addString("ORDER BY ingredient_name ASC");

        try (PreparedStatement ps = sb.build(dbConnection);
        	 ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                String amount = rs.getString("amount");
                String unit = rs.getString("amount_unit");

                StringBuilder ingredient = new StringBuilder();
                if (amount != null) ingredient.append(amount + " ");
                if (unit != null)   ingredient.append(unit + " ");
                ingredient.append(rs.getString("ingredient_name"));
                
                recipe.ingredients.add(ingredient.toString());
            }
        }
    }

    private static void loadAppliances(Connection dbConnection, Recipe recipe) throws SQLException {
    	SqlBuilder sb = new SqlBuilder();
    	
    	sb.addString("SELECT appliance_name ")
    	  .addString("FROM recipes_use_appliances NATURAL JOIN appliances ")
    	  .addString("WHERE recipe_id = ? ").addParam(recipe.id);
    	sb.addString("ORDER BY appliance_name ASC");

        loadSimpleList(dbConnection, sb, recipe.appliances);
    }

    private static void loadCuisines(Connection dbConnection, Recipe recipe) throws SQLException {
    	SqlBuilder sb = new SqlBuilder();

    	sb.addString("SELECT cuisine_name ")
	  	  .addString("FROM recipes_feature_cuisines NATURAL JOIN cuisines ")
	  	  .addString("WHERE recipe_id = ? ").addParam(recipe.id);
	  	sb.addString("ORDER BY cuisine_name ASC");

        loadSimpleList(dbConnection, sb, recipe.cuisines);
    }

    private static void loadDishTypes(Connection dbConnection, Recipe recipe) throws SQLException {
    	SqlBuilder sb = new SqlBuilder();

    	sb.addString("SELECT dish_type_name ")
	  	  .addString("FROM recipes_has_dish_types NATURAL JOIN dish_types ")
	  	  .addString("WHERE recipe_id = ? ").addParam(recipe.id);
	  	sb.addString("ORDER BY dish_type_name ASC");

        loadSimpleList(dbConnection, sb, recipe.dishTypes);
    }

    private static void loadSimpleList(Connection dbConnection, SqlBuilder sb, List<String> container) throws SQLException {
        try (PreparedStatement ps = sb.build(dbConnection);
        	 ResultSet rs = ps.executeQuery()) {
        	while (rs.next()) container.add(rs.getString(1));  
        }
    }
    
    public static Integer findAuthorId(int recipeId, Connection dbConnection) {
    	SqlBuilder sb = new SqlBuilder();
    	sb.addString("SELECT author_id FROM recipes WHERE recipe_id = ? AND recipe_status = 'Active'").addParam(recipeId);
        try (PreparedStatement ps = sb.build(dbConnection);
        	 ResultSet rs = ps.executeQuery()) {
            if (!rs.next()) return null;
            return rs.getInt("author_id");
        }
        catch (SQLException e) {
	        throw new RuntimeException("Find recipe author failed: " + e.getMessage(), e);
        }
    }
}