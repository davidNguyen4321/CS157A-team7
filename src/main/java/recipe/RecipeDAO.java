package recipe;

import database.DbAccess;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class RecipeDAO {

    public static Recipe findById(int recipeId) throws SQLException {
        Connection connection = DbAccess.getConnection();

        String sql =
            "SELECT r.RECIPE_ID, r.RECIPE_NAME, r.RECIPE_IMAGE_PATH, " +
            "r.RECIPE_DESCRIPTION, r.INSTRUCTIONS, r.TOTAL_MINUTES, r.COURSE, " +
            "r.AUTHOR_ID, u.USER_NAME, u.USER_IMAGE_PATH " +
            "FROM RECIPES r " +
            "LEFT JOIN USERS u ON r.AUTHOR_ID = u.USER_ID " +
            "WHERE r.RECIPE_ID = ? AND r.RECIPE_STATUS = 'Active'";

        Recipe recipe;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, recipeId);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }

                recipe = new Recipe();
                recipe.id = rs.getInt("RECIPE_ID");
                recipe.name = rs.getString("RECIPE_NAME");
                recipe.imagePath = rs.getString("RECIPE_IMAGE_PATH");
                recipe.description = rs.getString("RECIPE_DESCRIPTION");
                recipe.instructions = rs.getString("INSTRUCTIONS");
                recipe.totalMinutes = (Integer) rs.getObject("TOTAL_MINUTES");
                recipe.course = rs.getString("COURSE");
                recipe.authorId = rs.getInt("AUTHOR_ID");
                recipe.authorName = rs.getString("USER_NAME");
                recipe.authorImagePath = rs.getString("USER_IMAGE_PATH");
            }
        }

        loadIngredients(connection, recipe);
        loadAppliances(connection, recipe);
        loadCuisines(connection, recipe);
        loadDishTypes(connection, recipe);

        return recipe;
    }

    private static void loadIngredients(Connection connection, Recipe recipe)
            throws SQLException {

        String sql =
            "SELECT i.INGREDIENT_NAME, ri.AMOUNT, ri.AMOUNT_UNIT " +
            "FROM RECIPES_COOK_INGREDIENTS ri " +
            "JOIN INGREDIENTS i ON ri.INGREDIENT_ID = i.INGREDIENT_ID " +
            "WHERE ri.RECIPE_ID = ? " +
            "ORDER BY i.INGREDIENT_NAME";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, recipe.id);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String amount = rs.getString("AMOUNT");
                    String unit = rs.getString("AMOUNT_UNIT");
                    String name = rs.getString("INGREDIENT_NAME");

                    StringBuilder ingredient = new StringBuilder();

                    if (amount != null) {
                        ingredient.append(amount).append(" ");
                    }

                    if (unit != null) {
                        ingredient.append(unit).append(" ");
                    }

                    ingredient.append(name);
                    recipe.ingredients.add(ingredient.toString());
                }
            }
        }
    }

    private static void loadAppliances(Connection connection, Recipe recipe)
            throws SQLException {

        String sql =
            "SELECT a.APPLIANCE_NAME " +
            "FROM RECIPES_USE_APPLIANCES ra " +
            "JOIN APPLIANCES a ON ra.APPLIANCE_ID = a.APPLIANCE_ID " +
            "WHERE ra.RECIPE_ID = ? " +
            "ORDER BY a.APPLIANCE_NAME";

        loadSimpleList(connection, sql, recipe.id, recipe.appliances);
    }

    private static void loadCuisines(Connection connection, Recipe recipe)
            throws SQLException {

        String sql =
            "SELECT c.CUISINE_NAME " +
            "FROM RECIPES_FEATURE_CUISINES rc " +
            "JOIN CUISINES c ON rc.CUISINE_ID = c.CUISINE_ID " +
            "WHERE rc.RECIPE_ID = ? " +
            "ORDER BY c.CUISINE_NAME";

        loadSimpleList(connection, sql, recipe.id, recipe.cuisines);
    }

    private static void loadDishTypes(Connection connection, Recipe recipe)
            throws SQLException {

        String sql =
            "SELECT d.DISH_TYPE_NAME " +
            "FROM RECIPES_HAS_DISH_TYPES rd " +
            "JOIN DISH_TYPES d ON rd.DISH_TYPE_ID = d.DISH_TYPE_ID " +
            "WHERE rd.RECIPE_ID = ? " +
            "ORDER BY d.DISH_TYPE_NAME";

        loadSimpleList(connection, sql, recipe.id, recipe.dishTypes);
    }

    private static void loadSimpleList(
            Connection connection,
            String sql,
            int recipeId,
            List<String> output
    ) throws SQLException {

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, recipeId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    output.add(rs.getString(1));
                }
            }
        }
    }

    public static List<Review> getReviews(int recipeId) throws SQLException {
        List<Review> reviews = new ArrayList<>();

        String sql =
            "SELECT v.REVIEW_DESCRIPTION, v.RATING, " +
            "v.REVIEW_ACTIVE_TIMESTAMP, u.USER_NAME, u.USER_IMAGE_PATH " +
            "FROM REVIEWS v " +
            "LEFT JOIN USERS u ON v.REVIEWER_ID = u.USER_ID " +
            "WHERE v.RECIPE_ID = ? AND v.REVIEW_STATUS = 'Active' " +
            "ORDER BY v.REVIEW_ACTIVE_TIMESTAMP DESC";

        try (PreparedStatement ps = DbAccess.getConnection().prepareStatement(sql)) {
            ps.setInt(1, recipeId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Review review = new Review();
                    review.description = rs.getString("REVIEW_DESCRIPTION");
                    review.rating = rs.getInt("RATING");
                    review.timestamp = rs.getTimestamp("REVIEW_ACTIVE_TIMESTAMP");
                    review.reviewerName = rs.getString("USER_NAME");
                    review.reviewerImagePath = rs.getString("USER_IMAGE_PATH");

                    reviews.add(review);
                }
            }
        }

        return reviews;
    }
}