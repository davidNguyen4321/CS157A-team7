<%@ page language="java"
    contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>

<%@ page import="recipe.Recipe" %>
<%@ page import="recipe.Review" %>
<%@ page import="recipe.RecipeDAO" %>
<%@ page import="util.WebUtils" %>
<%@ page import="java.util.List" %>

<%@ include file="/WEB-INF/fragments/auth_check.jspf" %>

<%
    int recipeId;

    try {
        recipeId = Integer.parseInt(request.getParameter("recipeId"));
    } catch (Exception e) {
        response.sendError(400, "Invalid recipe ID.");
        return;
    }

    Recipe recipe;
    List<Review> reviews;

    try {
        recipe = RecipeDAO.findById(recipeId);

        if (recipe == null) {
            response.sendError(404, "Recipe not found.");
            return;
        }

        reviews = RecipeDAO.getReviews(recipeId);
    } catch (Exception e) {
        e.printStackTrace();
        response.sendError(500, "Unable to load recipe.");
        return;
    }

    String imagePath = recipe.imagePath;

    if (imagePath == null || imagePath.isEmpty()) {
        imagePath = "/images/recipes/recipe-placeholder.jpg";
    }
%>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">

    <title><%= WebUtils.esc(recipe.name) %> | Recipe Genie</title>

    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/index.css">
	<link rel="stylesheet" href="${pageContext.request.contextPath}/css/recipe.css">
</head>

<body>

<%@ include file="/WEB-INF/fragments/header.jspf" %>

<main class="recipe-page">

    <section class="recipe-hero">
        <img class="recipe-hero-image"
             src="${pageContext.request.contextPath}<%= imagePath %>"
             alt="<%= WebUtils.esc(recipe.name) %>">

        <div>
            <h1><%= WebUtils.esc(recipe.name) %></h1>

            <p class="recipe-description">
                <%= recipe.description == null
                        ? ""
                        : WebUtils.esc(recipe.description) %>
            </p>

            <div class="recipe-author">
                <% if (recipe.authorName != null) { %>
                    <%
                        String authorImage = recipe.authorImagePath;

                        if (authorImage == null || authorImage.isEmpty()) {
                            authorImage = "/images/users/user-placeholder.png";
                        }
                    %>

                    <img src="${pageContext.request.contextPath}<%= authorImage %>"
                         alt="Author">

                    <a href="profile.jsp?id=<%= recipe.authorId %>">
                        <%= WebUtils.esc(recipe.authorName) %>
                    </a>
                <% } %>
            </div>

            <p>
                <strong>Cooking time:</strong>
                <%= recipe.totalMinutes == null
                        ? "Not specified"
                        : recipe.totalMinutes + " minutes" %>
            </p>

            <% if (recipe.course != null) { %>
                <p><strong>Course:</strong>
                    <%= WebUtils.esc(recipe.course) %>
                </p>
            <% } %>

            <button class="recipe-button"
                    type="button"
                    onclick="alert('Review creation will be available soon!')">
                Leave a Review
            </button>
        </div>
    </section>

    <section class="recipe-section">
        <h2>Ingredients</h2>

        <ul class="recipe-list">
            <% for (String ingredient : recipe.ingredients) { %>
                <li><%= WebUtils.esc(ingredient) %></li>
            <% } %>
        </ul>
    </section>

    <section class="recipe-section recipe-columns">
        <div>
            <h2>Appliances</h2>

            <ul class="recipe-list">
                <% for (String appliance : recipe.appliances) { %>
                    <li><%= WebUtils.esc(appliance) %></li>
                <% } %>
            </ul>
        </div>

        <div>
            <h2>Tags</h2>

            <% for (String cuisine : recipe.cuisines) { %>
                <span class="badge-pill">
                    <%= WebUtils.esc(cuisine) %>
                </span>
            <% } %>

            <% for (String dishType : recipe.dishTypes) { %>
                <span class="badge-pill">
                    <%= WebUtils.esc(dishType) %>
                </span>
            <% } %>
        </div>
    </section>

    <section class="recipe-section">
        <h2>Instructions</h2>

        <p class="instructions">
            <%= recipe.instructions == null
                    ? ""
                    : WebUtils.esc(recipe.instructions) %>
        </p>
    </section>

    <section class="recipe-section">
        <h2>Reviews</h2>

        <% if (reviews.isEmpty()) { %>
            <p>No reviews yet.</p>
        <% } %>

        <% for (Review review : reviews) { %>
            <div class="review-card">

                <div>
                    <%
                        String reviewerImage = review.reviewerImagePath;

                        if (reviewerImage == null || reviewerImage.isEmpty()) {
                            reviewerImage = "/images/users/user-placeholder.png";
                        }
                    %>

                    <img class="reviewer-image"
                         src="${pageContext.request.contextPath}<%= reviewerImage %>"
                         alt="Reviewer">

                    <strong>
                        <%= review.reviewerName == null
                                ? "Anonymous"
                                : WebUtils.esc(review.reviewerName) %>
                    </strong>
                </div>

                <div class="review-stars">
                    <% for (int i = 0; i < review.rating; i++) { %>
                        ★
                    <% } %>
                </div>

                <p>
                    <%= WebUtils.esc(review.description) %>
                </p>

                <small>
                    <%= review.timestamp %>
                </small>
            </div>
        <% } %>
    </section>

</main>

</body>
</html>