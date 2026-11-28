package servlet.share;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.util.List;
import java.util.Set;

public class RecipeCreateDAO {

	public static int create(RecipeCreateRequest req, int authorId, Connection dbConnection) {
	    Set<String> fill = req.getAiFill();
	    String description = req.getDescription();
	    Integer minutes = req.getTotalMinutes();
	    String course = req.getCourse();
	    List<Integer> appliances = req.getApplianceIds();
	    List<Integer> cuisines = req.getCuisineIds();
	    List<Integer> dishTypes = req.getDishTypeIds();

        String sql = """
            INSERT INTO recipes (recipe_name, recipe_description, instructions, total_minutes, course,
                ai_gen_description, ai_gen_appliances, ai_gen_cuisines, ai_gen_dish_types, ai_gen_total_minutes, ai_gen_course, author_id)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

        try {
            boolean previousAutoCommit = dbConnection.getAutoCommit();
            dbConnection.setAutoCommit(false);
            try {
                int recipeId;
                try (PreparedStatement ps = dbConnection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, req.getName());
                    ps.setObject(2, description);
                    ps.setString(3, req.getInstructions());
                    ps.setObject(4, minutes);
                    ps.setObject(5, course);
                    ps.setBoolean(6, fill.contains("description") && description != null);
                    ps.setBoolean(7, fill.contains("appliances") && !appliances.isEmpty());
                    ps.setBoolean(8, fill.contains("cuisines") && !cuisines.isEmpty());
                    ps.setBoolean(9, fill.contains("dishTypes") && !dishTypes.isEmpty());
                    ps.setBoolean(10, fill.contains("totalMinutes") && minutes != null);
                    ps.setBoolean(11, fill.contains("course") && course != null);
                    ps.setInt(12, authorId);
                    ps.executeUpdate();
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        keys.next();
                        recipeId = keys.getInt(1);
                    }
                }

                try (PreparedStatement ps = dbConnection.prepareStatement(
                        "INSERT INTO recipes_cook_ingredients (recipe_id, ingredient_id, amount, amount_unit) VALUES (?, ?, ?, ?)")) {
                	for (RecipeCreateRequest.IngredientLine line : req.getIngredients()) {
                	    ps.setInt(1, recipeId);
                	    ps.setInt(2, line.id);
                	    ps.setObject(3, line.amount);
                	    ps.setObject(4, (line.unit == null || line.unit.isBlank()) ? null : line.unit.strip());
                	    ps.addBatch();
                	}
                    ps.executeBatch();
                }

                insertLinks(dbConnection, "INSERT INTO recipes_use_appliances (recipe_id, appliance_id) VALUES (?, ?)", recipeId, appliances);
                insertLinks(dbConnection, "INSERT INTO recipes_feature_cuisines (recipe_id, cuisine_id) VALUES (?, ?)", recipeId, cuisines);
                insertLinks(dbConnection, "INSERT INTO recipes_has_dish_types (recipe_id, dish_type_id) VALUES (?, ?)", recipeId, dishTypes);

                dbConnection.commit();
                return recipeId;
            } catch (SQLException | RuntimeException e) {
                dbConnection.rollback();
                throw e;
            } finally {
                dbConnection.setAutoCommit(previousAutoCommit);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Recipe creation failed: " + e.getMessage(), e);
        }
    }

    private static void insertLinks(Connection dbConnection, String sql, int recipeId, List<Integer> ids) throws SQLException {
        if (ids.isEmpty()) return;
        try (PreparedStatement ps = dbConnection.prepareStatement(sql)) {
            for (Integer id : ids) {
                ps.setInt(1, recipeId);
                ps.setInt(2, id);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }
}