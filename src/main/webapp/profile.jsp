<%@ include file="auth_check.jspf" %>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.sql.*, Auth.*, Search.DatabaseAccess" %>
<%
    int profileId;
    try { profileId = Integer.parseInt(request.getParameter("id")); }
    catch (Exception e) { profileId = (Integer) session.getAttribute("userId"); }

    Connection con = DatabaseAccess.getConnection();
    UserDAO.User profile = UserDAO.findById(profileId);
    boolean available = profile != null && "Active".equals(profile.status);
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><%= available ? Util.esc(profile.name) : "Profile" %> | Recipe Genie</title>
    <link rel="stylesheet" href="css/index.css">
    <link rel="stylesheet" href="css/auth.css">
</head>
<body>
<%@ include file="header.jspf" %>
<main class="profile-page">
<% if (!available) { %>
    <h1>This profile is unavailable.</h1>
<% } else {
       String pic = Util.or(profile.imagePath, "/images/users/user-placeholder.png");
%>
    <section class="profile-hero">
        <img class="profile-avatar" src="${pageContext.request.contextPath}<%= pic %>" alt="Avatar">
        <div>
            <h1 style="margin:0;"><%= Util.esc(profile.name) %></h1>
            <p class="profile-meta">Timezone: <%= Util.esc(profile.timezone) %></p>
        </div>
    </section>

    <section class="profile-section">
        <h2>Biography</h2>
        <p><%= profile.description == null || profile.description.trim().isEmpty()
                ? "This user hasn't written a biography yet." : Util.esc(profile.description) %></p>
    </section>

    <section class="profile-section">
        <div class="profile-section-head">
            <h2>Recipes</h2>
            <a class="see-all" href="profile_recipes.jsp?id=<%= profileId %>">See all &rarr;</a>
        </div>
        <div class="profile-grid">
        <%
            String rsql = "SELECT RECIPE_NAME, RECIPE_IMAGE_PATH, RECIPE_DESCRIPTION, TOTAL_MINUTES " +
                          "FROM RECIPES WHERE AUTHOR_ID = ? AND RECIPE_STATUS = 'Active' " +
                          "ORDER BY RECIPE_ACTIVE_TIMESTAMP DESC LIMIT 5";
            boolean anyRecipe = false;
            try (PreparedStatement ps = con.prepareStatement(rsql)) {
                ps.setInt(1, profileId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        anyRecipe = true;
                        String img = Util.or(rs.getString("RECIPE_IMAGE_PATH"), "/images/recipe-placeholder.jpg");
        %>
            <article class="recipe-card">
                <div class="recipe-card-img">
                    <img src="${pageContext.request.contextPath}<%= img %>" alt="Recipe">
                    <div class="recipe-card-badges"><span class="badge-pill">
                        <%= rs.getObject("TOTAL_MINUTES") == null ? "N/A" : rs.getInt("TOTAL_MINUTES") + "m" %>
                    </span></div>
                </div>
                <div class="recipe-card-body">
                    <h3 class="recipe-card-title"><%= Util.esc(rs.getString("RECIPE_NAME")) %></h3>
                    <p class="recipe-card-desc"><%= Util.esc(rs.getString("RECIPE_DESCRIPTION")) %></p>
                </div>
            </article>
        <%      }
                }
            }
            if (!anyRecipe) { %><p>No recipes yet.</p><% } %>
        </div>
    </section>

    <section class="profile-section">
        <div class="profile-section-head">
            <h2>Reviews</h2>
            <a class="see-all" href="profile_reviews.jsp?id=<%= profileId %>">See all &rarr;</a>
        </div>
        <div class="profile-grid">
        <%
            String vsql = "SELECT v.REVIEW_NAME, v.REVIEW_DESCRIPTION, v.RATING, v.REVIEW_ACTIVE_TIMESTAMP, " +
                          "r.RECIPE_NAME FROM REVIEWS v JOIN RECIPES r ON v.RECIPE_ID = r.RECIPE_ID " +
                          "WHERE v.REVIEWER_ID = ? AND v.REVIEW_STATUS = 'Active' " +
                          "ORDER BY v.REVIEW_ACTIVE_TIMESTAMP DESC LIMIT 5";
            boolean anyReview = false;
            try (PreparedStatement ps = con.prepareStatement(vsql)) {
                ps.setInt(1, profileId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        anyReview = true;
        %>
            <article class="review-card">
                <div class="review-body">
                    <p class="review-title"><%= Util.esc(rs.getString("REVIEW_NAME")) %>
                        <small>on <%= Util.esc(rs.getString("RECIPE_NAME")) %></small></p>
                    <p class="review-text"><%= Util.esc(rs.getString("REVIEW_DESCRIPTION")) %></p>
                    <div class="review-footer">
                        <span class="review-date"><%= rs.getTimestamp("REVIEW_ACTIVE_TIMESTAMP") %></span>
                        <span class="card-rating"><%= rs.getInt("RATING") %>/5</span>
                    </div>
                </div>
            </article>
        <%      }
                }
            }
            if (!anyReview) { %><p>No reviews yet.</p><% } %>
        </div>
    </section>
<% } %>
</main>
</body>
</html>