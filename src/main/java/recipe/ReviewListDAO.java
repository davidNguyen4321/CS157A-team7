package recipe;

import util.SqlBuilder;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.List;
import java.util.ArrayList;

public class ReviewListDAO {

    public static List<ReviewItem> findByRecipeId(int recipeId, Connection dbConnection) {
        SqlBuilder sb = new SqlBuilder();
        
        sb.addString("SELECT review_description, rating, review_active_timestamp, user_name, user_image_path ")
          .addString("FROM reviews LEFT JOIN users ON reviews.reviewer_id = users.user_id ")
          .addString("WHERE recipe_id = ? AND review_status = 'Active' ").addParam(recipeId);
        sb.addString("ORDER BY review_active_timestamp DESC");

        try (PreparedStatement ps = sb.build(dbConnection);
             ResultSet rs = ps.executeQuery()) {

            List<ReviewItem> reviews = new ArrayList<>();
            while (rs.next()) {
                ReviewItem review = new ReviewItem();
                review.description = rs.getString("review_description");
                review.rating = rs.getInt("rating");
                review.timestamp = rs.getDate("review_active_timestamp").toString();
                review.reviewerName = rs.getString("user_name");
                review.reviewerImagePath = rs.getString("user_image_path");
                reviews.add(review);
            }
            
            return reviews;
        }
        catch (SQLException e) {
            throw new RuntimeException("Recipe reviews load failed: " + e.getMessage(), e);
        }
    }
}