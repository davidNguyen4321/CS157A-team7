<%@ page language="java"
    contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>

<%@ page import="database.DbAccess" %>
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

    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/css/index.css">

    <style>
        .recipe-page {
            max-width: 1100px;
            margin: 2rem auto;
            padding: 0 1.5rem 4rem;
        }

        .recipe-hero {
            display: grid;
            grid-template-columns: 1fr 1fr;
            gap: 2rem;
            align-items: center;
        }

        .recipe-hero-image {
            width: 100%;
            height: 400px;
            object-fit: cover;
            border-radius: 12px;
        }

        .recipe-page h1 {
            font-family: 'Fraunces', serif;
            font-size: 3rem;
            margin-bottom: 1rem;
        }

        .recipe-description {
            color: var(--muted-foreground);
            font-size: 1.15rem;
            line-height: 1.6;
        }

        .recipe-author {
            margin: 1.5rem 0;
            color: var(--muted-foreground);
        }

        .recipe-author img,
        .reviewer-image {
            width: 42px;
            height: 42px;
            border-radius: 50%;
            object-fit: cover;
            vertical-align: middle;
            margin-right: .5rem;
        }

        .recipe-button {
            background: var(--accent);
            color: white;
            border: none;
            padding: .8rem 1.2rem;
            border-radius: 6px;
            cursor: pointer;
            font-size: 1rem;
        }

        .recipe-section {
            margin-top: 3rem;
        }

        .recipe-section h2 {
            font-family: 'Fraunces', serif;
            font-size: 2rem;
        }

        .recipe-columns {
            display: grid;
            grid-template-columns: 1fr 1fr;
            gap: 2rem;
        }

        .recipe-list li {
            margin-bottom: .6rem;
        }

        .instructions {
            white-space: pre-line;
            line-height: 1.7;
        }

        .review-card {
            background: white;
            border: 1px solid var(--border);
            border-radius: 8px;
            padding: 1rem;
            margin: 1rem 0;
        }

        .review-stars {
            color: var(--accent);
            font-size: 1.2rem;
        }

        @media (max-width: 700px) {
            .recipe-hero,
            .recipe-columns {
                grid-template-columns: 1fr;
            }

            .recipe-page h1 {
                font-size: 2.2rem;
            }
        }
    </style>
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