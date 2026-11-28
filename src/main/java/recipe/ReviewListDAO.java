package recipe;

import util.SqlBuilder;
import util.TimeFormatter;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

public class ReviewListDAO {

	public static List<ReviewItem> findByRecipeId(int recipeId, Connection dbConnection, String sessionUserTimezone) {
		SqlBuilder sb = new SqlBuilder();
        
        sb.addString("SELECT reviewer_id, review_name, review_description, rating, review_active_timestamp, review_image_path, user_name, user_image_path ")
          .addString("FROM reviews LEFT JOIN users ON reviews.reviewer_id = users.user_id ")
          .addString("WHERE recipe_id = ? AND review_status = 'Active' ").addParam(recipeId);
        sb.addString("ORDER BY review_active_timestamp DESC");

        try (PreparedStatement ps = sb.build(dbConnection);
             ResultSet rs = ps.executeQuery()) {

            List<ReviewItem> reviews = new ArrayList<>();
            while (rs.next()) {
            	String formattedTime = TimeFormatter.formatTimeAgo(rs.getObject("review_active_timestamp", LocalDateTime.class), sessionUserTimezone);
            	
                ReviewItem review = new ReviewItem();
                review.reviewerId = rs.getInt("reviewer_id");
                review.name = rs.getString("review_name");
                review.description = rs.getString("review_description");
                review.rating = rs.getInt("rating");
                review.timeAgo = formattedTime;
                review.reviewImagePath = rs.getString("review_image_path");
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