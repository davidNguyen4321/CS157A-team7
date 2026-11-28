<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@ page import="database.DbAccess" %>
<%@ page import="java.sql.Connection" %>
<%@ page import="recipe.ReviewListDAO" %>
<%@ page import="recipe.ReviewItem" %>
<%@ page import="recipe.RecipeDAO" %>
<%@ page import="recipe.Recipe" %>
<%@ page import="util.WebUtils" %>
<%@ page import="java.util.List" %>
<%@ page import="util.Loggers" %>
<%@ page import="java.util.logging.Level" %>

<%@ include file="/WEB-INF/fragments/auth_check.jspf" %>
<%@ include file="/WEB-INF/fragments/recipe_card_maker.jspf" %>

<%
int recipeId;
	int sessionUserId = (Integer) session.getAttribute("userId");
    String sessionUserTimezone = (String) session.getAttribute("userTimezone");

    try {
        recipeId = Integer.parseInt(request.getParameter("recipeId"));
    } catch (Exception e) {
        Loggers.SECURITY.warning(Loggers.describe(request) + " => Invalid recipe ID");
        response.sendError(400, "Invalid recipe ID.");
        return;
    }

    Recipe recipe;
    List<ReviewItem> reviews;

    try {
        Connection dbConnection = DbAccess.getConnection();

        recipe = RecipeDAO.findById(recipeId, dbConnection, sessionUserId);

        if (recipe == null) {
            Loggers.SECURITY.warning(Loggers.describe(request) + " => Recipe " + recipeId + " not found");
            response.sendError(404, "Recipe not found.");
            return;
        }

        reviews = ReviewListDAO.findByRecipeId(recipeId, dbConnection, sessionUserTimezone);
    } catch (Exception e) {
        Loggers.OPERATION.log(Level.SEVERE, Loggers.describe(request) + " => Unable to load recipe " + recipeId, e);
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

    <title><%=WebUtils.esc(recipe.name)%> | Recipe Genie</title>

    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/index.css">
	<link rel="stylesheet" href="${pageContext.request.contextPath}/css/recipe.css">
</head>

<body>

<%@ include file="/WEB-INF/fragments/header.jspf" %>

<main class="recipe-page">

    <section class="recipe-hero">
        <img class="recipe-hero-image"
             src="${pageContext.request.contextPath}<%= WebUtils.esc(imagePath) %>"
             alt="<%= WebUtils.esc(recipe.name) %>">

        <div>
            <h1><%=WebUtils.esc(recipe.name)%></h1>

            <p class="recipe-description">
                <%=recipe.description == null
                        ? ""
                        : WebUtils.esc(recipe.description)%>
            </p>

            <div class="recipe-author">
                <%
                if (recipe.authorName != null) {
                %>
                    <%
                    String authorImage = recipe.authorImagePath;

                                                                if (authorImage == null || authorImage.isEmpty()) {
                                                                    authorImage = "/images/users/user-placeholder.png";
                                                                }
                    %>

                    <img src="${pageContext.request.contextPath}<%= WebUtils.esc(authorImage) %>"
                         alt="Author">

                    <a href="profile.jsp?id=<%=recipe.authorId%>" class="author-link">
                        <%=WebUtils.esc(recipe.authorName)%>
                    </a>
                <%
                }
                %>
            </div>

            <p>
                <strong>Cooking time:</strong>
                <%=recipe.totalMinutes == null
                        ? "Not specified"
                        : recipe.totalMinutes + " minutes"%>
            </p>

            <%
            if (recipe.course != null) {
            %>
                <p><strong>Course:</strong>
                    <%=WebUtils.esc(recipe.course)%>
                </p>
            <%
            }
            %>
			<section id="recipe-engagement">
				<%
				int reviewCount = recipe.reviewCount;

						if (reviewCount>0){
							float rating = Float.parseFloat(recipe.averageRating);
							java.text.DecimalFormat df = new java.text.DecimalFormat("#.#");
							String displayRating = df.format(rating);
				%>
					<span class="rating">
						<%=displayRating%>/5
						<span class="stars"
							style="--rating: <%=rating%>;"
							aria-label="<%=displayRating%> out of 5 stars"></span>
						 (<%=reviewCount%>)
	                </span>
                <%
                }
                %>
	            <button class="recipe-button"
	                    type="button"
	                    onclick="alert('Review creation will be available soon!')">
	                Leave a Review
	            </button>
			</section>
			<script>
	       		let engagement = document.getElementById("recipe-engagement");
			</script>
	       	<%
	       	if (recipe.authorId == sessionUserId){
	       	%>
	            <script>
					engagement.append(createBookmarkCountLabel(<%=recipe.bookmarkCount%>, "32"));
				</script>
			<%
			}
				else{
			%>
		       	<script>
					let bookmark = document.createElement("button");
					createNewBookmark(bookmark, <%=recipeId%>, <%=recipe.bookmarkCount%>, <%=recipe.userBookmarked%>, "32");
					engagement.append(bookmark);
				</script>
			<%
			}
			%>
        </div>
    </section>

    <section class="recipe-section">
        <h2>Ingredients</h2>

        <ul class="recipe-list">
            <%
            for (String ingredient : recipe.ingredients) {
            %>
                <li><%=WebUtils.esc(ingredient)%></li>
            <%
            }
            %>
        </ul>
    </section>

    <section class="recipe-section recipe-columns">
        <div>
            <h2>Appliances</h2>

            <ul class="recipe-list">
                <%
                for (String appliance : recipe.appliances) {
                %>
                    <li><%=WebUtils.esc(appliance)%></li>
                <%
                }
                %>
            </ul>
        </div>

        <div>
            <h2>Tags</h2>

            <%
            for (String cuisine : recipe.cuisines) {
            %>
                <span class="badge-pill">
                    <%=WebUtils.esc(cuisine)%>
                </span>
            <%
            }
            %>

            <%
            for (String dishType : recipe.dishTypes) {
            %>
                <span class="badge-pill">
                    <%=WebUtils.esc(dishType)%>
                </span>
            <%
            }
            %>
        </div>
    </section>

    <section class="recipe-section">
        <h2>Instructions</h2>

        <p class="instructions">
            <%=recipe.instructions == null
                    ? ""
                    : WebUtils.esc(recipe.instructions)%>
        </p>
    </section>

	<script>
	function applySmartCrop(img) {
	    const maxCrop = 0.30;
	    
	    const naturalRatio = img.naturalWidth / img.naturalHeight;
	    const fullyScaledHeight = img.clientWidth / naturalRatio;
	    
	    const choppedHeight = fullyScaledHeight - 400;
	    const cropPercentage = choppedHeight > 0 ? (choppedHeight / fullyScaledHeight) : 0;
	    
	    if (cropPercentage > maxCrop) {
	        img.style.objectFit = 'contain';
	        img.style.backgroundColor = '#f8f9fa';
	    } else {
	        img.style.objectFit = 'cover';
	    }
	}
	</script>

    <section class="recipe-section">
        <h2>Reviews</h2>

        <%
        if (reviews.isEmpty()) {
        %>
            <p>No reviews yet.</p>
        <%
        }
        %>

		<%
        	for (ReviewItem review : reviews) {
        %>
            <div class="review-card">
            	<div class="review-header">
				    <strong class="review-name" style="font-size: 25px;"><%=WebUtils.esc(review.name) %></strong>
				
				    <div class="review-subheader">
				        <%
				        String reviewerImage = review.reviewerImagePath;
				        if (reviewerImage == null || reviewerImage.isEmpty()) {
				            reviewerImage = "/images/users/user-placeholder.png";
				        }
				        %>
				        <img class="reviewer-image"
				             src="${pageContext.request.contextPath}<%= WebUtils.esc(reviewerImage) %>"
				             alt="Reviewer">
				             
				        <div class="review-meta">
				            <div class="review-author-line">
				                <%
				                if (review.reviewerName != null) {
				                %>
				                    <a href="profile.jsp?id=<%=review.reviewerId%>" class="author-link">
				                        <%=WebUtils.esc(review.reviewerName)%>
				                    </a>
				                <%
				                } else {
				                %>
				                    <strong class="author-link">Anonymous</strong>
				                <%
				                }
				                %>
				            </div>
				            <div class="review-author-line">
				                <span class="rating">
				                    <%=review.rating %>/5
				                    <span class="stars"
				                        style="--rating: <%=review.rating %>;"
				                        aria-label="<%=review.rating %> out of 5 stars"></span>
				                </span>
				                <span class="review-date">
				                	<%=WebUtils.esc(review.timeAgo)%>
				                </span>
				            </div>
				        </div>
				    </div>
				</div>
				<% if (review.reviewImagePath != null && !review.reviewImagePath.trim().isEmpty()) { %>
				    <div class="review-image-wrapper">
				        <img class="review-attached-image" 
				             src="${pageContext.request.contextPath}<%= WebUtils.esc(review.reviewImagePath) %>" 
				             alt="Review photo from <%= review.reviewerName == null ? "Anonymous" : WebUtils.esc(review.reviewerName) %>">
				    </div>
				<% } %>
                <p class="review-text">
                    <%= WebUtils.esc(review.description) %>
                </p>
            </div>
        <% } %>
    </section>

</main>

</body>
</html>